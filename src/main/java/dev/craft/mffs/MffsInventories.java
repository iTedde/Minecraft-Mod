package dev.craft.mffs;

import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.block.SecurityStorageBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * {@code InventoryHelper} (spec 48 §9.3, §10): the attached inventory with the most slots. MFFS blocks are used
 * like the original's raw inventories: the security storage from slot 1 on (also while active), the defence station
 * from the given slot on.
 */
public final class MffsInventories {
    private MffsInventories() {
    }

    /** {@code findAttachedInventory}: neighbours in the order east, west, up, down, south, north. */
    public static IItemHandler findAttachedInventory(Level level, BlockPos pos, int defenseStationStart) {
        IItemHandler best = null;
        for (BlockPos neighbour : List.of(pos.east(), pos.west(), pos.above(), pos.below(), pos.south(),
                pos.north())) {
            IItemHandler handler = inventoryAt(level, neighbour, defenseStationStart);
            if (handler != null && (best == null || best.getSlots() < handler.getSlots())) {
                best = handler;
            }
        }
        return best;
    }

    private static IItemHandler inventoryAt(Level level, BlockPos pos, int defenseStationStart) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        if (blockEntity instanceof SecurityStorageBlockEntity storage) {
            return storage.getStorageHandler();
        }
        if (blockEntity instanceof DefenseStationBlockEntity station) {
            return new RangedWrapper(station.getRawInventory(), defenseStationStart,
                    station.getRawInventory().getSlots());
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }
}
