/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Map;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BarrelBlock extends FluidMachineBlock<BarrelBlockEntity> {
    public static final MapCodec<BarrelBlock> CODEC = simpleCodec(BarrelBlock::new);
    public static final int MAX_TAP_AMOUNT = FluidVolumes.BUCKET;
    public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
    public static final BooleanProperty TAPPED = BooleanProperty.create("tapped");

    private static final Map<Axis, VoxelShape> STANDING_SHAPES = Map.of(
        Axis.X, Shapes.or(
            Block.box(0, 5, 2, 16, 13, 14),
            Block.box(0, 3, 4, 16, 15, 12)
        ),
        Axis.Y, Shapes.or(
            Block.box(2, 0, 4, 14, 16, 12),
            Block.box(4, 0, 2, 12, 16, 14)
        ),
        Axis.Z, Shapes.or(
            Block.box(2, 5, 0, 14, 13, 16),
            Block.box(4, 3, 0, 12, 15, 16)
        )
    );

    public BarrelBlock(Properties settings) {
        super(settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(TAPPED, true));
    }

    @Override
    protected MapCodec<? extends BarrelBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, TAPPED);
    }

    @Override
    protected BlockEntityType<BarrelBlockEntity> getBlockEntityType() {
        return PSBlockEntities.BARREL;
    }

    @Override
    protected MenuType<FluidContraptionScreenHandler<BarrelBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.BARREL;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return STANDING_SHAPES.get(state.getValue(FACING).getAxis());
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
        Direction side = ctx.getNearestLookingDirection().getOpposite();
        return defaultBlockState().setValue(FACING, side.getAxis() == Axis.Y ? Direction.DOWN : side);
    }

    @Override
    protected ItemInteractionResult onInteractWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BarrelBlockEntity blockEntity) {

        if (stack.is(Items.STICK)) {
            world.playSound(player, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS);
            world.gameEvent(player, GameEvent.BLOCK_OPEN, pos);
            world.setBlockAndUpdate(pos, state.setValue(FACING, state.getValue(FACING).getAxis() == Axis.Y ? player.getDirection().getOpposite() : Direction.DOWN));
            return ItemInteractionResult.SUCCESS;
        }

        int capacity = FluidCapacity.get(stack);
        if (!state.getValue(TAPPED) || state.getValue(FACING).getAxis() == Axis.Y || capacity == 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (ItemFluids.of(stack).amount() < capacity) {
            Resovoir tank = blockEntity.getTankOnSide(Direction.DOWN);
            if (tank.getContents().amount() > 0 && stack.is(tank.getContents().fluid().getPreferredContainerTag())) {
                if (!world.isClientSide) {

                    ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copyWithCount(1));

                    if (tank.withdraw(t, MAX_TAP_AMOUNT) > 0) {
                        if (stack.getCount() > 1) {
                            stack.shrink(1);
                            player.getInventory().placeItemBackInInventory(t.toItemStack());
                        } else {
                            player.setItemInHand(hand, t.toItemStack());
                        }
                    }

                    blockEntity.setTapOpenTicks(20);
                    blockEntity.markForUpdate();

                    Direction updateDirection = state.getValue(BarrelBlock.FACING).getOpposite();
                    if (updateDirection.getAxis() != Axis.Y) {
                        world.updateNeighborsAtExceptFromFacing(pos.relative(updateDirection), this, updateDirection.getOpposite());
                    }
                }

                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.FAIL;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(FACING).getAxis() != Axis.Y;
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

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        // only output at the back
        if (direction != state.getValue(FACING)) {
            return 0;
        }
        // signal 15 = Someone is using it
        if (world.getBlockEntity(pos, getBlockEntityType()).map(BarrelBlockEntity::getTapOpenTicks).orElse(0) > 0) {
            return 15;
        }
        return 0;
    }
}
