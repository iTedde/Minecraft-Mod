/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TobaccoPlantBlock extends CannabisPlantBlock {
    public static final MapCodec<TobaccoPlantBlock> CODEC = simpleCodec(TobaccoPlantBlock::new);
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public TobaccoPlantBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(TOP, false));
    }

    @Override
    public MapCodec<? extends TobaccoPlantBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForHeight(int y) {
        return defaultBlockState().setValue(TOP, y > 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return BlockStateProperties.AGE_7;
    }

    @Override
    public int getMaxHeight() {
        return 2;
    }

    @Override
    public int getMaxAge(BlockState state) {
        return BlockStateProperties.MAX_AGE_7;
    }

    @Override
    protected float getRandomGrowthChance() {
        return 0.1F;
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        return floor.is(Blocks.FARMLAND) || floor.is(this) || floor.is(BlockTags.DIRT) || floor.is(Blocks.GRASS_BLOCK);
    }

    @Override
    protected int getMaxBonemealGrowth() {
        return 2;
    }
}
