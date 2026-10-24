package dev.craft.lightshield;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Who shines a light shield and what it costs (spec 30 §3a). The light itself is a client side cone drawn by
 * LambDynamicLights; the server only pays for the switched-on light.
 */
public final class ShieldLights {
    private ShieldLights() {
    }

    /** The lit shield {@code player} holds, main hand first, or an empty stack. */
    public static ItemStack litShield(Player player) {
        if (player.isSpectator()) {
            return ItemStack.EMPTY;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (ElectricLightShieldItem.isLit(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Server player tick: 1 EU every 4 ticks while the light is on. */
    public static void tick(Player player) {
        ItemStack shield = litShield(player);
        if (!shield.isEmpty() && player.level().getGameTime() % ElectricLightShieldItem.LIGHT_INTERVAL == 0) {
            ElectricItems.use(shield, 1, player);
        }
    }
}
