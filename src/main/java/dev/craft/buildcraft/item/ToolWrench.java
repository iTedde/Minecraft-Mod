package dev.craft.buildcraft.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * BuildCraft {@code IToolWrench}. BuildCraft blocks ask {@link #canWrench} before they react and call
 * {@link #wrenchUsed} afterwards; Forestry and Thermal Expansion only check for the interface.
 */
public interface ToolWrench {
    default boolean canWrench(ItemStack stack, Player player, BlockPos pos) {
        return true;
    }

    default void wrenchUsed(ItemStack stack, Player player, BlockPos pos) {
    }

    /** Forestry {@code Utils.isWrench}, Thermal Expansion {@code Utils.isHoldingWrench}. */
    static boolean isWrench(ItemStack stack) {
        return stack.getItem() instanceof ToolWrench;
    }

    /** BuildCraft: {@code equipped instanceof IToolWrench && canWrench(…)}. */
    static boolean canWrenchWith(ItemStack stack, Player player, BlockPos pos) {
        return stack.getItem() instanceof ToolWrench wrench && wrench.canWrench(stack, player, pos);
    }

    static void wrenchUsedWith(ItemStack stack, Player player, BlockPos pos) {
        if (stack.getItem() instanceof ToolWrench wrench) {
            wrench.wrenchUsed(stack, player, pos);
        }
    }
}
