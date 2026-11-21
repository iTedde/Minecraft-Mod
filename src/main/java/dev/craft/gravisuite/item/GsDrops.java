package dev.craft.gravisuite.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** GraviSuite {@code ItemGraviTool.dropAsEntity}: an item at a random spot inside the block, pickup delay 10. */
public final class GsDrops {
    private GsDrops() {
    }

    public static void dropAsEntity(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        RandomSource random = level.random;
        double spread = 0.7;
        double x = random.nextFloat() * spread + (1.0 - spread) * 0.5;
        double y = random.nextFloat() * spread + (1.0 - spread) * 0.5;
        double z = random.nextFloat() * spread + (1.0 - spread) * 0.5;
        ItemEntity item = new ItemEntity(level, pos.getX() + x, pos.getY() + y, pos.getZ() + z, stack.copy());
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }
}
