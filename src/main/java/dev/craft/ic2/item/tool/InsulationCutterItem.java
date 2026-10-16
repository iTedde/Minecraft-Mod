package dev.craft.ic2.item.tool;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.machine.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Insulation cutter (IC2 {@code ItemToolCutter}): right click adds an insulation layer to a cable for one rubber,
 * left click (see {@link CableBlock#attack}) strips one and drops the rubber.
 */
public class InsulationCutterItem extends Item {
    public static final int DURABILITY = 512;

    public InsulationCutterItem(Properties properties) {
        super(properties.durability(DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockState(pos).getBlock() instanceof CableBlock cable)) {
            return InteractionResult.PASS;
        }
        CableType target = cable.getCableType().insulated();
        if (target == cable.getCableType() || !hasRubber(player.getInventory())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            consumeRubber(player.getInventory());
            CableBlock.changeType(level, pos, target);
            damageCutter(context.getItemInHand(), 1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** IC2 {@code cutInsulationFrom}: strips one insulation layer and drops one rubber. */
    public static void cutInsulationFrom(ItemStack cutter, Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (level.isClientSide() || !(state.getBlock() instanceof CableBlock cable)) {
            return;
        }
        CableType target = cable.getCableType().stripped();
        if (target == cable.getCableType() || !CableBlock.changeType(level, pos, target)) {
            return;
        }
        StackUtil.dropAsEntity(level, pos, new ItemStack(Ic2Items.RUBBER.get()));
        damageCutter(cutter, 3);
        level.playSound(null, pos, Ic2Sounds.INSULATION_CUTTERS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** IC2 {@code damageCutter}: the cutter only breaks once the damage exceeds the maximum. */
    public static void damageCutter(ItemStack stack, int damage) {
        int newDamage = stack.getDamageValue() + damage;
        if (newDamage > stack.getMaxDamage()) {
            stack.shrink(1);
            if (!stack.isEmpty()) {
                stack.setDamageValue(0);
            }
        } else {
            stack.setDamageValue(newDamage);
        }
    }

    private static boolean hasRubber(Inventory inventory) {
        return inventory.items.stream().anyMatch(stack -> stack.is(Ic2Items.RUBBER.get()));
    }

    /** IC2 {@code consumeInventoryItem}: first main inventory slot holding rubber. */
    private static void consumeRubber(Inventory inventory) {
        for (ItemStack stack : inventory.items) {
            if (stack.is(Ic2Items.RUBBER.get())) {
                stack.shrink(1);
                return;
            }
        }
    }
}
