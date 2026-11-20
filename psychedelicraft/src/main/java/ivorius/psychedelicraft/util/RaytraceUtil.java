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
        // upstream scaled the absolute end position: huge box, overflow at large coordinates
        AABB box = entity.getBoundingBox().expandTowards(rotation.scale(sqrtDistance)).inflate(1, 1, 1);
        return ProjectileUtil.getEntityHitResult(entity, start, end, box, e -> !e.isSpectator() && e.isPickable(), maxDistance);
    }
}
