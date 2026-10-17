package dev.craft.powerconverters.power;

import dev.craft.core.energy.EnergyAcceptor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerconverters.block.PowerConverterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEntityIndustrialCraftProducer}: an EU source in all directions that only emits full packets of its
 * nominal voltage, at most one per tick; fractional EU of the returned energy are lost.
 */
public class Ic2ProducerBlockEntity extends EnergyProducerBlockEntity<EnergyAcceptor>
        implements EnergySource, OutputReadout {
    private final OutputMeter outputMeter = new OutputMeter();

    public Ic2ProducerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.IC2_PRODUCER.get(), pos, state, PowerSystem.INDUSTRIALCRAFT,
                ((PowerConverterBlock) state.getBlock()).getType().getVoltageIndex());
    }

    @Override
    protected @Nullable EnergyAcceptor findAdjacent(Direction side, BlockPos neighbor) {
        return level.getBlockEntity(neighbor) instanceof EnergyAcceptor acceptor ? acceptor : null;
    }

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

    @Override
    public int produceEnergy(int energy) {
        if (!EnergyNets.isAdded(level, this)) {
            return energy;
        }
        int perOutput = powerSystem.getEnergyPerOutput();
        int eu = energy / perOutput;
        int maxOutput = getMaxEnergyOutput();
        if (eu < maxOutput) {
            return energy;
        }
        int producedEu = Math.min(eu, maxOutput);
        int notDistributed = EnergyNets.emit(level, this, producedEu);
        outputMeter.record(level, producedEu - notDistributed);
        return (eu - (producedEu - notDistributed)) * perOutput;
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return powerSystem.getVoltageValue(getVoltageIndex());
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.EU);
    }
}
