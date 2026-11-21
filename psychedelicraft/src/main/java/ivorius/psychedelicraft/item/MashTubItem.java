/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Updated by Sollace on 8 Feb 2023
 */
public class MashTubItem extends FlaskItem {
    public MashTubItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return findPlacementPosition(context.getLevel(), context.getClickedPos()).map(position -> {
            return new BlockPlaceContext(
                    context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new BlockHitResult(position.getCenter(), Direction.UP, position, false)
            );
        }).orElse(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (super.placeBlock(context, state)) {
            BlockPos center = context.getClickedPos();
            BlockPos.withinManhattan(center, 1, 0, 1).forEach(p -> {
                if (!p.equals(center)) {
                    context.getLevel().setBlock(p, PSBlocks.MASH_TUB_EDGE.defaultBlockState(), Block.UPDATE_ALL);
                    context.getLevel().getBlockEntity(p, PSBlockEntities.MASH_TUB_EDGE).ifPresent(be -> be.setMasterPos(center));
                }
            });
            return true;
        }
        return false;
    }

    public static Optional<BlockPos> findPlacementPosition(LevelReader world, BlockPos pos) {
        return BlockPos.withinManhattanStream(pos, 1, 0, 1)
                .filter(center -> BlockPos.withinManhattanStream(center, 1, 0, 1).allMatch(p -> {
                    BlockState s = world.getBlockState(p);
                    return world.isEmptyBlock(p) || s.canBeReplaced();
                }))
                .findFirst()
                .map(p -> p.immutable());
    }
}
