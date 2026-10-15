package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** BuildCraft {@code RenderRefinery.inventoryRender}: the empty refinery. */
public class RefineryItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private RefineryRenderer renderer;

    public RefineryItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (renderer == null) {
            renderer = new RefineryRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(RefineryRenderer.LAYER));
        }
        renderer.render(null, pose, buffers, light, overlay);
    }
}
