package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.thermalexpansion.transport.ConduitBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** {@code RenderBlockConduit.renderInventoryBlock}. */
public class ConduitItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private ConduitRenderer renderer;

    public ConduitItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (renderer == null) {
            renderer = new ConduitRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(ConduitRenderer.LAYER));
        }
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ConduitBlock block) {
            renderer.renderItem(block.isEnergy(), pose, buffers, light, overlay);
        }
    }
}
