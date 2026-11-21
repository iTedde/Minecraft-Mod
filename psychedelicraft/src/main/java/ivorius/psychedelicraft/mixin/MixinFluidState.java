package ivorius.psychedelicraft.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.material.FluidState;

@Mixin(FluidState.class)
abstract class MixinFluidState {
    @Inject(method = "getDripParticle()Lnet/minecraft/core/particles/ParticleOptions;", at = @At("HEAD"), cancellable = true)
    private void getStateAwareParticle(CallbackInfoReturnable<@Nullable ParticleOptions> info) {
        FluidState self = (FluidState)(Object)this;
        if (self.getType() instanceof PlacedFluid p) {
            info.setReturnValue(p.getParticle(self));
        }
    }
}
