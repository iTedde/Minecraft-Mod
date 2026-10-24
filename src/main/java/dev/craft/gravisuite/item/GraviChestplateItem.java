package dev.craft.gravisuite.item;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.gravisuite.GsDataComponents;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ElectricArmorItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * GraviSuite {@code ItemGraviChestPlate} with {@code GraviChestPlate*Proxy} and the chest part of
 * {@code ServerTickHandler} (spec 28 §2, §5): creative-like flight for 416 EU per tick, boost for 2 × 1 248 EU,
 * protection like the QuantumSuit chestplate; 10 000 000 EU, transfer 20 000, provides energy.
 */
@EventBusSubscriber(modid = Craft.MODID)
public class GraviChestplateItem extends ElectricArmorItem {
    public static final int MAX_CHARGE = 10000000;
    public static final int MIN_CHARGE = 80000;
    public static final int ENERGY_PER_TICK = 416;
    public static final int BOOST_MULTIPLIER = 3;
    public static final float BOOST_SPEED = 0.11F;
    /** {@code ServerTickHandler}: 262 in 1.4.7 (256 + 6), here the build height + 6. */
    public static final int HEIGHT_ABOVE_BUILD_LIMIT = 6;

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Craft.MODID);
    /** The {@code ServerTickHandler} maps, per player and not saved (the pack keyed them by player object). */
    private static final Supplier<AttachmentType<FlightState>> FLIGHT_STATE =
            ATTACHMENT_TYPES.register("gravi_flight_state", () -> AttachmentType.builder(FlightState::new).build());

    public GraviChestplateItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.CHESTPLATE, properties, MAX_CHARGE, 20000, 2);
    }

    public static boolean isFlyActive(ItemStack stack) {
        return stack.getOrDefault(GsDataComponents.FLY_ACTIVE.get(), false);
    }

    private static void setFlyActive(ItemStack stack, boolean active) {
        stack.set(GsDataComponents.FLY_ACTIVE.get(), active);
    }

    /** Fly key ({@code switchFlyModeServer}). */
    public static void switchFlyMode(Player player, ItemStack stack) {
        Abilities abilities = player.getAbilities();
        FlightState state = player.getData(FLIGHT_STATE);
        if (isFlyActive(stack)) {
            if (!abilities.instabuild) {
                abilities.mayfly = false;
                abilities.flying = false;
            }
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_off"));
            state.flyActiveByMod = false;
            setFlyActive(stack, false);
        } else if (ElectricItems.getCharge(stack) < MIN_CHARGE && !abilities.instabuild) {
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_no_energy"));
        } else {
            player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_on"));
            abilities.mayfly = true;
            abilities.flying = true;
            state.flyActiveByMod = true;
            setFlyActive(stack, true);
        }
        syncAbilities(player);
    }

    /**
     * World load ({@code firstLoadServer}): an active engine is switched off and on again, so the minimum charge is
     * checked anew. Original error: if that fails, the abilities saved with the player keep {@code mayfly}.
     */
    public static void firstLoad(Player player, ItemStack stack) {
        if (isFlyActive(stack)) {
            setFlyActive(stack, false);
            switchFlyMode(player, stack);
        }
    }

    /** {@code ServerTickHandler.tickStart} for the chest slot (server side). */
    public static void serverTick(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Abilities abilities = player.getAbilities();
        FlightState state = player.getData(FLIGHT_STATE);
        if (chest.getItem() instanceof GraviChestplateItem) {
            if (abilities.instabuild && !state.lastCreative) {
                state.lastCreative = true;
            } else if (!abilities.instabuild && state.lastCreative && state.flyActiveByMod) {
                abilities.mayfly = true;
                abilities.flying = true;
                state.lastCreative = false;
            }
            tickWorn(player, chest, state);
            double maxY = player.level().getMaxBuildHeight() + HEIGHT_ABOVE_BUILD_LIMIT;
            if (player.getY() > maxY && !abilities.instabuild) {
                player.teleportTo(player.getX(), maxY, player.getZ());
            }
        } else if (state.flyActiveByMod) {
            abilities.mayfly = false;
            abilities.flying = false;
            state.flyActiveByMod = false;
            state.lastUndressed = true;
            syncAbilities(player);
        }
    }

    /** {@code onTickServer}; the boost movement itself happens on the client ({@link #onArmorTick}). */
    private static void tickWorn(Player player, ItemStack stack, FlightState state) {
        if (state.lastUndressed) {
            setFlyActive(stack, false);
            state.lastUndressed = false;
        }
        if (isFlyActive(stack)) {
            Abilities abilities = player.getAbilities();
            boolean creative = abilities.instabuild;
            int charge = ElectricItems.getCharge(stack);
            if (!creative) {
                if (charge < ENERGY_PER_TICK) {
                    player.sendSystemMessage(Component.translatable("message.craft.gravisuite.gravi_depleted"));
                    switchFlyMode(player, stack);
                } else {
                    use(stack, ENERGY_PER_TICK);
                }
            }
            player.resetFallDistance();
            if (!player.onGround() && abilities.flying && Ic2Keyboard.isBoostKeyDown(player)) {
                int boostCost = ENERGY_PER_TICK * BOOST_MULTIPLIER;
                // boostModeServer: the server moves with zero input, so only its discharge has an effect.
                if (isFlyActive(stack) && !player.isInWater() && !creative && ElectricItems.getCharge(stack) > boostCost) {
                    use(stack, boostCost);
                }
                if (charge <= boostCost && !creative) {
                    player.displayClientMessage(Component.translatable("message.craft.gravisuite.gravi_no_boost"), true);
                } else if (!creative) {
                    // Original error: the boost is paid a second time.
                    use(stack, boostCost);
                }
            }
        }
        syncAbilities(player);
    }

    /** Client side of {@code onTickClient}: boost movement with the player's input, height limit. */
    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        if (!level.isClientSide() || !player.isLocalPlayer()) {
            return;
        }
        Abilities abilities = player.getAbilities();
        boolean creative = abilities.instabuild;
        if (isFlyActive(stack) && !player.onGround() && abilities.flying && Ic2Keyboard.isBoostKeyDown(player)
                && (ElectricItems.getCharge(stack) > ENERGY_PER_TICK * BOOST_MULTIPLIER || creative)) {
            if (!player.isInWater()) {
                // xxa/zza are the movement inputs (±1, × 0.3 while sneaking) like KeyboardClient.updatePlayerMove.
                player.moveRelative(BOOST_SPEED, new Vec3(player.xxa, 0.0, player.zza));
            }
            double boostY = BOOST_SPEED + 0.03F;
            Vec3 motion = player.getDeltaMovement();
            if (Ic2Keyboard.isJumpKeyDown(player)) {
                motion = motion.add(0.0, boostY, 0.0);
            }
            if (player.isShiftKeyDown()) {
                motion = motion.add(0.0, -boostY, 0.0);
            }
            player.setDeltaMovement(motion);
        }
        double maxY = level.getMaxBuildHeight() + HEIGHT_ABOVE_BUILD_LIMIT;
        if (player.getY() > maxY && !creative) {
            player.setPos(player.getX(), maxY, player.getZ());
        }
    }

    /** Discharge at tier 3 without transfer limit ({@code ElectricItem.discharge(stack, n, 3, true, false)}). */
    private static void use(ItemStack stack, int amount) {
        ElectricItems.discharge(stack, amount, 3, true, false);
    }

    private static void syncAbilities(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.onUpdateAbilities();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) {
            serverTick(event.getEntity());
        }
    }

    /** Replaces the client's {@code worldLoad} packet: the server knows when a player joins. */
    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof GraviChestplateItem) {
            firstLoad(player, chest);
        }
    }

    @Override
    public boolean canProvideEnergy() {
        return true;
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 1.1;
    }

    @Override
    public int getEnergyPerDamage() {
        return 900;
    }

    /** Always 9, also when empty. */
    @Override
    public int getArmorDisplay(ItemStack stack) {
        return 9;
    }

    /** {@code isFlyActiveByMod}, {@code lastUndressed}, {@code isLastCreativeState}. */
    private static final class FlightState {
        boolean flyActiveByMod;
        boolean lastUndressed;
        boolean lastCreative;
    }
}
