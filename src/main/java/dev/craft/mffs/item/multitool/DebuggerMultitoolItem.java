package dev.craft.mffs.item.multitool;

import com.mojang.logging.LogUtils;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

/**
 * {@code ItemDebugger} (spec 48 §8, creative only): free pass at every security check and at access type 0 of the
 * field teleporter. Not part of the mode cycle (sneak + right-click does nothing); clicking a machine logs its active
 * flag like the original's console output.
 */
public class DebuggerMultitoolItem extends MultitoolItem {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DebuggerMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MffsMachineBlockEntity machine) {
            LOGGER.info("{}{}", context.getLevel().isClientSide() ? "client" : "server", machine.isActive());
        }
        return InteractionResult.PASS;
    }
}
