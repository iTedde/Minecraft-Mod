package dev.craft.core.energy;

import net.minecraft.core.BlockPos;

/**
 * A participant of the EU energy net. Implementations are usually block entities; the net identifies tiles by object
 * identity and locates neighbours by {@link #getBlockPos()}.
 */
public interface EnergyTile {
    BlockPos getBlockPos();
}
