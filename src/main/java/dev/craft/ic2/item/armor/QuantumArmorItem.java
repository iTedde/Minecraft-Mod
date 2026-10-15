package dev.craft.ic2.item.armor;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** IC2 {@code ItemArmorQuantumSuit}: 1 000 000 EU, tier 3, full absorption, 900 EU per damage, suit abilities. */
public class QuantumArmorItem extends ElectricArmorItem {
    /** IC2 config {@code enableQuantumSpeedOnSprint} as set in the pack. */
    private static final boolean SPEED_ON_SPRINT = true;

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
        // Eating filled tin cans when hungry follows with the tin can (canning machine).
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

    private static void tickLeggings(Player player, ItemStack stack) {
        Vec3 motion = player.getDeltaMovement();
        boolean sprint = SPEED_ON_SPRINT ? player.isSprinting() : Ic2Keyboard.isBoostKeyDown(player);
        if (ElectricItems.canUse(stack, 1000)
                && (player.onGround() && Math.abs(motion.x) + Math.abs(motion.z) > 0.1F || player.isInWater())
                && sprint) {
            int speedTicker = SPEED_TICKER.getOrDefault(player, 0);
            if (++speedTicker >= 10) {
                speedTicker = 0;
                ElectricItems.use(stack, 1000, null);
            }
            SPEED_TICKER.put(player, speedTicker);
            float speed = 0.22F;
            if (player.isInWater()) {
                speed = 0.1F;
                if (Ic2Keyboard.isJumpKeyDown(player)) {
                    player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.1F, 0.0));
                }
            }
            player.moveRelative(speed, new Vec3(0.0, 0.0, 1.0));
        }
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
                if (jumpCharge == 1.0F) {
                    x *= 3.5;
                    z *= 3.5;
                }
                player.setDeltaMovement(x, motion.y + jumpCharge * 0.3F, z);
                jumpCharge = (float) (jumpCharge * 0.75);
            } else if (jumpCharge < 1.0F) {
                jumpCharge = 0.0F;
            }
        }
        JUMP_CHARGE.put(player, jumpCharge);
    }
}
