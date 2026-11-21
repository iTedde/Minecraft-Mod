package dev.craft.gravitygun;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Gravity gun ({@code ItemGraviGun} meta 0 / 1, spec 50 §3). The keys are caught on the client
 * ({@code GravityGunClient}): use grabs/drops, attack throws. The item itself does nothing on use.
 */
public class GravityGunItem extends Item {
    private final boolean charged;

    public GravityGunItem(Properties properties, boolean charged) {
        super(properties.stacksTo(1).rarity(charged ? Rarity.EPIC : Rarity.UNCOMMON));
        this.charged = charged;
    }

    public boolean isCharged() {
        return charged;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return charged;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(charged ? "tooltip.craft.gravity_gun.charged" : "tooltip.craft.gravity_gun")
                .withStyle(ChatFormatting.GRAY));
    }
}
