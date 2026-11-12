package dev.craft.nuclearcontrol.item;

import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.nuclearcontrol.NcDataComponents;
import dev.craft.nuclearcontrol.NcItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Remote sensor kit ({@code ItemKitReactorSensor}, spec 46 §5.2): right click on a reactor or chamber turns the kit
 * in the hand into a reactor sensor location card for the reactor's position.
 */
public class ReactorSensorKitItem extends Item {
    public ReactorSensorKitItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        NuclearReactorBlockEntity reactor = ThermometerItem.reactorAt(context.getLevel(), context.getClickedPos());
        if (reactor == null) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            ItemStack card = new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get());
            card.set(NcDataComponents.SENSOR_TARGET.get(), reactor.getBlockPos());
            player.setItemInHand(context.getHand(), card);
            player.sendSystemMessage(Component.translatable("message.craft.nuclearcontrol.sensor_kit"));
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
