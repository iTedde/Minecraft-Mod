package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.energy.ForceEnergyItem;
import dev.craft.mffs.energy.ForceEnergyStorage;
import dev.craft.mffs.energy.PowerLinkItem;
import dev.craft.mffs.item.PowerLinkCardItem;
import dev.craft.mffs.menu.CapacitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityCapacitor} (spec 48 §6.1): stores 10 Mio. FE (+2 Mio. per capacity upgrade), sends it to every
 * machine with a power link card in its range (8 per range upgrade + 8), charges/discharges force energy items in
 * slot 2 or passes energy on to another capacitor linked there.
 */
public class CapacitorBlockEntity extends FePoweredBlockEntity implements ForceEnergyStorage {
    public static final int SLOT_CAPACITY = 0;
    public static final int SLOT_RANGE = 1;
    public static final int SLOT_LINK = 2;
    public static final int SLOT_SECURITY_LINK = 4;
    public static final int EVENT_LINK_MODE = 1;
    /** Power link modes: 0 pass on at ≥ 95 %, 1 balance, 2 pass on everything, 3 charge item, 4 discharge item. */
    public static final int MODE_CHARGE_ITEM = 3;
    public static final int MODE_DISCHARGE_ITEM = 4;

    private int forcePower;
    private int linkedDevices;
    private int capacity;
    private int transmitRange = 8;
    private int powerLinkMode;
    private boolean checkedSlots;

    public CapacitorBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.CAPACITOR.get(), pos, state, 5);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.CAPACITOR;
    }

    @Override
    public int getPowerLinkSlot() {
        return SLOT_LINK;
    }

    @Override
    public int getMinSwitchMode() {
        return 1;
    }

    @Override
    public int getMaxSwitchMode() {
        return 3;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CAPACITY -> stack.is(MffsItems.CAPACITOR_UPGRADE_CAPACITY.get());
            case SLOT_RANGE -> stack.is(MffsItems.CAPACITOR_UPGRADE_RANGE.get());
            case SLOT_LINK -> stack.getItem() instanceof ForceEnergyItem || stack.getItem() instanceof PowerLinkItem;
            case SLOT_SECURITY_LINK -> false; // security link card: phase 48.4
            default -> false;
        };
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return slot == SLOT_CAPACITY || slot == SLOT_RANGE ? 9 : 1;
    }

    @Override
    protected void tickMachine() {
        if (!checkedSlots) {
            checkSlots();
            checkedSlots = true;
        }
        followRedstone();
        if (getSwitchValue()) {
            if (!isActive()) {
                setActive(true);
            }
        } else if (isActive()) {
            setActive(false);
        }
        if (getTicker() == 10) {
            int connected = connectedDevices();
            int percent = forcePower / 1000 * 100 / (getStorageMaxPower() / 1000);
            if (connected != linkedDevices || percent != capacity) {
                linkedDevices = connected;
                capacity = percent;
            }
            checkSlots();
            if (isActive()) {
                powerTransfer();
            }
            setTicker(0);
            setChanged();
        }
        setTicker(getTicker() + 1);
    }

    /** {@code Linkgrid.connectedtoCapacitor}: machines whose power source is this capacitor and that are in range. */
    private int connectedDevices() {
        if (level == null) {
            return 0;
        }
        int count = 0;
        for (MffsDevices.Kind kind : MffsDevices.Kind.values()) {
            for (MffsMachineBlockEntity machine : MffsDevices.all(level, kind)) {
                if (machine instanceof FePoweredBlockEntity powered && powered.getPowerSourceId() == getDeviceId()
                        && transmitRange >= Math.sqrt(machine.getBlockPos().distSqr(worldPosition))) {
                    count++;
                }
            }
        }
        return count;
    }

    private void checkSlots() {
        ItemStack range = getStack(SLOT_RANGE);
        if (!range.isEmpty()) {
            if (range.is(MffsItems.CAPACITOR_UPGRADE_RANGE.get())) {
                transmitRange = 8 * (range.getCount() + 1);
            }
        } else {
            transmitRange = 8;
        }
        ItemStack stack = getStack(SLOT_LINK);
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof ForceEnergyItem item) {
            if (powerLinkMode != MODE_CHARGE_ITEM && powerLinkMode != MODE_DISCHARGE_ITEM) {
                powerLinkMode = MODE_CHARGE_ITEM;
            }
            if (powerLinkMode == MODE_CHARGE_ITEM) {
                chargeItem(stack, item);
            } else {
                dischargeItem(stack, item);
            }
        }
        if (stack.getItem() instanceof PowerLinkCardItem && powerLinkMode > 2) {
            powerLinkMode = 0;
        }
    }

    private void chargeItem(ItemStack stack, ForceEnergyItem item) {
        int available = item.getAvailablePower(stack);
        int max = item.getMaximumPower(stack);
        if (available >= max || getStorageAvailablePower() <= 0) {
            return;
        }
        int rate = item.getPowerTransferRate();
        int free = max - available;
        int amount;
        if (getStorageAvailablePower() > rate) {
            amount = Math.min(free, rate);
        } else {
            amount = Math.min(free, getStorageAvailablePower());
        }
        item.setAvailablePower(stack, available + amount);
        setForcePower(getStorageAvailablePower() - amount);
    }

    private void dischargeItem(ItemStack stack, ForceEnergyItem item) {
        int left = item.getAvailablePower(stack);
        if (left <= 0) {
            return;
        }
        int rate = item.getPowerTransferRate();
        int free = getStorageMaxPower() - getStorageAvailablePower();
        // Original: with enough room the rate limits; without, the free room moves at once (no rate limit).
        int amount = free >= left ? Math.min(left, rate) : free;
        item.setAvailablePower(stack, left - amount);
        setForcePower(getStorageAvailablePower() + amount);
    }

    /** {@code powertransfer} to the capacitor in slot 2, rate target max / 120 per 10 ticks. */
    private void powerTransfer() {
        if (!hasPowerSource()) {
            return;
        }
        int rate = getMaximumPower() / 120;
        int free = getMaximumPower() - getAvailablePower();
        int balance = getStorageAvailablePower() - getAvailablePower();
        switch (powerLinkMode) {
            case 0 -> {
                if (getPercentageStorageCapacity() >= 95 && getPercentageCapacity() != 100) {
                    send(Math.min(free, rate));
                }
            }
            case 1 -> {
                if (getPercentageCapacity() < getPercentageStorageCapacity()) {
                    send(Math.min(balance, rate));
                }
            }
            case 2 -> {
                if (getStorageAvailablePower() > 0 && getPercentageCapacity() != 100) {
                    if (getStorageAvailablePower() > rate) {
                        send(Math.min(free, rate));
                    } else {
                        send(Math.min(free, getStorageAvailablePower()));
                    }
                }
            }
            default -> {
            }
        }
    }

    private void send(int amount) {
        emitPower(amount, false);
        consumePowerFromStorage(amount, false);
    }

    @Override
    public void handleEvent(int key, String value) {
        if (key == EVENT_LINK_MODE) {
            ItemStack stack = getStack(SLOT_LINK);
            if (stack.getItem() instanceof ForceEnergyItem) {
                powerLinkMode = powerLinkMode == MODE_DISCHARGE_ITEM ? MODE_CHARGE_ITEM : MODE_DISCHARGE_ITEM;
                return;
            }
            if (stack.getItem() instanceof PowerLinkCardItem) {
                powerLinkMode = powerLinkMode < 2 ? powerLinkMode + 1 : 0;
                return;
            }
            if (powerLinkMode != MODE_DISCHARGE_ITEM) {
                powerLinkMode++;
            } else {
                powerLinkMode = 0;
            }
            return;
        }
        super.handleEvent(key, value);
    }

    public int getPowerLinkMode() {
        return powerLinkMode;
    }

    public int getLinkedDevices() {
        return linkedDevices;
    }

    public void setForcePower(int value) {
        forcePower = value;
    }

    // --- ForceEnergyStorage ---

    @Override
    public int getStorageAvailablePower() {
        return forcePower;
    }

    /** Clamps the stored energy when capacity upgrades were taken out. */
    @Override
    public int getStorageMaxPower() {
        ItemStack stack = getStack(SLOT_CAPACITY);
        int max = stack.is(MffsItems.CAPACITOR_UPGRADE_CAPACITY.get()) ? 10000000 + 2000000 * stack.getCount()
                : 10000000;
        if (forcePower > max) {
            forcePower = max;
        }
        return max;
    }

    @Override
    public int getPowerStorageId() {
        return getDeviceId();
    }

    @Override
    public int getPercentageStorageCapacity() {
        return capacity;
    }

    @Override
    public boolean consumePowerFromStorage(int amount, boolean simulate) {
        if (simulate) {
            return forcePower >= amount;
        }
        forcePower = Math.max(0, forcePower - amount);
        setChanged();
        return true;
    }

    @Override
    public boolean insertPowerToStorage(int amount, boolean simulate) {
        if (simulate) {
            return forcePower + amount <= getStorageMaxPower();
        }
        forcePower += amount;
        setChanged();
        return true;
    }

    @Override
    public int getFreeStorageAmount() {
        return getStorageMaxPower() - forcePower;
    }

    @Override
    public int getTransmitRange() {
        return transmitRange;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("forcepower", forcePower);
        tag.putShort("Powerlinkmode", (short) powerLinkMode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        forcePower = tag.getInt("forcepower");
        powerLinkMode = tag.getShort("Powerlinkmode");
    }

    @Override
    protected void saveState(CompoundTag tag) {
        super.saveState(tag);
        tag.putShort("Powerlinkmode", (short) powerLinkMode);
    }

    @Override
    protected void loadState(CompoundTag tag) {
        super.loadState(tag);
        powerLinkMode = tag.getShort("Powerlinkmode");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CapacitorMenu(id, playerInventory, this);
    }
}
