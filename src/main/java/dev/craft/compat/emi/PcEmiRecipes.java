package dev.craft.compat.emi;

import java.util.List;

import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;

/** FE producer (spec 33 §5): info page on the setup and the EU → FE rate; the recipe EMI shows anyway. */
final class PcEmiRecipes {
    private PcEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(PcItems.converter(ConverterType.FE_PRODUCER).get())),
                List.of(Component.translatable("emi.craft.info.fe_producer")),
                Ic2EmiRecipes.id("info/fe_producer")));
    }
}
