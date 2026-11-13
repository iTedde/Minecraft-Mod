package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerAreaDefenseStation}: power link (13, 27), security link (97, 27), warning modulators (14, 51),
 * action modulators (14, 88), item list 5–12 in 4 × 2 from (176, 26), storage 15–34 in 4 × 5 from (176, 98), player
 * inventory from (8, 134); no shift-click. Synced: contra type, action mode, scan mode, fill %, power source,
 * perimeters, security card.
 */
public class DefenseStationMenu extends MffsMachineMenu {
    public DefenseStationMenu(int id, Inventory inventory, DefenseStationBlockEntity station) {
        this(id, inventory, station.getBlockPos(), station, station.getInventory(),
                new SyncedInts(station::getContraType, station::getActionMode, station::getScanMode,
                        station::getPercentageCapacity, () -> station.getPowerSourceId() != 0 ? 1 : 0,
                        station::getInfoDistance, station::getActionDistance,
                        () -> station.hasSecurityCard() ? 1 : 0), station);
    }

    public DefenseStationMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(36), new SyncedInts(8),
                ExtractorMenu.rulesAt(inventory, pos));
    }

    private DefenseStationMenu(int id, Inventory inventory, BlockPos pos, DefenseStationBlockEntity station,
                               IItemHandler items, SyncedInts data, MffsMachineBlockEntity rules) {
        super(MffsMenus.DEFENSE_STATION.get(), id, pos, station, data);
        addMachineSlot(items, DefenseStationBlockEntity.SLOT_POWER_LINK, 13, 27, rules);
        addMachineSlot(items, DefenseStationBlockEntity.SLOT_SECURITY_LINK, 97, 27, rules);
        addMachineSlot(items, DefenseStationBlockEntity.SLOT_WARN_DISTANCE, 14, 51, rules);
        addMachineSlot(items, DefenseStationBlockEntity.SLOT_ACTION_DISTANCE, 14, 88, rules);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 4; column++) {
                addMachineSlot(items, column + row * 4 + DefenseStationBlockEntity.FIRST_LIST, 176 + column * 18,
                        26 + row * 18, rules);
            }
        }
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 4; column++) {
                addMachineSlot(items, column + row * 4 + DefenseStationBlockEntity.FIRST_STORAGE, 176 + column * 18,
                        98 + row * 18, rules);
            }
        }
        addPlayerInventory(inventory, 8, 134);
    }

    public int getContraType() {
        return getInt(0);
    }

    public int getActionMode() {
        return getInt(1);
    }

    public int getScanMode() {
        return getInt(2);
    }

    public int getCapacity() {
        return getInt(3);
    }

    public boolean hasPowerSource() {
        return getInt(4) != 0;
    }

    public int getInfoDistance() {
        return getInt(5);
    }

    public int getActionDistance() {
        return getInt(6);
    }

    public boolean hasSecurityCard() {
        return getInt(7) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
