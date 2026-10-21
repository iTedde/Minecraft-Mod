package dev.craft.thermalexpansion.energy;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.TeRedstoneControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEnergyCell}: 600 000 MJ. Every tick (while the redstone condition holds) it gives each adjacent MJ
 * receiver that asks for energy up to {@code energySend} (0–100) MJ, starting one side further each tick; other energy
 * cells get nothing. Pipes see a request of up to {@code energyReceive} (0–100) MJ; engines fill it regardless (up to
 * their stroke energy). It never loses energy. Light 0–10 with the charge; the client sees the charge only when the
 * light step changes.
 */
public class EnergyCellBlockEntity extends BlockEntity implements MenuProvider, PowerReceptor, TeRedstoneControl {
    public static final int STORAGE = 600000;
    public static final int MAX_SEND = 100;
    public static final int MAX_RECEIVE = 100;
    public static final int DEFAULT_SEND = 50;
    public static final int DEFAULT_RECEIVE = 100;

    private final PowerProvider provider = new PowerProvider();
    private int energySend;
    private int energyReceive;
    /** Last light step sent; -1 after loading, so the block state is checked once. */
    private int energyTracker = -1;
    private int outputTracker;
    private boolean redstoneDisable;
    private boolean redstoneState;

    public EnergyCellBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.ENERGY_CELL.get(), pos, state);
        // PowerProviderAdv, configure(latency 0, min 2, max 100, activation 0, 600 000); no loss.
        provider.configure(2, MAX_RECEIVE, 0, STORAGE);
        provider.configurePowerPerdition(0, 72000);
    }

    public float getEnergy() {
        return provider.getEnergyStored();
    }

    public void setEnergy(float energy) {
        provider.setEnergyStored(Math.max(0.0F, Math.min(STORAGE, energy)));
    }

    public int getEnergySend() {
        return energySend;
    }

    public int getEnergyReceive() {
        return energyReceive;
    }

    /** The GUI buttons ({@code setEnergyConfig}), clamped to 0–100. */
    public void setLimits(int send, int receive) {
        energySend = Math.max(0, Math.min(MAX_SEND, send));
        energyReceive = Math.max(0, Math.min(MAX_RECEIVE, receive));
        sync();
    }

    public void load(EnergyCellData data) {
        setEnergy(data.energy());
        energySend = Math.min(MAX_SEND, data.send());
        energyReceive = Math.min(MAX_RECEIVE, data.receive());
        redstoneDisable = data.redstoneDisable();
        redstoneState = data.redstoneState();
        energyTracker = lightLevel();
        updateLight();
        sync();
    }

    public EnergyCellData toData() {
        return new EnergyCellData(provider.getEnergyStored(), energySend, energyReceive, redstoneDisable,
                redstoneState);
    }

    /** {@code getScaledEnergyStored}. */
    public int scaledEnergy(int scale) {
        return (int) (provider.getEnergyStored() * scale / STORAGE);
    }

    /** {@code getLightValue}. */
    public int lightLevel() {
        return Math.min(10, scaledEnergy(11));
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    @Override
    public int powerRequest() {
        if (provider.getEnergyStored() == STORAGE) {
            return 0;
        }
        return (int) Math.ceil(Math.min(energyReceive, STORAGE - provider.getEnergyStored()));
    }

    @Override
    public boolean getRedstoneDisable() {
        return redstoneDisable;
    }

    @Override
    public boolean getRedstoneState() {
        return redstoneState;
    }

    @Override
    public void setRedstone(boolean disable, boolean state) {
        redstoneDisable = disable;
        redstoneState = state;
        sync();
    }

    public void serverTick() {
        if (redstoneDisable || level.hasNeighborSignal(worldPosition) == redstoneState) {
            for (int i = outputTracker; i < 6; i++) {
                transferEnergy(Direction.from3DDataValue(i));
            }
            for (int i = 0; i < outputTracker; i++) {
                transferEnergy(Direction.from3DDataValue(i));
            }
            outputTracker = (outputTracker + 1) % 6;
        }
        int energyStage = lightLevel();
        if (energyTracker != energyStage) {
            energyTracker = energyStage;
            updateLight();
            sync();
        }
        setChanged();
    }

    private void transferEnergy(Direction side) {
        BlockPos neighbour = worldPosition.relative(side);
        if (level.getBlockEntity(neighbour) instanceof EnergyCellBlockEntity) {
            return;
        }
        PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, neighbour, side.getOpposite());
        PowerProvider target = receptor != null ? receptor.getPowerProvider() : null;
        if (target == null) {
            return;
        }
        float powerRequested = receptor.powerRequest();
        if (powerRequested > 0.0F) {
            float adjusted = Math.min(powerRequested, target.getMaxEnergyStored() - target.getEnergyStored());
            float energy = provider.useEnergy(0.0F, Math.min(adjusted, energySend), true);
            target.receiveEnergy(energy, side.getOpposite());
        }
    }

    private void updateLight() {
        BlockState state = getBlockState();
        int light = lightLevel();
        if (level != null && !level.isClientSide() && state.getValue(EnergyCellBlock.CHARGE) != light) {
            level.setBlock(worldPosition, state.setValue(EnergyCellBlock.CHARGE, light), Block.UPDATE_ALL);
        }
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new EnergyCellMenu(id, playerInventory, this);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag);
        outputTracker = tag.getInt("output") % 6;
    }

    private void loadClientData(CompoundTag tag) {
        float energy = tag.getFloat("energy.stored");
        setEnergy(Float.isNaN(energy) ? 0.0F : energy);
        energySend = Math.min(MAX_SEND, tag.getInt("energy.send"));
        energyReceive = Math.min(MAX_RECEIVE, tag.getInt("energy.receive"));
        redstoneDisable = tag.getBoolean("red.disable");
        redstoneState = tag.getBoolean("red.state");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag);
        tag.putInt("output", outputTracker);
    }

    private void saveClientData(CompoundTag tag) {
        tag.putFloat("energy.stored", provider.getEnergyStored());
        tag.putInt("energy.send", energySend);
        tag.putInt("energy.receive", energyReceive);
        tag.putBoolean("red.disable", redstoneDisable);
        tag.putBoolean("red.state", redstoneState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveClientData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
