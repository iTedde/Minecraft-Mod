package dev.craft.buildcraft.util;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.engine.EngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Output of mined items by the quarry and the mining well ({@code TileQuarry.mineStack}, {@code TileMiningWell}). */
public final class ItemOutput {
    /** {@code BuildCraftCore.itemLifespan}. */
    public static final int ITEM_LIFESPAN = 1200;

    private ItemOutput() {
    }

    /** Random adjacent inventory first (pipes come later), then the rest flies out above the machine. */
    public static void output(Level level, BlockPos pos, ItemStack stack) {
        ItemStack rest = addToRandomInventory(level, pos, stack);
        if (!rest.isEmpty()) {
            float f = level.random.nextFloat() * 0.8F + 0.1F;
            float f1 = level.random.nextFloat() * 0.8F + 0.1F;
            float f2 = level.random.nextFloat() * 0.8F + 0.1F;
            ItemEntity entity = new ItemEntity(level, pos.getX() + f, pos.getY() + f1 + 0.5F, pos.getZ() + f2, rest);
            entity.lifespan = ITEM_LIFESPAN;
            entity.setPickUpDelay(10);
            float f3 = 0.05F;
            entity.setDeltaMovement((float) level.random.nextGaussian() * f3,
                    (float) level.random.nextGaussian() * f3 + 1.0F, (float) level.random.nextGaussian() * f3);
            level.addFreshEntity(entity);
        }
    }

    /** BuildCraft {@code Utils.addToRandomInventory}: one random neighbour (no engine) that takes something. */
    private static ItemStack addToRandomInventory(Level level, BlockPos pos, ItemStack stack) {
        List<IItemHandler> possible = new ArrayList<>();
        for (Direction side : Direction.values()) {
            BlockPos adjacent = pos.relative(side);
            if (level.getBlockEntity(adjacent) instanceof EngineBlockEntity) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, adjacent, side.getOpposite());
            if (handler != null
                    && ItemHandlerHelper.insertItemStacked(handler, stack.copy(), true).getCount() < stack.getCount()) {
                possible.add(handler);
            }
        }
        if (possible.isEmpty()) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(possible.get(level.random.nextInt(possible.size())), stack, false);
    }
}
