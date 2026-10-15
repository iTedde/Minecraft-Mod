package dev.craft.ic2.menu;

import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Container of an IC2 machine (IC2 {@code ContainerIC2}). Machine slots come first, then the player inventory.
 * Shift-click logic follows IC2: machine to player; player to the input slot first, then to slots that already
 * hold items, then to empty valid slots. Server values reach the client through {@link SyncedInts}.
 */
public abstract class MachineMenu<T extends MachineBlockEntity> extends AbstractContainerMenu {
    /** Server: the machine; client: {@code null}. */
    @Nullable
    protected final T machine;
    protected final BlockPos pos;
    protected final SyncedInts data;
    private int machineSlotCount;

    protected MachineMenu(@Nullable MenuType<?> type, int id, @Nullable T machine, BlockPos pos, SyncedInts data) {
        super(type, id);
        this.machine = machine;
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
    }

    /** Item storage the slots use: the machine's inventory, or a dummy of the same size on the client. */
    protected static IItemHandler items(@Nullable MachineBlockEntity machine, int size) {
        return machine != null ? machine.getInventory() : new ItemStackHandler(size);
    }

    protected static BlockPos readPos(RegistryFriendlyByteBuf buf) {
        return buf.readBlockPos();
    }

    @Nullable
    public T getMachine() {
        return machine;
    }

    public BlockPos getPos() {
        return pos;
    }

    /** Adds a machine slot; all machine slots must be added before the player inventory. */
    protected Slot addMachineSlot(Slot slot) {
        machineSlotCount++;
        return addSlot(slot);
    }

    /** Adds the player inventory at IC2's usual position (inventory at y 84, hotbar at y 142). */
    protected void addPlayerInventory(Inventory inventory) {
        addPlayerInventory(inventory, 84);
    }

    protected void addPlayerInventory(Inventory inventory, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, y + 58));
        }
    }

    /** IC2 {@code getInput}: slot index preferred for shift-clicked items, or -1. */
    protected int getInputSlot() {
        return -1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < machineSlotCount) {
            moveItemStackTo(stack, machineSlotCount, slots.size(), false);
            source.onQuickCraft(stack, original);
        } else {
            for (int run = 0; run < 3 && !stack.isEmpty(); run++) {
                if (run == 0) {
                    int input = getInputSlot();
                    if (input != -1 && slots.get(input).mayPlace(stack)) {
                        moveItemStackTo(stack, input, input + 1, false);
                    }
                } else {
                    for (int target = 0; target < machineSlotCount; target++) {
                        Slot slot = slots.get(target);
                        if (slot.mayPlace(stack) && (slot.hasItem() || run == 2)) {
                            moveItemStackTo(stack, target, target + 1, false);
                            if (stack.isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        source.onTake(player, stack);
        // IC2 returns null, which stops vanilla from repeating the shift click.
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return machine == null || machine.stillValid(player);
    }
}
