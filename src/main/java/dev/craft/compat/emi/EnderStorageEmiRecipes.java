package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;

import dev.craft.enderstorage.EsDataComponents;
import dev.craft.enderstorage.EsItems;
import dev.craft.enderstorage.Frequency;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * EnderStorage (spec 27 §6.2): the special recolour recipe as examples (EMI does not list custom recipes), and info
 * pages on colour buttons, diamond latch and pressure valve.
 */
final class EnderStorageEmiRecipes {
    private EnderStorageEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        recolour(registry, EsItems.ENDER_CHEST.get(), "ender_chest");
        recolour(registry, EsItems.ENDER_TANK.get(), "ender_tank");
        info(registry, "ender_chest", EsItems.ENDER_CHEST.get());
        info(registry, "ender_tank", EsItems.ENDER_TANK.get());
        info(registry, "ender_pouch", EsItems.ENDER_POUCH.get());
    }

    /** Red, lime and blue dye above a white chest or tank: red/lime/blue, owner kept. */
    private static void recolour(EmiRegistry registry, Item item, String name) {
        List<EmiIngredient> grid = new ArrayList<>(List.of(EmiStack.of(Items.RED_DYE), EmiStack.of(Items.LIME_DYE),
                EmiStack.of(Items.BLUE_DYE), EmiStack.EMPTY, EmiStack.of(item), EmiStack.EMPTY));
        ItemStack result = new ItemStack(item);
        result.set(EsDataComponents.FREQUENCY.get(), Frequency.of(14, 5, 11));
        registry.addRecipe(new EmiCraftingRecipe(grid, EmiStack.of(result),
                Ic2EmiRecipes.id("crafting/" + name + "_recolour"), false));
    }

    private static void info(EmiRegistry registry, String name, Item item) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(item)),
                List.of(Component.translatable("emi.craft.info." + name)), Ic2EmiRecipes.id("info/" + name)));
    }
}
