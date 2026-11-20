/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlock extends FluidMachineBlock<DistilleryBlockEntity> {
    public static final MapCodec<DistilleryBlock> CODEC = simpleCodec(DistilleryBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(5, 0, 5, 11, 6, 11),
        Block.box(4, 0, 6, 12, 5, 10),
        Block.box(6, 0, 4, 10, 5, 12),
        Block.box(6, 6, 6, 10, 14, 10),
        Block.box(5, 9, 6, 11, 13, 10),
        Block.box(6, 9, 5, 10, 13, 11)
    );
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public DistilleryBlock(Properties settings) {
        super(settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends DistilleryBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<DistilleryBlockEntity> getBlockEntityType() {
        return PSBlockEntities.DISTILLERY;
    }

    @Override
    protected MenuType<FluidContraptionScreenHandler<DistilleryBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.DISTILLERY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, Direction.Plane.HORIZONTAL.stream().filter(direction -> {
            return canConnectTo(ctx.getLevel().getBlockState(ctx.getClickedPos().relative(direction)), direction);
        }).findAny().orElse(Direction.UP));
    }

    public static boolean canConnectTo(BlockState state, Direction direction) {
        return state.is(PSTags.Blocks.BARRELS)
            || state.is(PSBlocks.MASH_TUB_EDGE)
            || state.is(PSBlocks.FLASK)
            || (state.is(PSBlocks.DISTILLERY) && state.getValue(FACING) != direction.getOpposite());
    }

    @Deprecated
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(FACING).getAxis() == Axis.Y || state.getValue(FACING) == direction) {
            return state.setValue(FACING, Direction.Plane.HORIZONTAL.stream().filter(d -> {
                return canConnectTo(world.getBlockState(pos.relative(d)), d);
            }).findAny().orElse(Direction.UP));
        }

        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        // only output at the back
        if (direction != state.getValue(FACING)) {
            return 0;
        }
        // signal 0-15 to indicate progress
        return super.getSignal(state, world, pos, direction);
    }
}
