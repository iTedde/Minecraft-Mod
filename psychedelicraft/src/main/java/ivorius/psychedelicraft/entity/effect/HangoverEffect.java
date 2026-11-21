package ivorius.psychedelicraft.entity.effect;

import java.util.Set;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

/**
 * Display effect of the hangover. The actual state lives in {@link ivorius.psychedelicraft.entity.drug.Hangover};
 * this effect only shows level and remaining time and drives the client-side glare.
 * Milk (or any other cure) does not remove it.
 */
public class HangoverEffect extends MobEffect {
    public HangoverEffect() {
        super(MobEffectCategory.HARMFUL, 0x9c8a3a);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // no cures: milk does not help against a hangover
    }
}
