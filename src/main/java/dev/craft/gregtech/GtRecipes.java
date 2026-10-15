package dev.craft.gregtech;

import dev.craft.Craft;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
            Craft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    static {
        for (GtRecipe.Kind kind : GtRecipe.Kind.values()) {
            String name = "gt_" + kind.getName();
            kind.bind(TYPES.register(name, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                    name))), SERIALIZERS.register(name, () -> new GtRecipe.Serializer(kind)));
        }
    }

    private GtRecipes() {
    }
}
