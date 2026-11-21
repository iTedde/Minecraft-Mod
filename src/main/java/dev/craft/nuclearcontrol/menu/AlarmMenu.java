package dev.craft.nuclearcontrol.menu;

import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Howler/industrial alarm GUI ({@code ContainerEmpty}): sound range and, for the howler alarm, the sound. */
public class AlarmMenu extends NcMenu {
    public AlarmMenu(int id, Inventory inventory, BlockPos pos) {
        super(NcMenus.ALARM.get(), id, pos);
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof AlarmBlockEntity;
    }
}
