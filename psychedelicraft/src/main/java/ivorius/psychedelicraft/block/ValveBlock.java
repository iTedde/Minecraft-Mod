/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.PSSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ValveBlock extends GlassTubeBlock {
    public static final MapCodec<GlassTubeBlock> CODEC = simpleCodec(ValveBlock::new);

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    protected ValveBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(OPEN, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Override
    protected boolean isBlocked(BlockState state) {
        return !state.getValue(OPEN);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        world.setBlockAndUpdate(pos, state.cycle(OPEN));
        world.playLocalSound(pos, state.getValue(OPEN) ? PSSounds.BLOCK_VALVE_CLOSE : PSSounds.BLOCK_VALVE_OPEN, SoundSource.BLOCKS, 1.5F, state.getValue(OPEN) ? 1 : 5, true);
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
