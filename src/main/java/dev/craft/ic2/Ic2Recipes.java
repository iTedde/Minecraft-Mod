package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.recipe.GradualRepairRecipe;
import dev.craft.ic2.recipe.Ic2ShapedRecipe;
import dev.craft.ic2.recipe.Ic2ShapelessRecipe;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Recipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Craft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    public static final Supplier<RecipeSerializer<Ic2ShapedRecipe>> SHAPED =
            SERIALIZERS.register("ic2_shaped", Ic2ShapedRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<Ic2ShapelessRecipe>> SHAPELESS =
            SERIALIZERS.register("ic2_shapeless", Ic2ShapelessRecipe.Serializer::new);
    public static final Supplier<RecipeSerializer<GradualRepairRecipe>> GRADUAL_REPAIR =
            SERIALIZERS.register("gradual_repair", GradualRepairRecipe.Serializer::new);

    private static final Map<MachineRecipe.Kind, Supplier<RecipeType<MachineRecipe>>> MACHINE_TYPES =
            new EnumMap<>(MachineRecipe.Kind.class);

    static {
        for (MachineRecipe.Kind kind : MachineRecipe.Kind.values()) {
            Supplier<RecipeType<MachineRecipe>> type = TYPES.register(kind.getName(),
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID, kind.getName())));
            Supplier<RecipeSerializer<MachineRecipe>> serializer = SERIALIZERS.register(kind.getName(),
                    () -> new MachineRecipe.Serializer(kind));
            MACHINE_TYPES.put(kind, type);
            kind.bind(type, serializer);
        }
    }

    private Ic2Recipes() {
    }

    public static RecipeType<MachineRecipe> type(MachineRecipe.Kind kind) {
        return MACHINE_TYPES.get(kind).get();
    }
}
