package dev.craft.compat.emi;

import java.util.Arrays;
import java.util.List;

import dev.craft.gravisuite.GsItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** GraviSuite (spec 28): info pages on keys, energy use and the modes; the recipes EMI shows anyway. */
final class GraviSuiteEmiRecipes {
    private GraviSuiteEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, "gravi_chestplate", GsItems.GRAVI_CHESTPLATE.get());
        info(registry, "advanced_jetpack", GsItems.ADVANCED_JETPACK.get(), GsItems.ADVANCED_NANO_CHESTPLATE.get());
        info(registry, "gravisuite_lappack", GsItems.ADVANCED_LAPPACK.get(), GsItems.ULTIMATE_LAPPACK.get());
        info(registry, "ultimate_solar_helmet", GsItems.ULTIMATE_SOLAR_HELMET.get());
        info(registry, "vajra", GsItems.VAJRA.get());
        info(registry, "gravitool", GsItems.GRAVITOOL.get());
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList();
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
