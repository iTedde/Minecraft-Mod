package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.FuelValues;
import dev.craft.ic2.generator.GeneratorBlockEntity;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.item.FilledFuelCanItem;
import dev.craft.ic2.item.FilledTinCanItem;
import dev.craft.ic2.item.ScrapBoxDrops;
import dev.craft.ic2.machine.CanningMachineBlockEntity;
import dev.craft.ic2.machine.ElectrolyzerBlockEntity;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import dev.craft.ic2.machine.RecipeMachineBlockEntity;
import dev.craft.ic2.machine.RecyclerBlockEntity;
import dev.craft.ic2.recipe.MachineRecipe;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

/** EMI categories of IndustrialCraft 2 (spec 16 §2.1/§2.2). */
final class Ic2EmiRecipes {
    static final EmiRecipeCategory MACERATOR = machineCategory(RecipeCategories.MACERATOR, Ic2Machine.MACERATOR);
    static final EmiRecipeCategory EXTRACTOR = machineCategory(RecipeCategories.EXTRACTOR, Ic2Machine.EXTRACTOR);
    static final EmiRecipeCategory COMPRESSOR = machineCategory(RecipeCategories.COMPRESSOR, Ic2Machine.COMPRESSOR);
    static final EmiRecipeCategory RECYCLER = machineCategory(RecipeCategories.RECYCLER, Ic2Machine.RECYCLER);
    static final EmiRecipeCategory SCRAP_BOX =
            new EmiRecipeCategory(RecipeCategories.SCRAP_BOX, EmiStack.of(Ic2Items.SCRAP_BOX.get()));
    static final EmiRecipeCategory MASS_FABRICATOR =
            machineCategory(RecipeCategories.MASS_FABRICATOR, Ic2Machine.MASS_FABRICATOR);
    static final EmiRecipeCategory CANNING_MACHINE =
            machineCategory(RecipeCategories.CANNING_MACHINE, Ic2Machine.CANNING_MACHINE);
    static final EmiRecipeCategory ELECTROLYZER = machineCategory(RecipeCategories.ELECTROLYZER, Ic2Machine.ELECTROLYZER);
    static final EmiRecipeCategory GENERATOR = machineCategory(RecipeCategories.GENERATOR, Ic2Machine.GENERATOR);
    static final EmiRecipeCategory GEOTHERMAL_GENERATOR =
            machineCategory(RecipeCategories.GEOTHERMAL_GENERATOR, Ic2Machine.GEOTHERMAL_GENERATOR);

    private Ic2EmiRecipes() {
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (EmiRecipeCategory category : List.of(MACERATOR, EXTRACTOR, COMPRESSOR, RECYCLER, SCRAP_BOX,
                MASS_FABRICATOR, CANNING_MACHINE, ELECTROLYZER, GENERATOR, GEOTHERMAL_GENERATOR)) {
            registry.addCategory(category);
            categories.put(category.getId(), category);
        }
        workstation(registry, MACERATOR, Ic2Machine.MACERATOR);
        workstation(registry, EXTRACTOR, Ic2Machine.EXTRACTOR);
        workstation(registry, COMPRESSOR, Ic2Machine.COMPRESSOR);
        workstation(registry, RECYCLER, Ic2Machine.RECYCLER);
        workstation(registry, MASS_FABRICATOR, Ic2Machine.MASS_FABRICATOR);
        workstation(registry, CANNING_MACHINE, Ic2Machine.CANNING_MACHINE);
        workstation(registry, ELECTROLYZER, Ic2Machine.ELECTROLYZER);
        workstation(registry, GENERATOR, Ic2Machine.GENERATOR);
        workstation(registry, GEOTHERMAL_GENERATOR, Ic2Machine.GEOTHERMAL_GENERATOR);
        workstation(registry, VanillaEmiRecipeCategories.SMELTING, Ic2Machine.IRON_FURNACE);
        workstation(registry, VanillaEmiRecipeCategories.SMELTING, Ic2Machine.ELECTRIC_FURNACE);
        workstation(registry, VanillaEmiRecipeCategories.SMELTING, Ic2Machine.INDUCTION_FURNACE);
        registry.addWorkstation(SCRAP_BOX, EmiStack.of(Ic2Items.SCRAP_BOX.get()));

        machineRecipes(registry, MACERATOR, MachineRecipe.Kind.MACERATOR);
        machineRecipes(registry, EXTRACTOR, MachineRecipe.Kind.EXTRACTOR);
        machineRecipes(registry, COMPRESSOR, MachineRecipe.Kind.COMPRESSOR);
        recycler(registry);
        scrapBox(registry);
        massFabricator(registry);
        canningMachine(registry);
        electrolyzer(registry);
        generator(registry);
        geothermalGenerator(registry);
    }

    private static EmiRecipeCategory machineCategory(ResourceLocation id, Ic2Machine machine) {
        return new EmiRecipeCategory(id, EmiStack.of(Ic2Items.machine(machine).get()));
    }

    private static void workstation(EmiRegistry registry, EmiRecipeCategory category, Ic2Machine machine) {
        registry.addWorkstation(category, EmiStack.of(Ic2Items.machine(machine).get()));
    }

    /** Synthetic recipe id (EMI convention: path starts with "/"). */
    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "/" + path);
    }

    static String path(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace() + "/" + key.getPath();
    }

    /** "x EU/t · y s · z EU". */
    static Component energy(int euPerTick, int ticks) {
        return Component.translatable("emi.craft.energy", euPerTick, EmiText.seconds(ticks),
                EmiText.number((long) euPerTick * ticks));
    }

    private static void machineRecipes(EmiRegistry registry, EmiRecipeCategory category, MachineRecipe.Kind kind) {
        Component energy = energy(RecipeMachineBlockEntity.ENERGY_PER_TICK, RecipeMachineBlockEntity.OPERATION_LENGTH);
        for (RecipeHolder<MachineRecipe> holder : registry.getRecipeManager().getAllRecipesFor(kind.type())) {
            MachineRecipe recipe = holder.value();
            registry.addRecipe(new CraftEmiRecipe(category, holder.id(),
                    List.of(EmiIngredient.of(recipe.getIngredient(), recipe.getCount())),
                    List.of(EmiStack.of(recipe.getResult())), RecipeMachineBlockEntity.OPERATION_LENGTH,
                    List.of(energy)));
        }
        if (kind == MachineRecipe.Kind.COMPRESSOR) {
            // IC2 TileEntityCompressor: an adjacent pump over water lets the empty compressor make snowballs
            registry.addRecipe(new CraftEmiRecipe(category, id("compressor/pump_snowball"),
                    List.of(EmiStack.of(Ic2Items.machine(Ic2Machine.PUMP).get()), EmiStack.of(Items.WATER_BUCKET)),
                    List.of(EmiStack.of(Items.SNOWBALL)), RecipeMachineBlockEntity.OPERATION_LENGTH,
                    List.of(energy, Component.translatable("emi.craft.compressor.pump"))));
        }
    }

    private static void recycler(EmiRegistry registry) {
        List<Component> lines = List.of(
                energy(RecyclerBlockEntity.ENERGY_PER_TICK, RecyclerBlockEntity.OPERATION_LENGTH),
                Component.translatable("emi.craft.recycler.any"));
        List<Item> recyclable = BuiltInRegistries.ITEM.stream()
                .filter(item -> item != Items.AIR && !item.builtInRegistryHolder().is(RecyclerBlockEntity.BLACKLIST))
                .toList();
        registry.addRecipe(new CraftEmiRecipe(RECYCLER, id("recycler/any"), List.of(),
                List.of(EmiStack.of(Ic2Items.SCRAP.get()).setChance(1.0F / RecyclerBlockEntity.RECYCLE_CHANCE)),
                RecyclerBlockEntity.OPERATION_LENGTH, lines) {
            @Override
            protected int arrowX() {
                return SLOT + 4;
            }

            @Override
            public void addWidgets(WidgetHolder widgets) {
                widgets.addGeneratedSlot(random -> EmiStack.of(recyclable.get(random.nextInt(recyclable.size()))),
                        0, 0, 0);
                super.addWidgets(widgets);
            }
        });
        registry.addRecipe(new CraftEmiRecipe(RECYCLER, id("recycler/blacklist"),
                List.of(EmiIngredient.of(RecyclerBlockEntity.BLACKLIST)), List.of(), 0,
                List.of(Component.translatable("emi.craft.recycler.blacklist"))));
    }

    private static void scrapBox(EmiRegistry registry) {
        List<ScrapBoxDrops.Drop> drops = ScrapBoxDrops.all();
        int total = drops.stream().mapToInt(ScrapBoxDrops.Drop::weight).sum();
        int index = 0;
        for (ScrapBoxDrops.Drop drop : drops) {
            registry.addRecipe(new CraftEmiRecipe(SCRAP_BOX, id("scrap_box/" + index++ + "/" + path(drop.stack().getItem())),
                    List.of(EmiStack.of(Ic2Items.SCRAP_BOX.get())),
                    List.of(EmiStack.of(drop.stack()).setChance((float) drop.weight() / total)), 0, List.of()));
        }
    }

    private static void massFabricator(EmiRegistry registry) {
        EmiStack matter = EmiStack.of(Ic2Items.UU_MATTER.get());
        registry.addRecipe(new CraftEmiRecipe(MASS_FABRICATOR, id("mass_fabricator/matter"), List.of(),
                List.of(matter), 0, List.of(Component.translatable("emi.craft.mass_fabricator.energy",
                EmiText.number(MassFabricatorBlockEntity.ENERGY_PER_MATTER)))));
        MassFabricatorBlockEntity.amplifiers().entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> registry.addRecipe(new CraftEmiRecipe(MASS_FABRICATOR,
                        id("mass_fabricator/amplifier/" + path(entry.getKey())),
                        List.of(EmiStack.of(entry.getKey())), List.of(matter), 0,
                        List.of(Component.translatable("emi.craft.mass_fabricator.amplifier",
                                EmiText.number(entry.getValue())),
                                Component.translatable("emi.craft.mass_fabricator.saves",
                                        EmiText.number(5L * entry.getValue()))))));
    }

    private static void canningMachine(EmiRegistry registry) {
        Component energy = energy(CanningMachineBlockEntity.ENERGY_PER_TICK, CanningMachineBlockEntity.OPERATION_LENGTH);
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack food = item.getDefaultInstance();
            int value = CanningMachineBlockEntity.getFoodValue(food);
            if (value <= 0) {
                continue;
            }
            List<EmiIngredient> inputs = new ArrayList<>();
            inputs.add(EmiStack.of(Ic2Items.TIN_CAN.get(), value));
            ItemStack remainder = CanningMachineBlockEntity.getFoodRemainder(food);
            inputs.add(remainder.isEmpty() ? EmiStack.of(food) : EmiStack.of(food).setRemainder(EmiStack.of(remainder)));
            registry.addRecipe(new CraftEmiRecipe(CANNING_MACHINE, id("canning_machine/food/" + path(item)), inputs,
                    List.of(EmiStack.of(FilledTinCanItem.create(CanningMachineBlockEntity.getFoodMeta(food), value))),
                    CanningMachineBlockEntity.OPERATION_LENGTH, List.of(energy)));
        }
        Component boosters = Component.translatable("emi.craft.canning_machine.boosters");
        fuelCan(registry, Ic2Items.COALFUEL_CELL.get(), CanningMachineBlockEntity.COALFUEL_VALUE, energy, boosters);
        fuelCan(registry, Ic2Items.BIOFUEL_CELL.get(), CanningMachineBlockEntity.BIOFUEL_VALUE, energy, boosters);
    }

    private static void fuelCan(EmiRegistry registry, Item cell, int value, Component energy, Component boosters) {
        registry.addRecipe(new CraftEmiRecipe(CANNING_MACHINE, id("canning_machine/fuel/" + path(cell)),
                List.of(EmiStack.of(Ic2Items.EMPTY_FUEL_CAN.get()), EmiStack.of(cell)),
                List.of(EmiStack.of(FilledFuelCanItem.create(value))), CanningMachineBlockEntity.OPERATION_LENGTH,
                List.of(energy, Component.translatable("emi.craft.canning_machine.fuel", value), boosters)));
    }

    private static void electrolyzer(EmiRegistry registry) {
        EmiStack water = EmiStack.of(Ic2Items.WATER_CELL.get());
        EmiStack electrolyzed = EmiStack.of(Ic2Items.ELECTROLYZED_WATER_CELL.get());
        registry.addRecipe(new CraftEmiRecipe(ELECTROLYZER, id("electrolyzer/store"), List.of(water),
                List.of(electrolyzed), 0, List.of(Component.translatable("emi.craft.electrolyzer.store",
                EmiText.number(ElectrolyzerBlockEntity.ENERGY_PER_CELL)))));
        registry.addRecipe(new CraftEmiRecipe(ELECTROLYZER, id("electrolyzer/release"), List.of(electrolyzed),
                List.of(water), 0, List.of(Component.translatable("emi.craft.electrolyzer.release",
                EmiText.number(14000), EmiText.number(16000), EmiText.number(18000)))));
    }

    private static void generator(EmiRegistry registry) {
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (stack.is(Items.LAVA_BUCKET)) {
                continue; // IC2 TileEntityGenerator refuses the lava bucket
            }
            int ticks = FuelValues.getFuelValueFor(stack) / 4;
            if (ticks <= 0) {
                continue;
            }
            registry.addRecipe(new CraftEmiRecipe(GENERATOR, id("generator/" + path(item)), List.of(EmiStack.of(stack)),
                    List.of(), ticks, List.of(Component.translatable("emi.craft.generator.output",
                    EmiText.number((long) ticks * GeneratorBlockEntity.PRODUCTION), GeneratorBlockEntity.PRODUCTION,
                    EmiText.seconds(ticks)))));
        }
    }

    private static void geothermalGenerator(EmiRegistry registry) {
        int ticks = GeothermalGeneratorBlockEntity.LAVA_PER_CONTAINER;
        registry.addRecipe(new CraftEmiRecipe(GEOTHERMAL_GENERATOR, id("geothermal_generator/lava"),
                List.of(EmiIngredient.of(List.of(EmiStack.of(Items.LAVA_BUCKET), EmiStack.of(Ic2Items.LAVA_CELL.get())))),
                List.of(), ticks, List.of(
                Component.translatable("emi.craft.generator.output",
                        EmiText.number((long) ticks * GeothermalGeneratorBlockEntity.PRODUCTION),
                        GeothermalGeneratorBlockEntity.PRODUCTION, EmiText.seconds(ticks)),
                Component.translatable("emi.craft.geothermal_generator.pump"))));
    }
}
