/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.block.DistilleryBlock;
import ivorius.psychedelicraft.block.MashTubWallBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Created by lukas on 25.10.14.
 */
public class DistilleryBlockEntity extends FluidProcessingBlockEntity {
    public static final int DISTILLERY_CAPACITY = FluidVolumes.FLASK;

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.DISTILLERY, pos, state, DISTILLERY_CAPACITY);
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.DISTILL;
    }

    @Override
    protected int getTickRate(ServerLevel world) {
        return Math.max(1, getBlockHeat(world.getBlockState(getBlockPos().below())));
    }

    public static int getBlockHeat(BlockState state) {
        if (state.getFluidState().is(FluidTags.LAVA)) {
            return 7;
        }
        if (state.is(BlockTags.FIRE) || (state.is(BlockTags.CAMPFIRES) && state.getOptionalValue(BlockStateProperties.LIT).orElse(true))) {
            return 3;
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return 2;
        }
        return 0;
    }

    @Override
    protected boolean canProcess(ServerLevel world, int timeNeeded) {
        return super.canProcess(world, timeNeeded)
                && getFacing().getAxis() != Axis.Y
                && DistilleryBlock.canConnectTo(world.getBlockState(getOutputPos()), getFacing())
                && getOutput(world, getBlockPos()) instanceof FlaskBlockEntity;
    }

    @Override
    public void accept(ItemFluids stack) {
        if (getLevel() instanceof ServerLevel world) {
            if (getOutput(world, getBlockPos()) instanceof FlaskBlockEntity destination) {
                int transferred = destination.getTankOnSide(getFacing().getOpposite()).deposit(stack);
                if (transferred < stack.amount()) {
                    onFluidRejected(world, stack.ofAmount(stack.amount() - transferred));
                }
            } else {
                world.sendParticles(ParticleTypes.CLOUD,
                        worldPosition.getX() + world.getRandom().triangle(0.5F, 0.5F),
                        worldPosition.getY() + 0.6F,
                        worldPosition.getZ() + world.getRandom().triangle(0.5F, 0.5F),
                        2, 0, 0, 0, 0);
                world.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.25F, 0.02F);
            }
        }
    }

    @Override
    protected void onProcessCompleted(ServerLevel world, Resovoir tank) {
        world.sendParticles(ParticleTypes.CLOUD,
                worldPosition.getX() + world.getRandom().triangle(0.5F, 0.5F),
                worldPosition.getY() + 0.6F,
                worldPosition.getZ() + world.getRandom().triangle(0.5F, 0.5F),
                2, 0, 0, 0, 0);
        if (world.getRandom().nextInt(10) == 0) {
            world.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.25F, 0.02F);
        }
        super.onProcessCompleted(world, tank);
    }

    private BlockEntity getOutput(ServerLevel world, BlockPos pos) {
        pos = getOutputPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof MashTubWallBlock) {
            pos = world.getBlockEntity(pos, PSBlockEntities.MASH_TUB_EDGE).map(p -> p.getMasterPos()).orElse(pos);
        }
        return world.getBlockEntity(pos);
    }

    private BlockPos getOutputPos() {
        return getBlockPos().relative(getFacing());
    }

    private Direction getFacing() {
        return getBlockState().getValue(DistilleryBlock.FACING);
    }
}
