package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.thermalexpansion.energy.TeEngineBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** {@code RenderItemEngine}: the engine facing up, inactive, piston a quarter out. */
public class TeEngineItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private TeEngineRenderer renderer;

    public TeEngineItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (renderer == null) {
            renderer = new TeEngineRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(TeEngineRenderer.LAYER));
        }
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof TeEngineBlock block) {
            renderer.render(block.getEngineType(), false, 0.25F, Direction.UP, pose, buffers, light, overlay);
        }
    }
}
