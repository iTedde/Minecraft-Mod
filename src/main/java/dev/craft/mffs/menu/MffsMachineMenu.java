package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Common part of the MFFS containers (spec 48 §13): machine slots with the machine's rules ({@code SlotHelper}),
 * the player inventory at the original position and the synced numbers.
 */
public abstract class MffsMachineMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final MffsMachineBlockEntity machine;
    protected final SyncedInts data;
    private int machineSlots;

    protected MffsMachineMenu(MenuType<?> type, int id, BlockPos pos, MffsMachineBlockEntity machine,
                              SyncedInts data) {
        super(type, id);
        this.pos = pos;
        this.machine = machine;
        this.data = data;
        addDataSlots(data);
    }

    /** {@code rules}: the block entity whose slot rules apply (on the client the client-side one, may be null). */
    protected void addMachineSlot(IItemHandler items, int index, int x, int y, MffsMachineBlockEntity rules) {
        addSlot(new MachineSlot(items, index, x, y, rules));
        machineSlots = slots.size();
    }

    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y + 58));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    /** The client-side block entity (name, switch mode, active) or the server one. */
    public MffsMachineBlockEntity getMachine(Player player) {
        return machine != null ? machine
                : player.level().getBlockEntity(pos) instanceof MffsMachineBlockEntity found ? found : null;
    }

    public int getInt(int index) {
        return data.getInt(index);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        int hotbar = machineSlots + 27;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            if (index < hotbar) {
                if (!moveItemStackTo(stack, hotbar, slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, machineSlots, hotbar, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return machine == null || machine.stillValid(player);
    }

    private static final class MachineSlot extends SlotItemHandler {
        private final MffsMachineBlockEntity machine;

        MachineSlot(IItemHandler handler, int index, int x, int y, MffsMachineBlockEntity machine) {
            super(handler, index, x, y);
            this.machine = machine;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return machine == null || machine.isItemValid(getSlotIndex(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return machine != null ? machine.getSlotStackLimit(getSlotIndex()) : 64;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return Math.min(getMaxStackSize(), stack.getMaxStackSize());
        }
    }
}
