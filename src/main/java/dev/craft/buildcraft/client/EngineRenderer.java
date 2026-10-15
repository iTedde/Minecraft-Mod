package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** BuildCraft {@code RenderEngine.renderTileEntityAt}. */
public class EngineRenderer implements BlockEntityRenderer<EngineBlockEntity> {
    private final EngineModel model;

    public EngineRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new EngineModel(context.bakeLayer(EngineModel.LAYER));
    }

    @Override
    public void render(EngineBlockEntity engine, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        model.render(engine.getEngineType(), engine.getEnergyStage(), engine.getProgress(), engine.getOrientation(),
                pose, buffers, light, overlay);
    }
}
