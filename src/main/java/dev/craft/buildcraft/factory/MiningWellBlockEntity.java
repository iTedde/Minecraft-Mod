package dev.craft.buildcraft.factory;

import java.util.List;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.util.BlockUtil;
import dev.craft.buildcraft.util.ItemOutput;
import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BuildCraft {@code TileMiningWell}: 25 MJ per block, replaces the block below the pipe column with another pipe and
 * hands the drops out like the quarry. {@code isDigging} is not saved, as in the original.
 */
public class MiningWellBlockEntity extends BlockEntity implements PowerReceptor, ConsumptionReadout {
    private final OutputMeter consumptionMeter = new OutputMeter();
    private final PowerProvider provider = new PowerProvider();
    private boolean isDigging = true;

    public MiningWellBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.MINING_WELL.get(), pos, state);
        provider.configure(1, 25, 25, 1000);
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    public void serverTick() {
        provider.update(this, level);
        setChanged();
    }

    @Override
    public void doWork() {
        if (provider.useEnergy(25.0F, 25.0F, true) < 25.0F) {
            return;
        }
        consumptionMeter.record(level, 25.0F);
        BlockPos.MutableBlockPos pos = worldPosition.below().mutable();
        while (level.getBlockState(pos).is(BcBlocks.MINING_PIPE.get())) {
            pos.move(0, -1, 0);
        }
        if (pos.getY() >= level.getMinBuildHeight() && BlockUtil.canChangeBlock(level, pos)) {
            BlockState state = level.getBlockState(pos);
            List<ItemStack> stacks = Block.getDrops(state, (ServerLevel) level, pos, level.getBlockEntity(pos));
            level.setBlock(pos, BcBlocks.MINING_PIPE.get().defaultBlockState(), 3);
            if (!state.isAir()) {
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty()) {
                        ItemOutput.output(level, worldPosition, stack);
                    }
                }
            }
        } else {
            isDigging = false;
        }
    }

    /** {@code TileMachine.powerRequest}: only while digging ({@code isActive}). */
    @Override
    public int powerRequest() {
        return isDigging ? provider.missingEnergyRequest() : 0;
    }

    public boolean isDigging() {
        return isDigging;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("power", provider.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.MJ);
    }
}
