package dev.craft.powerconverters.power;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEntityBuildCraftConsumer}: an MJ receptor ({@code PowerProviderAdvanced}: no power loss, its
 * {@code update} does nothing) that moves the whole MJ of its provider into the bridges every tick.
 */
public class BcConsumerBlockEntity extends EnergyConsumerBlockEntity<PowerReceptor>
        implements PowerReceptor, ConsumptionReadout {
    private final OutputMeter consumptionMeter = new OutputMeter();
    private final PowerProvider powerProvider = new PowerProvider();
    private int mjLastTick;

    public BcConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.BC_CONSUMER.get(), pos, state, PowerSystem.BUILDCRAFT, 0);
        powerProvider.configure(2, 100, 1, 1000);
        powerProvider.configurePowerPerdition(0, 0);
    }

    @Override
    protected @Nullable PowerReceptor findAdjacent(Direction side, BlockPos neighbor) {
        return level.getCapability(BcCapabilities.POWER_RECEPTOR, neighbor, side.getOpposite());
    }

    @Override
    protected void tick() {
        int mjStored = Mth.floor(powerProvider.getEnergyStored());
        if (mjStored > 0) {
            float used = powerProvider.useEnergy(1.0F, mjStored, true);
            consumptionMeter.record(level, used);
            mjLastTick = Mth.floor(used);
            storeEnergy((int) (used * powerSystem.getEnergyPerInput()));
            setChanged();
        } else {
            mjLastTick = 0;
        }
    }

    @Override
    public PowerProvider getPowerProvider() {
        return powerProvider;
    }

    @Override
    public void doWork() {
    }

    /** Demand of all bridges in MJ ({@code getTotalEnergyDemand / internalEnergyPerInput}). */
    @Override
    public int powerRequest() {
        return getTotalEnergyDemand() / powerSystem.getEnergyPerInput();
    }

    @Override
    public int getInputRate() {
        return mjLastTick;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("powerProvider", powerProvider.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("powerProvider")) {
            powerProvider.load(tag.getCompound("powerProvider"));
        }
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.MJ);
    }
}
