package ivorius.psychedelicraft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.RenderPhase;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
abstract class MixinGameRenderer {
    @Shadow
    private @Final Camera mainCamera;

    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void onRenderWorld(PoseStack matrices, float tickDelta, CallbackInfo info) {
        DrugRenderer.INSTANCE.distortScreen(matrices, mainCamera, tickDelta);
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void beforeRenderWorld(DeltaTracker tickCounter, CallbackInfo info) {
        RenderPhase.WORLD.push();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void afterRenderWorld(DeltaTracker tickCounter, CallbackInfo info) {
        RenderPhase.pop();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        // Minecraft's constructor resizes the window before mods are loaded; DrugRenderer's static init would then
        // register DrugTypes/sounds into frozen registries and crash the game. Shaders are not loaded yet anyway.
        if (!ivorius.psychedelicraft.Psychedelicraft.isBootstrapped()) {
            return;
        }
        DrugRenderer.INSTANCE.getPostEffects().setupDimensions(width, height);
    }
}
