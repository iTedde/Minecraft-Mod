package dev.craft.core;

import dev.craft.Craft;
import dev.craft.core.client.RecipeCategories;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Recipe viewer coverage (spec 16 §6): every recipe type of the mod has a category and recipes. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class RecipeCategoryGameTests {
    private static final String EMPTY = "empty_16x8x16";

    private RecipeCategoryGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void everyRecipeTypeHasCategory(GameTestHelper helper) {
        int types = 0;
        for (RecipeType<?> type : BuiltInRegistries.RECIPE_TYPE) {
            ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(type);
            if (!Craft.MODID.equals(id.getNamespace())) {
                continue;
            }
            types++;
            helper.assertTrue(RecipeCategories.BY_RECIPE_TYPE.containsKey(id.getPath()),
                    "no recipe viewer category for " + id);
            helper.assertFalse(helper.getLevel().getRecipeManager().getAllRecipesFor(cast(type)).isEmpty(),
                    "no recipes of type " + id);
        }
        helper.assertValueEqual(types, RecipeCategories.BY_RECIPE_TYPE.size(), "mapped recipe types");
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static <T extends RecipeInput, R extends Recipe<T>> RecipeType<R> cast(RecipeType<?> type) {
        return (RecipeType<R>) type;
    }
}
