package ivorius.psychedelicraft.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Mixin(SoundEngine.class)
abstract class MixinSoundSystem {
    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void getSoundVolume(@Nullable SoundSource category, CallbackInfoReturnable<Float> info) {
        DrugProperties.of((Entity)Minecraft.getInstance().player).ifPresent(properties -> {
            info.setReturnValue(Mth.clamp(info.getReturnValueF() * properties.getModifier(Drug.SOUND_VOLUME), 0, 1));
        });
    }
}
