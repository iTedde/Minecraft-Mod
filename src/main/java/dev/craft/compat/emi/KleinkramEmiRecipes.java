package dev.craft.compat.emi;

import java.util.List;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** Phase 50 small mods (spec 50 §5): info pages; recipes EMI shows anyway. */
final class KleinkramEmiRecipes {
    private KleinkramEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, dev.craft.omnitools.OmniTools.OMNIWRENCH.get(), "omniwrench");
        info(registry, dev.craft.obsidiplates.ObsidiPlates.OBSIDIAN_PRESSURE_PLATE.get(), "obsidian_pressure_plate");
        info(registry, dev.craft.omnitools.OmniTools.FORGE_LEXICON.get(), "forge_lexicon");
        info(registry, dev.craft.omnitools.OmniTools.MYSTICAL_PLINTH.get(), "mystical_plinth");
        info(registry, dev.craft.gravitygun.GravityGun.GRAVITY_GUN.get(), "gravity_gun");
        info(registry, dev.craft.gravitygun.GravityGun.CHARGED_GRAVITY_GUN.get(), "charged_gravity_gun");
    }

    static void info(EmiRegistry registry, ItemLike item, String key) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(item)),
                List.of(Component.translatable("emi.craft.info." + key)), Ic2EmiRecipes.id("info/" + key)));
    }
}
