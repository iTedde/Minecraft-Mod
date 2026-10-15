package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.GtBasicMachineBlockEntity;
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
 * {@code GT_Container_BasicMachine} (spec 13 §1.8): two inputs, battery, two outputs and three toggle buttons
 * ({@link #BUTTON_OUTPUT}, {@link #BUTTON_ITEM_TRANSFER}, {@link #BUTTON_SEPARATED_INPUTS}).
 */
public class GtBasicMachineMenu extends MachineMenu<GtBasicMachineBlockEntity> {
    public static final int BUTTON_OUTPUT = 0;
    public static final int BUTTON_ITEM_TRANSFER = 1;
    public static final int BUTTON_SEPARATED_INPUTS = 2;
    private static final int PROGRESS = 0;
    private static final int OUTPUT = 1;
    private static final int ITEM_TRANSFER = 2;
    private static final int SEPARATED_INPUTS = 3;
    private static final int ENERGY = 4;
    private static final int CAPACITY = 5;
    private static final int MACHINE_SLOTS = 5;

    public GtBasicMachineMenu(int id, Inventory inventory, GtBasicMachineBlockEntity machine) {
        this(id, inventory, machine, machine.getBlockPos(), new SyncedInts(machine::getProgressBar,
                () -> machine.isOutputEnabled() ? 1 : 0, () -> machine.isItemTransfer() ? 1 : 0,
                () -> machine.isSeparatedInputs() ? 1 : 0, machine::getStored, machine::getCapacity));
    }

    public GtBasicMachineMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(6));
    }

    private GtBasicMachineMenu(int id, Inventory inventory, @Nullable GtBasicMachineBlockEntity machine, BlockPos pos,
                               SyncedInts data) {
        super(GtMenus.BASIC_MACHINE.get(), id, machine, pos, data);
        IItemHandler items = items(machine, 6);
        addMachineSlot(new MachineSlot(items, GtBasicMachineBlockEntity.SLOT_INPUT_1, 35, 25));
        addMachineSlot(new MachineSlot(items, GtBasicMachineBlockEntity.SLOT_INPUT_2, 53, 25));
        addMachineSlot(new MachineSlot(items, GtBasicMachineBlockEntity.SLOT_BATTERY, 80, 63));
        addMachineSlot(new OutputSlot(items, GtBasicMachineBlockEntity.SLOT_OUTPUT_1, 107, 25, inventory.player,
                false));
        addMachineSlot(new OutputSlot(items, GtBasicMachineBlockEntity.SLOT_OUTPUT_2, 125, 25, inventory.player,
                false));
        addPlayerInventory(inventory);
    }

    public int getProgressBar() {
        return Math.min(20, data.getInt(PROGRESS));
    }

    public boolean isOutputEnabled() {
        return data.getInt(OUTPUT) != 0;
    }

    public boolean isItemTransfer() {
        return data.getInt(ITEM_TRANSFER) != 0;
    }

    public boolean isSeparatedInputs() {
        return data.getInt(SEPARATED_INPUTS) != 0;
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    public int getCapacity() {
        return data.getInt(CAPACITY);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (machine == null) {
            return false;
        }
        switch (id) {
            case BUTTON_OUTPUT -> machine.toggleOutput();
            case BUTTON_ITEM_TRANSFER -> machine.toggleItemTransfer();
            case BUTTON_SEPARATED_INPUTS -> machine.toggleSeparatedInputs();
            default -> {
                return false;
            }
        }
        return true;
    }

    /** {@code GT_Container.transferStackInSlot}: machine → player inventory (from the back), player → the two inputs. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
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
