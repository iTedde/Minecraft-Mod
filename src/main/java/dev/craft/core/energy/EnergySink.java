package dev.craft.core.energy;

import net.minecraft.core.Direction;

public interface EnergySink extends EnergyAcceptor {
    /** EU this sink still wants to receive; {@code > 0} makes it eligible for packets. */
    int demandsEnergy();

    /**
     * Injects a packet. Implementations explode on overvoltage ({@code amount > getMaxSafeInput()}).
     *
     * @param direction direction from this sink towards the tile the energy came from
     * @return EU that were not accepted
     */
    int injectEnergy(Direction direction, int amount);

    int getMaxSafeInput();
}
