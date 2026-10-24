package dev.craft.thermalexpansion.item;

import dev.craft.buildcraft.item.BcWrenchItem;
import dev.craft.buildcraft.item.ToolWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crescent Hammer ({@code ItemWrench}, a BuildCraft {@code IToolWrench}): rotates vanilla blocks (sneaking the other
 * way) and is the wrench for Thermal Expansion and BuildCraft blocks, which check for it themselves.
 */
public class CrescentHammerItem extends BcWrenchItem {
    public CrescentHammerItem(Properties properties) {
        super(properties);
    }

    /** {@code Utils.isHoldingWrench}: any BuildCraft-style wrench ({@code IToolWrench}). */
    public static boolean isWrench(ItemStack stack) {
        return ToolWrench.isWrench(stack);
    }

    /** CoFH {@code BlockUtils.rotateVanillaBlock}: vanilla blocks only, sneaking turns them back. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("minecraft")) {
            return InteractionResult.PASS;
        }
        boolean back = player != null && player.isShiftKeyDown();
        BlockState rotated = state.rotate(level, pos, back ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
        if (rotated == state) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, rotated, Block.UPDATE_ALL);
            level.playSound(null, pos, state.getSoundType(level, pos, player).getPlaceSound(), SoundSource.BLOCKS,
                    1.0F, back ? 0.6F : 0.8F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
