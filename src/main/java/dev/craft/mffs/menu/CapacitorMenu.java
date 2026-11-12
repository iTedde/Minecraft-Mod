package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerCapacitor}: security link (154, 88), capacity (154, 47), range (154, 67), link/item (87, 76),
 * player inventory from (8, 125). Synced: FE, fill %, transmit range, linked devices, power link mode.
 */
public class CapacitorMenu extends MffsMachineMenu {
    public CapacitorMenu(int id, Inventory inventory, CapacitorBlockEntity capacitor) {
        this(id, inventory, capacitor.getBlockPos(), capacitor, capacitor.getInventory(),
                new SyncedInts(capacitor::getStorageAvailablePower, capacitor::getPercentageStorageCapacity,
                        capacitor::getTransmitRange, capacitor::getLinkedDevices, capacitor::getPowerLinkMode),
                capacitor);
    }

    public CapacitorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(5), new SyncedInts(5),
                ExtractorMenu.rulesAt(inventory, pos));
    }

    private CapacitorMenu(int id, Inventory inventory, BlockPos pos, CapacitorBlockEntity capacitor,
                          IItemHandler items, SyncedInts data, MffsMachineBlockEntity rules) {
        super(MffsMenus.CAPACITOR.get(), id, pos, capacitor, data);
        addMachineSlot(items, CapacitorBlockEntity.SLOT_SECURITY_LINK, 154, 88, rules);
        addMachineSlot(items, CapacitorBlockEntity.SLOT_CAPACITY, 154, 47, rules);
        addMachineSlot(items, CapacitorBlockEntity.SLOT_RANGE, 154, 67, rules);
        addMachineSlot(items, CapacitorBlockEntity.SLOT_LINK, 87, 76, rules);
        addPlayerInventory(inventory, 8, 125);
    }

    public int getForcePower() {
        return getInt(0);
    }

    public int getCapacity() {
        return getInt(1);
    }

    public int getTransmitRange() {
        return getInt(2);
    }

    public int getLinkedDevices() {
        return getInt(3);
    }

    public int getPowerLinkMode() {
        return getInt(4);
    }
}
