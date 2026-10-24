package dev.craft.mps;

import dev.craft.Craft;
import dev.craft.mps.MpsDataComponents.FoodBuffer;
import dev.craft.mps.MpsDataComponents.KineticPosition;
import dev.craft.mps.module.MpsModules;
import dev.craft.mps.module.PowerModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Environment and special modules of the MPS {@code PlayerTickHandler} (spec 29 §7): Night Vision, Active Camouflage,
 * Auto-Feeder, Solar and Kinetic Generator, plus the Water Electrolyzer ({@code EventHandler}) and the keybinding
 * toggle ({@code toggleModuleForPlayer}). Effects, food and generated energy are server state.
 */
@EventBusSubscriber(modid = Craft.MODID)
public final class MpsEnvironment {
    public static final int EFFECT_DURATION = 500;
    public static final int EFFECT_RENEW_BELOW = 210;
    public static final double NIGHT_VISION_ENERGY = 5.0;
    public static final double CAMOUFLAGE_ENERGY = 50.0;

    private MpsEnvironment() {
    }

    /**
     * Night Vision (helmet) and Active Camouflage (torso) before the movement; returns the energy drain. The search
     * for the own effects stops at the first one found and night vision comes first, so with both effects the
     * camouflage is renewed every tick and not removed (original loop).
     */
    public static double potionEffects(Player player, ItemStack helmet, ItemStack torso, double totalEnergy) {
        double totalEnergyDrain = 0.0;
        boolean hasNightVision = active(helmet, MpsModules.NIGHT_VISION);
        boolean hasInvis = active(torso, MpsModules.ACTIVE_CAMOUFLAGE);
        MobEffectInstance nightVision = ownEffect(player, MobEffects.NIGHT_VISION);
        MobEffectInstance invis = nightVision == null ? ownEffect(player, MobEffects.INVISIBILITY) : null;
        if (hasNightVision && totalEnergyDrain + NIGHT_VISION_ENERGY < totalEnergy) {
            if (nightVision == null || nightVision.getDuration() < EFFECT_RENEW_BELOW) {
                addEffect(player, MobEffects.NIGHT_VISION);
                totalEnergyDrain += NIGHT_VISION_ENERGY;
            }
        } else if (nightVision != null) {
            removeEffect(player, MobEffects.NIGHT_VISION);
        }
        if (hasInvis && totalEnergyDrain + CAMOUFLAGE_ENERGY < totalEnergy) {
            if (invis == null || invis.getDuration() < EFFECT_RENEW_BELOW) {
                addEffect(player, MobEffects.INVISIBILITY);
                totalEnergyDrain += CAMOUFLAGE_ENERGY;
            }
        } else if (invis != null) {
            removeEffect(player, MobEffects.INVISIBILITY);
        }
        return totalEnergyDrain;
    }

    /** The module's own effect: no particles, not ambient, amplifier 0, at most 500 ticks (spec 29 §14). */
    static MobEffectInstance ownEffect(Player player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        if (instance != null && !instance.isVisible() && !instance.isAmbient() && instance.getAmplifier() == 0
                && instance.getDuration() <= EFFECT_DURATION) {
            return instance;
        }
        return null;
    }

    private static void addEffect(Player player, Holder<MobEffect> effect) {
        if (!player.level().isClientSide()) {
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, 0, false, false, true));
        }
    }

    private static void removeEffect(Player player, Holder<MobEffect> effect) {
        if (!player.level().isClientSide()) {
            player.removeEffect(effect);
        }
    }

    /** Auto-Feeder, Solar and Kinetic Generator after the movement; returns the new total drain. */
    public static double generators(Player player, ItemStack helmet, ItemStack pants, double totalEnergy,
                                    double totalEnergyDrain) {
        if (player.level().isClientSide()) {
            return totalEnergyDrain;
        }
        if (active(helmet, MpsModules.AUTO_FEEDER)) {
            totalEnergyDrain = autoFeeder(player, helmet, totalEnergy, totalEnergyDrain);
        }
        if (active(helmet, MpsModules.SOLAR_GENERATOR)) {
            solarGenerator(player, helmet);
        }
        if (active(pants, MpsModules.KINETIC_GENERATOR) && !player.hasImpulse) {
            kineticGenerator(player, pants);
        }
        return totalEnergyDrain;
    }

    /**
     * Eats food from the inventory (hotbar first) into the helmet's buffer, then feeds the player from it. The energy
     * limit multiplies instead of dividing like the original.
     */
    static double autoFeeder(Player player, ItemStack helmet, double totalEnergy, double totalEnergyDrain) {
        FoodBuffer buffer = helmet.getOrDefault(MpsDataComponents.FOOD.get(), FoodBuffer.EMPTY);
        double foodLevel = buffer.food();
        double saturationLevel = buffer.saturation();
        double efficiency = ModularItems.computeProperty(helmet, "Auto-Feeder Efficiency");
        FoodData foodStats = player.getFoodData();
        int foodNeeded = 20 - foodStats.getFoodLevel();
        double eatingEnergyConsumption = foodNeeded
                * ModularItems.computeProperty(helmet, "Eating Energy Consumption");
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size() && foodNeeded > foodLevel; i++) {
            ItemStack stack = inventory.items.get(i);
            FoodProperties food = stack.isEmpty() ? null : stack.getFoodProperties(player);
            if (food != null) {
                while (!stack.isEmpty() && foodNeeded > foodLevel) {
                    foodLevel += food.nutrition() * efficiency / 100.0;
                    saturationLevel += saturationModifier(food) * efficiency / 100.0;
                    player.sendSystemMessage(Component.translatable("message.craft.mps.feeder_ate",
                            stack.getHoverName()));
                    stack.shrink(1);
                }
            }
        }
        int foodConsumed = (int) Math.min(foodNeeded,
                Math.min(foodLevel, eatingEnergyConsumption * (totalEnergy - totalEnergyDrain)));
        if (foodConsumed > 0) {
            if (saturationLevel >= 1.0) {
                foodStats.eat(foodConsumed, 1.0F);
                saturationLevel--;
            } else {
                foodStats.eat(foodConsumed, 0.0F);
            }
            foodLevel -= foodConsumed;
            totalEnergyDrain += eatingEnergyConsumption * foodConsumed;
        }
        helmet.set(MpsDataComponents.FOOD.get(), new FoodBuffer(foodLevel, saturationLevel));
        return totalEnergyDrain;
    }

    /** The 1.4.7 {@code ItemFood} saturation modifier; 1.21 stores nutrition × modifier × 2. */
    static double saturationModifier(FoodProperties food) {
        return food.nutrition() > 0 ? food.saturation() / (food.nutrition() * 2.0) : 0.0;
    }

    /**
     * Every 80 world ticks with the sky visible above the head and no rain: daytime or nighttime generation. The
     * original checks the biome's rain only every 20 ticks, which always includes the payout ticks.
     */
    static void solarGenerator(Player player, ItemStack helmet) {
        Level level = player.level();
        if (level.dimensionType().hasSkyLight() && level.getGameTime() % 80L == 0L) {
            solarPayout(player, helmet);
        }
    }

    /** One payout check of the Solar Generator; returns the generated joules. */
    static double solarPayout(Player player, ItemStack helmet) {
        Level level = player.level();
        BlockPos feet = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        boolean canRain = level.getBiome(feet).value().hasPrecipitation();
        boolean isRaining = canRain && (level.isThundering() || level.isRaining());
        boolean skyVisible = !isRaining && level.canSeeSky(feet.above());
        double generated = 0.0;
        if (skyVisible) {
            generated = ModularItems.computeProperty(helmet, level.isDay() ? "Daytime Solar Energy Generation"
                    : "Nighttime Solar Energy Generation");
            MpsEnergy.givePlayerEnergy(player, generated);
        }
        return generated;
    }

    /**
     * Pays out every 5 blocks of horizontal distance from the stored position (truncated coordinates); riding or
     * swimming only moves the stored position. Skipped while {@code hasImpulse} ({@code isAirBorne}) is set.
     */
    static void kineticGenerator(Player player, ItemStack pants) {
        KineticPosition stored = pants.get(MpsDataComponents.KINETIC.get());
        boolean isNotWalking = player.isPassenger() || player.isInWater();
        int px = (int) player.getX();
        int pz = (int) player.getZ();
        int x = stored == null || isNotWalking ? px : stored.x();
        int z = stored == null || isNotWalking ? pz : stored.z();
        double distance = Math.sqrt((double) (x - px) * (x - px) + (double) (z - pz) * (z - pz));
        if (distance >= 5.0) {
            x = px;
            z = pz;
            MpsEnergy.givePlayerEnergy(player,
                    ModularItems.computeProperty(pants, "Energy Generation Per 5 Blocks"));
        }
        pants.set(MpsDataComponents.KINETIC.get(), new KineticPosition(x, z));
    }

    /** {@code EventHandler.onLivingUpdate}: Water Electrolyzer refills the air below 10. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            waterElectrolyzer(player);
        }
    }

    static void waterElectrolyzer(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (ModularItems.isModular(helmet) && ModularItems.hasActiveModule(helmet, MpsModules.WATER_ELECTROLYZER)) {
            double energy = MpsEnergy.getPlayerEnergy(player);
            double energyConsumption = ModularItems.computeProperty(helmet, "Jolt Energy");
            if (energy > energyConsumption && player.getAirSupply() < 10) {
                MpsEnergy.drainPlayerEnergy(player, energyConsumption);
                player.setAirSupply(300);
            }
        }
    }

    /**
     * {@code toggleModuleForPlayer}: switches the module on every worn and held modular item that has it; only
     * toggleable modules (the keybinding GUI offers no others).
     */
    public static void toggleForPlayer(Player player, String module, boolean active) {
        PowerModule definition = MpsModules.get(module);
        if (definition == null || definition.getKind() != PowerModule.Kind.TOGGLEABLE) {
            return;
        }
        for (ItemStack stack : ModularItems.equipped(player)) {
            if (ModularItems.isModular(stack)) {
                ModularItems.setActive(stack, module, active);
            }
        }
    }

    private static boolean active(ItemStack stack, String module) {
        return !stack.isEmpty() && ModularItems.hasActiveModule(stack, module);
    }
}
