package dev.craft.ic2.item;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.block.RubberWoodBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Treetap (IC2 {@code ItemTreetap}): extracts sticky resin from resin holes of rubber wood. */
public class TreetapItem extends Item {
    public static final int DURABILITY = 16;

    public TreetapItem(Properties properties) {
        super(properties.durability(DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!(state.getBlock() instanceof RubberWoodBlock)) {
            return InteractionResult.PASS;
        }
        attemptExtract(context.getPlayer(), level, context.getClickedPos(), context.getClickedFace());
        if (!level.isClientSide() && context.getPlayer() != null) {
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(),
                    context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** IC2 {@code attemptExtract}: wet hole → 1–3 resin and dry hole; dry hole → small chances. */
    public static boolean attemptExtract(@Nullable Player player, Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        RubberWoodBlock.Resin resin = state.getValue(RubberWoodBlock.RESIN);
        if (!resin.hasHole() || state.getValue(RubberWoodBlock.FACING) != side) {
            return false;
        }
        if (resin == RubberWoodBlock.Resin.WET) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.DRY), Block.UPDATE_ALL);
                ejectResin(level, pos, side, level.random.nextInt(3) + 1);
                level.scheduleTick(pos, state.getBlock(), RubberWoodBlock.TICK_RATE);
                playSound(level, pos);
            }
            return true;
        }
        if (level.random.nextInt(5) == 0 && !level.isClientSide()) {
            level.setBlock(pos, state.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.NATURAL), Block.UPDATE_ALL);
        }
        if (level.random.nextInt(5) == 0) {
            if (!level.isClientSide()) {
                // Original quirk: two resin items are ejected here.
                ejectResin(level, pos, side, 1);
                ejectResin(level, pos, side, 1);
                playSound(level, pos);
            }
            return true;
        }
        return false;
    }

    private static void ejectResin(Level level, BlockPos pos, Direction side, int quantity) {
        double x = pos.getX() + 0.5 + side.getStepX() * 0.3;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5 + side.getStepZ() * 0.3;
        for (int i = 0; i < quantity; i++) {
            ItemEntity entity = new ItemEntity(level, x, y, z, new ItemStack(Ic2Items.RESIN.get()));
            entity.setPickUpDelay(10);
            level.addFreshEntity(entity);
        }
    }

    private static void playSound(Level level, BlockPos pos) {
        level.playSound(null, pos, Ic2Sounds.TREETAP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
