package dev.craft.thermalexpansion.transport;

import dev.craft.buildcraft.power.PowerProvider;
import net.minecraft.core.Direction;

/**
 * {@code PowerProviderTeleport}: 100 MJ buffer, accepts up to 100 MJ, everything received loses 25 %
 * ({@code transport.teleport.energy.loss}); no power loss over time.
 */
public class TesseractPowerProvider extends PowerProvider implements LossPowerProvider {
    public static final float TELEPORT_ENERGY_LOSS = 0.75F;
    public static final int TELEPORT_MAX_ENERGY = 100;

    public TesseractPowerProvider() {
        configure(0, TELEPORT_MAX_ENERGY, 0, TELEPORT_MAX_ENERGY);
        configurePowerPerdition(0, 72000);
    }

    @Override
    public void receiveEnergy(float quantity, Direction from) {
        setEnergyStored(Math.min(getMaxEnergyStored(), getEnergyStored() + quantity * TELEPORT_ENERGY_LOSS));
    }
}
