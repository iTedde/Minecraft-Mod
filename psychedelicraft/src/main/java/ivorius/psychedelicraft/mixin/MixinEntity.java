package ivorius.psychedelicraft.mixin;

import java.util.stream.DoubleStream;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.entity.TouchingWaterAccessor;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

@Mixin(Entity.class)
abstract class MixinEntity implements TouchingWaterAccessor {
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Override
    @Accessor("wasTouchingWater")
    public abstract void setTouchingWater(boolean touchingWater);

    private FluidState collidedFluid = Fluids.EMPTY.defaultFluidState();

    // NeoForge: the fluid scan moved into the parameterless, FluidType based variant
    @ModifyReceiver(method = "updateFluidHeightAndDoFluidPushing()V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;getFlow(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"
    ))
    private FluidState captureFluid(FluidState state, BlockGetter view, BlockPos pos) {
        if (state.is(FluidTags.WATER)) {
            collidedFluid = state;
        }
        return state;
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z", at = @At("HEAD"))
    private void clearCollidedFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        if (tag == FluidTags.WATER) {
            collidedFluid = Fluids.EMPTY.defaultFluidState();
        }
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z", at = @At("RETURN"), cancellable = true)
    private void onUpdateMovementInFluid(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> info) {
        if (!info.getReturnValueZ()) {
            Entity self = (Entity)(Object)this;
            AABB box = self.getBoundingBox().inflate(0.001);
            BlockPos.betweenClosedStream(self.getBoundingBox().deflate(0.001)).flatMapToDouble(pos -> {
                BlockState state = self.level().getBlockState(pos);
                if (!(state.getBlock() instanceof FluidFilled tub)) {
                    return DoubleStream.empty();
                }

                return tub.getContainedFluid(self.level(), state, pos).stream()
                        .filter(fluidState -> fluidState.is(tag))
                        .mapToDouble(fluidState -> {
                    if (fluidState.is(tag)) {
                        if (tag == FluidTags.WATER) {
                            collidedFluid = fluidState;
                        }
                        AABB fluidBox = tub.getFluidCollisionBox(self.level(), state, pos);
                        if (fluidBox.intersects(box)) {
                            return fluidBox.getYsize();
                        }
                    }
                    return -1;
                });
            }).filter(l -> l > 0).findFirst().ifPresent(level -> {
                fluidHeight.put(tag, level);
                info.setReturnValue(true);
            });
        }
    }

    @ModifyArg(method = "doWaterSplashEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private ParticleOptions replaceSplashParticle(ParticleOptions parameters) {
        SimpleFluid fluid = SimpleFluid.of(collidedFluid.getType());
        if (fluid.isCustomFluid()) {
            if (parameters == ParticleTypes.BUBBLE) {
                return new FluidParticleEffect(PSParticles.FLUID_BUBBLE, fluid.getStack(collidedFluid, 1));
            }
            if (parameters == ParticleTypes.SPLASH) {
                return new FluidParticleEffect(PSParticles.FLUID_SPLASH, fluid.getStack(collidedFluid, 1));
            }
        }
        return parameters;
    }
}
