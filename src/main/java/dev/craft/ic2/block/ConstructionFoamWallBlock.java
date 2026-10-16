package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;

/**
 * Hardened construction foam (IC2 {@code BlockWall}): the IC2 dye metadata is the {@link #COLOR} property, the painter
 * recolors it. Drops nothing; pick block gives construction foam.
 */
public class ConstructionFoamWallBlock extends Block implements PaintableBlock {
    public static final MapCodec<ConstructionFoamWallBlock> CODEC = simpleCodec(ConstructionFoamWallBlock::new);
    /** IC2 dye metadata 0 (black) – 15 (white). */
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 15);

    public ConstructionFoamWallBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COLOR, ConstructionFoamBlock.HARDENED_COLOR));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    public BlockState withColor(int color) {
        return defaultBlockState().setValue(COLOR, color);
    }

    @Override
    public boolean colorBlock(Level level, BlockPos pos, int color) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(COLOR) == color) {
            return false;
        }
        level.setBlock(pos, state.setValue(COLOR, color), Block.UPDATE_ALL);
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
                                       Player player) {
        return new ItemStack(Ic2Items.CONSTRUCTION_FOAM.get());
    }
}
