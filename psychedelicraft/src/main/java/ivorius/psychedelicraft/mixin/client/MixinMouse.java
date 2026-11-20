package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ivorius.psychedelicraft.client.render.SmoothCameraHelper;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
abstract class MixinMouse {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void beforeUpdateMouse(CallbackInfo info) {
        SmoothCameraHelper.INSTANCE.setCursorDelta((float)accumulatedDX, (float)accumulatedDY);
    }

    @Inject(method = "turnPlayer", at = @At("RETURN"))
    private void onUpdateMouse(CallbackInfo info) {
        SmoothCameraHelper.INSTANCE.applyCameraChange();
    }
}
