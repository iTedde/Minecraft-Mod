/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CannabisPlantBlock extends CropBlock {
    public static final MapCodec<CannabisPlantBlock> CODEC = simpleCodec(CannabisPlantBlock::new);
    public static final int MAX_AGE = 15;
    public static final int MAX_AGE_WHILE_COVERED = 11;

    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public static final BooleanProperty GROWING = BooleanProperty.create("growing");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    public CannabisPlantBlock(Properties settings) {
        super(settings.randomTicks().noOcclusion());
        registerDefaultState(defaultBlockState().setValue(NATURAL, false));
    }

    @Override
    public MapCodec<? extends CannabisPlantBlock> codec() {
        return CODEC;
    }

    public BlockState getStateForHeight(int y) {
        return defaultBlockState();
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return BlockStateProperties.AGE_15;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getAgeProperty(), GROWING, NATURAL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @Deprecated
    public final int getMaxAge() {
        return getMaxAge(defaultBlockState());
    }

    public int getMaxAge(BlockState state) {
        return MAX_AGE;
    }

    public int getMaxHeight() {
        return 3;
    }

    protected float getRandomGrowthChance() {
        return 0.12F;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return asItem();
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return floor.is(this) || super.mayPlaceOn(floor, world, pos);
    }

    @Override
    protected final boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(NATURAL)) {
            BlockState floor = world.getBlockState(pos.below());
            return floor.is(this) || floor.is(Blocks.GRASS_BLOCK) || floor.is(BlockTags.DIRT);
        }
        return super.canSurvive(state, world, pos);
    }

    @Override
    public boolean isMaxAge(BlockState state) {
        return state.getValue(getAgeProperty()) >= getMaxAge(state) && !state.getValue(GROWING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getRawBrightness(pos.above(), 0) >= 9 && random.nextFloat() < getRandomGrowthChance()) {
            if (isValidBonemealTarget(world, pos, state)) {
                applyGrowth(world, pos, state, false);
            }
        }
    }

    protected int getMaxBonemealGrowth() {
        return 4;
    }

    public void applyGrowth(Level world, final BlockPos initialPos, BlockState state, boolean bonemeal) {
        int number = bonemeal ? world.random.nextInt(getMaxBonemealGrowth()) + 1 : 1;

        BlockPos.MutableBlockPos pos = initialPos.mutable();
        BlockPos.MutableBlockPos up = initialPos.above().mutable();

        while (number > 0) {
            int age = state.getValue(getAgeProperty());
            int maxAge = getMaxAge(state);
            if (age < maxAge) {
                int increment = Math.min(number, maxAge - age);
                number -= increment;
                state = state.setValue(getAgeProperty(), age + increment).setValue(GROWING, world.isEmptyBlock(up) && getPlantSize(world, pos) < getMaxHeight());
                world.setBlock(pos, state, Block.UPDATE_ALL);
            } else {
                int y = getPlantSize(world, pos);
                if (y >= getMaxHeight()) {
                    break;
                }

                BlockState above = world.getBlockState(up);
                if (above.is(this)) {
                    pos.move(Direction.UP);
                    up.move(Direction.UP);
                    state = above;
                    continue;
                }

                number--;
                if (canGrowUpwards(world, pos, state)) {
                    pos.move(Direction.UP);
                    up.move(Direction.UP);
                    state = getStateForHeight(y + 1).setValue(GROWING, world.isEmptyBlock(up) && getPlantSize(world, pos) < getMaxHeight());
                    world.setBlock(pos, state, Block.UPDATE_ALL);
                }
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        if (!isMaxAge(state)) {
            return true;
        }
        pos = pos.above();
        state = world.getBlockState(pos);
        return state.is(this) && isValidBonemealTarget(world, pos, state);
    }

    protected int getPlantSize(LevelReader world, BlockPos pos) {
        int plantSize = 1;
        while (world.getBlockState(pos.below(plantSize)).is(this)) {
            ++plantSize;
        }
        return plantSize;
    }

    protected boolean canGrowUpwards(LevelReader world, BlockPos pos, BlockState state) {
        return world.isEmptyBlock(pos.above())
                && getPlantSize(world, pos) < getMaxHeight()
                && state.getValue(getAgeProperty()) > Math.min(MAX_AGE_WHILE_COVERED, getMaxAge(state) - 1);
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        applyGrowth(world, pos, state, true);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            return state.setValue(GROWING, neighborState.isAir() && getPlantSize(world, pos) < getMaxHeight());
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }
}
