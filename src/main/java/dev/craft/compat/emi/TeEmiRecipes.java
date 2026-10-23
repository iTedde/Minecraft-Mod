package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.recipe.MachineRecipe;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.thermalexpansion.energy.MagmaticEngineBlockEntity;
import dev.craft.thermalexpansion.energy.SteamEngineBlockEntity;
import dev.craft.thermalexpansion.machine.GlacialPrecipitatorBlockEntity;
import dev.craft.thermalexpansion.machine.IgneousExtruderBlockEntity;
import dev.craft.thermalexpansion.machine.SelectionMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineType;
import dev.craft.thermalexpansion.recipe.TeFluidRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipes;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * EMI for Thermal Expansion (spec 26): the machine recipe lists including the GregTech macerator fall-back of the
 * pulverizer and sawmill, the selection machines and the engine fuels. The powered furnace also smelts every furnace
 * recipe; it is a workstation of the vanilla category.
 */
final class TeEmiRecipes {
    private static final int BUCKET = 1000;

    static final EmiRecipeCategory FURNACE = category(RecipeCategories.TE_FURNACE, TeItems.POWERED_FURNACE.get());
    static final EmiRecipeCategory PULVERIZER = category(RecipeCategories.TE_PULVERIZER, TeItems.PULVERIZER.get());
    static final EmiRecipeCategory SAWMILL = category(RecipeCategories.TE_SAWMILL, TeItems.SAWMILL.get());
    static final EmiRecipeCategory SMELTER = category(RecipeCategories.TE_SMELTER, TeItems.INDUCTION_SMELTER.get());
    static final EmiRecipeCategory CRUCIBLE = category(RecipeCategories.TE_CRUCIBLE, TeItems.MAGMA_CRUCIBLE.get());
    static final EmiRecipeCategory TRANSPOSER_FILL =
            category(RecipeCategories.TE_TRANSPOSER_FILL, TeItems.LIQUID_TRANSPOSER.get());
    static final EmiRecipeCategory TRANSPOSER_EXTRACT =
            category(RecipeCategories.TE_TRANSPOSER_EXTRACT, TeItems.LIQUID_TRANSPOSER.get());
    static final EmiRecipeCategory GLACIAL_PRECIPITATOR =
            category(RecipeCategories.TE_GLACIAL_PRECIPITATOR, TeItems.GLACIAL_PRECIPITATOR.get());
    static final EmiRecipeCategory IGNEOUS_EXTRUDER =
            category(RecipeCategories.TE_IGNEOUS_EXTRUDER, TeItems.IGNEOUS_EXTRUDER.get());
    static final EmiRecipeCategory STEAM_ENGINE = category(RecipeCategories.TE_STEAM_ENGINE, TeItems.STEAM_ENGINE.get());
    static final EmiRecipeCategory MAGMATIC_ENGINE =
            category(RecipeCategories.TE_MAGMATIC_ENGINE, TeItems.MAGMATIC_ENGINE.get());

    private TeEmiRecipes() {
    }

    private static EmiRecipeCategory category(ResourceLocation id, ItemLike icon) {
        return new EmiRecipeCategory(id, EmiStack.of(icon));
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (EmiRecipeCategory category : List.of(FURNACE, PULVERIZER, SAWMILL, SMELTER, CRUCIBLE, TRANSPOSER_FILL,
                TRANSPOSER_EXTRACT, GLACIAL_PRECIPITATOR, IGNEOUS_EXTRUDER, STEAM_ENGINE, MAGMATIC_ENGINE)) {
            registry.addCategory(category);
            categories.put(category.getId(), category);
            registry.addWorkstation(category, (EmiStack) category.icon);
        }
        registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(TeItems.POWERED_FURNACE.get()));

        machineRecipes(registry, TeRecipe.Kind.FURNACE, FURNACE, TeMachineType.POWERED_FURNACE);
        machineRecipes(registry, TeRecipe.Kind.PULVERIZER, PULVERIZER, TeMachineType.PULVERIZER);
        machineRecipes(registry, TeRecipe.Kind.SAWMILL, SAWMILL, TeMachineType.SAWMILL);
        machineRecipes(registry, TeRecipe.Kind.SMELTER, SMELTER, TeMachineType.INDUCTION_SMELTER);
        maceratorFallback(registry);
        fluidRecipes(registry);
        selectionMachines(registry);
        engines(registry);

        registry.addRecipe(new EmiInfoRecipe(List.of(EmiStack.of(TeItems.POWERED_FURNACE.get()),
                EmiStack.of(TeItems.PULVERIZER.get()), EmiStack.of(TeItems.SAWMILL.get())),
                List.of(Component.translatable("emi.info.craft.te_fallback")), Ic2EmiRecipes.id("info/te_fallback")));
    }

    /** "x MJ · y s at z MJ/t": a machine runs at most at its maximum power. */
    private static List<Component> energy(int energy, TeMachineType type) {
        return List.of(Component.translatable("emi.craft.te.energy", EmiText.number(energy),
                EmiText.seconds(ticks(energy, type)), type.getMaxPower()));
    }

    private static int ticks(int energy, TeMachineType type) {
        return (energy + type.getMaxPower() - 1) / type.getMaxPower();
    }

    private static EmiIngredient sized(SizedIngredient input) {
        return EmiIngredient.of(input.ingredient(), input.count());
    }

    private static EmiStack chanced(ItemStack stack, int chance) {
        EmiStack emi = EmiStack.of(stack);
        return chance < 100 ? emi.setChance(chance / 100.0F) : emi;
    }

    private static EmiStack fluid(FluidStack stack) {
        return EmiStack.of(stack.getFluid(), stack.getAmount());
    }

    private static void machineRecipes(EmiRegistry registry, TeRecipe.Kind kind, EmiRecipeCategory category,
                                       TeMachineType type) {
        for (RecipeHolder<TeRecipe> holder : registry.getRecipeManager().getAllRecipesFor(kind.type())) {
            TeRecipe recipe = holder.value();
            List<EmiIngredient> inputs = recipe.getInputs().stream().map(TeEmiRecipes::sized).toList();
            List<EmiStack> outputs = new ArrayList<>();
            outputs.add(EmiStack.of(recipe.getPrimary()));
            if (!recipe.getSecondary().isEmpty()) {
                outputs.add(chanced(recipe.getSecondary(), recipe.getChance()));
            }
            registry.addRecipe(new CraftEmiRecipe(category, holder.id(), inputs, outputs,
                    ticks(recipe.getEnergy(), type), energy(recipe.getEnergy(), type)));
        }
    }

    /**
     * GregTech's {@code addPulverisationRecipe}: every macerator recipe without an own TE recipe – (tiny) wood pulp results in
     * the sawmill (80 MJ), everything else in the pulverizer (400 MJ).
     */
    private static void maceratorFallback(EmiRegistry registry) {
        for (RecipeHolder<MachineRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(MachineRecipe.Kind.MACERATOR.type())) {
            MachineRecipe recipe = holder.value();
            ItemStack[] items = recipe.getIngredient().getItems();
            if (items.length == 0) {
                continue;
            }
            boolean wood = TeRecipes.isWoodPulp(recipe.getResult());
            TeRecipe.Kind kind = wood ? TeRecipe.Kind.SAWMILL : TeRecipe.Kind.PULVERIZER;
            if (hasOwnRecipe(registry, kind, items[0])) {
                continue;
            }
            TeMachineType type = wood ? TeMachineType.SAWMILL : TeMachineType.PULVERIZER;
            int energy = wood ? TeRecipes.GT_SAWMILL_ENERGY : TeRecipes.GT_PULVERIZER_ENERGY;
            registry.addRecipe(new CraftEmiRecipe(wood ? SAWMILL : PULVERIZER,
                    Ic2EmiRecipes.id(kind.getName() + "/macerator/" + holder.id().getNamespace() + "/"
                            + holder.id().getPath()),
                    List.of(EmiIngredient.of(recipe.getIngredient(), recipe.getCount())),
                    List.of(EmiStack.of(recipe.getResult())), ticks(energy, type), energy(energy, type)));
        }
    }

    private static boolean hasOwnRecipe(EmiRegistry registry, TeRecipe.Kind kind, ItemStack stack) {
        for (RecipeHolder<TeRecipe> holder : registry.getRecipeManager().getAllRecipesFor(kind.type())) {
            if (holder.value().matchesItem(stack)) {
                return true;
            }
        }
        return false;
    }

    private static void fluidRecipes(EmiRegistry registry) {
        for (RecipeHolder<TeFluidRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(TeFluidRecipe.Kind.CRUCIBLE.type())) {
            TeFluidRecipe recipe = holder.value();
            registry.addRecipe(new CraftEmiRecipe(CRUCIBLE, holder.id(), List.of(sized(recipe.getInput())),
                    List.of(fluid(recipe.getFluid())), ticks(recipe.getEnergy(), TeMachineType.MAGMA_CRUCIBLE),
                    energy(recipe.getEnergy(), TeMachineType.MAGMA_CRUCIBLE)));
        }
        for (RecipeHolder<TeFluidRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(TeFluidRecipe.Kind.FILL.type())) {
            TeFluidRecipe recipe = holder.value();
            registry.addRecipe(new CraftEmiRecipe(TRANSPOSER_FILL, holder.id(),
                    List.of(sized(recipe.getInput()), fluid(recipe.getFluid())),
                    List.of(chanced(recipe.getOutput(), recipe.getChance())),
                    ticks(recipe.getEnergy(), TeMachineType.LIQUID_TRANSPOSER),
                    energy(recipe.getEnergy(), TeMachineType.LIQUID_TRANSPOSER)));
        }
        for (RecipeHolder<TeFluidRecipe> holder
                : registry.getRecipeManager().getAllRecipesFor(TeFluidRecipe.Kind.EXTRACT.type())) {
            TeFluidRecipe recipe = holder.value();
            List<EmiStack> outputs = new ArrayList<>();
            outputs.add(fluid(recipe.getFluid()));
            if (!recipe.getOutput().isEmpty()) {
                outputs.add(chanced(recipe.getOutput(), recipe.getChance()));
            }
            registry.addRecipe(new CraftEmiRecipe(TRANSPOSER_EXTRACT, holder.id(), List.of(sized(recipe.getInput())),
                    outputs, ticks(recipe.getEnergy(), TeMachineType.LIQUID_TRANSPOSER),
                    energy(recipe.getEnergy(), TeMachineType.LIQUID_TRANSPOSER)));
        }
    }

    private static void selectionMachines(EmiRegistry registry) {
        List<ItemStack> ice = SelectionMachineBlockEntity.products(TeMachineType.GLACIAL_PRECIPITATOR);
        for (int i = 0; i < ice.size(); i++) {
            int energy = GlacialPrecipitatorBlockEntity.processEnergy(i);
            registry.addRecipe(new CraftEmiRecipe(GLACIAL_PRECIPITATOR,
                    Ic2EmiRecipes.id("glacial_precipitator/" + i),
                    List.of(EmiStack.of(Fluids.WATER, GlacialPrecipitatorBlockEntity.processWater(i))),
                    List.of(EmiStack.of(ice.get(i))), ticks(energy, TeMachineType.GLACIAL_PRECIPITATOR),
                    energy(energy, TeMachineType.GLACIAL_PRECIPITATOR)));
        }
        List<ItemStack> rock = SelectionMachineBlockEntity.products(TeMachineType.IGNEOUS_EXTRUDER);
        for (int i = 0; i < rock.size(); i++) {
            List<EmiIngredient> inputs = new ArrayList<>();
            if (IgneousExtruderBlockEntity.processLava(i) > 0) {
                inputs.add(EmiStack.of(Fluids.LAVA, IgneousExtruderBlockEntity.processLava(i)));
            }
            if (IgneousExtruderBlockEntity.processWater(i) > 0) {
                inputs.add(EmiStack.of(Fluids.WATER, IgneousExtruderBlockEntity.processWater(i)));
            }
            int ticks = IgneousExtruderBlockEntity.processTime(i);
            registry.addRecipe(new CraftEmiRecipe(IGNEOUS_EXTRUDER, Ic2EmiRecipes.id("igneous_extruder/" + i),
                    inputs, List.of(EmiStack.of(rock.get(i))), ticks, List.of(
                    Component.translatable("emi.craft.te_igneous_extruder.time", EmiText.seconds(ticks)),
                    Component.translatable("emi.craft.te_igneous_extruder.start",
                            IgneousExtruderBlockEntity.START_LIQUID))));
        }
    }

    /** Steam engine fuels grouped by their MJ value (highest first), the magmatic engine's lava. */
    private static void engines(EmiRegistry registry) {
        Map<Integer, List<EmiStack>> fuels = new TreeMap<>((a, b) -> Integer.compare(b, a));
        for (Item item : BuiltInRegistries.ITEM) {
            int value = SteamEngineBlockEntity.getItemEnergyValue(new ItemStack(item));
            if (value > 0) {
                fuels.computeIfAbsent(value, v -> new ArrayList<>()).add(EmiStack.of(item));
            }
        }
        fuels.forEach((value, items) -> registry.addRecipe(new CraftEmiRecipe(STEAM_ENGINE,
                Ic2EmiRecipes.id("te_steam_engine/" + value), List.of(EmiIngredient.of(items)), List.of(), 0, List.of(
                Component.translatable("emi.craft.te_steam_engine.fuel", EmiText.number(value)),
                Component.translatable("emi.craft.te_steam_engine.water")))));
        int perBucket = MagmaticEngineBlockEntity.ENERGY_PER_PROCESS * BUCKET / MagmaticEngineBlockEntity.LAVA_PER_PROCESS;
        registry.addRecipe(new CraftEmiRecipe(MAGMATIC_ENGINE, Ic2EmiRecipes.id("te_magmatic_engine/lava"),
                List.of(EmiStack.of(Fluids.LAVA, BUCKET)), List.of(), 0, List.of(
                Component.translatable("emi.craft.te_magmatic_engine.lava", EmiText.number(perBucket),
                        MagmaticEngineBlockEntity.ENERGY_PER_PROCESS, MagmaticEngineBlockEntity.LAVA_PER_PROCESS))));
    }
}
