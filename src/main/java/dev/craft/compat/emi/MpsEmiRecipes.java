package dev.craft.compat.emi;

import java.util.Arrays;
import java.util.List;

import dev.craft.mps.MpsItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** Modular Powersuits (spec 29): info pages on the Tinker Table, energy, keys and modes; recipes EMI shows anyway. */
final class MpsEmiRecipes {
    private MpsEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, "tinker_table", MpsItems.TINKER_TABLE.get());
        info(registry, "power_armor", MpsItems.POWER_ARMOR_HEAD.get(), MpsItems.POWER_ARMOR_TORSO.get(),
                MpsItems.POWER_ARMOR_LEGS.get(), MpsItems.POWER_ARMOR_FEET.get());
        info(registry, "power_tool", MpsItems.POWER_TOOL.get());
        info(registry, "mps_unused_component", MpsItems.CARBON_MYOFIBER.get(), MpsItems.MYOFIBER_GEL.get());
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList();
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
