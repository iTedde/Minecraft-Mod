package dev.craft.buildcraft.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * BuildCraft {@code bucketFuel}: a plain item registered as 1000 mB fuel container. Fuel has no block, so the bucket
 * cannot be emptied into the world; it only fills tanks.
 */
public class FuelBucketItem extends BucketItem {
    public FuelBucketItem(Fluid fuel, Properties properties) {
        super(fuel, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
