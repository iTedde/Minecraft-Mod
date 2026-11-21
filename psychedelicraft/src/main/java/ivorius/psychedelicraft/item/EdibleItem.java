/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.item.component.ItemDrugs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EdibleItem extends Item {
    public static final FoodProperties NON_FILLING_EDIBLE = new FoodProperties.Builder().nutrition(0).saturationModifier(0.1F).alwaysEdible().build();
    public static final FoodProperties HASH_MUFFIN = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).alwaysEdible().build();
    public static final FoodProperties TOMATO = new FoodProperties.Builder().nutrition(1).saturationModifier(1.9F).alwaysEdible().build();

    public EdibleItem(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack copy = stack.copy();
        ItemStack remainder = super.finishUsingItem(stack, world, user);

        if (stack.get(DataComponents.FOOD) == null && (!(user instanceof Player) || ((Player)user).isCreative())) {
            remainder.shrink(1);
        }

        DrugProperties.of(user).ifPresent(drugProperties -> ItemDrugs.get(copy).applyTo(copy, drugProperties));
        return remainder;
    }
}
