/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;

public class TrayBlockEntityRenderer extends LabelledBlockEntityRenderer<TrayBlockEntity> {
    private static final ResourceLocation FLUID_TEXTURE = Psychedelicraft.id("textures/entity/tray/fluid.png");

    private TrayContentsModel contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().bakeRoot());

    public TrayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TrayBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        if (entity.getFillLevel() > 0 || entity.isHardened()) {
            matrices.pushPose();
            matrices.translate(0.5, 1 / 16D, 0.5);
            contentsModel = new TrayContentsModel(TrayContentsModel.getTexturedModelData().bakeRoot());
            contentsModel.setAngles(entity, tickDelta);
            contentsModel.renderToBuffer(matrices, vertices.getBuffer(
                    entity.isHardened() ? RenderType.entitySolid(FLUID_TEXTURE) : RenderType.entityTranslucent(FLUID_TEXTURE)
            ), light, overlay);

            matrices.popPose();
        }
        super.render(entity, tickDelta, matrices, vertices, light, overlay);
    }

    @Override
    protected void renderLabels(TrayBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        if (entity.getFillLevel() > 0) {
            int percentage = (int)(100 * entity.getFillLevel() / 50F);

            Component fillText = switch (percentage) {
                case 100 -> Component.literal("Full");
                case 0 -> Component.literal("Empty");
                default -> Component.literal(percentage + "%");
            };

            textRenderer.drawInBatch(fillText, -(textRenderer.width(fillText) - 5) / 2F, -textRenderer.lineHeight - 2, CommonColors.WHITE, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);
        }

        if (entity.getCraftingResult().isPresent()) {

            Component text = entity.getCraftingResult().get().getHoverName();

            textRenderer.drawInBatch(text, -(textRenderer.width(text) - 5) / 2F, 0, CommonColors.BLUE, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);

        }
    }
}
