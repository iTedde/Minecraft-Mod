package dev.craft.thermalexpansion.client;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.thermalexpansion.transport.TesseractBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** {@code renderInventoryBlock}: frame and ender core like in the world. */
public class TesseractItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private TesseractRenderer renderer;

    public TesseractItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (!(stack.getItem() instanceof BlockItem item && item.getBlock() instanceof TesseractBlock block)) {
            return;
        }
        if (renderer == null) {
            renderer = new TesseractRenderer(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(EnergyCellRenderer.LAYER));
        }
        renderer.render(block.getType(), pose, buffers, light, overlay);
    }
}
