/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.RiftJarBlockEntity;
import ivorius.psychedelicraft.client.render.*;
import ivorius.psychedelicraft.client.render.bezier.*;
import ivorius.psychedelicraft.item.component.RiftFractionComponent;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Random;

import org.joml.Vector3d;

public class RiftJarBlockEntityRenderer implements BlockEntityRenderer<RiftJarBlockEntity> {
    public static final ResourceLocation TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar.png");
    public static final ResourceLocation CRACKED_TEXTURE = Psychedelicraft.id("textures/entity/rift_jar/rift_jar_cracked.png");
    private static final ResourceLocation FONT = ResourceLocation.withDefaultNamespace("alt");

    private static final Bezier SPHERE_BEZIER_PATH = Bezier.sphere(3, 8, 0.2);
    private static final Bezier OUTGOING_PATH = Bezier.spiral(0.06, 6, 6, 1, 0.2, 0);

    private static final BezierLabelRenderer.Style LABEL_STYLE = new BezierLabelRenderer.Style().spread(true);
    private static final Component SMALL_SPIRAL_TEXT = Component.literal("This is a small spiral.").withStyle(s -> s.withFont(FONT));

    private final RiftJarModel model = new RiftJarModel(RiftJarModel.getTexturedModelData().bakeRoot());

    private static final RiftJarBlockEntity ITEM_ENTITY = PSBlockEntities.RIFT_JAR.create(BlockPos.ZERO, PSBlocks.RIFT_JAR.defaultBlockState());

    public static void renderStack(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        ITEM_ENTITY.currentRiftFraction = RiftFractionComponent.getRiftFraction(stack);
        ITEM_ENTITY.ticksAliveVisual = (int)((System.currentTimeMillis() % 500) / 100);
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(ITEM_ENTITY, matrices, vertices, light, overlay);
    }

    public RiftJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(RiftJarBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        float ticks = entity.ticksAliveVisual + tickDelta;

        matrices.pushPose();
        matrices.translate(0.5F, 0.5f, 0.5F);

        matrices.pushPose();
        matrices.mulPose(Axis.YP.rotationDegrees(90 - entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));

        model.setAngles(entity, tickDelta);
        matrices.translate(0, 1.001F, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(180));

        model.renderToBuffer(matrices, vertices.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, overlay, CommonColors.WHITE);

        float crackedVisibility = entity.jarBroken ? 1 : Math.min((entity.currentRiftFraction - 0.5F) * 2, 1);

        if (crackedVisibility > 0) {
            model.renderToBuffer(matrices, vertices.getBuffer(model.renderType(CRACKED_TEXTURE)), light, overlay, MathUtils.withAlpha(CommonColors.WHITE, crackedVisibility));
        }

        if (entity.currentRiftFraction > 0) {
            matrices.pushPose();
            matrices.translate(0, 1.5F, 0);
            matrices.mulPose(Axis.XN.rotationDegrees(180));
            matrices.scale(0.9F, 1, 0.9F);
            ZeroScreen.render(ticks, (layer, u, v) -> {
                model.renderInterior(matrices, vertices.getBuffer(layer), 0, 0, MathUtils.withAlpha(CommonColors.WHITE, Math.min(entity.currentRiftFraction * 2, 1)));
            });
            matrices.popPose();
        }

        matrices.popPose();
        matrices.popPose();

        matrices.pushPose();
        matrices.translate(0.5F, 0.5f, 0.5F);
        RenderSystem.disableCull();

        Vec3 jarPosition = entity.getBlockPos().getCenter();

        for (RiftJarBlockEntity.JarRiftConnection connection : entity.getConnections()) {
            Vector3d connectionPoint = new Vector3d(
                    connection.position.x - jarPosition.x,
                    connection.position.y - (jarPosition.y + 0.1F),
                    connection.position.z - jarPosition.z
            );
            if (connection.bezier == null) {
                connection.bezier = Bezier.spiral(0.1, 0.5, 8, connectionPoint, 0.2, 0);
            }

            BezierLabelRenderer.INSTANCE.render(matrices, vertices, light, connection.bezier, LABEL_STYLE.shift(ticks * -0.002F).topCap(connection.fractionUp), SMALL_SPIRAL_TEXT);

            if (connection.fractionUp > 0) {
                matrices.pushPose();
                matrices.translate(
                        connectionPoint.x,
                        connectionPoint.y,
                        connectionPoint.z
                );
                BezierLabelRenderer.INSTANCE.render(matrices, vertices, light,
                        SPHERE_BEZIER_PATH,
                        LABEL_STYLE.shift(ticks * -0.002F).topCap(1),
                        Component.literal(cheeseString("This is a small circle.", 1 - connection.fractionUp, new Random(42))).withStyle(s -> s.withFont(FONT)));

                matrices.popPose();
            }
        }

        float outgoingStrength = entity.fractionHandleUp * entity.fractionOpen;
        if (outgoingStrength > 0) {
            BezierLabelRenderer.INSTANCE.render(matrices, vertices, light, OUTGOING_PATH, LABEL_STYLE.shift(ticks * -0.002F).topCap(outgoingStrength), SMALL_SPIRAL_TEXT);
        }

        RenderSystem.enableCull();
        matrices.popPose();
    }

    public static String cheeseString(String string, float effect, Random rand) {
        if (effect <= 0) {
            return string;
        }

        StringBuilder builder = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            if (rand.nextFloat() <= effect) {
                builder.append(' ');
            } else {
                builder.append(string.charAt(i));
            }
        }

        return builder.toString();
    }
}
