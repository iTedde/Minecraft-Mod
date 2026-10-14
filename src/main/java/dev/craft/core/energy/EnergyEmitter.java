package dev.craft.core.energy;

import net.minecraft.core.Direction;

public interface EnergyEmitter extends EnergyTile {
    /**
     * @param receiver  the neighbouring tile
     * @param direction direction from this tile towards {@code receiver}
     */
    boolean emitsEnergyTo(EnergyTile receiver, Direction direction);
}
