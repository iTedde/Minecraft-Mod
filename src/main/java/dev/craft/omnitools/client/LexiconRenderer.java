package dev.craft.omnitools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.omnitools.LexiconBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** {@code RenderLexicon}: the enchanting table book with the lexicon texture, 0.75 above the plinth, no bobbing. */
public class LexiconRenderer implements BlockEntityRenderer<LexiconBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/omnitools/lexicon.png");

    private final BookModel book;

    public LexiconRenderer(BlockEntityRendererProvider.Context context) {
        book = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(LexiconBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.75F, 0.5F);
        float turn = entity.rot - entity.oRot;
        while (turn >= Mth.PI) {
            turn -= Mth.TWO_PI;
        }
        while (turn < -Mth.PI) {
            turn += Mth.TWO_PI;
        }
        float rotation = entity.oRot + turn * partialTick;
        poseStack.mulPose(Axis.YP.rotation(-rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float flip = Mth.lerp(partialTick, entity.oFlip, entity.flip);
        float left = Mth.frac(flip + 0.25F) * 1.6F - 0.3F;
        float right = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
        float open = Mth.lerp(partialTick, entity.oOpen, entity.open);
        book.setupAnim(entity.time + partialTick, Mth.clamp(left, 0.0F, 1.0F), Mth.clamp(right, 0.0F, 1.0F), open);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(TEXTURE));
        book.render(poseStack, consumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }
}
