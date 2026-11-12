package dev.craft.nuclearcontrol.menu;

import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Thermal monitor GUI ({@code ContainerEmpty}): heat level and redstone inversion only. */
public class ThermalMonitorMenu extends NcMenu {
    public ThermalMonitorMenu(int id, Inventory inventory, BlockPos pos) {
        super(NcMenus.THERMAL_MONITOR.get(), id, pos);
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof ThermalMonitorBlockEntity
                && !(blockEntity instanceof RemoteThermalMonitorBlockEntity);
    }
}
