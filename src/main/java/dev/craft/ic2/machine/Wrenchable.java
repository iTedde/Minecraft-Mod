package dev.craft.ic2.machine;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code IWrenchable}: tiles the wrench can rotate or dismantle. */
public interface Wrenchable {
    Direction getFacing();

    boolean wrenchCanSetFacing(Player player, Direction side);

    void setFacing(Direction facing);

    default boolean wrenchCanRemove(Player player) {
        return true;
    }

    /** Chance that dismantling with the wrench drops the block itself instead of its normal drop. */
    float getWrenchDropRate();

    ItemStack getWrenchDrop(Player player);
}
