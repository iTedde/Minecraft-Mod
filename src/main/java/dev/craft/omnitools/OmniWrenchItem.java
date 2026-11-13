package dev.craft.omnitools;

import java.util.ArrayList;
import java.util.List;

import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.machine.Wrenchable;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Prototype OmniWrench (spec 50 §1.1): unbreakable, no energy. Rotates vanilla blocks like the crescent hammer
 * (sneaking the other way), turns IC2-style {@link Wrenchable} blocks to the clicked side (sneaking: opposite) and
 * removes them when the front is clicked, always dropping the block itself. As a BuildCraft/TE wrench the other blocks
 * check for it themselves.
 */
public class OmniWrenchItem extends CrescentHammerItem {
    public OmniWrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (player == null || !(blockEntity instanceof Wrenchable wrenchable)) {
            return InteractionResult.PASS;
        }
        Direction side = player.isShiftKeyDown() ? context.getClickedFace().getOpposite() : context.getClickedFace();
        if (blockEntity instanceof MffsMachineBlockEntity machine && !machine.wrenchCanManipulate(player)) {
            return InteractionResult.PASS;
        }
        if (side == wrenchable.getFacing() && wrenchable.wrenchCanRemove(player)) {
            if (level instanceof ServerLevel serverLevel) {
                BlockState state = level.getBlockState(pos);
                List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, serverLevel, pos, blockEntity));
                if (drops.isEmpty()) {
                    drops.add(wrenchable.getWrenchDrop(player));
                } else {
                    drops.set(0, wrenchable.getWrenchDrop(player));
                }
                drops.forEach(drop -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                playSound(level, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (side != wrenchable.getFacing() && wrenchable.wrenchCanSetFacing(player, side)) {
            if (!level.isClientSide()) {
                wrenchable.setFacing(side);
                playSound(level, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    /** {@code shouldPassSneakingClickToBlock}. */
    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    private static void playSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2Sounds.WRENCH.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
