package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.buildcraft.engine.EnergyStage;
import dev.craft.buildcraft.engine.EngineBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** BuildCraft {@code RenderEngine.inventoryRender}: blue engine, piston at 0.25, output up. */
public class EngineItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private EngineModel model;

    public EngineItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof EngineBlock block)) {
            return;
        }
        if (model == null) {
            model = new EngineModel(Minecraft.getInstance().getEntityModels().bakeLayer(EngineModel.LAYER));
        }
        model.render(block.getEngineType(), EnergyStage.BLUE, 0.25F, Direction.UP, pose, buffers, light, overlay);
    }
}
