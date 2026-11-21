package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.client.render.blocks.VoxelShapeUtil;
import ivorius.psychedelicraft.mixin.MixinAbstractBlockSettings;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created by lukas on 16.11.14.
 */
public class BottleRackBlock extends BaseEntityBlock {
    public static final MapCodec<BottleRackBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("z_offset").forGetter(BottleRackBlock::getZOffset),
                    propertiesCodec()
            ).apply(instance, BottleRackBlock::new));
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(0, 0, 3, 1, 16, 11),
            Block.box(5, 0, 3, 6, 16, 11),
            Block.box(10, 0, 3, 11, 16, 11),
            Block.box(15, 0, 3, 16, 16, 11),

            Block.box(1, 0, 2.75F, 15, 1, 12.35F),
            Block.box(1, 5, 2.75F, 15, 6, 12.35F),
            Block.box(1, 10, 2.75F, 15, 11, 12.35F),
            Block.box(1, 15, 2.75F, 15, 16, 12.35F)
    );

    private final int zOffset;
    private final Function<Direction, VoxelShape> shapes;

    public BottleRackBlock(int zOffset, Properties settings) {
        this(zOffset, settings,
                Util.memoize(direction -> VoxelShapeUtil.rotate(new Vec3(0, 0, -zOffset / 16D), direction)),
                Util.memoize(VoxelShapeUtil.rotator(BASE_SHAPE.move(0, 0, zOffset / 16D)))
        );
    }

    public BottleRackBlock(int zOffset, Properties settings,
            Function<Direction, Vec3> offsets,
            Function<Direction, VoxelShape> shapes) {
        super(Util.make(settings, s -> {
            if (zOffset != 0) {
                ((MixinAbstractBlockSettings)s).setOffsetter((state, world, pos) -> {
                    offsets.apply(state.getValue(FACING));
                    return VoxelShapeUtil.rotate(new Vec3(0, 0, -3 / 16D), state.getValue(FACING));
                });
            }
        }).noOcclusion().dynamicShape());
        this.shapes = shapes;
        this.zOffset = zOffset;
    }

    @Override
    protected MapCodec<? extends BottleRackBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public int getZOffset() {
        return zOffset;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    @Deprecated
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapes.apply(state.getValue(FACING));
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
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, PSBlockEntities.BOTTLE_RACK).map(be -> {
            if (heldStack.isEmpty()) {
                InteractionResultHolder<ItemStack> extracted = be.extractItem(hit, state.getValue(FACING));
                if (extracted.getResult().consumesAction()) {
                    player.setItemInHand(hand, extracted.getObject());
                }
                return extracted.getResult() == InteractionResult.SUCCESS ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            return be.insertItem(heldStack, hit, state.getValue(FACING));
        }).orElse(ItemInteractionResult.FAIL);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !world.isClientSide) {
            world.getBlockEntity(pos, PSBlockEntities.BOTTLE_RACK).ifPresent(be -> {
                Containers.dropContents(world, pos, be);
                world.updateNeighbourForOutputSignal(pos, this);
            });
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BottleRackBlockEntity(pos, state);
    }
}
