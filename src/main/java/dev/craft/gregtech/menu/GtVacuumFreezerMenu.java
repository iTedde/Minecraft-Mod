package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.VacuumFreezerBlockEntity;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** {@code GT_Container_VacuumFreezer} (spec 13 §11): input, output, progress arrow, structure state, energy error. */
public class GtVacuumFreezerMenu extends MachineMenu<VacuumFreezerBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int MACHINE = 1;
    private static final int ERROR = 2;
    private static final int MACHINE_SLOTS = 2;

    public GtVacuumFreezerMenu(int id, Inventory inventory, VacuumFreezerBlockEntity freezer) {
        this(id, inventory, freezer, freezer.getBlockPos(), new SyncedInts(freezer::getProgressScale,
                () -> freezer.isMachine() ? 1 : 0, freezer::getDisplayErrorCode));
    }

    public GtVacuumFreezerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private GtVacuumFreezerMenu(int id, Inventory inventory, @Nullable VacuumFreezerBlockEntity freezer, BlockPos pos,
                                SyncedInts data) {
        super(GtMenus.VACUUM_FREEZER.get(), id, freezer, pos, data);
        IItemHandler items = items(freezer, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, VacuumFreezerBlockEntity.SLOT_INPUT, 34, 25));
        addMachineSlot(new OutputSlot(items, VacuumFreezerBlockEntity.SLOT_OUTPUT, 86, 25, inventory.player, false));
        addPlayerInventory(inventory);
    }

    /** Progress arrow width 0–20. */
    public int getProgressScale() {
        return Math.min(20, data.getInt(PROGRESS));
    }

    public boolean isMachine() {
        return data.getInt(MACHINE) != 0;
    }

    /** {@code mDisplayErrorCode} bit 1: insufficient energy. */
    public boolean isMissingEnergy() {
        return (data.getInt(ERROR) & 1) != 0;
    }

    /** {@code GT_Container.transferStackInSlot}: machine → player inventory (from the back), player → the input. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        int before = stack.getCount();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        if (stack.getCount() != before) {
            source.onTake(player, stack);
        }
        return ItemStack.EMPTY;
    }
}
