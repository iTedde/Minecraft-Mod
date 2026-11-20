package dev.craft.compat.emi;

import java.util.List;

import dev.craft.ic2.Ic2Items;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/**
 * Info pages for the Craft dimensions (spec 18 §4, spec 53 §5): the portal blocks have no item, so the pages sit on
 * the frame blocks and the lighter; the seed bag page points to the IC2 crops of the farming world.
 */
final class WorldEmiRecipes {
    private WorldEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Items.SMOOTH_STONE),
                EmiStack.of(Items.FLINT_AND_STEEL)),
                List.of(Component.translatable("emi.craft.info.mining_portal")),
                Ic2EmiRecipes.id("info/mining_portal")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Items.MOSSY_COBBLESTONE),
                EmiStack.of(Items.FLINT_AND_STEEL)),
                List.of(Component.translatable("emi.craft.info.farming_portal")),
                Ic2EmiRecipes.id("info/farming_portal")));
        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(Ic2Items.CROP_SEED_BAG.get())),
                List.of(Component.translatable("emi.craft.info.farming_seed_bag")),
                Ic2EmiRecipes.id("info/farming_seed_bag")));
    }
}
