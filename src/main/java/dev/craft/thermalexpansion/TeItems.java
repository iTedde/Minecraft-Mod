package dev.craft.thermalexpansion;

import dev.craft.Craft;
import dev.craft.thermalexpansion.energy.EnergyCellItem;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import dev.craft.thermalexpansion.item.MultimeterItem;
import dev.craft.thermalexpansion.item.PortableTankItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Thermal Expansion items (spec 26 §1). Materials Craft already has (silver, lead, nickel, platinum, electrum, bronze,
 * brass, sulfur, saltpeter, copper, tin, the tin gear) are not duplicated (decision E1).
 */
public final class TeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> SILVER_ORE = ITEMS.registerSimpleBlockItem(TeBlocks.SILVER_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_SILVER_ORE =
            ITEMS.registerSimpleBlockItem(TeBlocks.DEEPSLATE_SILVER_ORE);
    public static final DeferredItem<BlockItem> LEAD_ORE = ITEMS.registerSimpleBlockItem(TeBlocks.LEAD_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_LEAD_ORE =
            ITEMS.registerSimpleBlockItem(TeBlocks.DEEPSLATE_LEAD_ORE);
    public static final DeferredItem<BlockItem> NICKEL_ORE = ITEMS.registerSimpleBlockItem(TeBlocks.NICKEL_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_NICKEL_ORE =
            ITEMS.registerSimpleBlockItem(TeBlocks.DEEPSLATE_NICKEL_ORE);
    public static final DeferredItem<BlockItem> HARDENED_GLASS = ITEMS.registerSimpleBlockItem(TeBlocks.HARDENED_GLASS);
    public static final DeferredItem<BlockItem> POWERED_FURNACE = ITEMS.registerSimpleBlockItem(TeBlocks.POWERED_FURNACE);
    public static final DeferredItem<BlockItem> PULVERIZER = ITEMS.registerSimpleBlockItem(TeBlocks.PULVERIZER);
    public static final DeferredItem<BlockItem> SAWMILL = ITEMS.registerSimpleBlockItem(TeBlocks.SAWMILL);
    public static final DeferredItem<BlockItem> INDUCTION_SMELTER =
            ITEMS.registerSimpleBlockItem(TeBlocks.INDUCTION_SMELTER);
    public static final DeferredItem<BlockItem> MAGMA_CRUCIBLE = ITEMS.registerSimpleBlockItem(TeBlocks.MAGMA_CRUCIBLE);
    public static final DeferredItem<BlockItem> LIQUID_TRANSPOSER =
            ITEMS.registerSimpleBlockItem(TeBlocks.LIQUID_TRANSPOSER);
    public static final DeferredItem<BlockItem> GLACIAL_PRECIPITATOR =
            ITEMS.registerSimpleBlockItem(TeBlocks.GLACIAL_PRECIPITATOR);
    public static final DeferredItem<BlockItem> IGNEOUS_EXTRUDER =
            ITEMS.registerSimpleBlockItem(TeBlocks.IGNEOUS_EXTRUDER);
    public static final DeferredItem<BlockItem> AQUEOUS_ACCUMULATOR =
            ITEMS.registerSimpleBlockItem(TeBlocks.AQUEOUS_ACCUMULATOR);
    public static final DeferredItem<BlockItem> STEAM_ENGINE = ITEMS.registerSimpleBlockItem(TeBlocks.STEAM_ENGINE);
    public static final DeferredItem<BlockItem> MAGMATIC_ENGINE =
            ITEMS.registerSimpleBlockItem(TeBlocks.MAGMATIC_ENGINE);
    /** {@code ItemBlockEnergyCell}: stack size 1, uncommon. */
    public static final DeferredItem<EnergyCellItem> ENERGY_CELL = ITEMS.registerItem("redstone_energy_cell",
            properties -> new EnergyCellItem(TeBlocks.ENERGY_CELL.get(), properties),
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<PortableTankItem> PORTABLE_TANK = ITEMS.registerItem("portable_tank",
            properties -> new PortableTankItem(TeBlocks.PORTABLE_TANK.get(), properties),
            new Item.Properties().stacksTo(1));

    public static final DeferredItem<Item> INVAR_DUST = ITEMS.registerSimpleItem("invar_dust");
    public static final DeferredItem<Item> INVAR_INGOT = ITEMS.registerSimpleItem("invar_ingot");
    public static final DeferredItem<Item> WOOD_CHIPS = ITEMS.registerSimpleItem("wood_chips");
    public static final DeferredItem<Item> SAWDUST = ITEMS.registerSimpleItem("sawdust");
    public static final DeferredItem<Item> COMPRESSED_SAWDUST = ITEMS.registerSimpleItem("compressed_sawdust");
    public static final DeferredItem<Item> SLAG = ITEMS.registerSimpleItem("slag");
    public static final DeferredItem<Item> RICH_SLAG = ITEMS.registerSimpleItem("rich_slag");
    public static final DeferredItem<Item> PNEUMATIC_SERVO = ITEMS.registerSimpleItem("pneumatic_servo");
    /** {@code powerCoilGold}, {@code powerCoilSilver}, {@code powerCoilElectrum}. */
    public static final DeferredItem<Item> REDSTONE_RECEPTION_COIL = ITEMS.registerSimpleItem("redstone_reception_coil");
    public static final DeferredItem<Item> REDSTONE_TRANSMISSION_COIL =
            ITEMS.registerSimpleItem("redstone_transmission_coil");
    public static final DeferredItem<Item> REDSTONE_CONDUCTANCE_COIL =
            ITEMS.registerSimpleItem("redstone_conductance_coil");
    public static final DeferredItem<Item> COPPER_GEAR = ITEMS.registerSimpleItem("copper_gear");
    public static final DeferredItem<Item> INVAR_GEAR = ITEMS.registerSimpleItem("invar_gear");
    public static final DeferredItem<Item> MACHINE_FRAME = ITEMS.registerSimpleItem("machine_frame");
    public static final DeferredItem<Item> ENERGY_CELL_FRAME = ITEMS.registerSimpleItem("energy_cell_frame");
    /** {@code energyFrameFull}: rarity 1 (uncommon). */
    public static final DeferredItem<Item> FULL_ENERGY_CELL_FRAME = ITEMS.registerSimpleItem("full_energy_cell_frame",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EMPTY_ENERGY_CONDUIT = ITEMS.registerSimpleItem("empty_energy_conduit",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TESSERACT_FRAME = ITEMS.registerSimpleItem("tesseract_frame");
    public static final DeferredItem<Item> UNATTUNED_TESSERACT = ITEMS.registerSimpleItem("unattuned_tesseract",
            new Item.Properties().rarity(Rarity.UNCOMMON));
    /** {@code ItemWrench}: stack size 1. */
    public static final DeferredItem<CrescentHammerItem> CRESCENT_HAMMER = ITEMS.registerItem("crescent_hammer",
            CrescentHammerItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<MultimeterItem> MULTIMETER = ITEMS.registerItem("multimeter",
            MultimeterItem::new, new Item.Properties().stacksTo(1));
    /** {@code ItemBlockConduit}: the energy conduit is uncommon. */
    public static final DeferredItem<BlockItem> ENERGY_CONDUIT = ITEMS.registerSimpleBlockItem(TeBlocks.ENERGY_CONDUIT,
            new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<BlockItem> LIQUIDUCT = ITEMS.registerSimpleBlockItem(TeBlocks.LIQUIDUCT);

    static {
        for (DyeColor color : DyeColor.values()) {
            ITEMS.registerSimpleBlockItem(TeBlocks.rockwool(color));
        }
    }

    private TeItems() {
    }
}
