package dev.craft.ic2.storage;

import java.util.function.BiFunction;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Storage block: weak redstone signal on all sides while its redstone mode says so. */
public class ElectricStorageBlock extends MachineBlock {
    public ElectricStorageBlock(Properties properties, Ic2Machine machine,
                                BiFunction<BlockPos, BlockState, ElectricStorageBlockEntity> factory) {
        super(properties, machine, factory);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ElectricStorageBlockEntity storage && storage.isEmittingRedstone()
                ? 15 : 0;
    }
}
