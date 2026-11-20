package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TrayContentsModel extends Model {
    private final ModelPart tree;

    public TrayContentsModel(ModelPart tree) {
        super(RenderType::entityTranslucent);
        this.tree = tree;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition data = new MeshDefinition();
        PartDefinition root = data.getRoot();
        root.addOrReplaceChild("contents", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 10, 1, 16), PartPose.offset(-5, 0, -8));

        return LayerDefinition.create(data, 32, 32);
    }

    public void setAngles(TrayBlockEntity entity, float tickDelta) {
        tree.yScale = 0.1F + entity.getFillLevel() / 60F;
        if (entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X) {
            tree.yRot = Mth.HALF_PI;
        } else {
            tree.yRot = 0;
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        tree.render(matrices, vertices, light, overlay, color);
    }
}
