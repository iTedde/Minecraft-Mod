/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidFilled;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.client.render.BlockBreakingProgressAccessor;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Renders fluid in the mash tub, or the solid contents
 */
public class MashTubBlockEntityRenderer extends LabelledBlockEntityRenderer<MashTubBlockEntity> {
    private static final MashTubBlockEntity ITEM_ENTITY = PSBlockEntities.MASH_TUB.create(BlockPos.ZERO, PSBlocks.MASH_TUB.defaultBlockState());

    public MashTubBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void renderStack(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        ITEM_ENTITY.getPrimaryTank().setContents(ItemFluids.of(stack));

        BlockState state = ITEM_ENTITY.getBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        Minecraft.getInstance().getBlockRenderer()
            .getModelRenderer().renderModel(matrices.last(), vertices.getBuffer(ItemBlockRenderTypes.getChunkRenderType(state)), state, model, 1, 1, 1, light, overlay);
        Minecraft.getInstance().getBlockRenderer()
            .getModelRenderer().renderModel(matrices.last(), vertices.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)), state, model, 1, 1, 1, light, overlay);
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(ITEM_ENTITY, matrices, vertices, light, overlay);
    }

    @Override
    public void render(MashTubBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {

        int damageStage = getDamageStage(entity.getLevel(), entity.getBlockPos());
        if (damageStage != 0) {
            BlockState state = entity.getBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            VertexConsumer consumer = new SheetedDecalTextureGenerator(vertices.getBuffer(ModelBakery.DESTROY_TYPES.get(damageStage)), matrices.last(), 1F);
            Minecraft.getInstance().getBlockRenderer()
                .getModelRenderer().renderModel(matrices.last(), consumer, state, model, 1, 1, 1, light, overlay);
        }

        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();

        float fluidHeight = 0.3F;

        FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices);

        if (!stack.isEmpty()) {
            float fillPercentage = Mth.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight += fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        }

        stack = entity.getAuxiliaryFluids();

        if (!stack.isEmpty()) {
            float fillPercentage = Mth.clamp((float)stack.amount() / tank.getCapacity(), 0, 2);

            fluidHeight += fillPercentage * 0.6F;

            FluidBoxRenderer.getInstance()
                .texture(vertices, stack)
                .draw(-0.5F, 0, -0.5F, 2, fluidHeight, 2, Direction.UP);
        }

        if (!entity.solidContents.isEmpty() && entity.solidContents.getItem() instanceof BlockItem) {
            FluidBoxRenderer.getInstance()
                .texture(vertices, entity.solidContents)
                .draw(-0.3F, 0, -0.3F, 1.6F, 0.2F, 1.6F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                .draw(-0.2F, 0, -0.2F, 1.4F, 0.3F, 1.4F, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
        }

        matrices.pushPose();
        matrices.translate(0, 0.75f, 0);

        long seed = entity.getBlockPos().asLong() + 1;
        RandomSource random = RandomSource.create();
        random.setSeed(seed);

        for (int i = 0; i < entity.getSuppliedIngredients().size(); i++) {
            ItemStack s = entity.getSuppliedIngredients().getStack(i);
            for (int c = 0; c < s.getCount(); c++) {
                float positionX = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float positionZ = 0.5F + (random.nextFloat() - 0.5F) * 1.5F;
                float rotation = random.nextFloat() * 360.0f;

                matrices.pushPose();
                matrices.translate(positionX, fluidHeight / 16F - 0.02F, positionZ);

                matrices.mulPose(Axis.YP.rotationDegrees(rotation));
                matrices.scale(0.3F, 0.3F, 0.3F);

                int singleDifference = c * 5;
                float bob = Mth.sin((ShaderContext.ticks() + singleDifference) / 8F) * 0.2F;
                float spin = Mth.cos((ShaderContext.ticks() + singleDifference) / 8F) * 0.12F;

                matrices.translate(0, bob, -0.2F);
                matrices.mulPose(Axis.ZN.rotationDegrees(-50 * spin));
                matrices.mulPose(Axis.YN.rotationDegrees((ShaderContext.ticks() + c) % 360));
                Minecraft.getInstance().getItemRenderer().renderStatic(s, ItemDisplayContext.FIXED, light, overlay, matrices, vertices, entity.getLevel(), (int)seed);

                matrices.popPose();
            }
        }

        matrices.popPose();

        super.render(entity, tickDelta, matrices, vertices, light, overlay);

        if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes() && !Minecraft.getInstance().showOnlyReducedInfo()) {
            if (entity.getLevel() != null && entity.getBlockPos() != null && entity.getBlockState().getBlock() instanceof FluidFilled tub) {
                AABB box = new AABB(
                        0, 0, 0,
                        1, tub.getFluidHeight(entity.getLevel(), entity.getBlockState(), entity.getBlockPos()), 1
                ).inflate(0.001);

                matrices.pushPose();
                LevelRenderer.renderLineBox(matrices, vertices.getBuffer(RenderType.lines()), box, 0, 1, 0, 0.2F);

                box = tub.getFluidCollisionBox(entity.getLevel(), entity.getBlockState(), entity.getBlockPos());

                matrices.translate(-box.minX - ((box.getXsize() - 1) / 2), -box.minY, -box.minZ - ((box.getZsize() - 1) / 2));
                LevelRenderer.renderLineBox(matrices, vertices.getBuffer(RenderType.lines()), box, 1, 1, 1, 1);
                matrices.popPose();
            }
        }
    }

    @Override
    protected double getLabelDistanceFromCenter(MashTubBlockEntity entity) {
        return 1.8;
    }

    @Override
    protected void renderLabels(MashTubBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        ProcessType processType = entity.getActiveProcess();
        Component process = entity.getActiveProcess().getStatus();
        if (processType != ProcessType.IDLE) {
            int progress = (int)(entity.getProgress(tickDelta) * 100);
            process = process.copy().append("... " + progress + "%");
        }
        textRenderer.drawInBatch(process, -(textRenderer.width(process) - 5) / 2F, 0, CommonColors.WHITE, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);
        matrices.scale(0.9F, 0.9F, 0.9F);
        Component fill = getFillPercentage(entity, FluidVolumes.VAT);
        textRenderer.drawInBatch(fill, -(textRenderer.width(fill) - 5) / 2F, -textRenderer.lineHeight - 2, CommonColors.WHITE, true, matrices.last().pose(), vertices, DisplayMode.NORMAL, 0, light);
    }

    static int getDamageStage(@Nullable Level world, @Nullable BlockPos center) {
        if (world == null || center == null || BlockBreakingProgressAccessor.getStage(center) != 0) {
            return 0;
        }
        int stage = 0;
        for (BlockPos pos : BlockPos.spiralAround(center, 1, Direction.EAST, Direction.SOUTH)) {
            if (world.getBlockState(pos).is(PSBlocks.MASH_TUB_EDGE)) {
                stage = Math.max(stage, BlockBreakingProgressAccessor.getStage(pos));
            }
        }

        return stage;
    }
}
