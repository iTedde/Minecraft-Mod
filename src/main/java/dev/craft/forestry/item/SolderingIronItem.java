package dev.craft.forestry.item;

import dev.craft.forestry.menu.SolderingIronMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Forestry {@code ItemSolderingIron}: right click opens the soldering GUI. The durability of 5 is never used.
 */
public class SolderingIronItem extends Item {
    public SolderingIronItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new SolderingIronMenu(id, inventory),
                    Component.translatable(getDescriptionId())));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
