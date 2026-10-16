package dev.craft.powerplants.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Casing block of a power plant multiblock; placing or removing it makes nearby controllers re-check at once. */
public class PlantPartBlock extends BaseEntityBlock {
    /** Largest distance between a part and its controller (3×3×5 turbine). */
    public static final int NOTIFY_RANGE = 4;

    public PlantPartBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlantPartBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            notifyControllers(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof PlantPartBlockEntity part) {
            PlantControllerBlockEntity controller = part.getController();
            if (controller != null) {
                controller.markStructureDirty();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Makes every controller within {@link #NOTIFY_RANGE} re-check its structure on its next tick. */
    public static void notifyControllers(Level level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-NOTIFY_RANGE, -NOTIFY_RANGE, -NOTIFY_RANGE),
                pos.offset(NOTIFY_RANGE, NOTIFY_RANGE, NOTIFY_RANGE))) {
            if (level.isLoaded(near) && level.getBlockEntity(near) instanceof PlantControllerBlockEntity controller) {
                controller.markStructureDirty();
            }
        }
    }
}
