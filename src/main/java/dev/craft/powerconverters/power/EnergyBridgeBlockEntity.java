package dev.craft.powerconverters.power;

import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.menu.EnergyBridgeMenu;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Power Converters {@code TileEntityEnergyBridge}: a 16 000 000 PCU buffer filled by adjacent consumers; every tick
 * the adjacent producers are offered the stored energy one after another.
 */
public class EnergyBridgeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_ENERGY = 16_000_000;
    public static final int SCALE = 120;

    private int energyStored;
    private int energyStoredLast;
    private boolean inputLimited;
    private Map<Direction, EnergyProducerBlockEntity<?>> producers = new EnumMap<>(Direction.class);
    private final int[] producerOutputRates = new int[6];
    private boolean initialized;

    public EnergyBridgeBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.ENERGY_BRIDGE.get(), pos, state);
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getEnergyStoredMax() {
        return MAX_ENERGY;
    }

    public void setEnergyStored(int energyStored) {
        this.energyStored = energyStored;
        setChanged();
    }

    /** Stores what fits; returns the rest. */
    public int storeEnergy(int energy) {
        int toStore = Math.min(energy, MAX_ENERGY - energyStored);
        energyStored += toStore;
        if (toStore != 0) {
            setChanged();
        }
        return energy - toStore;
    }

    public void serverTick() {
        if (!initialized) {
            onNeighborChanged();
            initialized = true;
        }
        int energyRemaining = Math.min(energyStored, MAX_ENERGY);
        for (Map.Entry<Direction, EnergyProducerBlockEntity<?>> entry : producers.entrySet()) {
            EnergyProducerBlockEntity<?> producer = entry.getValue();
            int side = entry.getKey().get3DDataValue();
            if (producer.isRemoved()) {
                producerOutputRates[side] = 0;
            } else if (energyRemaining > 0) {
                int energyNotProduced = Math.min(producer.produceEnergy(energyRemaining), energyRemaining);
                producerOutputRates[side] = (energyRemaining - energyNotProduced)
                        / producer.getPowerSystem().getEnergyPerOutput();
                energyRemaining = energyNotProduced;
            } else {
                producer.produceEnergy(0);
                producerOutputRates[side] = 0;
            }
        }
        int stored = Math.max(0, energyRemaining);
        if (stored != energyStored) {
            energyStored = stored;
            setChanged();
        }
        inputLimited = (energyStored != energyStoredLast || energyStored != MAX_ENERGY)
                && energyStored <= energyStoredLast;
        energyStoredLast = energyStored;
    }

    public void onNeighborChanged() {
        if (level == null) {
            return;
        }
        Map<Direction, EnergyProducerBlockEntity<?>> found = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(side)) instanceof EnergyProducerBlockEntity<?> producer) {
                found.put(side, producer);
            }
        }
        producers = found;
    }

    /** {@code getDataForSide} (server side). */
    public SideData getDataForSide(Direction side) {
        if (level != null && level.getBlockEntity(worldPosition.relative(side))
                instanceof BridgeComponentBlockEntity<?> component) {
            boolean consumer = component instanceof EnergyConsumerBlockEntity<?>;
            int rate = component instanceof EnergyConsumerBlockEntity<?> c ? c.getInputRate()
                    : producerOutputRates[side.get3DDataValue()];
            return new SideData(consumer, !consumer, component.getPowerSystem().getId(), component.isConnected(),
                    component.getVoltageIndex(), rate);
        }
        return SideData.NONE;
    }

    public boolean isInputLimited() {
        return inputLimited;
    }

    public int getEnergyScaled() {
        return SCALE * energyStored / MAX_ENERGY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energyStored", energyStored);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getInt("energyStored");
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EnergyBridgeMenu(id, inventory, this);
    }

    /** {@code BridgeSideData}: what the GUI shows for one side; {@code powerSystem} −1 = none. */
    public record SideData(boolean consumer, boolean producer, int powerSystem, boolean connected, int voltageIndex,
                           int outputRate) {
        public static final SideData NONE = new SideData(false, false, -1, false, 0, 0);
    }
}
