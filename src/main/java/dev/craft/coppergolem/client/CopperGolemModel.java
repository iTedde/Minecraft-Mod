package dev.craft.coppergolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.coppergolem.CopperGolem;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Own model in the spirit of the 1.21.9 golem (spec 35 §2.5): wide head with nose and antenna, short body, arms and
 * legs; 15 px up to the top of the head, matching the 0.98 hitbox. Texture layout in
 * {@code tools/scripts/copper_golem_textures.py}.
 */
public class CopperGolemModel extends HierarchicalModel<CopperGolem> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public CopperGolemModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -5.0F, -3.0F, 8.0F, 5.0F, 6.0F)
                        .texOffs(28, 0).addBox(-1.0F, -3.0F, -5.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(36, 0).addBox(-0.5F, -8.0F, -0.5F, 1.0F, 3.0F, 1.0F)
                        .texOffs(40, 0).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        parts.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        parts.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(20, 12).addBox(-2.0F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(-3.0F, 14.5F, 0.0F));
        parts.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(28, 12).addBox(0.0F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(3.0F, 14.5F, 0.0F));
        parts.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(36, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(-1.5F, 19.0F, 0.0F));
        parts.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(44, 12).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(1.5F, 19.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(CopperGolem golem, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        rightLeg.xRot = swing;
        leftLeg.xRot = -swing;
        if (golem.isInteracting()) {
            // Looking into a container: head bowed, both arms rummaging inside.
            head.xRot += 0.35F;
            float rummage = Mth.sin(ageInTicks * 0.6F) * 0.25F;
            rightArm.xRot = -1.5F + rummage;
            leftArm.xRot = -1.5F - rummage;
            rightArm.zRot = 0.0F;
            leftArm.zRot = 0.0F;
        } else if (golem.getMainHandItem().isEmpty()) {
            rightArm.xRot = -swing * 0.6F;
            leftArm.xRot = swing * 0.6F;
            rightArm.zRot = 0.0F;
            leftArm.zRot = 0.0F;
        } else {
            // Both arms forward around the carried item, with a little bob while walking.
            float bob = Mth.cos(ageInTicks * 0.1F) * 0.05F + swing * 0.1F;
            rightArm.xRot = -1.2F + bob;
            leftArm.xRot = -1.2F - bob;
            rightArm.zRot = -0.25F;
            leftArm.zRot = 0.25F;
        }
    }

    /** Where the carried item sits: in front of the body between the hands. */
    public void translateToHands(PoseStack poseStack) {
        root.translateAndRotate(poseStack);
        poseStack.translate(0.0F, 17.0F / 16.0F, -5.5F / 16.0F);
    }
}
