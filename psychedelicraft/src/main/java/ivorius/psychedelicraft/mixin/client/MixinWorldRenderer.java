package ivorius.psychedelicraft.mixin.client;

import java.util.SortedSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import ivorius.psychedelicraft.client.render.DrugRenderer;
import ivorius.psychedelicraft.client.render.BlockBreakingProgressAccessor;
import ivorius.psychedelicraft.client.render.RenderPhase;

@Mixin(LevelRenderer.class)
abstract class MixinWorldRenderer implements BlockBreakingProgressAccessor {
    private static final String SKY = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V";
    private static final String CLOUDS = "renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V";

    @Override
    @Accessor("destructionProgress")
    public abstract Long2ObjectMap<SortedSet<BlockDestructionProgress>> getBlockBreakingProgressions();

    @Inject(method = SKY, at = @At("HEAD"))
    private void beforeRenderSky(CallbackInfo info) {
        RenderPhase.SKY.push();
    }

    @Inject(method = CLOUDS, at = @At("HEAD"))
    private void beforeRenderClouds(CallbackInfo info) {
        RenderPhase.CLOUDS.push();
    }

    @Inject(method = { CLOUDS, SKY }, at = @At("RETURN"))
    private void afterRenderClouds(CallbackInfo info) {
        RenderPhase.pop();
    }

    @Inject(method = "doEntityOutline()V", at = @At("RETURN"))
    public void onDrawEntityOutlinesFramebuffer(CallbackInfo info) {
        DrugRenderer.INSTANCE.onAfterRenderWorld();
    }
}
