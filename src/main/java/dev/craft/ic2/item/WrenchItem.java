package dev.craft.ic2.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.machine.Wrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wrench (IC2 {@code ItemToolWrench}): rotates rotatable tiles to the clicked side (sneaking: opposite side; ALT key:
 * next facing, previous when sneaking) for 1 durability, otherwise dismantles them for 10 durability with the
 * tile's wrench drop rate.
 */
public class WrenchItem extends Item {
    public static final int DURABILITY = 160;
    public static final int ROTATE_COST = 1;
    public static final int REMOVE_COST = 10;
    public static final int LOSSLESS_COST = 200;

    public WrenchItem(Properties properties) {
        super(properties.durability(DURABILITY));
    }

    /** For subclasses without durability (electric wrench). */
    protected WrenchItem(Properties properties, boolean durable) {
        super(durable ? properties.durability(DURABILITY) : properties.stacksTo(1));
    }

    /** Whether {@code amount} durability points can be taken. */
    public boolean canTakeDamage(ItemStack stack, int amount) {
        return true;
    }

    protected void damage(ItemStack stack, int amount, Player player, InteractionHand hand) {
        stack.hurtAndBreak(amount, player, hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    /** Whether dismantling always drops the original block (electric wrench in lossless mode). */
    protected boolean overrideWrenchSuccessRate(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !canTakeDamage(stack, ROTATE_COST)) {
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Wrenchable wrenchable)) {
            return InteractionResult.PASS;
        }
        Direction side = context.getClickedFace();
        if (Ic2Keyboard.isAltKeyDown(player)) {
            int facing = wrenchable.getFacing().get3DDataValue();
            side = Direction.from3DDataValue(player.isShiftKeyDown() ? (facing + 5) % 6 : (facing + 1) % 6);
        } else if (player.isShiftKeyDown()) {
            side = side.getOpposite();
        }
        if (wrenchable.wrenchCanSetFacing(player, side)) {
            if (!level.isClientSide()) {
                wrenchable.setFacing(side);
                damage(stack, ROTATE_COST, player, context.getHand());
                playSound(level, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!canTakeDamage(stack, REMOVE_COST) || !wrenchable.wrenchCanRemove(player)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            BlockState state = level.getBlockState(pos);
            Craft.LOGGER.info("Player {} used the wrench to remove the {} at {}/{}/{}",
                    player.getName().getString(), state.getBlock().getName().getString(),
                    pos.getX(), pos.getY(), pos.getZ());
            boolean dropOriginalBlock;
            if (wrenchable.getWrenchDropRate() < 1.0F && overrideWrenchSuccessRate(stack)) {
                if (!canTakeDamage(stack, LOSSLESS_COST)) {
                    player.sendSystemMessage(Component.translatable("message.craft.wrench.lossless_no_energy"));
                    return InteractionResult.SUCCESS;
                }
                dropOriginalBlock = true;
                damage(stack, LOSSLESS_COST, player, context.getHand());
            } else {
                dropOriginalBlock = level.random.nextFloat() <= wrenchable.getWrenchDropRate();
                damage(stack, REMOVE_COST, player, context.getHand());
            }
            List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, serverLevel, pos, blockEntity));
            if (dropOriginalBlock) {
                if (drops.isEmpty()) {
                    drops.add(wrenchable.getWrenchDrop(player));
                } else {
                    drops.set(0, wrenchable.getWrenchDrop(player));
                }
            }
            for (ItemStack drop : drops) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            playSound(level, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void playSound(Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2Sounds.WRENCH.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
