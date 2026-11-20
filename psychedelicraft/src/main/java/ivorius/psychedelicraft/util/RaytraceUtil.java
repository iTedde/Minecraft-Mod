package ivorius.psychedelicraft.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public interface RaytraceUtil {

    static EntityHitResult raycastEntities(Entity entity, double maxDistance) {
        double sqrtDistance = Math.sqrt(maxDistance);
        Vec3 start = entity.getEyePosition();
        Vec3 rotation = entity.getViewVector(1);
        Vec3 end = start.add(rotation.x * sqrtDistance, rotation.y * sqrtDistance, rotation.z * sqrtDistance);
        AABB box = entity.getBoundingBox().expandTowards(end.scale(sqrtDistance)).inflate(1, 1, 1);
        return ProjectileUtil.getEntityHitResult(entity, start, end, box, e -> !e.isSpectator() && e.isPickable(), maxDistance);
    }
}
