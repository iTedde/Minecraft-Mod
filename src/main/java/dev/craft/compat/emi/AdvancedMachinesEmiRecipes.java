package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.craft.advancedmachines.AdvancedMachineBlockEntity;
import dev.craft.advancedmachines.AdvancedMachinesItems;
import dev.craft.advancedmachines.RotaryMaceratorBlockEntity;
import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.recipe.MachineRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** EMI for Advanced Machines (spec 25 §7): workstations, the rotary macerator's supplements and an info page. */
final class AdvancedMachinesEmiRecipes {
    static final EmiRecipeCategory SUPPLEMENT = new EmiRecipeCategory(RecipeCategories.ROTARY_MACERATOR_SUPPLEMENT,
            EmiStack.of(Ic2Items.machine(Ic2Machine.ROTARY_MACERATOR).get()));

    private AdvancedMachinesEmiRecipes() {
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        registry.addCategory(SUPPLEMENT);
        categories.put(SUPPLEMENT.getId(), SUPPLEMENT);
        workstation(registry, Ic2EmiRecipes.MACERATOR, Ic2Machine.ROTARY_MACERATOR);
        workstation(registry, Ic2EmiRecipes.COMPRESSOR, Ic2Machine.SINGULARITY_COMPRESSOR);
        workstation(registry, Ic2EmiRecipes.EXTRACTOR, Ic2Machine.CENTRIFUGE_EXTRACTOR);
        workstation(registry, SUPPLEMENT, Ic2Machine.ROTARY_MACERATOR);

        supplement(registry, "refined_iron_dust", maceratedInto(registry, Ic2Items.IRON_DUST.get()),
                EmiIngredient.of(Ingredient.of(Items.COAL, Items.CHARCOAL)),
                EmiStack.of(AdvancedMachinesItems.REFINED_IRON_DUST.get()), RotaryMaceratorBlockEntity.ITEMS_PER_COAL);
        supplement(registry, "bronze_dust", maceratedInto(registry, Ic2Items.COPPER_DUST.get()),
                maceratedInto(registry, Ic2Items.TIN_DUST.get()), EmiStack.of(Ic2Items.BRONZE_DUST.get()),
                RotaryMaceratorBlockEntity.ITEMS_PER_TIN);
        supplement(registry, "hydrated_coal_dust", maceratedInto(registry, Ic2Items.COAL_DUST.get()),
                EmiStack.of(Ic2Items.WATER_CELL.get()), EmiStack.of(Ic2Items.HYDRATED_COAL_DUST.get()),
                RotaryMaceratorBlockEntity.ITEMS_PER_WATER_CELL);

        List<EmiIngredient> machines = new ArrayList<>();
        for (Ic2Machine machine : List.of(Ic2Machine.ROTARY_MACERATOR, Ic2Machine.SINGULARITY_COMPRESSOR,
                Ic2Machine.CENTRIFUGE_EXTRACTOR)) {
            machines.add(EmiStack.of(Ic2Items.machine(machine).get()));
        }
        registry.addRecipe(new EmiInfoRecipe(machines,
                List.of(Component.translatable("emi.info.craft.advanced_machine")),
                Ic2EmiRecipes.id("info/advanced_machine")));
    }

    /** All macerator inputs whose result is {@code dust} (the supplement rules look at the result, not the input). */
    private static EmiIngredient maceratedInto(EmiRegistry registry, Item dust) {
        List<EmiIngredient> inputs = new ArrayList<>();
        for (RecipeHolder<MachineRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(MachineRecipe.Kind.MACERATOR.type())) {
            ItemStack result = holder.value().getResult();
            if (result.is(dust)) {
                inputs.add(EmiIngredient.of(holder.value().getIngredient()));
            }
        }
        return EmiIngredient.of(inputs);
    }

    private static void supplement(EmiRegistry registry, String name, EmiIngredient input, EmiIngredient supplement,
                                   EmiStack output, int uses) {
        int ticks = AdvancedMachineBlockEntity.MAX_PROGRESS / (AdvancedMachineBlockEntity.BASE_MAX_SPEED / 30) + 1;
        registry.addRecipe(new CraftEmiRecipe(SUPPLEMENT, Ic2EmiRecipes.id("rotary_macerator/" + name),
                List.of(input, supplement), List.of(output), ticks,
                List.of(Component.translatable("emi.craft.rotary_macerator_supplement.uses", uses))));
    }

    private static void workstation(EmiRegistry registry, EmiRecipeCategory category, Ic2Machine machine) {
        registry.addWorkstation(category, EmiStack.of(Ic2Items.machine(machine).get()));
    }
}
