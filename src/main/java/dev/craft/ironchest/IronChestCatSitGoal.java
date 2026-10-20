package dev.craft.ironchest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;

/**
 * Iron Chests {@code IronChestAIOcelotSit} (spec 22 §5): tamed cats also sit on the seven chests, whether open or not.
 * In 1.21 cats take the role of the tamed ocelots.
 */
public class IronChestCatSitGoal extends CatSitOnBlockGoal {
    public IronChestCatSitGoal(Cat cat, double speedModifier) {
        super(cat, speedModifier);
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (level.isEmptyBlock(pos.above()) && level.getBlockState(pos).getBlock() instanceof IronChestBlock) {
            return true;
        }
        return super.isValidTarget(level, pos);
    }
}
