package dev.craft.thermalexpansion.transport;

import java.util.List;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileTeleportEnergy}: a sender passes its whole buffer (100 MJ, input × 0.75) to its partners every tick; a
 * receiver gives at most 100 MJ per tick ({@code tickEnergyLimit}) to the adjacent MJ receivers that ask for energy,
 * other energy tesseracts excluded. Pipes and conduits see a request only while it can send somewhere.
 */
public class EnergyTesseractBlockEntity extends TesseractBlockEntity implements PowerReceptor {
    /** {@code Utils.dummyProvider}: what a tesseract without its redstone condition offers. */
    private static final PowerProvider DUMMY = new PowerProvider();

    private final TesseractPowerProvider provider = new TesseractPowerProvider();
    private int tickEnergyLimit;

    public EnergyTesseractBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.ENERGY_TESSERACT.get(), TesseractType.ENERGY, pos, state);
    }

    public float getEnergy() {
        return provider.getEnergyStored();
    }

    @Override
    public void serverTick() {
        if (canReceive()) {
            tickEnergyLimit = TesseractPowerProvider.TELEPORT_MAX_ENERGY;
        }
        if (redstoneControlOrDisable() && provider.getEnergyStored() > 0.0F) {
            sendEnergy();
        }
    }

    private void sendEnergy() {
        List<TesseractBlockEntity> outputs = getValidOutputs();
        for (int i = outputTrackerRemote; i < outputs.size() && provider.getEnergyStored() > 0.0F; i++) {
            send((EnergyTesseractBlockEntity) outputs.get(i));
        }
        for (int i = 0; i < outputs.size() && i < outputTrackerRemote && provider.getEnergyStored() > 0.0F; i++) {
            send((EnergyTesseractBlockEntity) outputs.get(i));
        }
        incrementOutputTrackerRemote(outputs.size() - 1);
        setChanged();
    }

    private void send(EnergyTesseractBlockEntity target) {
        float used = target.receiveEnergy(provider.getEnergyStored());
        provider.setEnergyStored(Math.max(0.0F, provider.getEnergyStored() - used));
    }

    /** {@code receiveEnergy(maxAdd)}: hand out up to the tick limit to the neighbours in turn. */
    float receiveEnergy(float maxAdd) {
        if (tickEnergyLimit <= 0) {
            return 0.0F;
        }
        maxAdd = Math.min(maxAdd, tickEnergyLimit);
        float wasMax = maxAdd;
        for (int side = outputTrackerAdjacent; side < 6 && maxAdd > 0.0F; side++) {
            float used = addToAdjacent(Direction.from3DDataValue(side), maxAdd);
            maxAdd -= used;
            tickEnergyLimit = (int) (tickEnergyLimit - used);
        }
        for (int side = 0; side < outputTrackerAdjacent && maxAdd > 0.0F; side++) {
            float used = addToAdjacent(Direction.from3DDataValue(side), maxAdd);
            maxAdd -= used;
            tickEnergyLimit = (int) (tickEnergyLimit - used);
        }
        incrementOutputTrackerAdjacent();
        return wasMax - maxAdd;
    }

    /** {@code Utils.addToAdjPowerProvider}: what the neighbour asks for and has room for, at most {@code maxAdd}. */
    private float addToAdjacent(Direction side, float maxAdd) {
        BlockPos neighbour = worldPosition.relative(side);
        if (level.getBlockEntity(neighbour) instanceof EnergyTesseractBlockEntity) {
            return 0.0F;
        }
        PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, neighbour, side.getOpposite());
        PowerProvider target = receptor != null ? receptor.getPowerProvider() : null;
        if (target == null) {
            return 0.0F;
        }
        float powerRequested = receptor.powerRequest();
        if (powerRequested <= 0.0F) {
            return 0.0F;
        }
        float energy = Math.min(Math.min(powerRequested, target.getMaxEnergyStored() - target.getEnergyStored()),
                maxAdd);
        target.receiveEnergy(energy, side.getOpposite());
        return energy;
    }

    /** {@code Utils.isAdjacentPoweredTile}. */
    @Override
    protected boolean hasAdjacentTarget(Direction side) {
        PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, worldPosition.relative(side),
                side.getOpposite());
        return receptor != null && receptor.getPowerProvider() != null;
    }

    @Override
    public PowerProvider getPowerProvider() {
        return redstoneControlOrDisable() ? provider : DUMMY;
    }

    @Override
    public void doWork() {
    }

    @Override
    public int powerRequest() {
        if (getFrequency() == -1 || !redstoneControlOrDisable() || !canSend()
                || provider.getEnergyStored() == provider.getMaxEnergyStored() || getValidOutputs().isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(Math.min(provider.getMaxEnergyReceived(),
                provider.getMaxEnergyStored() - provider.getEnergyStored()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        float energy = tag.getFloat("energy.stored");
        provider.setEnergyStored(Float.isNaN(energy) ? 0.0F
                : Math.max(0.0F, Math.min(provider.getMaxEnergyStored(), energy)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("energy.stored", provider.getEnergyStored());
    }
}
