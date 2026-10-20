package dev.craft.powerplants;

import dev.craft.Craft;
import net.minecraft.world.item.BlockItem;
import dev.craft.powerplants.windturbine.WindRotorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<Item> COKE = ITEMS.registerSimpleItem("coke");
    public static final DeferredItem<BlockItem> COKE_BLOCK = ITEMS.registerSimpleBlockItem(PpBlocks.COKE_BLOCK);
    public static final DeferredItem<BlockItem> COKE_OVEN_BRICKS =
            ITEMS.registerSimpleBlockItem(PpBlocks.COKE_OVEN_BRICKS);
    public static final DeferredItem<BlockItem> COKE_OVEN = ITEMS.registerSimpleBlockItem(PpBlocks.COKE_OVEN);
    public static final DeferredItem<BlockItem> BOILER_CASING = ITEMS.registerSimpleBlockItem(PpBlocks.BOILER_CASING);
    public static final DeferredItem<BlockItem> BOILER_FIREBOX =
            ITEMS.registerSimpleBlockItem(PpBlocks.BOILER_FIREBOX);
    public static final DeferredItem<BlockItem> TURBINE_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.TURBINE_CASING);
    public static final DeferredItem<BlockItem> TURBINE_ROTOR = ITEMS.registerSimpleBlockItem(PpBlocks.TURBINE_ROTOR);
    public static final DeferredItem<BlockItem> STEAM_TURBINE = ITEMS.registerSimpleBlockItem(PpBlocks.STEAM_TURBINE);
    public static final DeferredItem<BlockItem> WATER_TURBINE_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.WATER_TURBINE_CASING);
    public static final DeferredItem<BlockItem> WATER_TURBINE = ITEMS.registerSimpleBlockItem(PpBlocks.WATER_TURBINE);
    public static final DeferredItem<BlockItem> HIGH_PRESSURE_BOILER_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.HIGH_PRESSURE_BOILER_CASING);
    public static final DeferredItem<BlockItem> HIGH_PRESSURE_BOILER_FIREBOX =
            ITEMS.registerSimpleBlockItem(PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX);
    public static final DeferredItem<BlockItem> LARGE_TURBINE_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_TURBINE_CASING);
    public static final DeferredItem<BlockItem> LARGE_TURBINE_ROTOR =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_TURBINE_ROTOR);
    public static final DeferredItem<BlockItem> LARGE_STEAM_TURBINE =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_STEAM_TURBINE);
    public static final DeferredItem<BlockItem> LARGE_WATER_TURBINE_CASING =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_WATER_TURBINE_CASING);
    public static final DeferredItem<BlockItem> LARGE_WATER_TURBINE =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_WATER_TURBINE);
    public static final DeferredItem<BlockItem> WIND_TURBINE_MAST =
            ITEMS.registerSimpleBlockItem(PpBlocks.WIND_TURBINE_MAST);
    public static final DeferredItem<BlockItem> LARGE_WIND_TURBINE =
            ITEMS.registerSimpleBlockItem(PpBlocks.LARGE_WIND_TURBINE);
    public static final DeferredItem<WindRotorItem> IRON_WIND_ROTOR = ITEMS.register("iron_wind_rotor",
            () -> new WindRotorItem(0.5, "iron"));
    public static final DeferredItem<WindRotorItem> STEEL_WIND_ROTOR = ITEMS.register("steel_wind_rotor",
            () -> new WindRotorItem(0.75, "steel"));
    public static final DeferredItem<WindRotorItem> CARBON_WIND_ROTOR = ITEMS.register("carbon_wind_rotor",
            () -> new WindRotorItem(1.0, "carbon"));

    private PpItems() {
    }
}
