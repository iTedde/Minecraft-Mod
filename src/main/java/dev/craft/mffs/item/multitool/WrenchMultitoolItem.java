package dev.craft.mffs.item.multitool;

import dev.craft.buildcraft.item.ToolWrench;
import dev.craft.ic2.machine.Wrenchable;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code ItemWrench} (spec 48 §8): on an MFFS machine (right EB, not while active) a different side turns it there and
 * the front side removes it, 1 000 FE each; on other wrenchable Craft blocks turning costs 1 000 FE and removing
 * 25 000 FE. Also a BuildCraft wrench (1 000 FE per use). Repairing a burnt-out projector follows in phase 48.3.
 */
public class WrenchMultitoolItem extends MultitoolItem implements ToolWrench {
    public static final int MANIPULATE_COST = 1000;
    public static final int REMOVE_COST = 25000;

    public WrenchMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Direction side = context.getClickedFace();
        if (blockEntity instanceof MffsMachineBlockEntity machine) {
            return useOnMachine(stack, player, machine, side);
        }
        if (blockEntity instanceof Wrenchable wrenchable) {
            if (consumePower(stack, MANIPULATE_COST, true)) {
                if (wrenchable.wrenchCanSetFacing(player, side)) {
                    wrenchable.setFacing(side);
                    consumePower(stack, MANIPULATE_COST, false);
                    return InteractionResult.SUCCESS;
                }
            } else {
                notEnough(player, "message.craft.mffs.multitool.not_enough_facing", MANIPULATE_COST);
            }
            if (consumePower(stack, REMOVE_COST, true)) {
                if (wrenchable.wrenchCanRemove(player) && level instanceof ServerLevel serverLevel) {
                    BlockState state = level.getBlockState(pos);
                    List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, serverLevel, pos, blockEntity));
                    if (drops.isEmpty()) {
                        drops.add(wrenchable.getWrenchDrop(player));
                    } else {
                        drops.set(0, wrenchable.getWrenchDrop(player));
                    }
                    drops.forEach(drop -> Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    consumePower(stack, REMOVE_COST, false);
                }
            } else {
                notEnough(player, "message.craft.mffs.multitool.not_enough_remove", REMOVE_COST);
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult useOnMachine(ItemStack stack, Player player, MffsMachineBlockEntity machine,
                                           Direction side) {
        if (!consumePower(stack, MANIPULATE_COST, true)) {
            notEnough(player, "message.craft.mffs.multitool.not_enough", MANIPULATE_COST);
            return InteractionResult.PASS;
        }
        if (!machine.wrenchCanManipulate(player) || !machine.canBeRemovedByMultitool()) {
            return InteractionResult.PASS;
        }
        if (machine.getFacing() != side) {
            machine.setFacing(side);
            consumePower(stack, MANIPULATE_COST, false);
            return InteractionResult.SUCCESS;
        }
        Level level = machine.getLevel();
        BlockPos pos = machine.getBlockPos();
        ItemStack drop = new ItemStack(machine.getBlockState().getBlock());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
        consumePower(stack, MANIPULATE_COST, false);
        return InteractionResult.PASS;
    }

    @Override
    public boolean canWrench(ItemStack stack, Player player, BlockPos pos) {
        return consumePower(stack, MANIPULATE_COST, true);
    }

    @Override
    public void wrenchUsed(ItemStack stack, Player player, BlockPos pos) {
        consumePower(stack, MANIPULATE_COST, false);
    }
}
