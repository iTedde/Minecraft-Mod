package dev.craft.nuclearcontrol.item;

import javax.annotation.Nullable;

import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.ic2.reactor.ReactorChamberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Thermometer ({@code ItemToolThermometer}, spec 46 §3.3): right click on a reactor or chamber reports the hull heat
 * and costs 10 of 102 durability (11 readings).
 */
public class ThermometerItem extends Item {
    public static final int DURABILITY = 102;
    public static final int DAMAGE_PER_USE = 10;

    public ThermometerItem(Properties properties) {
        this(properties.durability(DURABILITY), true);
    }

    protected ThermometerItem(Properties properties, boolean durable) {
        super(durable ? properties : properties.stacksTo(1));
    }

    /** {@code NuclearHelper.getReactorAt}, else the reactor of a chamber. */
    @Nullable
    public static NuclearReactorBlockEntity reactorAt(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof NuclearReactorBlockEntity reactor) {
            return reactor;
        }
        return entity instanceof ReactorChamberBlockEntity chamber ? chamber.getReactor() : null;
    }

    /** {@code canTakeDamage(stack, 2)}. */
    protected boolean canTakeDamage(ItemStack stack) {
        return true;
    }

    protected Component message(NuclearReactorBlockEntity reactor) {
        return Component.translatable("message.craft.nuclearcontrol.thermo", reactor.getHeat());
    }

    protected void damage(ItemStack stack, Player player, UseOnContext context) {
        stack.hurtAndBreak(DAMAGE_PER_USE, player, context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !canTakeDamage(stack)) {
            return InteractionResult.PASS;
        }
        NuclearReactorBlockEntity reactor = reactorAt(context.getLevel(), context.getClickedPos());
        if (reactor == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            player.sendSystemMessage(message(reactor));
            damage(stack, player, context);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
