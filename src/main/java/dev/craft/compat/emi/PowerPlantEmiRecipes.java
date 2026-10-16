package dev.craft.compat.emi;

import java.util.List;
import java.util.Map;

import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.GtItems;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/** EMI categories of the new power plants (spec 14, spec 16 §2.2): coke oven, boiler fuels and steam turbine. */
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
        registry.addWorkstation(STEAM_TURBINE, EmiStack.of(PpItems.STEAM_TURBINE.get()));

        registry.addRecipe(new CraftEmiRecipe(COKE_OVEN, Ic2EmiRecipes.id("coke_oven/coal"),
                List.of(EmiStack.of(Items.COAL)), List.of(EmiStack.of(PpItems.COKE.get())),
                CokeOvenBlockEntity.OPERATION_LENGTH, List.of(Component.translatable("emi.craft.coke_oven.time",
                EmiText.seconds(CokeOvenBlockEntity.OPERATION_LENGTH)))));

        boilerFuel(registry, Items.COAL, BoilerBlockEntity.COAL_BURN_TIME, 1);
        boilerFuel(registry, PpItems.COKE.get(), BoilerBlockEntity.COKE_BURN_TIME, 2);

        registry.addRecipe(new CraftEmiRecipe(STEAM_TURBINE, Ic2EmiRecipes.id("steam_turbine/steam"),
                List.of(EmiStack.of(PpFluids.STEAM.get(), BUCKET)), List.of(), 0, List.of(
                Component.translatable("emi.craft.steam_turbine.output", EmiText.number(BUCKET)),
                Component.translatable("emi.craft.steam_turbine.max", SteamTurbineBlockEntity.MAX_STEAM_PER_TICK,
                        SteamTurbineBlockEntity.MAX_OUTPUT))));
    }

    /** At 1000 °C the boiler makes {@link BoilerBlockEntity#MAX_STEAM_RATE} mB per tick; ash every 4 points. */
    private static void boilerFuel(EmiRegistry registry, Item fuel, int burnTime, int ashPoints) {
        int steam = burnTime * BoilerBlockEntity.MAX_STEAM_RATE;
        registry.addRecipe(new CraftEmiRecipe(BOILER, Ic2EmiRecipes.id("boiler/" + Ic2EmiRecipes.path(fuel)),
                List.of(EmiStack.of(fuel), EmiStack.of(Fluids.WATER, steam)),
                List.of(EmiStack.of(PpFluids.STEAM.get(), steam),
                        EmiStack.of(GtItems.ASH.get()).setChance((float) ashPoints / BoilerBlockEntity.ASH_PER_ITEM)),
                burnTime, List.of(
                Component.translatable("emi.craft.boiler.burn", EmiText.seconds(burnTime),
                        BoilerBlockEntity.MAX_STEAM_RATE, BoilerBlockEntity.MAX_TEMPERATURE),
                Component.translatable("emi.craft.boiler.energy", EmiText.number(steam)),
                Component.translatable("emi.craft.boiler.cold"))));
    }
}
