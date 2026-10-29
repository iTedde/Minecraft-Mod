package dev.craft.mps;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import dev.craft.Craft;
import dev.craft.core.MovementCap;
import dev.craft.mps.item.PowerArmorItem;
import dev.craft.mps.module.MpsModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * MPS {@code PlayerTickHandler}, {@code MovementManager} and the leg tick modules (spec 29 §6): runs at the start of
 * every player tick on both sides with the player's last input; energy is only taken on the server.
 */
@EventBusSubscriber(modid = Craft.MODID)
public final class MpsMovement {
    public static final double WEIGHT_CAPACITY = 25000.0;
    public static final double DEFAULT_GRAVITY = -0.0784000015258789;
    static final ResourceLocation SPRINT_ASSIST_ID = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "mps_sprint_assist");
    static final ResourceLocation STEP_ASSIST_ID = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "mps_step_assist");
    /** 1.4.7 step height 1.001 on top of the 1.21 default 0.6. */
    private static final double STEP_ASSIST_BONUS = 0.401;

    private static final Map<Player, MpsInput> INPUTS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Player, Double> JUMP_TICKS = Collections.synchronizedMap(new WeakHashMap<>());
    /** {@code jumpMovementFactor} of the current tick, read by the flying speed mixin and cleared after the tick. */
    private static final Map<Player, Float> AIR_SPEED = Collections.synchronizedMap(new WeakHashMap<>());
    /** Spec 37 §3.6: players whose Swim Boost paid this tick and mine under water without penalty. */
    private static final Set<Player> UNDERWATER_MINING = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    private MpsMovement() {
    }

    public static void setInput(Player player, MpsInput input) {
        INPUTS.put(player, input);
    }

    public static double getJumpTicks(Player player) {
        return JUMP_TICKS.getOrDefault(player, 0.0);
    }

    public static void setJumpTicks(Player player, double ticks) {
        JUMP_TICKS.put(player, ticks);
    }

    /** Spec 37 §3.6: the Swim Boost lifts the under-water mining penalty for this player. */
    public static boolean minesUnderwater(Player player) {
        return UNDERWATER_MINING.contains(player);
    }

    /** The air acceleration set for this tick, {@code null} for the vanilla value. */
    public static Float getAirSpeed(Player player) {
        return AIR_SPEED.get(player);
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        MpsInput input = INPUTS.get(player);
        if (input != null) {
            tick(player, input);
        } else {
            setSpeedMultiplier(player, null);
        }
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        AIR_SPEED.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        INPUTS.remove(event.getEntity());
        JUMP_TICKS.remove(event.getEntity());
        AIR_SPEED.remove(event.getEntity());
        UNDERWATER_MINING.remove(event.getEntity());
    }

    /**
     * Spec 37 §3.6: with a paid Swim Boost, mining with any tool under water is as fast as on land: the
     * {@code SUBMERGED_MINING_SPEED} factor and the ÷5 for swimming without ground are undone.
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!minesUnderwater(player) || !player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        float speed = event.getNewSpeed();
        double submerged = player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
        if (submerged > 0.0 && submerged < 1.0) {
            speed /= (float) submerged;
        }
        if (!player.onGround()) {
            speed *= 5.0F;
        }
        event.setNewSpeed(speed);
    }

    /** {@code PlayerTickHandler.handle}. */
    public static void tick(Player player, MpsInput input) {
        // Spec 37 §3.6: Sprint Assist no longer changes the speed; removes modifiers of older versions.
        setSpeedMultiplier(player, null);
        if (!ModularItems.usesPowersuit(player)) {
            UNDERWATER_MINING.remove(player);
            return;
        }
        ItemStack helmet = modular(player, EquipmentSlot.HEAD);
        ItemStack torso = modular(player, EquipmentSlot.CHEST);
        ItemStack pants = modular(player, EquipmentSlot.LEGS);
        ItemStack boots = modular(player, EquipmentSlot.FEET);
        double totalEnergy = MpsEnergy.getPlayerEnergy(player);
        double totalWeight = ModularItems.getPlayerWeight(player);
        Vec3 horzFacing = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z).normalize();
        Float airSpeed = null;

        // IPlayerTickModule: Sprint Assist and Uphill Step Assist on the leggings
        double capacity = WEIGHT_CAPACITY;
        if (!pants.isEmpty()) {
            if (ModularItems.hasActiveModule(pants, MpsModules.SPRINT_ASSIST)) {
                sprintAssist(player, pants);
                capacity += ModularItems.computeProperty(pants, MpsModules.CARRY_CAPACITY);
            }
            stepAssist(player, ModularItems.hasActiveModule(pants, MpsModules.UPHILL_STEP_ASSIST));
        }

        double totalEnergyDrain = MpsEnvironment.potionEffects(player, helmet, torso, totalEnergy);
        boolean hasFlightControl = active(helmet, MpsModules.FLIGHT_CONTROL);
        boolean hasJumpAssist = active(pants, MpsModules.JUMP_ASSIST);
        boolean hasSwimAssist = active(pants, MpsModules.SWIM_BOOST);
        boolean hasJetboots = active(boots, MpsModules.JET_BOOTS);
        boolean hasJetpack = active(torso, MpsModules.JETPACK);
        boolean hasGlider = active(torso, MpsModules.GLIDER);
        boolean hasParachute = active(torso, MpsModules.PARACHUTE);
        double weightRatio = getWeightPenaltyRatio(totalWeight, capacity);
        // spec 37 §1: set when jets or the glider pushed this tick; then the horizontal speed is capped
        boolean propelled = false;
        float forward = input.forward();
        float strafe = input.strafe();
        Vec3 motion = player.getDeltaMovement();
        double mx = motion.x;
        double my = motion.y;
        double mz = motion.z;

        boolean minesUnderwater = false;
        if (player.isInWater()) {
            // Spec 37 §3.6: no swim thrust; each tick with the eyes under water the module pays and lifts the
            // under-water mining penalty (onBreakSpeed).
            if (hasSwimAssist && player.isEyeInFluid(FluidTags.WATER)) {
                double consumption = ModularItems.computeProperty(pants, "Swim Boost Energy Consumption");
                if (consumption + totalEnergyDrain < totalEnergy) {
                    totalEnergyDrain += consumption;
                    minesUnderwater = true;
                }
            }
        } else {
            if (hasJumpAssist && input.jump()) {
                double multiplier = getJumpTicks(player);
                if (multiplier > 0.0) {
                    my += 0.15 * Math.min(multiplier, 1.0) * weightRatio;
                    setJumpTicks(player, multiplier - 1.0);
                }
                // spec 37 §3.6: the extra air control (0.7 × land speed) is gone
            } else {
                setJumpTicks(player, 0.0);
            }

            if (hasJetpack || hasJetboots) {
                double jetEnergy = 0.0;
                double thrust = 0.0;
                if (hasJetpack) {
                    jetEnergy += ModularItems.computeProperty(torso, "Jet Energy Consumption");
                    thrust += ModularItems.computeProperty(torso, "Jet Thrust");
                }
                if (hasJetboots) {
                    jetEnergy += ModularItems.computeProperty(boots, "Jet Energy Consumption");
                    thrust += ModularItems.computeProperty(boots, "Jet Thrust");
                }
                if (jetEnergy + totalEnergyDrain < totalEnergy) {
                    double drainBefore = totalEnergyDrain;
                    thrust *= weightRatio;
                    if (hasFlightControl && thrust > 0.0) {
                        Vec3 look = player.getLookAngle();
                        double strafeX = look.z;
                        double strafeZ = -look.x;
                        Vec3 desired = new Vec3(
                                look.x * Math.signum(forward) + strafeX * Math.signum(strafe),
                                look.y * Math.signum(forward) + (input.jump() ? 1 : 0) - (input.down() ? 1 : 0),
                                look.z * Math.signum(forward) + strafeZ * Math.signum(strafe)).normalize();
                        if (my < 0.0 && desired.y >= 0.0) {
                            if (-my > thrust) {
                                totalEnergyDrain += jetEnergy * thrust;
                                my += thrust;
                                thrust = 0.0;
                            } else {
                                totalEnergyDrain += jetEnergy * Math.abs(my);
                                thrust -= my;
                                my = 0.0;
                            }
                        }
                        if (my < -1.0) {
                            totalEnergyDrain += jetEnergy * Math.abs(1.0 + my);
                            thrust += 1.0 + my;
                            my = -1.0;
                        }
                        if (Math.abs(mx) > 0.0 && desired.length() == 0.0) {
                            if (Math.abs(mx) > thrust) {
                                totalEnergyDrain += jetEnergy * thrust;
                                mx -= Math.signum(mx) * thrust;
                                thrust = 0.0;
                            } else {
                                totalEnergyDrain += jetEnergy * Math.abs(mx);
                                thrust -= Math.abs(mx);
                                mx = 0.0;
                            }
                        }
                        if (Math.abs(mz) > 0.0 && desired.length() == 0.0) {
                            if (Math.abs(mz) > thrust) {
                                totalEnergyDrain += jetEnergy * thrust;
                                mz -= Math.signum(mz) * thrust;
                                thrust = 0.0;
                            } else {
                                totalEnergyDrain += jetEnergy * Math.abs(mz);
                                thrust -= Math.abs(mz);
                                mz = 0.0;
                            }
                        }
                        double vx = thrust * desired.x;
                        double vy = thrust * desired.y;
                        double vz = thrust * desired.z;
                        mx += vx;
                        my += vy;
                        mz += vz;
                        totalEnergyDrain += jetEnergy * (vx * vx + vy * vy + vz * vz);
                    } else if (input.jump() && my < 0.5) {
                        totalEnergyDrain += jetEnergy;
                        if (forward == 0.0F) {
                            my += thrust;
                        } else {
                            my += thrust / 2.0;
                            mx += horzFacing.x * thrust / 2.0 * Math.signum(forward);
                            mz += horzFacing.z * thrust / 2.0 * Math.signum(forward);
                        }
                    }
                    if (totalEnergyDrain > drainBefore) {
                        propelled = true;
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.aboveGroundTickCount = 0;
                        }
                    }
                }
            }

            if (hasGlider && input.sneak() && my < -0.1 && (!hasParachute || forward > 0.0F)) {
                double change = Math.min(0.08, -0.1 - my);
                my += change;
                mx += horzFacing.x * change;
                mz += horzFacing.z * change;
                airSpeed = (airSpeed != null ? airSpeed : vanillaAirSpeed(player)) + 0.03F;
                propelled = true;
            }

            if (hasParachute && input.sneak() && my < -0.1 && (!hasGlider || forward <= 0.0F)) {
                double totalVelocity = Math.sqrt(mx * mx + mz * mz + my * my) * weightRatio;
                if (totalVelocity > 0.0) {
                    mx = mx * 0.1 / totalVelocity;
                    my = my * 0.1 / totalVelocity;
                    mz = mz * 0.1 / totalVelocity;
                }
            }
        }

        totalEnergyDrain = MpsEnvironment.generators(player, helmet, pants, totalEnergy, totalEnergyDrain);
        if (totalEnergyDrain > 0.0) {
            drain(player, totalEnergyDrain);
        }
        player.fallDistance = (float) computeFallHeightFromVelocity(Math.max(-1000.0, Math.min(my, 0.0)));
        if (totalWeight > capacity) {
            mx *= capacity / totalWeight;
            mz *= capacity / totalWeight;
        }
        Vec3 result = new Vec3(mx, my, mz);
        player.setDeltaMovement(propelled ? MovementCap.clampHorizontal(result) : result);
        if (minesUnderwater) {
            UNDERWATER_MINING.add(player);
        } else {
            UNDERWATER_MINING.remove(player);
        }
        if (airSpeed != null) {
            AIR_SPEED.put(player, airSpeed);
        } else {
            AIR_SPEED.remove(player);
        }
    }

    /**
     * {@code SprintAssistModule.onPlayerTickActive} without the speed multipliers (spec 37 §3.6): sprinting pays at once
     * and compensates exhaustion; walking costs nothing any more.
     */
    private static void sprintAssist(Player player, ItemStack pants) {
        if (!player.isSprinting()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double horzMovement = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        double exhaustion = Math.round(horzMovement * 100.0) * 0.01;
        double sprintCost = ModularItems.computeProperty(pants, "Sprint Energy Consumption");
        if (sprintCost < MpsEnergy.getPlayerEnergy(player)) {
            double compensation = ModularItems.computeProperty(pants, "Sprint Exhaustion Compensation");
            drain(player, sprintCost * horzMovement * 5.0);
            addExhaustion(player, -0.01 * exhaustion * compensation);
        }
    }

    /**
     * {@code StepAssistModule}: active sets the step height to 1.001, inactive on worn leggings resets it; taking the
     * leggings off keeps it like the original.
     */
    private static void stepAssist(Player player, boolean active) {
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step == null) {
            return;
        }
        if (active) {
            if (step.getModifier(STEP_ASSIST_ID) == null) {
                step.addTransientModifier(new AttributeModifier(STEP_ASSIST_ID, STEP_ASSIST_BONUS,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (step.getModifier(STEP_ASSIST_ID) != null) {
            step.removeModifier(STEP_ASSIST_ID);
        }
    }

    /** {@code landMovementFactor × multiplier} as a transient movement speed modifier; {@code null} removes it. */
    private static void setSpeedMultiplier(Player player, Double multiplier) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        AttributeModifier current = speed.getModifier(SPRINT_ASSIST_ID);
        if (multiplier == null) {
            if (current != null) {
                speed.removeModifier(SPRINT_ASSIST_ID);
            }
        } else if (current == null || current.amount() != multiplier - 1.0) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(SPRINT_ASSIST_ID, multiplier - 1.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** {@code jumpMovementFactor} without MPS: 0.02, sprinting 0.026. */
    private static float vanillaAirSpeed(Player player) {
        return player.isSprinting() ? 0.025999999F : 0.02F;
    }

    /** {@code MovementManager.handleLivingJumpEvent}: Jump Assist pays once per jump and sets the jump ticks. */
    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack pants = player.getItemBySlot(EquipmentSlot.LEGS);
        if (pants.getItem() instanceof PowerArmorItem && ModularItems.hasActiveModule(pants, MpsModules.JUMP_ASSIST)) {
            double jumpAssist = ModularItems.computeProperty(pants, "Jump Boost") * 2.0;
            double cost = ModularItems.computeProperty(pants, "Jump Energy Consumption");
            if (cost < MpsEnergy.getPlayerEnergy(player)) {
                drain(player, cost);
                setJumpTicks(player, jumpAssist);
                double compensation = ModularItems.computeProperty(pants, "Jump Exhaustion Compensation");
                addExhaustion(player, (player.isSprinting() ? -0.8 : -0.2) * compensation);
            }
        }
    }

    /** {@code MovementManager.handleFallEvent}: Shock Absorber above 3 m. */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (ModularItems.hasActiveModule(boots, MpsModules.SHOCK_ABSORBER) && event.getDistance() > 3.0F) {
            double absorb = event.getDistance() * ModularItems.computeProperty(boots, "Distance Reduction");
            double cost = absorb * ModularItems.computeProperty(boots, "Impact Energy consumption");
            if (cost < MpsEnergy.getPlayerEnergy(player)) {
                drain(player, cost);
                event.setDistance((float) (event.getDistance() - absorb));
            }
        }
    }

    /** {@code MovementManager.computeFallHeightFromVelocity}. */
    public static double computeFallHeightFromVelocity(double velocity) {
        double ticks = velocity / DEFAULT_GRAVITY;
        return 0.03920000076293945 * ticks * ticks;
    }

    public static double getWeightPenaltyRatio(double currentWeight, double capacity) {
        return currentWeight < capacity ? 1.0 : capacity / currentWeight;
    }

    private static ItemStack modular(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return ModularItems.isModular(stack) ? stack : ItemStack.EMPTY;
    }

    private static boolean active(ItemStack stack, String module) {
        return !stack.isEmpty() && ModularItems.hasActiveModule(stack, module);
    }

    /** Energy and food are server state; the client only moves. */
    private static void drain(Player player, double joules) {
        if (!player.level().isClientSide()) {
            MpsEnergy.drainPlayerEnergy(player, joules);
        }
    }

    private static void addExhaustion(Player player, double amount) {
        if (!player.level().isClientSide()) {
            player.getFoodData().addExhaustion((float) amount);
        }
    }
}
