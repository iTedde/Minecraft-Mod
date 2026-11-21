package ivorius.psychedelicraft.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.client.render.DrugRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.injection.At.Shift;

@Mixin(ItemInHandRenderer.class)
abstract class MixinHeldItemRenderer {
    @Inject(
        method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
        at = @At(
            value = "FIELD",
            target = "net/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection.renderMainHand:Z",
            opcode = Opcodes.GETFIELD,
            shift = Shift.BEFORE
        ))
    private void onRenderItem(float tickDelta, PoseStack matrices, MultiBufferSource.BufferSource vertexConsumers, LocalPlayer player, int light, CallbackInfo info) {
        DrugRenderer.INSTANCE.distortHand(matrices);
    }
}
