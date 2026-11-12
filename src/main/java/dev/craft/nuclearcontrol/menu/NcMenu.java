package dev.craft.nuclearcontrol.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Base of the Nuclear Control menus: bound to a block position, valid within 8 blocks ({@code isUseableByPlayer}). */
public abstract class NcMenu extends AbstractContainerMenu {
    protected final BlockPos pos;

    protected NcMenu(MenuType<?> type, int id, BlockPos pos) {
        super(type, id);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    protected abstract boolean isOwnBlockEntity(BlockEntity blockEntity);

    @Override
    public boolean stillValid(Player player) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        return blockEntity != null && !blockEntity.isRemoved() && isOwnBlockEntity(blockEntity)
                && player.distanceToSqr(pos.getCenter()) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
