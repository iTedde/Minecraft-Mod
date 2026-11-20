/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FluidProcessingBlockEntity extends FlaskBlockEntity implements Processable.ByProductConsumer {
    public FluidProcessingBlockEntity(
            BlockEntityType<? extends FluidProcessingBlockEntity> type,
            BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state, capacity);
    }

    private float prevProgress;
    private int repeatCount;

    @Override
    protected int getTotalProperties() {
        return super.getTotalProperties() + 2;
    }

    public int getTimeNeeded() {
        return propertyDelegate.get(4);
    }

    public void setTimeNeeded(int value) {
        if (value > 0 && getLevel() instanceof ServerLevel sw) {
            value = Math.max(1, value / getTickRate(sw));
        }
        propertyDelegate.set(4, value);
    }

    public int getTimeProcessed() {
        return propertyDelegate.get(5);
    }

    public void setTimeProcessed(int value) {
        propertyDelegate.set(5, value);
        markForUpdate();
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeats) {
        repeatCount = repeats;
        markForUpdate();
    }

    private float getProgress() {
        if (isActive()) {
            return (float)getTimeProcessed() / getTimeNeeded();
        }
        return 0;
    }

    public float getProgress(float tickDelta) {
        return Mth.lerp(tickDelta, prevProgress, getProgress());
    }

    public abstract Processable.ProcessType getProcessType();

    public Processable.ProcessType getActiveProcess() {
        Resovoir tank = getPrimaryTank();

        if (isActive() && tank.getContents().fluid() instanceof Processable p) {
            return p.modifyProcess(tank, getProcessType());
        }
        return Processable.ProcessType.IDLE;
    }

    public boolean isActive() {
        return getTimeNeeded() != Processable.UNCONVERTABLE;
    }

    protected int getTickRate(ServerLevel world) {
        return 1;
    }

    @Override
    public void clientTick(Level world) {
        prevProgress = getProgress();
    }

    @Override
    public void tick(ServerLevel world) {
        super.tick(world);

        Resovoir tank = getPrimaryTank();

        if (tank.getContents().fluid() instanceof Processable p) {
            Processable.ProcessType type = p.modifyProcess(tank, getProcessType());
            setTimeNeeded(p.getProcessingTime(tank, type));

            if (canProcess(world, getTimeNeeded())) {
                if (getTimeProcessed() >= getTimeNeeded()) {
                    p.process(this, type, this);
                    onProcessCompleted(world, tank);
                } else {
                    setTimeProcessed(getTimeProcessed() + 1);
                    world.blockUpdated(getBlockPos(), getBlockState().getBlock());
                }
            } else {
                setTimeProcessed(0);
            }
        } else {
            setTimeNeeded(Processable.UNCONVERTABLE);
        }
    }

    protected boolean canProcess(ServerLevel world, int timeNeeded) {
        return timeNeeded >= 0;
    }

    protected void onProcessCompleted(ServerLevel world, Resovoir tank) {
        setTimeProcessed(0);
        setTimeNeeded(Processable.UNCONVERTABLE);
        repeatCount++;

        markForUpdate();
    }

    @Override
    public void onLevelChange(Resovoir resovoir, int difference) {
        if (resovoir.getContents().isEmpty()) {
            setTimeProcessed(0);
            repeatCount = 0;
            markForUpdate();
        }
        super.onLevelChange(resovoir, difference);
    }

    @Override
    public void saveAdditional(CompoundTag compound, Provider lookup) {
        super.saveAdditional(compound, lookup);
        compound.putInt("timeProcessed", getTimeProcessed());
        compound.putInt("timeNeeded", getTimeNeeded());
        compound.putInt("repeatCount", repeatCount);
    }

    @Override
    public void loadAdditional(CompoundTag compound, Provider lookup) {
        super.loadAdditional(compound, lookup);
        setTimeProcessed(compound.getInt("timeProcessed"));
        setTimeNeeded(compound.getInt("timeNeeded"));
        repeatCount = compound.getInt("repeatCount");
    }

    @Override
    public void accept(ItemStack stack) {
        if (level != null) {
            Block.popResource(level, getBlockPos(), stack);
        }
    }

    @Override
    public void accept(ItemFluids stack) {
        if (getLevel() instanceof ServerLevel world) {
            int transferred = getPrimaryTank().deposit(stack);
            if (transferred < stack.amount()) {
                onFluidRejected(world, stack.ofAmount(stack.amount() - transferred));
            }
        }
    }

    protected void onFluidRejected(ServerLevel world, ItemFluids fluids) {
        // TODO: Droplets item?
        world.playSound(null, getBlockPos(), SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.25F, 0.02F);
        world.sendParticles(ParticleTypes.SPLASH,
                worldPosition.getX() + world.getRandom().triangle(0.5F, 0.5F),
                worldPosition.getY() + 0.6F,
                worldPosition.getZ() + world.getRandom().triangle(0.5F, 0.5F),
                2, 0, 0, 0, 0);
    }
}
