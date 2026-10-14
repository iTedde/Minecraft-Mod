package dev.craft.core.energy;

import net.minecraft.world.phys.AABB;

/**
 * A living entity that may be shocked by an energy path.
 *
 * @param key    identity used to accumulate shock energy (the entity itself in game)
 * @param bounds current bounding box
 */
public record ShockTarget(Object key, AABB bounds) {
}
