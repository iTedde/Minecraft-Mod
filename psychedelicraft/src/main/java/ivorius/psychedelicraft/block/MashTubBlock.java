/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.advancement.PSCriteria;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.MashTubItem;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import ivorius.psychedelicraft.screen.PSScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Created by lukas on 27.10.14.
 * Updated by Sollace on 12 Jan 2023
 */
public class MashTubBlock extends FluidMachineBlock<MashTubBlockEntity> implements FluidFilled {
    public static final MapCodec<MashTubBlock> CODEC = simpleCodec(MashTubBlock::new);
    public static final int SIZE = 15;
    public static final int BORDER_SIZE = 1;
    public static final int HEIGHT = 16;

    public static final IntegerProperty LIGHT = BlockStateProperties.LEVEL;

    static final VoxelShape COLLISSION_SHAPE = Shapes.or(
            createShape(-8, 0, -8, 32, 16,  1),
            createShape(-8, 0, 23, 32, 16,  1),
            createShape(23, 0, -8,  1, 16, 32),
            createShape(-8, 0, -8,  1, 16, 32),
            createShape(-8, 0, -8, 32,  1, 32)
    );

    static final VoxelShape RAYCAST_SHAPE = createShape(-8, 0, -8, 32, 16, 32);

    static final VoxelShape CORNER_RAYCAST_SHAPE = box(0, 0, 0, 8, 16, 8);
    static final VoxelShape[] RAYCAST_SHAPES = {
            CORNER_RAYCAST_SHAPE.move(0.5F, 0, 0.5F), box(0, 0, 8, 16, 16, 16), CORNER_RAYCAST_SHAPE.move(0, 0, 0.5F),
            box(8, 0, 0, 16, 16, 16), Shapes.block(), box(0, 0, 0, 16, 16, 8),
            CORNER_RAYCAST_SHAPE.move(0.5F, 0, 0), Shapes.block(), CORNER_RAYCAST_SHAPE
    };
    static VoxelShape getShape(int x, int z) {
        x = Mth.clamp(x, 0, 3);
        z = Mth.clamp(z, 0, 3);
        return new VoxelShape[] {
            CORNER_RAYCAST_SHAPE.move(0, 0, 0), box(0, 0, 0, 16, 16, 8), CORNER_RAYCAST_SHAPE.move(0.5F, 0, 0),
            box(0, 0, 0, 8, 16, 16), Shapes.block(), box(8, 0, 0, 16, 16, 16),
            CORNER_RAYCAST_SHAPE.move(0, 0, 0.5F), box(0, 0, 8, 16, 16, 16), CORNER_RAYCAST_SHAPE.move(0.5F, 0, 0.5F)
        }[x + z * 3];
    }

    private static VoxelShape createShape(double x, double y, double z, double width, double height, double depth) {
        return Block.box(x, y, z, x + width, y + height, z + depth);
    }

    public MashTubBlock(Properties settings) {
        super(settings.lightLevel(LightBlock.LIGHT_EMISSION).dynamicShape());
    }

    @Override
    protected MapCodec<? extends MashTubBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 0.2F;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return COLLISSION_SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return RAYCAST_SHAPE;
    }

    @Override
    protected BlockEntityType<MashTubBlockEntity> getBlockEntityType() {
        return PSBlockEntities.MASH_TUB;
    }

    @Override
    protected MenuType<FluidContraptionScreenHandler<MashTubBlockEntity>> getScreenHandlerType() {
        return PSScreenHandlers.MASH_TUB;
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        world.getBlockEntity(getBlockEntityPosition(world, pos), getBlockEntityType()).ifPresent(be -> {
            ItemFluids fluid = be.getPrimaryTank().getContents();
            fluid.fluid().randomDisplayTick(world, pos, fluid.fluid().getFluidState(fluid), random);
        });
    }

    @Override
    protected ItemInteractionResult onInteractWithItem(ItemStack heldStack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, MashTubBlockEntity blockEntity) {
        if (!heldStack.isEmpty()) {
            return Either.unwrap(blockEntity.interactWithItem(heldStack, player).mapLeft(stack -> {
                if (!world.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(heldStack, player, stack));
                }
                return ItemInteractionResult.SUCCESS;
            }));
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult onInteract(BlockState state, Level world, BlockPos pos, Player player, MashTubBlockEntity blockEntity) {
        if (!blockEntity.solidContents.isEmpty()) {
            PSCriteria.SIMPLY_MASHING.trigger(player, blockEntity.solidContents);
            Block.popResource(world, pos, blockEntity.solidContents);
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1, 1);
            blockEntity.solidContents = ItemStack.EMPTY;
            blockEntity.markForUpdate();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return MashTubItem.findPlacementPosition(world, pos).isPresent();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
    }

    @Override
    public double getFluidHeight(Level world, BlockState state, BlockPos pos) {
        var h = world.getBlockEntity(pos, getBlockEntityType())
                .map(be -> be.getPrimaryTank())
                .map(tank -> (double)tank.getContents().amount() / tank.getCapacity())
                .orElse(-1D);

        return h < 0 ? -1 : Mth.clamp(0.3F + h, 0, 1);
    }

    @Override
    public Optional<FluidState> getContainedFluid(Level world, BlockState state, BlockPos pos) {
        return world.getBlockEntity(pos, getBlockEntityType())
                .map(be -> be.getPrimaryTank())
                .map(tank -> tank.getContents().fluid().getFluidState(tank.getContents()));
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        if (player == null) {
            return false;
        }
        return world.getBlockEntity(pos, getBlockEntityType()).filter(be -> {
            Resovoir tank = be.getPrimaryTank();
            return (tank.getContents().isEmpty()
                || tank.getContents().fluid().getPhysical().isOf(fluid))
                && tank.getCapacity() - tank.getContents().amount() >=  FluidVolumes.BUCKET;
        }).isPresent();
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !world.isClientSide) {
            BlockPos.withinManhattan(pos, 1, 0, 1).forEach(p -> {
                if (!p.equals(pos)) {
                    BlockState neighbourState = world.getBlockState(p);
                    if (neighbourState.is(PSBlocks.MASH_TUB_EDGE)) {
                        world.removeBlockEntity(p);
                        world.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            });
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return world.getBlockEntity(getBlockEntityPosition(world, pos), getBlockEntityType()).filter(be -> {
            SimpleFluid f = SimpleFluid.of(fluidState.getType());

            if (be.getPrimaryTank().deposit(f.getStack(fluidState, FluidVolumes.BUCKET)) > 0) {
                be.markForUpdate();
                return true;
            }
            return false;
        }).isPresent();
    }

    protected BlockPos getBlockEntityPosition(BlockGetter world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(p -> p.getMasterPos()).orElse(pos);
    }
}
