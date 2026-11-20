/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.contents.LargeContents;
import ivorius.psychedelicraft.block.entity.contents.SmallContents;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BurnerBlockEntityRenderer extends LabelledBlockEntityRenderer<BurnerBlockEntity> {

    private final ItemRenderer itemRenderer;

    public BurnerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BurnerBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        if (entity.getContents() instanceof Processable.Context contents && !entity.getContainer().isEmpty()) {
            matrices.pushPose();

            if (entity.getTemperature() >= 99) {
                float ticks = Minecraft.getInstance().player.tickCount + tickDelta;
                float amplitude = 5;//1.65F;
                matrices.translate(0.5, 0, 0.5);

                matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(ticks) * amplitude));
                matrices.mulPose(Axis.ZP.rotationDegrees(Mth.sin(ticks + 9 + Mth.sin(ticks)) * amplitude));
                matrices.translate(-0.5, 0, -0.5);
            }
            matrices.translate(0, 0.12, 0);

            PlacedDrinksModelProvider.INSTANCE.renderDrink("burner", ItemFluids.getItemForFluids(entity.getContainer(), ItemFluids.EMPTY), matrices, vertices, light, overlay);

            if (contents instanceof LargeContents largeContents) {
                renderFlaskMultiFluids(largeContents, matrices, vertices, light, overlay);
                renderIngredients(entity, largeContents, matrices, vertices, light, overlay);
            }
            if (contents instanceof SmallContents smallContents) {
                renderFlaskSingleFluid(smallContents, matrices, vertices, light, overlay);
            }

            matrices.popPose();
        }

        super.render(entity, tickDelta, matrices, vertices, light, overlay);
    }

    @Override
    protected float getLabelScale(BurnerBlockEntity entity, float tickDelta) {
        if (entity.getTemperature() > 100) {
            return super.getLabelScale(entity, tickDelta) + tickDelta * 0.001F;
        }
        return super.getLabelScale(entity, tickDelta);
    }

    @Override
    protected void renderLabels(BurnerBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        int temperature = entity.getTemperature();

        String text = temperature + "";
        int width = textRenderer.width(text);

        int temperatureColorComponent = Mth.clamp((int)(255 * (1 - temperature/100F)), 0, 255);
        int temperatureColor = FastColor.ARGB32.color(255, 255, temperatureColorComponent, temperatureColorComponent);

        textRenderer.drawInBatch(text, -width / 2F, 0, temperatureColor, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);
        matrices.scale(0.9F, 0.9F, 0.9F);
        textRenderer.drawInBatch(" o", width / 4F, -textRenderer.lineHeight / 2F, temperatureColor, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);

        int maxCapacity = FluidCapacity.get(entity.getContainer());
        if (maxCapacity > 0) {
            Component fillText = getFillPercentage(entity, maxCapacity);

            textRenderer.drawInBatch(fillText, -(textRenderer.width(fillText) - 5) / 2F, -textRenderer.lineHeight - 2, CommonColors.WHITE, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);
        }
    }

    static void renderFlaskSingleFluid(SmallContents contents, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.scale(1/16F, 1/16F, 1/16F);
        Resovoir tank = contents.getPrimaryTank();
        float fluidHeight = 2.4F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
        FluidBoxRenderer.getInstance()
            .light(light).overlay(overlay)
            .position(matrices)
            .texture(vertices, tank.getContents())
            .draw(6.7F, 0.5F, 6.7F, 2.6F, fluidHeight, 2.6F, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        matrices.popPose();
    }

    static void renderFlaskMultiFluids(LargeContents contents, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.scale(1/16F, 1/16F, 1/16F);
        FluidBoxRenderer renderer = FluidBoxRenderer.getInstance().light(light).overlay(overlay).position(matrices);

        float fluidStartY = 0;
        var tanks = contents.getAuxiliaryTanks();
        for (Resovoir tank : tanks) {
            renderer.texture(vertices, tank.getContents());

            float fluidHeight = 6F * ((float)tank.getContents().amount() / (float)tank.getCapacity());
            float fluidEndY = fluidStartY + fluidHeight;

            if (fluidStartY < 1 && fluidEndY > 0) {
                if (fluidStartY <= 0) {
                    renderer.draw(5, 1, 5, 6, 1, 6, Direction.DOWN);
                }

                float maxY = Math.min(1, fluidEndY);
                renderer.draw(5, 1, 5, 6, maxY, 6, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
                if (fluidEndY <= 1) {
                    renderer.draw(5, 1, 5, 6, maxY, 6, Direction.UP);
                }
            }

            if (fluidStartY < 2 && fluidEndY > 1) {
                if (fluidStartY <= 1) {
                    renderer.draw(4, 2, 6, 1, 1, 4, Direction.DOWN);
                    renderer.draw(11, 2, 6, 1, 1, 4, Direction.DOWN);
                    renderer.draw(6, 2, 4, 4, 1, 1, Direction.DOWN);
                    renderer.draw(6, 2, 11, 4, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 1) + 1;
                float maxY = Math.min(1, fluidEndY - 1) - minY + 2;
                renderer.draw(5, minY, 5, 1, maxY, 1, Direction.WEST, Direction.NORTH);
                renderer.draw(5, minY, 10, 1, maxY, 1, Direction.WEST, Direction.SOUTH);

                renderer.draw(10, minY, 5, 1, maxY, 1, Direction.EAST, Direction.NORTH);
                renderer.draw(10, minY, 10, 1, maxY, 1, Direction.EAST, Direction.SOUTH);

                renderer.draw(4, minY, 6, 1, maxY, 4, Direction.WEST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(11, minY, 6, 1, maxY, 4, Direction.EAST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(6, minY, 4, 4, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST);
                renderer.draw(6, minY, 11, 4, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST);

                if (fluidEndY <= 2) {
                    renderer.draw(5, 2, 5, 6, maxY, 6, Direction.UP);
                    renderer.draw(4, 2, 6, 1, maxY, 4, Direction.UP);
                    renderer.draw(11, 2, 6, 1, maxY, 4, Direction.UP);
                    renderer.draw(6, 2, 4, 4, maxY, 1, Direction.UP);
                    renderer.draw(6, 2, 11, 4, maxY, 1, Direction.UP);
                }
            }

            if (fluidStartY <= 3 && fluidEndY > 2) {
                if (fluidStartY <= 2) {
                    renderer.draw(4, 3, 5, 1, 1, 1, Direction.DOWN);
                    renderer.draw(4, 3, 10, 1, 1, 1, Direction.DOWN);

                    renderer.draw(11, 3, 5, 1, 1, 1, Direction.DOWN);
                    renderer.draw(11, 3, 10, 1, 1, 1, Direction.DOWN);

                    renderer.draw(5, 3, 4, 1, 1, 1, Direction.DOWN);
                    renderer.draw(10, 3, 4, 1, 1, 1, Direction.DOWN);

                    renderer.draw(5, 3, 11, 1, 1, 1, Direction.DOWN);
                    renderer.draw(10, 3, 11, 1, 1, 1, Direction.DOWN);
                }

                float minY = Math.max(fluidStartY, 2) + 1;
                float maxY = Math.min(4, fluidEndY - 2) - minY + 3;
                renderer.draw(4, minY, 5, 1, maxY, 6, Direction.WEST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(11, minY, 5, 1, maxY, 6, Direction.EAST, Direction.NORTH, Direction.SOUTH);
                renderer.draw(5, minY, 4, 6, maxY, 1, Direction.NORTH, Direction.EAST, Direction.WEST);
                renderer.draw(5, minY, 11, 6, maxY, 1, Direction.SOUTH, Direction.EAST, Direction.WEST);

                renderer.draw(4, minY, 5, 1, maxY, 6, Direction.UP);
                renderer.draw(11, minY, 5, 1, maxY, 6, Direction.UP);
                renderer.draw(5, minY, 4, 6, maxY, 8, Direction.UP);
            }

            fluidStartY = fluidEndY;
        }

        matrices.popPose();
    }

    private void renderIngredients(BurnerBlockEntity entity, LargeContents contents, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        float itemScale = 0.35F;

        matrices.pushPose();
        matrices.translate(0.5, 0, 0.5);
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.scale(itemScale, itemScale, itemScale);

        RandomSource rng = RandomSource.create(entity.getBlockPos().asLong());

        float y = 0;

        for (var i = 0; i < contents.getIngredients().size(); i++) {
            ItemStack stack = contents.getIngredients().getStack(i);
            for (int j = 0; j < stack.getCount(); j++) {
                matrices.pushPose();

                matrices.translate((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + y);
                matrices.mulPose(Axis.XP.rotationDegrees((rng.nextFloat() * 360) - 180));
                matrices.mulPose(Axis.ZP.rotationDegrees((rng.nextFloat() * 360) - 180));
                y -= 0.1F;

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertices, entity.getLevel(), 0);
                matrices.popPose();
            }
        }

        matrices.popPose();
    }
}
