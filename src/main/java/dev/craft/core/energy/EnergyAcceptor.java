package dev.craft.core.energy;

import net.minecraft.core.Direction;

public interface EnergyAcceptor extends EnergyTile {
    /**
     * @param emitter   the neighbouring tile
     * @param direction direction from this tile towards {@code emitter}
     */
    boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction);
}
