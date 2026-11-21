/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Created by lukas on 06.03.14.
 * Updated by Sollace on 3 Jan 2023
 */
class GlitchedBlock extends Block {
    public static final MapCodec<GlitchedBlock> CODEC = simpleCodec(GlitchedBlock::new);

    public GlitchedBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends GlitchedBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            world.removeBlock(pos, true);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (!world.isClientSide) {
            world.removeBlock(pos, true);
        }
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(world, state, pos, entity, fallDistance);
        if (!world.isClientSide) {
            world.removeBlock(pos, true);
        }
    }
}
