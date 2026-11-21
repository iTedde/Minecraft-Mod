package dev.craft.compat.emi;

import dev.craft.coppergolem.CopperGolems;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestType;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/** Copper golem (spec 35 §3): info on building and behaviour, and on the copper chest's oxidation. */
final class CopperGolemEmiRecipes {
    private CopperGolemEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(CopperGolems.SPAWN_EGG.get()),
                EmiStack.of(Items.COPPER_BLOCK), EmiStack.of(Items.CARVED_PUMPKIN)),
                List.of(Component.translatable("emi.craft.info.copper_golem_build")),
                Ic2EmiRecipes.id("info/copper_golem_build")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(CopperGolems.SPAWN_EGG.get())),
                List.of(Component.translatable("emi.craft.info.copper_golem")),
                Ic2EmiRecipes.id("info/copper_golem")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(IronChestItems.chest(IronChestType.COPPER).get())),
                List.of(Component.translatable("emi.craft.info.copper_chest")),
                Ic2EmiRecipes.id("info/copper_chest")));
    }
}
