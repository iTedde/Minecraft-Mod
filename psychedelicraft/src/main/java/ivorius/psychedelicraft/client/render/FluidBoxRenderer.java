/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 5 Jan 2023
 */
public class FluidBoxRenderer {
    public static final Direction[] ALL = Direction.values();
    private static final Vector4f POSITION_VECTOR = new Vector4f(0, 0, 0, 1);
    private static final FluidBoxRenderer INSTANCE = new FluidBoxRenderer();

    public static FluidBoxRenderer getInstance() {
        return INSTANCE;
    }

    private float scale = 1;
    private int light = 0;
    private int overlay = 0;

    @Nullable
    private TextureAtlasSprite sprite;

    @Nullable
    private VertexConsumer buffer;

    private int color = CommonColors.WHITE;

    @Nullable
    private Matrix4f position;

    private FluidBoxRenderer() { }

    public FluidBoxRenderer scale(float scale) {
        this.scale = scale;
        return this;
    }

    public FluidBoxRenderer light(int light) {
        this.light = light;
        return this;
    }

    public FluidBoxRenderer overlay(int overlay) {
        this.overlay = overlay;
        return this;
    }

    public FluidBoxRenderer position(PoseStack position) {
        this.position = position.last().pose();
        return this;
    }

    public FluidBoxRenderer texture(MultiBufferSource vertices, ItemFluids fluids) {
        if (fluids.isEmpty()) {
            sprite = null;
            buffer = vertices.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
            color = CommonColors.WHITE;
        } else {
            FluidAppearance appearance = FluidAppearance.of(fluids);

            sprite = appearance.sprite();
            color = appearance.color();
            buffer = vertices.getBuffer(RenderType.entityTranslucent(appearance.texture()));
        }

        return this;
    }

    public FluidBoxRenderer texture(MultiBufferSource vertices, ItemStack stack) {
        sprite = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack).getParticleIcon();
        buffer = vertices.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        color = CommonColors.WHITE;
        return this;
    }

    public FluidBoxRenderer draw(VoxelShape shape) {
        for (AABB box : shape.toAabbs()) {
            draw(box, ALL);
        }
        return this;
    }

    public FluidBoxRenderer draw(AABB box, Direction... directions) {
        return draw((float)box.minX, (float)box.minY, (float)box.minZ, (float)box.getXsize(), (float)box.getYsize(), (float)box.getZsize(), directions);
    }

    public FluidBoxRenderer draw(float x, float y, float z, float width, float height, float length, Direction... directions) {
        renderFluidFace(x, y, z, width, height, length, directions);
        return this;
    }

    private void vertex(float x, float y, float z, float u, float v, Direction direction) {
        POSITION_VECTOR.set(x, y, z, 1);
        position.transform(POSITION_VECTOR);
        buffer.addVertex(
                POSITION_VECTOR.x * scale, POSITION_VECTOR.y * scale, POSITION_VECTOR.z * scale,
                FastColor.ARGB32.opaque(color),
                u, v,
                overlay, light,
                direction.getStepX(), direction.getStepY(), direction.getStepZ()
        );
    }

    private void renderFluidFace(float x, float y, float z, float width, float height, float length, Direction... directions) {
        if (sprite == null) {
            return;
        }
        for (Direction direction : directions) {
            switch (direction) {
                case DOWN:
                    vertex(x, y, z, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x + width, y, z, sprite.getU1(), sprite.getV0(), direction);
                    vertex(x + width, y, z + length, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x, y, z + length, sprite.getU0(), sprite.getV1(), direction);
                    break;
                case UP:
                    vertex(x, y + height, z, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x, y + height, z + length, sprite.getU0(), sprite.getV1(), direction);
                    vertex(x + width, y + height, z + length, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x + width, y + height, z, sprite.getU1(), sprite.getV0(), direction);
                    break;
                case EAST:
                    vertex(x + width, y, z, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x + width, y + height, z, sprite.getU1(), sprite.getV0(), direction);
                    vertex(x + width, y + height, z + length, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x + width, y, z + length, sprite.getU0(), sprite.getV1(), direction);
                    break;
                case WEST:
                    vertex(x, y, z, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x, y, z + length, sprite.getU1(), sprite.getV0(), direction);
                    vertex(x, y + height, z + length, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x, y + height, z, sprite.getU0(), sprite.getV1(), direction);
                    break;
                case NORTH:
                    vertex(x, y, z, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x, y + height, z, sprite.getU0(), sprite.getV1(), direction);
                    vertex(x + width, y + height, z, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x + width, y, z, sprite.getU1(), sprite.getV0(), direction);
                    break;
                case SOUTH:
                    vertex(x, y, z + length, sprite.getU0(), sprite.getV0(), direction);
                    vertex(x + width, y, z + length, sprite.getU1(), sprite.getV0(), direction);
                    vertex(x + width, y + height, z + length, sprite.getU1(), sprite.getV1(), direction);
                    vertex(x, y + height, z + length, sprite.getU0(), sprite.getV1(), direction);
                    break;
            }
        }
    }

    public record FluidAppearance(ResourceLocation texture, TextureAtlasSprite sprite, int color) {
        public static FluidAppearance of(ItemFluids stack) {
            int color = stack.fluid().getColor(stack);
            TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();

            FluidState state = stack.fluid().getFluidState(stack);
            if (!state.isEmpty()) {
                var handler = IClientFluidTypeExtensions.of(state);
                var level = Minecraft.getInstance().level;
                var pos = Minecraft.getInstance().player == null ? BlockPos.ZERO : Minecraft.getInstance().player.blockPosition();
                color = level == null ? handler.getTintColor() : handler.getTintColor(state, level, pos);
                ResourceLocation still = level == null ? handler.getStillTexture() : handler.getStillTexture(state, level, pos);
                if (still != null) {
                    sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
                }
            }

            return new FluidAppearance(sprite.atlasLocation(), sprite, color);
        }

        public static int getItemColor(ItemFluids stack) {
            if (!stack.fluid().isCustomFluid()) {
                FluidState state = stack.fluid().getPhysical().getStandingFluid().defaultFluidState();
                var level = Minecraft.getInstance().level;
                if (!state.isEmpty() && level != null && Minecraft.getInstance().player != null) {
                    return FastColor.ARGB32.opaque(IClientFluidTypeExtensions.of(state).getTintColor(state, level, Minecraft.getInstance().player.blockPosition()));
                }
            }

            return FastColor.ARGB32.opaque(stack.fluid().getColor(stack));
        }

        public float[] rgba() {
            return new float[] {
                    MathUtils.r(color),
                    MathUtils.g(color),
                    MathUtils.b(color),
                    1
            };
        }
    }

}
