package ivorius.psychedelicraft.entity.effect;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public interface PSEffects {
    Holder<MobEffect> TEETH_GRINDING = register("teeth_grinding", new MobEffect(MobEffectCategory.HARMFUL, 0) {});

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Psychedelicraft.id(name), effect);
    }

    static void bootstrap() {}
}
