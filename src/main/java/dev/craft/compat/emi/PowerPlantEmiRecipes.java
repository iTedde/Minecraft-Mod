package dev.craft.compat.emi;

import java.util.List;
import java.util.Map;

import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.GtItems;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import dev.craft.powerplants.boiler.BoilerTier;
import dev.craft.powerplants.turbine.SteamTurbineTier;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * EMI categories of the new power plants (spec 14, spec 16 §2.2): coke oven, boiler fuels and steam turbine, each with
 * the large stage (spec 23) as second workstation.
 */
final class PowerPlantEmiRecipes {
    private static final int BUCKET = 1000;

    static final EmiRecipeCategory COKE_OVEN =
            new EmiRecipeCategory(RecipeCategories.COKE_OVEN, EmiStack.of(PpItems.COKE_OVEN.get()));
    static final EmiRecipeCategory BOILER =
            new EmiRecipeCategory(RecipeCategories.BOILER, EmiStack.of(PpItems.BOILER_FIREBOX.get()));
    static final EmiRecipeCategory STEAM_TURBINE =
            new EmiRecipeCategory(RecipeCategories.STEAM_TURBINE, EmiStack.of(PpItems.STEAM_TURBINE.get()));

    private PowerPlantEmiRecipes() {
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (EmiRecipeCategory category : List.of(COKE_OVEN, BOILER, STEAM_TURBINE)) {
            registry.addCategory(category);
            categories.put(category.getId(), category);
        }
        registry.addWorkstation(COKE_OVEN, EmiStack.of(PpItems.COKE_OVEN.get()));
        registry.addWorkstation(BOILER, EmiStack.of(PpItems.BOILER_FIREBOX.get()));
        registry.addWorkstation(BOILER, EmiStack.of(PpItems.HIGH_PRESSURE_BOILER_FIREBOX.get()));
        registry.addWorkstation(STEAM_TURBINE, EmiStack.of(PpItems.STEAM_TURBINE.get()));
        registry.addWorkstation(STEAM_TURBINE, EmiStack.of(PpItems.LARGE_STEAM_TURBINE.get()));

        registry.addRecipe(new CraftEmiRecipe(COKE_OVEN, Ic2EmiRecipes.id("coke_oven/coal"),
                List.of(EmiStack.of(Items.COAL)), List.of(EmiStack.of(PpItems.COKE.get())),
                CokeOvenBlockEntity.OPERATION_LENGTH, List.of(Component.translatable("emi.craft.coke_oven.time",
                EmiText.seconds(CokeOvenBlockEntity.OPERATION_LENGTH)))));

        boilerFuel(registry, BoilerTier.SMALL, "", Items.COAL, 1);
        boilerFuel(registry, BoilerTier.SMALL, "", PpItems.COKE.get(), 2);
        boilerFuel(registry, BoilerTier.HIGH_PRESSURE, "high_pressure/", Items.COAL, 1);
        boilerFuel(registry, BoilerTier.HIGH_PRESSURE, "high_pressure/", PpItems.COKE.get(), 2);

        steamTurbine(registry, SteamTurbineTier.SMALL, "steam");
        steamTurbine(registry, SteamTurbineTier.LARGE, "large");
    }

    private static void steamTurbine(EmiRegistry registry, SteamTurbineTier tier, String name) {
        registry.addRecipe(new CraftEmiRecipe(STEAM_TURBINE, Ic2EmiRecipes.id("steam_turbine/" + name),
                List.of(EmiStack.of(PpFluids.STEAM.get(), BUCKET)), List.of(), 0, List.of(
                Component.translatable("emi.craft.steam_turbine.output", EmiText.number(BUCKET)),
                Component.translatable("emi.craft.steam_turbine.max", tier.maxSteamPerTick(), tier.maxOutput()))));
    }

    /** At its top temperature a boiler makes its full steam rate per tick; ash every 4 points. */
    private static void boilerFuel(EmiRegistry registry, BoilerTier tier, String prefix, Item fuel, int ashPoints) {
        int burnTime = fuel == Items.COAL ? tier.coalBurnTime() : tier.cokeBurnTime();
        int steam = burnTime * tier.maxSteamRate();
        registry.addRecipe(new CraftEmiRecipe(BOILER, Ic2EmiRecipes.id("boiler/" + prefix + Ic2EmiRecipes.path(fuel)),
                List.of(EmiStack.of(fuel), EmiStack.of(Fluids.WATER, steam)),
                List.of(EmiStack.of(PpFluids.STEAM.get(), steam),
                        EmiStack.of(GtItems.ASH.get()).setChance((float) ashPoints / BoilerBlockEntity.ASH_PER_ITEM)),
                burnTime, List.of(
                Component.translatable("emi.craft.boiler.burn", EmiText.seconds(burnTime), tier.maxSteamRate(),
                        tier.maxTemperature()),
                Component.translatable("emi.craft.boiler.energy", EmiText.number(steam)),
                Component.translatable("emi.craft.boiler.cold"))));
    }
}
