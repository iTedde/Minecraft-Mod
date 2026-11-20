/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.hallucination;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ivorius.psychedelicraft.client.render.RenderLayerUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.client.render.PassThroughVertexConsumer;

public abstract class AbstractEntityHallucination extends Hallucination {

    protected final RandomSource random;
    protected final Entity entity;

    protected int maxAge;

    protected float[] color = {1, 1, 1, 1};

    protected float scale;

    private float dAlpha;
    private final PassThroughVertexConsumer.Parameters colourSpace = new PassThroughVertexConsumer.Parameters().color((parent, r, g, b, a) -> {
        parent.setColor(color[0], color[1], color[2], dAlpha);
    });

    public AbstractEntityHallucination(Player player, Entity entity) {
        super(player);
        this.random = player.getRandom();
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public int getMaxHallucinations() {
        return UNLIMITED;
    }

    @Override
    public boolean isDead() {
        return age >= maxAge;
    }

    @Override
    public void update() {
        super.update();

        entity.tickCount++;
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();

        entity.yRotO = entity.getYRot();
        entity.xRotO = entity.getXRot();

        if (entity instanceof LivingEntity living) {
            living.yHeadRotO = living.yHeadRot;
        }

        animateEntity();

        if (entity instanceof LivingEntity living) {
            living.calculateEntityAnimation(false);
        }
    }

    protected abstract void animateEntity();

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertices, Camera camera, float tickDelta, float dAlpha) {
        this.dAlpha = Math.min(1,
                Mth.sin(
                        (float) Math.min(age, maxAge - 2) / (float) (maxAge - 2)
                        * Mth.PI) * 18) * dAlpha;

        if (Mth.equal(this.dAlpha, 0)) {
            return;
        }

        Vec3 cameraPos = camera.getPosition();

        double x = Mth.lerp(tickDelta, entity.xo, entity.getX()) - cameraPos.x;
        double y = Mth.lerp(tickDelta, entity.yo, entity.getY()) - cameraPos.y;
        double z = Mth.lerp(tickDelta, entity.zo, entity.getZ()) - cameraPos.z;
        float pitch = Mth.lerp(tickDelta, entity.xRotO, entity.getViewXRot(tickDelta));
        float yaw = Mth.lerp(tickDelta, entity.yRotO, entity.getViewYRot(tickDelta));

        entity.xOld = entity.getX();
        entity.yOld = entity.getY();
        entity.zOld = entity.getZ();

        matrices.pushPose();
        matrices.translate(x, y, z);
        matrices.scale(scale, scale, scale);
        matrices.translate(-x, -y, -z);

        renderModel(matrices, layer -> {
            return PassThroughVertexConsumer.of(vertices.getBuffer(getRenderLayer(layer)), colourSpace);
        }, x, y, z, pitch, yaw, tickDelta);
        matrices.popPose();
    }

    protected RenderType getRenderLayer(RenderType layer) {
        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        if (renderer != null) {
            return RenderLayerUtil.getTexture(layer).map(RenderType::entityTranslucent).orElseGet(RenderType::translucent);
        }
        return layer;
    }

    protected void renderModel(PoseStack matrices, MultiBufferSource vertices, double x, double y, double z, float pitch, float yaw, float tickDelta) {
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, vertices, dispatcher.getPackedLightCoords(entity, tickDelta));
        dispatcher.setRenderShadow(true);
    }
}
