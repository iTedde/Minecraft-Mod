package dev.craft.ic2.item.armor;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.FilledTinCanItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** IC2 {@code ItemArmorQuantumSuit}: 1 000 000 EU, tier 3, full absorption, 900 EU per damage, suit abilities. */
public class QuantumArmorItem extends ElectricArmorItem {
    // Per player as in IC2 (client and server players are separate objects); weak keys forget logged-out players.
    private static final Map<Player, Integer> SPEED_TICKER = new WeakHashMap<>();
    private static final Map<Player, Float> JUMP_CHARGE = new WeakHashMap<>();

    public QuantumArmorItem(Type type, Properties properties) {
        super(Ic2ArmorMaterials.QUANTUM, type, properties, 1000000, 1000, 3);
    }

    private static int getPotionRemovalCost(Holder<MobEffect> effect) {
        if (effect.is(MobEffects.POISON) || effect.is(Ic2MobEffects.RADIATION)) {
            return 10000;
        }
        return effect.is(MobEffects.WITHER) ? 25000 : 0;
    }

    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        if (source.is(DamageTypes.FALL) && type == Type.BOOTS) {
            return new ArmorProperties(10, 1.0, getDamageLimit(stack));
        }
        return super.getProperties(entity, stack, source, damage, slot);
    }

    @Override
    public boolean onFall(Player player, ItemStack stack, int fallDamage) {
        if (type != Type.BOOTS) {
            return false;
        }
        int energyCost = getEnergyPerDamage() * fallDamage;
        if (energyCost <= ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true)) {
            ElectricItems.discharge(stack, energyCost, Integer.MAX_VALUE, true, false);
            return true;
        }
        return false;
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return type == Type.CHESTPLATE ? 1.1 : 1.0;
    }

    @Override
    public int getEnergyPerDamage() {
        return 900;
    }

    @Override
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
        switch (type) {
            case HELMET -> {
                if (!level.isClientSide()) {
                    tickHelmet(player, stack);
                }
            }
            case CHESTPLATE -> {
                if (!level.isClientSide()) {
                    player.clearFire();
                }
            }
            case LEGGINGS -> tickLeggings(player, stack);
            case BOOTS -> tickBoots(player, stack);
            default -> {
            }
        }
    }

    private static void tickHelmet(Player player, ItemStack stack) {
        int air = player.getAirSupply();
        if (ElectricItems.canUse(stack, 1000) && air < 100) {
            player.setAirSupply(air + 200);
            ElectricItems.use(stack, 1000, null);
        }
        if (ElectricItems.canUse(stack, 1000) && player.getFoodData().needsFood()) {
            eatTinCan(player, stack);
        }
        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
            int cost = getPotionRemovalCost(effect.getEffect());
            if (cost > 0) {
                cost *= effect.getAmplifier() + 1;
                if (ElectricItems.canUse(stack, cost)) {
                    ElectricItems.use(stack, cost, null);
                    player.removeEffect(effect.getEffect());
                }
            }
        }
    }

    /** Eats the first filled tin can of the main inventory: food, variant effects, heal and empty can, 1000 EU. */
    private static void eatTinCan(Player player, ItemStack helmet) {
        for (ItemStack can : player.getInventory().items) {
            if (can.is(Ic2Items.FILLED_TIN_CAN.get())) {
                player.getFoodData().eat(FilledTinCanItem.FOOD);
                FilledTinCanItem.applyEffects(player, FilledTinCanItem.getMeta(can));
                FilledTinCanItem.onEaten(player);
                can.shrink(1);
                ElectricItems.use(helmet, 1000, null);
                return;
            }
        }
    }

    /**
     * Spec 37 §3.1: no sprint boost any more. Sprinting still costs 1 000 EU every 10 ticks as in IC2; in return the
     * sprint exhaustion is cancelled ({@link #sprintsWithoutHunger}) and knockback is ignored ({@link #ignoresKnockback}).
     */
    private static void tickLeggings(Player player, ItemStack stack) {
        Vec3 motion = player.getDeltaMovement();
        if (ElectricItems.canUse(stack, 1000)
                && (player.onGround() && Math.abs(motion.x) + Math.abs(motion.z) > 0.1F || player.isInWater())
                && player.isSprinting()) {
            int speedTicker = SPEED_TICKER.getOrDefault(player, 0);
            if (++speedTicker >= 10) {
                speedTicker = 0;
                ElectricItems.use(stack, 1000, null);
            }
            SPEED_TICKER.put(player, speedTicker);
        }
    }

    /** Charged quantum leggings (at least 1 000 EU) are worn. */
    private static boolean wearsChargedLeggings(Player player) {
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        return legs.getItem() instanceof QuantumArmorItem armor && armor.type == Type.LEGGINGS
                && ElectricItems.canUse(legs, 1000);
    }

    /** Spec 37 §3.1: sprinting (walking, swimming, jumping) costs no hunger with charged quantum leggings. */
    public static boolean sprintsWithoutHunger(Player player) {
        return player.isSprinting() && wearsChargedLeggings(player);
    }

    /** Spec 37 §3.1: charged quantum leggings give full knockback resistance. */
    public static boolean ignoresKnockback(Player player) {
        return wearsChargedLeggings(player);
    }

    private static void tickBoots(Player player, ItemStack stack) {
        float jumpCharge = JUMP_CHARGE.getOrDefault(player, 1.0F);
        if (ElectricItems.canUse(stack, 1000) && player.onGround() && jumpCharge < 1.0F) {
            jumpCharge = 1.0F;
            ElectricItems.use(stack, 1000, null);
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= 0.0 && jumpCharge > 0.0F && !player.isInWater()) {
            if (Ic2Keyboard.isJumpKeyDown(player) && Ic2Keyboard.isBoostKeyDown(player)) {
                double x = motion.x;
                double z = motion.z;
                player.setDeltaMovement(x, motion.y + jumpCharge * 0.3F, z);
                jumpCharge = (float) (jumpCharge * 0.75);
            } else if (jumpCharge < 1.0F) {
                jumpCharge = 0.0F;
            }
        }
        JUMP_CHARGE.put(player, jumpCharge);
    }
}
