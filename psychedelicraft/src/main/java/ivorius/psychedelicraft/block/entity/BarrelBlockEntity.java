/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.block.BarrelBlock;
import ivorius.psychedelicraft.fluid.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends FluidProcessingBlockEntity {

    public int timeFermented;

    private float prevTapRotation;
    private float tapRotation;

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PSBlockEntities.BARREL, pos, state, FluidVolumes.BARREL);
    }

    @Override
    protected int getTotalProperties() {
        return super.getTotalProperties() + 1;
    }

    @Override
    public Processable.ProcessType getProcessType() {
        return Processable.ProcessType.MATURE;
    }

    @Override
    public void clientTick(Level world) {
        super.clientTick(world);

        prevTapRotation = tapRotation;

        if (getTapOpenTicks() > 0 && tapRotation < Mth.HALF_PI) {
            tapRotation += Mth.PI * 0.1F;
        }

        if (getTapOpenTicks() == 0 && tapRotation > 0) {
            tapRotation -= Mth.PI * 0.1F;
        }
    }

    @Override
    public void tick(ServerLevel world) {
        super.tick(world);
        int tapOpenTicks = getTapOpenTicks();
        if (tapOpenTicks > 0) {
            setTapOpenTicks(tapOpenTicks - 1);
            markForUpdate();
        }

        if (tapOpenTicks == 1) {
            Direction updateDirection = getBlockState().getValue(BarrelBlock.FACING).getOpposite();
            if (updateDirection.getAxis() != Axis.Y) {
                BlockPos pos = getBlockPos().relative(updateDirection);
                world.updateNeighborsAtExceptFromFacing(pos, getBlockState().getBlock(), updateDirection.getOpposite());
            }
        }

        if (tapOpenTicks > 0 && tapOpenTicks % 5 == 0) {
            world.playSound(null, getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.025F, 0.5F);
        }
    }

    public int getTapOpenTicks() {
        return propertyDelegate.get(6);
    }

    public void setTapOpenTicks(int ticks) {
        propertyDelegate.set(6, ticks);
    }

    public float getTapRotation(float tickDelta) {
        return Mth.lerp(tickDelta, prevTapRotation, tapRotation);
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        compound.putInt("timeLeftTapOpen", getTapOpenTicks());
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        setTapOpenTicks(compound.getInt("timeLeftTapOpen"));
    }
}
