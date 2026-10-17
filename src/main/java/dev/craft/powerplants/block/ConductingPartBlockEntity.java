package dev.craft.powerplants.block;

import dev.craft.core.energy.EnergyConductor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Casing part of a generating power plant (turbines): while its controller holds it in a formed structure, it is a
 * lossless conductor that only takes EU from that controller or from other parts of the same structure, so cables can
 * be attached to any casing block (user, 2026-10-17). Otherwise it neither accepts nor emits.
 */
public class ConductingPartBlockEntity extends PlantPartBlockEntity implements EnergyConductor {
    public ConductingPartBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    /** The formed controller holding this part if it produces EU, otherwise {@code null}. */
    @Nullable
    private EnergySource getSource() {
        return getController() instanceof EnergySource source ? source : null;
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        EnergySource source = getSource();
        return source != null && (emitter == source
                || emitter instanceof ConductingPartBlockEntity part && part.getSource() == source);
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return getSource() != null;
    }

    @Override
    public double getConductionLoss() {
        return 0.0;
    }

    @Override
    public int getInsulationEnergyAbsorption() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getInsulationBreakdownEnergy() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getConductorBreakdownEnergy() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void removeInsulation() {
    }

    @Override
    public void removeConductor() {
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }
}
