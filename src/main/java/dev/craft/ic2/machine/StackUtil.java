package dev.craft.ic2.machine;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/** IC2 {@code StackUtil.distributeDrop}: hands items to adjacent inventories, drops the rest (spec 04, pump). */
public final class StackUtil {
    /** IC2 {@code Direction.values()}: −X, +X, −Y, +Y, −Z, +Z. */
    private static final Direction[] DIRECTIONS = {Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP,
            Direction.NORTH, Direction.SOUTH};

    private StackUtil() {
    }

    /**
     * Puts the stacks into neighbouring inventories with at least 6 slots (seen from that side; NeoForge's chest
     * handler already joins double chests) and drops what is left at {@code pos}. Empties {@code stacks}.
     */
    public static void distributeDrop(Level level, BlockPos pos, List<ItemStack> stacks) {
        for (Direction direction : DIRECTIONS) {
            if (stacks.isEmpty()) {
                break;
            }
            IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(direction),
                    direction.getOpposite());
            if (inventory == null || inventory.getSlots() < 6) {
                continue;
            }
            Iterator<ItemStack> it = stacks.iterator();
            while (it.hasNext()) {
                ItemStack stack = it.next();
                ItemStack rest = putInInventory(inventory, stack);
                if (rest.isEmpty()) {
                    it.remove();
                } else {
                    stack.setCount(rest.getCount());
                }
            }
        }
        for (ItemStack stack : stacks) {
            dropAsEntity(level, pos, stack);
        }
        stacks.clear();
    }

    /** IC2 {@code putInInventory}: first tops up equal stacks, then fills empty slots. Returns the rest. */
    private static ItemStack putInInventory(IItemHandler inventory, ItemStack source) {
        ItemStack rest = source.copy();
        for (int i = 0; i < inventory.getSlots() && !rest.isEmpty(); i++) {
            ItemStack current = inventory.getStackInSlot(i);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, rest)) {
                rest = inventory.insertItem(i, rest, false);
            }
        }
        for (int i = 0; i < inventory.getSlots() && !rest.isEmpty(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                rest = inventory.insertItem(i, rest, false);
            }
        }
        return rest;
    }

    /**
     * IC2 {@code dropAsEntity}: random offset 0.15–0.85, pickup delay 10. Oversized stacks (the pump turns whole
     * bucket stacks into filled buckets) are split into full stacks, which 1.21 requires.
     */
    public static void dropAsEntity(Level level, BlockPos pos, ItemStack stack) {
        int count = stack.getCount();
        while (count > 0) {
            int part = Math.min(count, stack.getMaxStackSize());
            dropSingle(level, pos, stack.copyWithCount(part));
            count -= part;
        }
    }

    private static void dropSingle(Level level, BlockPos pos, ItemStack stack) {
        double f = 0.7;
        double dx = level.random.nextFloat() * f + (1.0 - f) * 0.5;
        double dy = level.random.nextFloat() * f + (1.0 - f) * 0.5;
        double dz = level.random.nextFloat() * f + (1.0 - f) * 0.5;
        ItemEntity entity = new ItemEntity(level, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, stack);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
