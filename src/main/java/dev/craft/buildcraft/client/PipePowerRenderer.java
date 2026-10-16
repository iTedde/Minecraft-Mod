package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.craft.Craft;
import dev.craft.buildcraft.transport.PowerPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * BuildCraft {@code RenderPipe.renderPower}: per side a square beam from the block edge to just past the center,
 * edge length {@code 0.005 + stage · (0.5 − 0.005) / 100} (100 display lists), texture 4 or 6 when overloaded.
 */
public class PipePowerRenderer implements BlockEntityRenderer<PowerPipeBlockEntity> {
    private static final ResourceLocation NORMAL =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/pipe/power_normal");
    private static final ResourceLocation OVERLOAD =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/pipe/power_overload");
    private static final float MIN_SIZE = 0.005F;
    private static final float UNIT = (0.5F - MIN_SIZE) / 2.0F / 100.0F;

    public PipePowerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PowerPipeBlockEntity pipe, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        short[] displayPower = pipe.getDisplayPower();
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(pipe.isOverloaded() ? OVERLOAD : NORMAL);
        for (Direction side : Direction.values()) {
            int stage = displayPower[side.get3DDataValue()];
            if (stage < 1) {
                continue;
            }
            float half = MIN_SIZE / 2.0F + UNIT * Math.min(stage, 99);
            float[] min = {0.5F - half, 0.5F - half, 0.5F - half};
            float[] max = {0.5F + half, 0.5F + half, 0.5F + half};
            int axis = side.getAxis().ordinal();
            if (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                min[axis] = 0.0F;
            } else {
                max[axis] = 1.0F;
            }
            FluidBoxRenderer.render(pose, buffers, sprite, 0xFFFFFFFF, min[0], min[1], min[2], max[0], max[1],
                    max[2], light);
        }
    }
}
