package dev.craft.core.energy;

import java.util.List;

import net.minecraft.world.phys.AABB;

@FunctionalInterface
public interface ShockTargetQuery {
    ShockTargetQuery NONE = area -> List.of();

    List<ShockTarget> find(AABB area);
}
