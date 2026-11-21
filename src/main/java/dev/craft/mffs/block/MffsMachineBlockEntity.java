package dev.craft.mffs.block;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.mffs.MffsChunkLoading;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.item.CardItem;
import dev.craft.mffs.item.PersonalIdCardItem;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code TileEntityMachines} (spec 48 §5): device name and ID, switch mode (1 redstone, 2 switch, 3 control system)
 * and switch value, active flag (block state), chunk loading, slot rules, plugin drops, wrench access and the GUI
 * events 0 (switch mode), 10/11/12 (name: clear, append, backspace).
 */
public abstract class MffsMachineBlockEntity extends BlockEntity implements MenuProvider, Wrenchable {
    public static final String DEFAULT_NAME = "Please set Name";
    public static final int EVENT_SWITCH_MODE = 0;
    public static final int EVENT_NAME_CLEAR = 10;
    public static final int EVENT_NAME_APPEND = 11;
    public static final int EVENT_NAME_BACKSPACE = 12;

    protected final ItemStackHandler inventory;
    private int deviceId;
    private String deviceName = DEFAULT_NAME;
    private int switchMode;
    private boolean switchValue;
    private boolean active;
    private int ticker;
    private boolean initialized;

    protected MffsMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state);
        this.inventory = new ItemStackHandler(inventorySize) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return MffsMachineBlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return getSlotStackLimit(slot);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    onInventoryChanged(slot);
                }
            }
        };
    }

    public abstract MffsDevices.Kind getDeviceKind();

    public abstract boolean isItemValid(int slot, ItemStack stack);

    public abstract int getSlotStackLimit(int slot);

    /** {@code SlotHelper.isItemValid}: what the GUI slots take (automation may differ, e.g. defence storage). */
    public boolean mayPlaceInGui(int slot, ItemStack stack) {
        return isItemValid(slot, stack);
    }

    /** The machine's own work, after the device has been registered. */
    protected abstract void tickMachine();

    public int getMinSwitchMode() {
        return 0;
    }

    public int getMaxSwitchMode() {
        return 0;
    }

    public final void serverTick() {
        if (!initialized) {
            init();
        }
        tickMachine();
    }

    /** {@code init}: device ID from the link grid, forced chunk (pack {@code Chunkloader=true}). */
    private void init() {
        deviceId = MffsDevices.register(this, deviceId);
        if (level instanceof ServerLevel serverLevel) {
            MffsChunkLoading.force(serverLevel, worldPosition, true);
        }
        initialized = true;
        markForUpdate();
    }

    public void releaseChunk(ServerLevel serverLevel) {
        MffsChunkLoading.force(serverLevel, worldPosition, false);
    }

    @Override
    public void setRemoved() {
        if (initialized && level != null) {
            MffsDevices.unregister(this);
        }
        super.setRemoved();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public void setStack(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String name) {
        deviceName = name;
        markForUpdate();
    }

    public int getSwitchMode() {
        if (switchMode < getMinSwitchMode()) {
            switchMode = getMinSwitchMode();
        }
        return switchMode;
    }

    public void toggleSwitchMode() {
        if (getSwitchMode() == getMaxSwitchMode()) {
            switchMode = getMinSwitchMode();
        } else {
            switchMode++;
        }
        markForUpdate();
    }

    public boolean getSwitchValue() {
        return switchValue;
    }

    /** {@code ISwitchabel.isSwitchabel}: only in switch mode 2 the multitool switch works. */
    public boolean isSwitchable() {
        return getSwitchMode() == 2;
    }

    public void toggleSwitchValue() {
        switchValue = !switchValue;
        markForUpdate();
    }

    /** Switch mode 1: the switch value follows the redstone signal. */
    protected void followRedstone() {
        boolean signal = isRedstoneSignal();
        if (getSwitchMode() == 1 && switchValue != signal) {
            toggleSwitchValue();
        }
    }

    public boolean isRedstoneSignal() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    /** {@code getLinkedSecurityStation}: the station deciding the rights for this machine, or null. */
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return null;
    }

    /** {@code hasPowerSource}: machines without a power link slot have none. */
    public boolean hasPowerSource() {
        return false;
    }

    /** {@code getPercentageCapacity}: fill of the power source in percent. */
    public int getPercentageCapacity() {
        return 0;
    }

    /**
     * Rights needed to open the GUI ({@code BlockMFFSBase.onBlockActivated}): EB; an active security station also
     * CSR, the control system also UCS.
     */
    public List<SecurityRight> getOpenRights() {
        return List.of(SecurityRight.EB);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean flag) {
        active = flag;
        if (level != null && getBlockState().getBlock() instanceof MffsMachineBlock
                && getBlockState().getValue(MffsMachineBlock.ACTIVE) != flag) {
            level.setBlock(worldPosition, getBlockState().setValue(MffsMachineBlock.ACTIVE, flag), 3);
        }
        markForUpdate();
    }

    protected int getTicker() {
        return ticker;
    }

    protected void setTicker(int ticker) {
        this.ticker = ticker;
    }

    /** GUI events of the original network handler; the machine kinds add their own keys. */
    public void handleEvent(int key, String value, ServerPlayer player) {
        handleEvent(key, value);
    }

    public void handleEvent(int key, String value) {
        switch (key) {
            case EVENT_SWITCH_MODE -> toggleSwitchMode();
            case EVENT_NAME_CLEAR -> setDeviceName("");
            case EVENT_NAME_APPEND -> {
                // The original appends while the name is at most 20 characters long (so up to 21).
                if (deviceName.length() <= 20 && value.length() == 1) {
                    setDeviceName(deviceName + value);
                }
            }
            case EVENT_NAME_BACKSPACE -> {
                if (!deviceName.isEmpty()) {
                    setDeviceName(deviceName.substring(0, deviceName.length() - 1));
                }
            }
            default -> {
            }
        }
    }

    /** {@code onInventoryChanged} on the server ({@code TileEntityProjector.d → checkslots}). */
    protected void onInventoryChanged(int slot) {
    }

    /** {@code dropplugins}: every slot falls out; cards holding data fall as blank cards. */
    public void dropPlugins() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            dropPlugin(slot);
        }
    }

    /** {@code dropplugins(slot, inventory)}: one slot falls out. */
    public void dropPlugin(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (level == null || stack.isEmpty()) {
            return;
        }
        ItemStack drop = stack.getItem() instanceof CardItem || stack.getItem() instanceof PersonalIdCardItem
                ? new ItemStack(MffsItems.CARD_BLANK.get()) : stack;
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), drop);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    public void markForUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // --- Wrench (IC2 IWrenchable and the multitool's IMFFS_Wrench) ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(MffsMachineBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(MffsMachineBlock.FACING, facing), 3);
        }
    }

    /** {@code wrenchCanManipulate}: right EB. */
    public boolean wrenchCanManipulate(Player player) {
        return MffsSecurity.isAccessGranted(this, player, SecurityRight.EB);
    }

    /**
     * Multitool wrench: projector, security station and defence station refuse turning and removal while active;
     * the other machines always allow it.
     */
    public boolean canBeRemovedByMultitool() {
        return true;
    }

    /** Security storage and security station cannot be turned; active machines neither. */
    protected boolean canBeTurned() {
        return true;
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        if (side == getFacing() || !canBeTurned() || active) {
            return false;
        }
        return wrenchCanManipulate(player);
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        return !active && wrenchCanManipulate(player);
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- Saving and client sync ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveState(tag);
        tag.put("Items", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadState(tag);
        if (tag.contains("Items")) {
            inventory.deserializeNBT(registries, tag.getCompound("Items"));
        }
    }

    protected void saveState(CompoundTag tag) {
        tag.putShort("SwitchModi", (short) switchMode);
        tag.putBoolean("active", active);
        tag.putBoolean("SwitchValue", switchValue);
        tag.putInt("DeviceID", deviceId);
        tag.putString("DeviceName", deviceName);
    }

    protected void loadState(CompoundTag tag) {
        switchMode = tag.getShort("SwitchModi");
        active = tag.getBoolean("active");
        switchValue = tag.getBoolean("SwitchValue");
        deviceId = tag.getInt("DeviceID");
        deviceName = tag.contains("DeviceName") ? tag.getString("DeviceName") : DEFAULT_NAME;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveState(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
