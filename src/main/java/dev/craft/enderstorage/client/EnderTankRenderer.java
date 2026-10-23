package dev.craft.enderstorage.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.buildcraft.client.FluidBoxRenderer;
import dev.craft.enderstorage.EnderLiquidStorage;
import dev.craft.enderstorage.EnderTankBlock;
import dev.craft.enderstorage.EnderTankBlockEntity;
import dev.craft.enderstorage.EsDataComponents;
import dev.craft.enderstorage.Frequency;
import dev.craft.enderstorage.TankSynchroniser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

/**
 * EnderStorage {@code EnderTankRenderer} (spec 27 §5): the original {@code endertank.obj} (blaze rods, top, base,
 * glass, valve base; the valve knob turning with the pressure, diamond knob for personal tanks), the three colour
 * buttons on top, the end portal floor, the floating pearl (the ender pearl item instead of CodeChickenCore's
 * icosahedron, as on the chest) and the liquid inside.
 */
public class EnderTankRenderer implements BlockEntityRenderer<EnderTankBlockEntity> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "models/enderstorage/ender_tank.obj");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/enderstorage/ender_tank.png");
    private static final ResourceLocation BUTTONS =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/enderstorage/buttons.png");
    /** Valve knob of personal tanks: the diamond knob 13 pixels lower in the texture. */
    private static final float PERSONAL_VALVE_V = 0.203125F;
    private static final float VALVE_PIVOT_Y = 0.4165F;

    /** Triangles: 3 × (x, y, z, u, v, nx, ny, nz) each. */
    private static float[] tank;
    private static float[] valve;

    public EnderTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EnderTankBlockEntity tankEntity, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        BlockPos pos = tankEntity.getBlockPos();
        // getTimeOffset: tanks do not bob in step.
        double time = (tankEntity.getLevel() != null ? tankEntity.getLevel().getGameTime() : 0) + partialTick
                + pos.getX() * 3 + pos.getY() * 5 + pos.getZ() * 9;
        renderTank(tankEntity.getBlockState().getValue(EnderTankBlock.FACING).get2DDataValue(),
                tankEntity.getValveAngle(partialTick), tankEntity.getFrequency(), time, pose, buffers, light, overlay);
        renderLiquid(tankEntity.getShownLiquid(), pose, buffers, light);
    }

    /** {@code renderTank}; {@code rotation} 0 = valve to the south, 1 = west, 2 = north, 3 = east. */
    static void renderTank(int rotation, float valveAngle, Frequency frequency, double time, PoseStack pose,
                           MultiBufferSource buffers, int light, int overlay) {
        if (tank == null) {
            load();
        }
        renderPortal(pose, buffers);
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F * (rotation + 2)));
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        triangles(pose.last(), buffer, tank, 0.0F, light, overlay);
        renderButtons(frequency, pose, buffers, light, overlay);
        pose.translate(0.0F, VALVE_PIVOT_Y, 0.0F);
        pose.mulPose(Axis.ZP.rotation(valveAngle));
        pose.translate(0.0F, -VALVE_PIVOT_Y, 0.0F);
        triangles(pose.last(), buffer, valve, frequency.isPersonal() ? PERSONAL_VALVE_V : 0.0F, light, overlay);
        pose.popPose();
        renderPearl(time, pose, buffers, light, overlay);
    }

    /** {@code RenderCustomEndPortal(0.1205, 0.24, 0.76, 0.24, 0.76)}: the floor inside the glass. */
    private static void renderPortal(PoseStack pose, MultiBufferSource buffers) {
        Matrix4f matrix = pose.last().pose();
        VertexConsumer buffer = buffers.getBuffer(RenderType.endPortal());
        float y = 0.1205F;
        buffer.addVertex(matrix, 0.24F, y, 0.76F);
        buffer.addVertex(matrix, 0.76F, y, 0.76F);
        buffer.addVertex(matrix, 0.76F, y, 0.24F);
        buffer.addVertex(matrix, 0.24F, y, 0.24F);
    }

    /** Buttons ({@code selection_button} × 0.8) at x 0.15, 0, -0.15 on top; button 1 is left seen from the valve. */
    private static void renderButtons(Frequency frequency, PoseStack pose, MultiBufferSource buffers, int light,
                                      int overlay) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(BUTTONS));
        for (int button = 0; button < 3; button++) {
            int colour = frequency.colour(button);
            float u = 0.25F * (colour % 4);
            float v = 0.25F * (colour / 4);
            float x = 0.15F - 0.15F * button;
            EnderChestRenderer.box(pose.last(), buffer, x - 0.05F, 0.91F, -0.1F, x + 0.05F, 0.96F, 0.1F,
                    u + 0.0938F, v + 0.0625F, u + 0.1562F, v + 0.1875F, light, overlay);
        }
    }

    /** The pearl floats at 0.45, bobs ±0.2 and turns. */
    private static void renderPearl(double time, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float bob = (float) Math.sin(time / 25.0 * Math.PI) * 0.1F;
        pose.pushPose();
        pose.translate(0.5, 0.45 + bob * 2.0F, 0.5);
        pose.mulPose(Axis.YP.rotation((float) (time / 3.0)));
        pose.scale(0.4F, 0.4F, 0.4F);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.ENDER_PEARL),
                ItemDisplayContext.FIXED, light, overlay, pose, buffers, null, 0);
        pose.popPose();
    }

    /** {@code renderLiquid}: 0.22–0.78 wide, from 0.12 up to 0.121 + 0.63 × fill. */
    static void renderLiquid(FluidStack liquid, PoseStack pose, MultiBufferSource buffers, int light) {
        if (liquid.isEmpty()) {
            return;
        }
        float top = 0.121F + 0.63F * liquid.getAmount() / EnderLiquidStorage.CAPACITY;
        FluidBoxRenderer.render(pose, buffers, liquid, 0.22F, 0.12F, 0.22F, 0.78F, top, 0.78F, light);
    }

    private static void triangles(PoseStack.Pose pose, VertexConsumer buffer, float[] data, float vOffset, int light,
                                  int overlay) {
        for (int i = 0; i < data.length; i += 24) {
            for (int corner = 0; corner < 4; corner++) {
                // Quads only: the last corner twice.
                int at = i + Math.min(corner, 2) * 8;
                buffer.addVertex(pose, data[at], data[at + 1], data[at + 2]).setColor(-1)
                        .setUv(data[at + 3], data[at + 4] + vOffset).setOverlay(overlay).setLight(light)
                        .setNormal(pose, data[at + 5], data[at + 6], data[at + 7]);
            }
        }
    }

    /**
     * {@code CCModel.parseObjModels(..., new SwapYZ())}: y and z swapped, v flipped, moved by (-0.5099, 0, -0.5027)
     * so the tank stands centred on the origin; the valve knob apart from the rest.
     */
    private static void load() {
        List<float[]> positions = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<float[]> normals = new ArrayList<>();
        List<Float> tankData = new ArrayList<>();
        List<Float> valveData = new ArrayList<>();
        List<Float> group = tankData;
        try (BufferedReader reader = Minecraft.getInstance().getResourceManager().openAsReader(MODEL)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                switch (parts[0]) {
                    case "v" -> positions.add(new float[]{Float.parseFloat(parts[1]) - 0.5099F,
                            Float.parseFloat(parts[3]), Float.parseFloat(parts[2]) - 0.5027F});
                    case "vt" -> uvs.add(new float[]{Float.parseFloat(parts[1]), 1.0F - Float.parseFloat(parts[2])});
                    case "vn" -> normals.add(new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[3]),
                            Float.parseFloat(parts[2])});
                    case "g" -> group = parts.length > 1 && parts[1].equals("Valve") ? valveData : tankData;
                    case "f" -> {
                        for (int corner = 1; corner <= 3; corner++) {
                            String[] index = parts[corner].split("/");
                            float[] p = positions.get(Integer.parseInt(index[0]) - 1);
                            float[] t = uvs.get(Integer.parseInt(index[1]) - 1);
                            float[] n = normals.get(Integer.parseInt(index[2]) - 1);
                            for (float value : new float[]{p[0], p[1], p[2], t[0], t[1], n[0], n[1], n[2]}) {
                                group.add(value);
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + MODEL, e);
        }
        tank = toArray(tankData);
        valve = toArray(valveData);
    }

    private static float[] toArray(List<Float> values) {
        float[] array = new float[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    /** The tank with the valve to the south, the colours of the item and the liquid of its storage. */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            Frequency frequency = stack.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
            renderTank(0, 0.0F, frequency, 0.0, pose, buffers, light, overlay);
            renderLiquid(TankSynchroniser.clientLiquid(frequency), pose, buffers, light);
        }
    }
}
