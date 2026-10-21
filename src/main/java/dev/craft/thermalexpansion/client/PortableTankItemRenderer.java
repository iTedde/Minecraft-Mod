package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.thermalexpansion.TeDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.jetbrains.annotations.Nullable;

/** {@code RenderItemTank}: the tank in normal mode with the liquid of the item. */
public class PortableTankItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private PortableTankRenderer renderer;

    public PortableTankItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (renderer == null) {
            renderer = new PortableTankRenderer(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(PortableTankRenderer.LAYER));
        }
        SimpleFluidContent liquid = stack.getOrDefault(TeDataComponents.TANK_LIQUID.get(), SimpleFluidContent.EMPTY);
        renderer.render(false, liquid.copy(), pose, buffers, light, overlay);
    }
}
