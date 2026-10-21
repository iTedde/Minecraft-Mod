package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.thermalexpansion.TeDataComponents;
import dev.craft.thermalexpansion.energy.EnergyCellData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@code renderInventoryBlock}: frame and core; the level comes from the 1.4.7 damage, {@code (150 − damage) / 10}
 * (empty 0, full 14).
 */
public class EnergyCellItemRenderer extends BlockEntityWithoutLevelRenderer {
    @Nullable
    private EnergyCellRenderer renderer;

    public EnergyCellItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                             int light, int overlay) {
        if (renderer == null) {
            renderer = new EnergyCellRenderer(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(EnergyCellRenderer.LAYER));
        }
        EnergyCellData data = stack.getOrDefault(TeDataComponents.ENERGY_CELL.get(), EnergyCellData.DEFAULT);
        int damage = 151 - data.scaledEnergy(150);
        renderer.render(Math.max(0, (150 - damage) / 10), pose, buffers, light, overlay);
    }
}
