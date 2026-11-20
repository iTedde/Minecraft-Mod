package ivorius.psychedelicraft.mixin.client;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.recipe.MultiResultRecipe;

@Mixin(ClientRecipeBook.class)
abstract class MixinClientRecipeBook {
    @ModifyVariable(method = "categorizeAndGroupRecipes", at = @At("HEAD"), argsOnly = true)
    private static Iterable<RecipeHolder<?>> explodeRecipes(Iterable<RecipeHolder<?>> recipes) {
        return StreamSupport.stream(recipes.spliterator(), false).flatMap(entry -> {
            if (entry.value() instanceof MultiResultRecipe<?> multi) {
                return multi.flatten().map(i -> new RecipeHolder<>(entry.id(), i));
            }
            return Stream.of(entry);
        }).toList();
    }
}

@Mixin(RecipeBookComponent.class)
abstract class MixinRecipeBookWidget {
    @Shadow
    private @Final RecipeBookPage recipeBookPage;

    @ModifyVariable(method = "setupGhostRecipe", at = @At("HEAD"), argsOnly = true)
    private RecipeHolder<?> explodeRecipe(RecipeHolder<?> recipe) {
        var lastClicked = recipeBookPage.getLastClickedRecipe();
        if (lastClicked != null
                && lastClicked.id().equals(recipe.id())
                && recipe.value() instanceof MultiResultRecipe<?>
                && lastClicked.value() instanceof MultiResultRecipe<?>) {
            return lastClicked;
        }
        return recipe;
    }
}