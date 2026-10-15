package dev.craft.gregtech.machine;

import java.util.HashSet;
import java.util.Set;

import dev.craft.gregtech.block.GtCasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * {@code GT_Mod.causeMachineUpdate}: tells the GT machines connected to {@code pos} through casings and other GT
 * machines that their structure may have changed (spec 13 §5).
 */
public final class MachineBlockUpdates {
    private MachineBlockUpdates() {
    }

    public static void cause(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            step(level, pos, new HashSet<>());
        }
    }

    /**
     * {@code stepToUpdateMachine}: depth-first in the order +X, −X, +Y, −Y, +Z, −Z; continues while fewer than five
     * positions were visited or the position is a GT machine or a machine casing.
     */
    private static void step(Level level, BlockPos pos, Set<BlockPos> visited) {
        visited.add(pos);
        boolean machine = false;
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GtMachineBlockEntity be) {
            be.onMachineBlockUpdate();
            machine = true;
        }
        if (visited.size() < 5 || machine
                || level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof GtCasingBlock) {
            for (BlockPos next : new BlockPos[]{pos.east(), pos.west(), pos.above(), pos.below(), pos.south(),
                    pos.north()}) {
                if (!visited.contains(next)) {
                    step(level, next, visited);
                }
            }
        }
    }
}
