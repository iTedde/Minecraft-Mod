package dev.craft.ic2.personal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code StackUtil} inventory helpers as used by the O-Mats, on top of item handlers. */
public final class PersonalInventories {
    /** IC2 {@code Direction.values()}: −X, +X, −Y, +Y, −Z, +Z. */
    private static final Direction[] DIRECTIONS = {Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP,
            Direction.NORTH, Direction.SOUTH};

    private PersonalInventories() {
    }

    /**
     * Adjacent inventories in IC2 order; a personal safe counts only if it has the same owner (IC2 reads its
     * {@code IInventory} directly, the safe has no automation otherwise).
     */
    public static List<IItemHandler> adjacent(Level level, BlockPos pos, PersonalBlockEntity owner) {
        List<IItemHandler> inventories = new ArrayList<>();
        for (Direction direction : DIRECTIONS) {
            BlockPos target = pos.relative(direction);
            if (level.getBlockEntity(target) instanceof PersonalSafeBlockEntity safe) {
                if (safe.sameOwner(owner)) {
                    inventories.add(safe.getInventory());
                }
                continue;
            }
            IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, target, null);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        return inventories;
    }

    /** IC2 {@code isStackEqual}: same item; metadata (here: components) is not compared. */
    public static boolean isStackEqual(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItem(a, b);
    }

    /**
     * IC2 {@code getFromInventory}: takes {@code count} matching items; returns them, or {@code null} when there were
     * not enough (the taken items are lost then, as in the original).
     */
    @Nullable
    public static ItemStack getFromInventory(IItemHandler inventory, ItemStack pattern, int count) {
        ItemStack taken = ItemStack.EMPTY;
        int toTransfer = count;
        for (int i = 0; i < inventory.getSlots() && toTransfer > 0; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isStackEqual(stack, pattern)) {
                ItemStack part = inventory.extractItem(i, Math.min(toTransfer, stack.getCount()), false);
                if (taken.isEmpty()) {
                    taken = part.copy();
                } else {
                    taken.grow(part.getCount());
                }
                toTransfer -= part.getCount();
            }
        }
        return toTransfer == 0 && !taken.isEmpty() ? taken : null;
    }

    /** IC2 {@code putInInventory}: equal stacks first, then empty slots; true when everything fitted. */
    public static boolean putInInventory(IItemHandler inventory, ItemStack source) {
        ItemStack rest = source.copy();
        for (int i = 0; i < inventory.getSlots() && !rest.isEmpty(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, rest)) {
                rest = inventory.insertItem(i, rest, false);
            }
        }
        for (int i = 0; i < inventory.getSlots() && !rest.isEmpty(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                rest = inventory.insertItem(i, rest, false);
            }
        }
        source.setCount(rest.getCount());
        return rest.isEmpty();
    }
}
