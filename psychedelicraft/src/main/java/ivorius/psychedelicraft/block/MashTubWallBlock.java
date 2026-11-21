/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Updated by Sollace on 7 Feb 2023
 */
public class MashTubWallBlock extends BaseEntityBlock implements FluidFilled, PipeInsertable {
    public static final MapCodec<MashTubWallBlock> CODEC = simpleCodec(MashTubWallBlock::new);

    public MashTubWallBlock(Properties settings) {
        super(settings.lightLevel(LightBlock.LIGHT_EMISSION));
    }

    @Override
    protected MapCodec<? extends MashTubWallBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MashTubBlock.LIGHT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getValidMasterPosition(world, pos)
                .map(center -> MashTubBlock.COLLISSION_SHAPE.move(center.getX() - pos.getX(), 0, center.getZ() - pos.getZ()))
                .orElseGet(Shapes::empty);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getValidMasterPosition(world, pos)
                .map(center -> MashTubBlock.getShape(center.getX() - pos.getX() + 1, center.getZ() - pos.getZ() + 1))
                .orElseGet(Shapes::empty);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return getValidMasterPosition(world, pos).map(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock().getCloneItemStack(world, center, masterState);
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        getValidMasterPosition(world, pos).ifPresent(p -> {
            BlockState masterState = world.getBlockState(p);
            masterState.getBlock().animateTick(masterState, world, pos, random);
        });
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return getValidMasterPosition(world, pos).map(p -> {
            return world.getBlockState(p).useWithoutItem(world, player, new BlockHitResult(hit.getLocation(), hit.getDirection(), p, hit.isInside()));
        }).orElse(InteractionResult.PASS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return getValidMasterPosition(world, pos).map(p -> {
            return world.getBlockState(p).useItemOn(stack, world, player, hand, new BlockHitResult(hit.getLocation(), hit.getDirection(), p, hit.isInside()));
        }).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public double getFluidHeight(Level world, BlockState state, BlockPos pos) {
        return getValidMasterPosition(world, pos).map(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFilled vat ? vat.getFluidHeight(world, masterState, center) : -1D;
        }).orElse(-1D);
    }

    @Override
    public Optional<FluidState> getContainedFluid(Level world, BlockState state, BlockPos pos) {
        return getValidMasterPosition(world, pos).flatMap(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof FluidFilled vat ? vat.getContainedFluid(world, masterState, center) : Optional.empty();
        });
    }

    @Override
    public boolean canPlaceLiquid(@Nullable Player player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        if (player == null) {
            return false;
        }
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof LiquidBlockContainer fillable && fillable.canPlaceLiquid(player, world, center, masterState, fluid);
        }).isPresent();
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return getValidMasterPosition(world, pos).filter(center -> {
            BlockState masterState = world.getBlockState(center);
            return masterState.getBlock() instanceof LiquidBlockContainer fillable && fillable.placeLiquid(world, center, masterState, fluidState);
        }).isPresent();
    }

    @Override
    public boolean acceptsConnectionFrom(LevelAccessor world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return true;
    }

    @Override
    public Either<PipeFluids, Unit> tryInsert(ServerLevel world, BlockState state, BlockPos pos, Direction direction, PipeFluids fluids) {
        return getValidMasterPosition(world, pos).map(center -> PipeInsertable.tryInsert(world, center, direction, fluids)).orElse(PipeInsertable.reject(fluids));
    }

    @Override
    public Optional<PipeFluids> tryExtract(ServerLevel world, BlockState state, BlockPos pos, Direction direction) {
        return getValidMasterPosition(world, pos).flatMap(center -> PipeInsertable.tryExtract(world, center, direction));
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !world.isClientSide) {
            getMasterPosition(world, pos).ifPresent(center -> {
                BlockState masterState = world.getBlockState(center);
                if (masterState.is(PSBlocks.MASH_TUB) || masterState.is(this)) {
                    world.destroyBlock(center, true);
                }
            });
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    private Optional<BlockPos> getMasterPosition(BlockGetter world, BlockPos pos) {
        return world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(MasterPosition::getMasterPos).filter(p -> !p.equals(pos));
    }

    private Optional<BlockPos> getValidMasterPosition(BlockGetter world, BlockPos pos) {
        return getMasterPosition(world, pos).filter(p -> world.getBlockState(p).is(PSBlocks.MASH_TUB));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MasterPosition(pos, state);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getValidMasterPosition(world, pos)
                .map(p -> world.getBlockState(p).getSignal(world, p, direction))
                .orElse(0);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return getValidMasterPosition(world, pos)
                .map(p -> world.getBlockState(p).getAnalogOutputSignal(world, p))
                .orElse(0);
    }

    public static class MasterPosition extends SyncedBlockEntity {

        private BlockPos masterPos;

        public MasterPosition(BlockPos pos, BlockState state) {
            super(PSBlockEntities.MASH_TUB_EDGE, pos, state);
            masterPos = pos;
        }

        public void setMasterPos(BlockPos pos) {
            masterPos = pos;
            setChanged();
            if (level instanceof ServerLevel sw) {
                sw.getChunkSource().blockChanged(getBlockPos());
            }
        }

        public BlockPos getMasterPos() {
            return masterPos;
        }

        @Override
        public void saveAdditional(CompoundTag compound, Provider lookup) {
            super.saveAdditional(compound, lookup);
            compound.put("masterPos", NbtUtils.writeBlockPos(masterPos));
        }

        @Override
        public void loadAdditional(CompoundTag compound, Provider lookup) {
            super.loadAdditional(compound, lookup);
            masterPos = NbtUtils.readBlockPos(compound, "masterPos").orElse(BlockPos.ZERO);
        }
    }
}
