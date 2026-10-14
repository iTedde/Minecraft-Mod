package dev.craft.ic2.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Single-use battery (IC2 {@code ItemBatterySU}): 1000 EU, charges the hotbar or powers machines once. */
public class SingleUseBatteryItem extends Item {
    public static final int CAPACITY = 1000;
    public static final int TIER = 1;

    public SingleUseBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int energy = CAPACITY;
        for (int i = 0; i < Inventory.getSelectionSize() && energy > 0; i++) {
            ItemStack target = player.getInventory().getItem(i);
            if (ElectricItems.isElectric(target) && target != stack) {
                energy -= ElectricItems.charge(target, energy, TIER, true, false);
            }
        }
        if (energy != CAPACITY) {
            stack.shrink(1);
        }
        return InteractionResultHolder.pass(stack);
    }
}
