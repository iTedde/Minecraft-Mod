package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Resin sheet (IC2 {@code BlockResin}, {@code blockHarz}): a 2 px layer placed with sticky resin. No collision; slows
 * down entities inside it and reduces their fall distance. Drops sticky resin with 80 % (loot table).
 */
public class ResinSheetBlock extends Block {
    public static final MapCodec<ResinSheetBlock> CODEC = simpleCodec(ResinSheetBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);

    public ResinSheetBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** IC2: the block below must be an opaque cube of a solid material. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isSolidRender(level, below);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.fallDistance *= 0.75F;
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6F, 0.85F, 0.6F));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Ic2Items.RESIN.get());
    }
}
