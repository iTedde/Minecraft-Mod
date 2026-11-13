package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerAdvSecurityStation}: master (177, 33), editing slot (15, 30), copy source 39 (88, 102) and target 38
 * (146, 102), storage 2–33 in 4 × 8 from (176, 62), player inventory from (8, 134).
 */
public class SecurityStationMenu extends MffsMachineMenu {
    private final IItemHandler items;

    public SecurityStationMenu(int id, Inventory inventory, SecurityStationBlockEntity station) {
        this(id, inventory, station.getBlockPos(), station, station.getInventory(), station);
    }

    public SecurityStationMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(40), ExtractorMenu.rulesAt(inventory, pos));
    }

    private SecurityStationMenu(int id, Inventory inventory, BlockPos pos, SecurityStationBlockEntity station,
                                IItemHandler items, MffsMachineBlockEntity rules) {
        super(MffsMenus.SECURITY_STATION.get(), id, pos, station, new SyncedInts(0));
        this.items = items;
        addMachineSlot(items, SecurityStationBlockEntity.SLOT_MASTER, 177, 33, rules);
        addMachineSlot(items, SecurityStationBlockEntity.SLOT_EDIT, 15, 30, rules);
        addMachineSlot(items, SecurityStationBlockEntity.SLOT_COPY_SOURCE, 88, 102, rules);
        addMachineSlot(items, SecurityStationBlockEntity.SLOT_COPY_TARGET, 146, 102, rules);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 4; column++) {
                addMachineSlot(items, column + row * 4 + 2, 176 + column * 18, 62 + row * 18, rules);
            }
        }
        addPlayerInventory(inventory, 8, 134);
    }

    /** {@code getModCardStack}: the card in the editing slot. */
    public ItemStack getEditCard() {
        return items.getStackInSlot(SecurityStationBlockEntity.SLOT_EDIT);
    }
}
