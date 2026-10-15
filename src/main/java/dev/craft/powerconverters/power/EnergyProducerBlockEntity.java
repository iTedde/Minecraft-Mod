package dev.craft.powerconverters.power;

import dev.craft.powerconverters.PowerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** {@code TileEntityEnergyProducer}: supplied by the adjacent bridges. */
public abstract class EnergyProducerBlockEntity<A> extends BridgeComponentBlockEntity<A> {
    protected EnergyProducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                        PowerSystem powerSystem, int voltageIndex) {
        super(type, pos, state, powerSystem, voltageIndex);
    }

    /** Produces from {@code energy} PCU; returns the PCU not used. */
    public abstract int produceEnergy(int energy);
}
