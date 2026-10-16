package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.GrinderBlockEntity;
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

/**
 * {@code GT_Container_Grinder} (spec 13 §10): ore and cell input, four outputs, progress arrow, water level, structure
 * state and the error code.
 */
public class GtGrinderMenu extends MachineMenu<GrinderBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int MACHINE = 1;
    private static final int WATER = 2;
    private static final int ERROR = 3;
    private static final int MACHINE_SLOTS = 2 + GrinderBlockEntity.OUTPUTS;

    public GtGrinderMenu(int id, Inventory inventory, GrinderBlockEntity grinder) {
        this(id, inventory, grinder, grinder.getBlockPos(), new SyncedInts(grinder::getProgressScale,
                () -> grinder.isMachine() ? 1 : 0, grinder::getWaterAmount, grinder::getDisplayErrorCode));
    }

    public GtGrinderMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(4));
    }

    private GtGrinderMenu(int id, Inventory inventory, @Nullable GrinderBlockEntity grinder, BlockPos pos,
                          SyncedInts data) {
        super(GtMenus.GRINDER.get(), id, grinder, pos, data);
        IItemHandler items = items(grinder, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, GrinderBlockEntity.SLOT_INPUT, 34, 16));
        addMachineSlot(new MachineSlot(items, GrinderBlockEntity.SLOT_CELL, 34, 34));
        for (int i = 0; i < GrinderBlockEntity.OUTPUTS; i++) {
            addMachineSlot(new OutputSlot(items, GrinderBlockEntity.SLOT_OUTPUT_1 + i, 86 + i * 18, 25,
                    inventory.player, false));
        }
        addPlayerInventory(inventory);
    }

    /** Progress arrow width 0–20. */
    public int getProgressScale() {
        return Math.min(20, data.getInt(PROGRESS));
    }

    public boolean isMachine() {
        return data.getInt(MACHINE) != 0;
    }

    /** Water level behind the cell slot: {@code mWaterAmount / 550}, at most 18 pixels. */
    public int getWaterScale() {
        return Math.min(18, data.getInt(WATER) / 550);
    }

    /** {@code mDisplayErrorCode} bit 1: insufficient energy. */
    public boolean isMissingEnergy() {
        return (data.getInt(ERROR) & 1) != 0;
    }

    /** {@code GT_Container.transferStackInSlot}: machine → player inventory (from the back), player → the two inputs. */
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
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
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
