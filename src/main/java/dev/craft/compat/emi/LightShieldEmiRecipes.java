package dev.craft.compat.emi;

import java.util.List;

import dev.craft.lightshield.LsItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;

/** Electric light shield (spec 30 §7): info page on energy, light, key and enchantments; the recipe EMI shows anyway. */
final class LightShieldEmiRecipes {
    private LightShieldEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(LsItems.ELECTRIC_LIGHT_SHIELD.get())),
                List.of(Component.translatable("emi.craft.info.electric_light_shield")),
                Ic2EmiRecipes.id("info/electric_light_shield")));
    }
}
