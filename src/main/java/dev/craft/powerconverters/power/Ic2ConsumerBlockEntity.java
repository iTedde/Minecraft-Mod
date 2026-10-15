package dev.craft.powerconverters.power;

import dev.craft.core.energy.EnergyEmitter;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerconverters.block.PowerConverterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEntityIndustrialCraftConsumer}: an EU sink on all sides that stores {@code amount · 1800} PCU in the
 * bridges. It never explodes – IC2 does not check {@code getMaxSafeInput} in the energy net.
 */
public class Ic2ConsumerBlockEntity extends EnergyConsumerBlockEntity<EnergyEmitter> implements EnergySink {
    private int euLastTick;
    private long lastTickInjected;

    public Ic2ConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.IC2_CONSUMER.get(), pos, state, PowerSystem.INDUSTRIALCRAFT,
                ((PowerConverterBlock) state.getBlock()).getType().getVoltageIndex());
    }

    @Override
    protected @Nullable EnergyEmitter findAdjacent(Direction side, BlockPos neighbor) {
        return level.getBlockEntity(neighbor) instanceof EnergyEmitter emitter ? emitter : null;
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
    protected void tick() {
        if (level.getGameTime() - lastTickInjected > 20L) {
            euLastTick = 0;
        }
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return getTotalEnergyDemand() / powerSystem.getEnergyPerInput();
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        int pcuNotStored = storeEnergy(amount * powerSystem.getEnergyPerInput());
        int euNotStored = pcuNotStored / powerSystem.getEnergyPerInput();
        int euThisInjection = amount - euNotStored;
        long time = level.getGameTime();
        if (lastTickInjected == time) {
            euLastTick += euThisInjection;
        } else {
            euLastTick = euThisInjection;
            lastTickInjected = time;
        }
        return euNotStored;
    }

    @Override
    public int getMaxSafeInput() {
        return getVoltageIndex() == ConverterType.IC2_EV_CONSUMER.getVoltageIndex() ? Integer.MAX_VALUE
                : powerSystem.getVoltageValue(getVoltageIndex());
    }

    @Override
    public int getInputRate() {
        return euLastTick;
    }
}
