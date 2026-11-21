/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import java.util.Optional;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RastaHeadModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RastaHeadHallucination extends AbstractEntityHallucination {
    private static final ResourceLocation TEXTURE = Psychedelicraft.id("textures/drug/cannabis/rasta_head_hallucination.png");

    private final LookControl lookControl;

    private final Model modelRastaHead = new RastaHeadModel();

    private final float distance;

    private final float planeRotationX;
    private final float planeRotationZ;

    public RastaHeadHallucination(Player playerEntity) {
        super(playerEntity, EntityType.PIG.create(playerEntity.level()));

        maxAge = (random.nextInt(59) + 120) * 20;
        scale = 1 + random.nextFloat() / 2F;
        distance = 2 + random.nextFloat() * 5;

        planeRotationX = random.nextFloat() * Mth.HALF_PI;
        planeRotationZ = random.nextFloat() * Mth.HALF_PI;

        entity.setPos(playerEntity.position());
        lookControl = ((Mob)entity).getLookControl();

        chatBot = Optional.of(new ChatBot(new RastaheadPersonality(), playerEntity));
    }

    @Override
    protected void animateEntity() {
        this.lookControl.setLookAt(player);
        this.lookControl.tick();

        int seed = player.tickCount + (entity.getId() * 3);

        Vec3 offset = new Vec3(
                Mth.sin(seed / 50F) * distance,
                Mth.sin(seed / 10F) + (entity.getId() % 5) - 1,
                Mth.cos(seed / 50F) * distance
        ).yRot(planeRotationX).zRot(planeRotationZ);

        Vec3 wanted = player.getEyePosition().add(offset);

        double totalDist = wanted.distanceTo(entity.position());

        Vec3 vel = entity.getDeltaMovement().scale(0.9D);

        vel = wanted.subtract(entity.position()).normalize().scale(Math.log((float)totalDist));

        entity.setDeltaMovement(vel);
        entity.setPos(entity.position().add(vel));
    }

    @Override
    protected RenderType getRenderLayer(RenderType layer) {
        return layer;
    }

    @Override
    protected void renderModel(PoseStack matrices, MultiBufferSource vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        yaw = 180 - Mth.lerp(tickDelta, ((LivingEntity)entity).yHeadRotO, ((LivingEntity)entity).yHeadRot);

        matrices.translate(x, y, z);
        matrices.mulPose(Axis.ZN.rotationDegrees(180));
        matrices.mulPose(Axis.XN.rotationDegrees(pitch));
        matrices.mulPose(Axis.YN.rotationDegrees(yaw));

        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        modelRastaHead.renderToBuffer(matrices, vertices.getBuffer(modelRastaHead.renderType(TEXTURE)), dispatcher.getPackedLightCoords(entity, tickDelta), 0, CommonColors.WHITE);
    }
}
