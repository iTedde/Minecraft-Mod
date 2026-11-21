/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DryingTableBlock extends BaseEntityBlock {
    public static final MapCodec<DryingTableBlock> CODEC = simpleCodec(DryingTableBlock::new);
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public DryingTableBlock(Properties settings) {
        super(settings.noOcclusion());
    }

    @Override
    protected MapCodec<? extends DryingTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.DRYING_TABLE).map(be -> {
            player.openMenu(be);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }).orElse(InteractionResult.FAIL);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, PSBlockEntities.DRYING_TABLE, (w, p, s, entity) -> entity.tick((ServerLevel)w));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingTableBlockEntity(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.DRYING_TABLE).map(be -> {
            return (int)(be.getHeatRatio() * 15);
        }).orElse(0);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return world.getBlockEntity(pos, PSBlockEntities.DRYING_TABLE).map(be -> {
            return (int)(be.getDryingProgress() * 15);
        }).orElse(0);
    }
}
