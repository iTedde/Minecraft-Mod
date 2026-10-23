package dev.craft.thermalexpansion.recipe;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

/**
 * Recipe lookup of the Thermal Expansion machines with the pack's fall-backs (spec 26 §4):
 * <ul>
 * <li>Powered Furnace: own recipes, otherwise every furnace recipe at 160 MJ ({@code FurnaceManager.getOutput}).</li>
 * <li>Pulverizer: own recipes, otherwise every IC2 macerator recipe at 400 MJ – GregTech's
 * {@code addPulverisationRecipe} puts each of its macerator recipes into the pulverizer as well.</li>
 * <li>Sawmill: own recipes, otherwise macerator recipes giving wood pulp or tiny wood pulp at 80 MJ (GT's branch
 * for wood).</li>
 * <li>Induction Smelter, Magma Crucible, Liquid Transposer: own recipes only.</li>
 * </ul>
 */
public final class TeRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE,
            Craft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    /** {@code TileFurnace.DEFAULT_PROCESS_ENERGY}. */
    public static final int FURNACE_DEFAULT_ENERGY = 160;
    /** {@code GT_ModHandler.addPulverisationRecipe}: pulverizer 400 MJ, sawmill 80 MJ. */
    public static final int GT_PULVERIZER_ENERGY = 400;
    public static final int GT_SAWMILL_ENERGY = 80;

    static {
        for (TeRecipe.Kind kind : TeRecipe.Kind.values()) {
            String name = "te_" + kind.getName();
            kind.bind(TYPES.register(name, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                    name))), SERIALIZERS.register(name, () -> new TeRecipe.Serializer(kind)));
        }
        for (TeFluidRecipe.Kind kind : TeFluidRecipe.Kind.values()) {
            String name = "te_" + kind.getName();
            kind.bind(TYPES.register(name, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                    name))), SERIALIZERS.register(name, () -> new TeFluidRecipe.Serializer(kind)));
        }
    }

    private TeRecipes() {
    }

    /** The recipe of {@code kind} for {@code stack} (item match only), including the fall-backs, or {@code null}. */
    @Nullable
    public static TeRecipe find(Level level, TeRecipe.Kind kind, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (RecipeHolder<TeRecipe> holder : level.getRecipeManager().getAllRecipesFor(kind.type())) {
            if (holder.value().matchesItem(stack)) {
                return holder.value();
            }
        }
        return switch (kind) {
            case FURNACE -> furnaceFallback(level, stack);
            case PULVERIZER -> maceratorFallback(level, stack, false);
            case SAWMILL -> maceratorFallback(level, stack, true);
            case SMELTER -> null;
        };
    }

    /** {@code SmelterManager.getRecipe}: both inputs by item, in either order. */
    @Nullable
    public static TeRecipe findSmelter(Level level, ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return null;
        }
        for (RecipeHolder<TeRecipe> holder : level.getRecipeManager().getAllRecipesFor(TeRecipe.Kind.SMELTER.type())) {
            List<SizedIngredient> inputs = holder.value().getInputs();
            if (inputs.size() == 2 && (inputs.get(0).ingredient().test(first) && inputs.get(1).ingredient().test(second)
                    || inputs.get(0).ingredient().test(second) && inputs.get(1).ingredient().test(first))) {
                return holder.value();
            }
        }
        return null;
    }

    /** {@code SmelterManager.isItemValid}: the item is part of any alloy recipe. */
    public static boolean isSmelterInput(Level level, ItemStack stack) {
        for (RecipeHolder<TeRecipe> holder : level.getRecipeManager().getAllRecipesFor(TeRecipe.Kind.SMELTER.type())) {
            for (SizedIngredient input : holder.value().getInputs()) {
                if (input.ingredient().test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Crucible or extraction recipe of {@code stack} (item match only), or {@code null}. */
    @Nullable
    public static TeFluidRecipe find(Level level, TeFluidRecipe.Kind kind, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (RecipeHolder<TeFluidRecipe> holder : level.getRecipeManager().getAllRecipesFor(kind.type())) {
            if (holder.value().matchesItem(stack)) {
                return holder.value();
            }
        }
        return null;
    }

    /** {@code TransposerManager.getFillRecipe}: by item and liquid type. */
    @Nullable
    public static TeFluidRecipe findFill(Level level, ItemStack stack, FluidStack fluid) {
        if (stack.isEmpty() || fluid.isEmpty()) {
            return null;
        }
        for (RecipeHolder<TeFluidRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(TeFluidRecipe.Kind.FILL.type())) {
            if (holder.value().matchesItem(stack) && FluidStack.isSameFluidSameComponents(holder.value().getFluid(),
                    fluid)) {
                return holder.value();
            }
        }
        return null;
    }

    /** {@code TransposerManager.isItemValid}: input of any fill or extraction recipe. */
    public static boolean isTransposerInput(Level level, ItemStack stack) {
        for (TeFluidRecipe.Kind kind : new TeFluidRecipe.Kind[]{TeFluidRecipe.Kind.FILL, TeFluidRecipe.Kind.EXTRACT}) {
            if (find(level, kind, stack) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static TeRecipe furnaceFallback(Level level, ItemStack stack) {
        Optional<RecipeHolder<SmeltingRecipe>> smelting = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
        if (smelting.isEmpty()) {
            return null;
        }
        ItemStack result = smelting.get().value().getResultItem(level.registryAccess());
        return new TeRecipe(TeRecipe.Kind.FURNACE, List.of(new SizedIngredient(
                smelting.get().value().getIngredients().get(0), 1)), result, ItemStack.EMPTY, 0, FURNACE_DEFAULT_ENERGY);
    }

    /** {@code addPulverisationRecipe} sends wood pulp and tiny wood pulp results to the sawmill. */
    public static boolean isWoodPulp(ItemStack result) {
        return result.is(GtItems.WOOD_PULP.get()) || result.is(GtItems.SMALL_WOOD_PULP.get());
    }

    /** GregTech routes wood-pulp results to the sawmill and everything else to the pulverizer. */
    @Nullable
    private static TeRecipe maceratorFallback(Level level, ItemStack stack, boolean wood) {
        Optional<RecipeHolder<MachineRecipe>> macerator = level.getRecipeManager()
                .getRecipeFor(MachineRecipe.Kind.MACERATOR.type(), new SingleRecipeInput(stack), level);
        if (macerator.isEmpty()) {
            return null;
        }
        MachineRecipe recipe = macerator.get().value();
        if (isWoodPulp(recipe.getResult()) != wood) {
            return null;
        }
        return new TeRecipe(wood ? TeRecipe.Kind.SAWMILL : TeRecipe.Kind.PULVERIZER,
                List.of(new SizedIngredient(recipe.getIngredient(), recipe.getCount())), recipe.getResult().copy(),
                ItemStack.EMPTY, 0, wood ? GT_SAWMILL_ENERGY : GT_PULVERIZER_ENERGY);
    }
}
