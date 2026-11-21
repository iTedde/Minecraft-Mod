package ivorius.psychedelicraft.entity.drug.hallucination;

import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.util.Pool;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class EntityIdentitySwapHallucination extends Hallucination {

    private final EntityType<?> targetType;
    private final Pool<EntityType<?>> transformedType;

    @Nullable
    private Selection selection;

    public EntityIdentitySwapHallucination(Player player, EntityType<?> targetType, Pool<EntityType<?>> transformedType) {
        super(player);
        this.targetType = targetType;
        this.transformedType = transformedType;
    }

    @Nullable
    public Entity matchOrAttach(Entity entity) {

        if (entity.getType() != targetType || (selection != null && !entity.getUUID().equals(selection.selection().getUUID()))) {
            return null;
        }

        if (selection == null) {
            selection = new Selection(entity, transformedType.get(entity.level().getRandom()));
        }

        return selection.attachment();
    }

    @Override
    public void update() {
        super.update();
        if (selection != null) {
            selection.update();
        }
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertices, Camera camera, float tickDelta, float alpha) {
    }

    @Override
    public boolean isDead() {
        if (age >= 300 && selection != null) {
            selection.selection().setSilent(false);
        }
        return age > 300;
    }

    @Override
    public int getMaxHallucinations() {
        return 100;
    }

    record Selection(Entity selection, Entity attachment) {
        Selection(Entity selection, EntityType<?> attachmentType) {
            this(selection, attachmentType.create(selection.level()));
            attachment.setSilent(true);
            attachment.restoreFrom(selection);
        }

        public void update() {
            attachment.tickCount++;
            attachment.absMoveTo(
                    selection.position().x, selection.position().y, selection.position().z,
                    selection.getYRot(), selection.getXRot()
            );
            attachment.setYBodyRot(selection.getVisualRotationYInDegrees());

            attachment.xOld = selection.xOld;
            attachment.yOld = selection.yOld;
            attachment.zOld = selection.zOld;
            attachment.xo = selection.xo;
            attachment.yo = selection.yo;
            attachment.zo = selection.zo;
            attachment.yRotO = selection.yRotO;
            attachment.xRotO = selection.xRotO;
            attachment.setOnGround(selection.onGround());

            if (attachment instanceof LivingEntity living) {
                living.yHeadRotO = ((LivingEntity)selection).yHeadRotO;
                living.yHeadRot = ((LivingEntity)selection).yHeadRot;
            }

            attachment.tick();
        }
    }
}
