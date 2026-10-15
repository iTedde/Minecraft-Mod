package dev.craft.ic2;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2MobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Craft.MODID);

    /** IC2 {@code IC2Potion.radiation}: {@code amplifier + 1} damage every {@code 25 >> amplifier} ticks. */
    public static final DeferredHolder<MobEffect, RadiationEffect> RADIATION =
            EFFECTS.register("radiation", RadiationEffect::new);

    private Ic2MobEffects() {
    }

    public static final class RadiationEffect extends MobEffect {
        private RadiationEffect() {
            super(MobEffectCategory.HARMFUL, 0x4E9331);
        }

        @Override
        public boolean applyEffectTick(LivingEntity entity, int amplifier) {
            entity.hurt(entity.damageSources().source(CraftDamageTypes.RADIATION), amplifier + 1);
            return true;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            int rate = 25 >> amplifier;
            return rate <= 0 || duration % rate == 0;
        }
    }
}
