package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/** EMI categories of the GregTech machines (spec 16 §2.1): one per {@link GtRecipe.Kind}. */
final class GtEmiRecipes {
    /** Fluid per holo item / water cell taken from a machine tank. */
    private static final int MB_PER_ITEM = 1000;

    private static final Map<GtRecipe.Kind, EmiRecipeCategory> CATEGORIES = new EnumMap<>(GtRecipe.Kind.class);
    private static final Map<GtRecipe.Kind, GtMachine> MACHINES = new EnumMap<>(GtRecipe.Kind.class);

    static {
        category(GtRecipe.Kind.BENDER, RecipeCategories.GT_BENDER, GtMachine.PLATE_BENDING_MACHINE);
        category(GtRecipe.Kind.ELECTROLYZER, RecipeCategories.GT_ELECTROLYZER, GtMachine.INDUSTRIAL_ELECTROLYZER);
        category(GtRecipe.Kind.BLAST_FURNACE, RecipeCategories.GT_BLAST_FURNACE, GtMachine.INDUSTRIAL_BLAST_FURNACE);
        category(GtRecipe.Kind.ASSEMBLER, RecipeCategories.GT_ASSEMBLER, GtMachine.ASSEMBLING_MACHINE);
        category(GtRecipe.Kind.CANNER, RecipeCategories.GT_CANNER, GtMachine.AUTOMATIC_CANNING_MACHINE);
        category(GtRecipe.Kind.IMPLOSION, RecipeCategories.GT_IMPLOSION, GtMachine.IMPLOSION_COMPRESSOR);
        category(GtRecipe.Kind.CENTRIFUGE, RecipeCategories.GT_CENTRIFUGE, GtMachine.INDUSTRIAL_CENTRIFUGE);
        category(GtRecipe.Kind.GRINDER, RecipeCategories.GT_GRINDER, GtMachine.INDUSTRIAL_GRINDER);
        category(GtRecipe.Kind.VACUUM_FREEZER, RecipeCategories.GT_VACUUM_FREEZER, GtMachine.VACUUM_FREEZER);
    }

    private GtEmiRecipes() {
    }

    private static void category(GtRecipe.Kind kind, ResourceLocation id, GtMachine machine) {
        CATEGORIES.put(kind, new EmiRecipeCategory(id, EmiStack.of(GtItems.machine(machine).get())));
        MACHINES.put(kind, machine);
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (GtRecipe.Kind kind : GtRecipe.Kind.values()) {
            EmiRecipeCategory category = CATEGORIES.get(kind);
            registry.addCategory(category);
            registry.addWorkstation(category, EmiStack.of(GtItems.machine(MACHINES.get(kind)).get()));
            categories.put(category.getId(), category);
            for (RecipeHolder<GtRecipe> holder : registry.getRecipeManager().getAllRecipesFor(kind.type())) {
                GtRecipe recipe = holder.value();
                List<EmiIngredient> inputs = new ArrayList<>();
                for (SizedIngredient input : recipe.getInputs()) {
                    inputs.add(input(kind, input));
                }
                List<EmiStack> outputs = new ArrayList<>();
                for (ItemStack output : recipe.getOutputs()) {
                    if (!output.isEmpty()) {
                        outputs.add(EmiStack.of(output));
                    }
                }
                registry.addRecipe(new CraftEmiRecipe(category, holder.id(), inputs, outputs, recipe.getDuration(),
                        lines(kind, recipe)));
            }
        }
    }

    /**
     * Tank contents appear as holo items ({@code craft:water}/{@code craft:lava}, 1000 mB each) and are shown as
     * fluid; the grinder also takes its water cells from the tank if no recipe matches slot 1.
     */
    private static EmiIngredient input(GtRecipe.Kind kind, SizedIngredient input) {
        if (input.test(new ItemStack(GtItems.WATER.get()))) {
            return EmiStack.of(Fluids.WATER, (long) input.count() * MB_PER_ITEM);
        }
        if (input.test(new ItemStack(GtItems.LAVA.get()))) {
            return EmiStack.of(Fluids.LAVA, (long) input.count() * MB_PER_ITEM);
        }
        EmiIngredient ingredient = EmiIngredient.of(input.ingredient(), input.count());
        if (kind == GtRecipe.Kind.GRINDER && input.test(new ItemStack(Ic2Items.WATER_CELL.get()))) {
            return EmiIngredient.of(List.of(ingredient, EmiStack.of(Fluids.WATER, (long) input.count() * MB_PER_ITEM)));
        }
        return ingredient;
    }

    private static List<Component> lines(GtRecipe.Kind kind, GtRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        lines.add(Ic2EmiRecipes.energy(recipe.getEuPerTick(), recipe.getDuration()));
        if (kind == GtRecipe.Kind.BLAST_FURNACE) {
            lines.add(Component.translatable("emi.craft.gt.heat", EmiText.number(recipe.getSpecial())));
        }
        if (kind.isShapeless() && recipe.getInputs().size() > 1) {
            lines.add(Component.translatable("emi.craft.gt.shapeless"));
        }
        return lines;
    }
}
