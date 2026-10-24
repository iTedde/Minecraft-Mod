package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;

import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.powerplants.PpItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;

/** Info pages with the building plan of every multiblock (spec 16 §2.3), text in {@code emi.craft.info.<name>}. */
final class MultiblockEmiInfo {
    private MultiblockEmiInfo() {
    }

    static void register(EmiRegistry registry) {
        info(registry, "coke_oven", PpItems.COKE_OVEN.get(), PpItems.COKE_OVEN_BRICKS.get());
        info(registry, "boiler", PpItems.BOILER_FIREBOX.get(), PpItems.BOILER_CASING.get());
        info(registry, "steam_turbine", PpItems.STEAM_TURBINE.get(), PpItems.TURBINE_CASING.get(),
                PpItems.TURBINE_ROTOR.get());
        info(registry, "water_turbine", PpItems.WATER_TURBINE.get(), PpItems.WATER_TURBINE_CASING.get());
        info(registry, "high_pressure_boiler", PpItems.HIGH_PRESSURE_BOILER_FIREBOX.get(),
                PpItems.HIGH_PRESSURE_BOILER_CASING.get());
        info(registry, "large_steam_turbine", PpItems.LARGE_STEAM_TURBINE.get(), PpItems.LARGE_TURBINE_CASING.get(),
                PpItems.LARGE_TURBINE_ROTOR.get());
        info(registry, "large_water_turbine", PpItems.LARGE_WATER_TURBINE.get(),
                PpItems.LARGE_WATER_TURBINE_CASING.get());
        info(registry, "large_wind_turbine", PpItems.LARGE_WIND_TURBINE.get(), PpItems.WIND_TURBINE_MAST.get(),
                PpItems.IRON_WIND_ROTOR.get(), PpItems.STEEL_WIND_ROTOR.get(), PpItems.CARBON_WIND_ROTOR.get());
        info(registry, "industrial_blast_furnace", GtItems.machine(GtMachine.INDUSTRIAL_BLAST_FURNACE).get(),
                casing(GtCasingBlock.Type.STANDARD), casing(GtCasingBlock.Type.REINFORCED),
                casing(GtCasingBlock.Type.ADVANCED), GtItems.KANTHAL_HEATING_COIL.get(),
                GtItems.NICHROME_HEATING_COIL.get());
        info(registry, "industrial_grinder", GtItems.machine(GtMachine.INDUSTRIAL_GRINDER).get(),
                casing(GtCasingBlock.Type.STANDARD), casing(GtCasingBlock.Type.REINFORCED));
        info(registry, "implosion_compressor", GtItems.machine(GtMachine.IMPLOSION_COMPRESSOR).get(),
                casing(GtCasingBlock.Type.STANDARD), casing(GtCasingBlock.Type.REINFORCED));
        info(registry, "vacuum_freezer", GtItems.machine(GtMachine.VACUUM_FREEZER).get(),
                casing(GtCasingBlock.Type.REINFORCED), casing(GtCasingBlock.Type.ADVANCED));
        info(registry, "solar_field", Ic2Items.machine(Ic2Machine.SOLAR_PANEL).get());
    }

    /** Info pages of the terraformer blueprints (spec 17 §4), text in {@code emi.craft.info.<blueprint>}. */
    static void registerTerraformer(EmiRegistry registry) {
        ItemLike terraformer = Ic2Items.machine(Ic2Machine.TERRAFORMER).get();
        for (ItemLike blueprint : List.<ItemLike>of(Ic2Items.CULTIVATION_BLUEPRINT.get(),
                Ic2Items.IRRIGATION_BLUEPRINT.get(), Ic2Items.CHILLING_BLUEPRINT.get(),
                Ic2Items.DESERTIFICATION_BLUEPRINT.get(), Ic2Items.FLATIFICATION_BLUEPRINT.get(),
                Ic2Items.MUSHROOM_BLUEPRINT.get())) {
            info(registry, BuiltInRegistries.ITEM.getKey(blueprint.asItem()).getPath(),
                    blueprint, terraformer);
        }
    }

    /** Info pages of the booze barrel and the drinks (spec 17 §4). */
    static void registerBrewing(EmiRegistry registry) {
        info(registry, "booze_barrel", Ic2Items.BOOZE_BARREL.get(), Ic2Items.WOODEN_SCAFFOLD.get(),
                Ic2Items.TREETAP.get(), Ic2Items.STONE_MUG.get(), Ic2Items.HOPS.get());
        info(registry, "booze_mug", Ic2Items.BOOZE_MUG.get());
        info(registry, "coffee", Ic2Items.COLD_COFFEE.get(), Ic2Items.DARK_COFFEE.get(), Ic2Items.COFFEE.get());
    }

    private static ItemLike casing(GtCasingBlock.Type type) {
        return GtItems.casing(type).get();
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = new ArrayList<>();
        for (ItemLike item : items) {
            stacks.add(EmiStack.of(item));
        }
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
