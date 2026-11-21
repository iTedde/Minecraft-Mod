package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** {@code ContainerSecStorage}: security link (12, 24), storage 1–54 in 9 × 6 from (12, 43), player from (12, 155). */
public class SecurityStorageMenu extends MffsMachineMenu {
    public SecurityStorageMenu(int id, Inventory inventory, SecurityStorageBlockEntity storage) {
        this(id, inventory, storage.getBlockPos(), storage, storage.getInventory(), storage);
    }

    public SecurityStorageMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(60), ExtractorMenu.rulesAt(inventory, pos));
    }

    private SecurityStorageMenu(int id, Inventory inventory, BlockPos pos, SecurityStorageBlockEntity storage,
                                IItemHandler items, MffsMachineBlockEntity rules) {
        super(MffsMenus.SECURITY_STORAGE.get(), id, pos, storage, new SyncedInts(0));
        addMachineSlot(items, SecurityStorageBlockEntity.SLOT_SECURITY_LINK, 12, 24, rules);
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                addMachineSlot(items, column + row * 9 + 1, 12 + column * 18, 43 + row * 18, rules);
            }
        }
        addPlayerInventory(inventory, 12, 155);
    }
}
