package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerControlSystem}: security link (236, 4), reader (203, 30), storage 4–31 in 4 × 7 from (176, 80),
 * player inventory from (8, 134); no shift-click (as in the original).
 */
public class ControlSystemMenu extends MffsMachineMenu {
    private final IItemHandler items;

    public ControlSystemMenu(int id, Inventory inventory, ControlSystemBlockEntity control) {
        this(id, inventory, control.getBlockPos(), control, control.getInventory(), control);
    }

    public ControlSystemMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(40), ExtractorMenu.rulesAt(inventory, pos));
    }

    private ControlSystemMenu(int id, Inventory inventory, BlockPos pos, ControlSystemBlockEntity control,
                              IItemHandler items, MffsMachineBlockEntity rules) {
        super(MffsMenus.CONTROL_SYSTEM.get(), id, pos, control, new SyncedInts(0));
        this.items = items;
        addMachineSlot(items, ControlSystemBlockEntity.SLOT_SECURITY_LINK, 236, 4, rules);
        addMachineSlot(items, ControlSystemBlockEntity.SLOT_READER, 203, 30, rules);
        for (int row = 0; row < 7; row++) {
            for (int column = 0; column < 4; column++) {
                addMachineSlot(items, column + row * 4 + 4, 176 + column * 18, 80 + row * 18, rules);
            }
        }
        addPlayerInventory(inventory, 8, 134);
    }

    public boolean hasReaderCard() {
        return !items.getStackInSlot(ControlSystemBlockEntity.SLOT_READER).isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
