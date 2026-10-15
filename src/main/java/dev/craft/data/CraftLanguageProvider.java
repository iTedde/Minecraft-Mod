package dev.craft.data;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrBlocks;
import dev.craft.forestry.FrFluids;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.CircuitLayout;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcBlocks;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class CraftLanguageProvider extends LanguageProvider {
    protected CraftLanguageProvider(PackOutput output, String locale) {
        super(output, Craft.MODID, locale);
    }

    protected void cable(CableType type, String name) {
        addBlock(Ic2Blocks.cable(type), name);
    }

    protected void machine(Ic2Machine machine, String name) {
        addBlock(Ic2Blocks.machine(machine), name);
    }

    protected void block(Supplier<? extends Block> block, String name) {
        addBlock(block, name);
    }

    protected void item(Supplier<? extends Item> item, String name) {
        addItem(item, name);
    }

    /**
     * Forestry names: {@code general} = tab, fabricator, carpenter, sturdy casing, soldering iron, liquid glass,
     * "No effect", layout name, layout usage, electrical engine, tin gear, "Empty Socket"; tubes, boards and circuits
     * in enum order. The circuit descriptions are literal English strings in Forestry.
     */
    protected void forestry(String[] general, String[] tubes, String[] boards, String[] circuits) {
        add("itemGroup.craft.forestry", general[0]);
        block(FrBlocks.THERMIONIC_FABRICATOR, general[1]);
        block(FrBlocks.CARPENTER, general[2]);
        item(FrItems.STURDY_CASING, general[3]);
        item(FrItems.SOLDERING_IRON, general[4]);
        add(FrFluids.LIQUID_GLASS_TYPE.get().getDescriptionId(), general[5]);
        add("gui.craft.forestry.no_effect", general[6]);
        add(CircuitLayout.ENGINE_TIN.getNameKey(), general[7]);
        add(CircuitLayout.ENGINE_TIN.getUsageKey(), general[8]);
        block(FrBlocks.ELECTRICAL_ENGINE, general[9]);
        item(FrItems.TIN_GEAR, general[10]);
        add("gui.craft.forestry.empty_socket", general[11]);
        for (ElectronTube tube : ElectronTube.values()) {
            item(FrItems.tube(tube), tubes[tube.ordinal()]);
        }
        for (CircuitBoardType type : CircuitBoardType.values()) {
            item(FrItems.circuitBoard(type), boards[type.ordinal()]);
        }
        for (Circuit circuit : Circuit.values()) {
            add(circuit.getNameKey(), circuits[circuit.ordinal()]);
        }
        String[][] descriptions = {{"Reduces ouput by 1 MJ/t", "Reduces intake by 2 EU/t"},
                {"Increases output by 2 MJ/t", "Increases intake by 7 EU/t"},
                {"Increases output by 4 MJ/t", "Increases intake by 15 EU/t"},
                {"Reduces intake by 1 EU/t"}};
        for (Circuit circuit : Circuit.values()) {
            for (int line = 0; line < descriptions[circuit.ordinal()].length; line++) {
                add(circuit.getDescriptionKeys().get(line), descriptions[circuit.ordinal()][line]);
            }
        }
    }

    /** Power Converters names ({@code LanguageRegistry.addName}, English only). */
    protected void powerConverters() {
        add("itemGroup.craft.power_converters", "Power Converters");
        block(PcBlocks.ENERGY_BRIDGE, "Energy Bridge");
        block(PcBlocks.UNIVERSAL_CHARGER, "Universal Charger");
        block(PcBlocks.converter(ConverterType.BC_CONSUMER), "BC Consumer");
        block(PcBlocks.converter(ConverterType.BC_PRODUCER), "BC Producer");
        for (ConverterType type : ConverterType.values()) {
            if (type.getSystem() == PowerSystem.INDUSTRIALCRAFT) {
                block(PcBlocks.converter(type), "IC2 " + type.getSystem().getVoltageNames()[type.getVoltageIndex()]
                        + (type.isConsumer() ? " Consumer" : " Producer"));
            }
        }
    }

    /** English names as in the pack's {@code IC2.lang}. */
    public static final class English extends CraftLanguageProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.craft.ic2", "IndustrialCraft 2");
            add("death.attack.craft.electricity", "%1$s was electrocuted");
            add("death.attack.craft.electricity.player", "%1$s was electrocuted whilst fighting %2$s");
            add("container.craft.electric_block.level", "Power Level:");
            add("container.craft.electric_block.output", "Out: %1$s EU/t");
            cable(CableType.INSULATED_COPPER, "Copper Cable");
            cable(CableType.COPPER, "Uninsulated Copper Cable");
            cable(CableType.GOLD, "Gold Cable");
            cable(CableType.INSULATED_GOLD, "Insulated Gold Cable");
            cable(CableType.DOUBLE_INSULATED_GOLD, "2xIns. Gold Cable");
            cable(CableType.HV, "HV Cable");
            cable(CableType.INSULATED_HV, "Insulated HV Cable");
            cable(CableType.DOUBLE_INSULATED_HV, "2xIns. HV Cable");
            cable(CableType.QUADRUPLE_INSULATED_HV, "4xIns. HV Cable");
            cable(CableType.GLASS_FIBRE, "Glass Fibre Cable");
            cable(CableType.TIN, "Ultra-Low-Current Cable");
            cable(CableType.DETECTOR, "EU-Detector Cable");
            cable(CableType.SPLITTER, "EU-Splitter Cable");
            machine(Ic2Machine.GENERATOR, "Generator");
            machine(Ic2Machine.GEOTHERMAL_GENERATOR, "Geothermal Generator");
            machine(Ic2Machine.WATER_MILL, "Water Mill");
            machine(Ic2Machine.SOLAR_PANEL, "Solar Panel");
            machine(Ic2Machine.WIND_MILL, "Wind Mill");
            machine(Ic2Machine.IRON_FURNACE, "Iron Furnace");
            machine(Ic2Machine.ELECTRIC_FURNACE, "Electric Furnace");
            machine(Ic2Machine.MACERATOR, "Macerator");
            machine(Ic2Machine.EXTRACTOR, "Extractor");
            machine(Ic2Machine.COMPRESSOR, "Compressor");
            machine(Ic2Machine.RECYCLER, "Recycler");
            machine(Ic2Machine.BATBOX, "BatBox");
            machine(Ic2Machine.MFE, "MFE");
            machine(Ic2Machine.MFSU, "MFSU");
            machine(Ic2Machine.LV_TRANSFORMER, "LV-Transformer");
            machine(Ic2Machine.MV_TRANSFORMER, "MV-Transformer");
            machine(Ic2Machine.HV_TRANSFORMER, "HV-Transformer");
            block(Ic2Blocks.MACHINE_CASING, "Machine Block");
            block(Ic2Blocks.TIN_ORE, "Tin Ore");
            block(Ic2Blocks.DEEPSLATE_TIN_ORE, "Deepslate Tin Ore");
            block(Ic2Blocks.TIN_BLOCK, "Tin Block");
            block(Ic2Blocks.BRONZE_BLOCK, "Bronze Block");
            block(Ic2Blocks.RUBBER_WOOD, "Rubber Wood");
            block(Ic2Blocks.RUBBER_LEAVES, "Rubber Leaves");
            block(Ic2Blocks.RUBBER_SAPLING, "Rubber Tree Sapling");
            block(Ic2Blocks.RESIN_SHEET, "Resin Sheet");
            item(Ic2Items.RESIN, "Sticky Resin");
            item(Ic2Items.RUBBER, "Rubber");
            item(Ic2Items.BRONZE_DUST, "Bronze Dust");
            item(Ic2Items.CLAY_DUST, "Clay Dust");
            item(Ic2Items.COAL_DUST, "Coal Dust");
            item(Ic2Items.COPPER_DUST, "Copper Dust");
            item(Ic2Items.GOLD_DUST, "Gold Dust");
            item(Ic2Items.IRON_DUST, "Iron Dust");
            item(Ic2Items.SMALL_IRON_DUST, "Small Pile of Iron Dust");
            item(Ic2Items.TIN_DUST, "Tin Dust");
            item(Ic2Items.HYDRATED_COAL_DUST, "Hydrated Coal Dust");
            item(Ic2Items.REFINED_IRON_INGOT, "Refined Iron");
            item(Ic2Items.TIN_INGOT, "Tin");
            item(Ic2Items.BRONZE_INGOT, "Bronze");
            item(Ic2Items.ELECTRONIC_CIRCUIT, "Electronic Circuit");
            item(Ic2Items.ADVANCED_CIRCUIT, "Advanced Circuit");
            item(Ic2Items.COAL_BALL, "Coal Ball");
            item(Ic2Items.COMPRESSED_COAL_BALL, "Compressed Coal Ball");
            item(Ic2Items.COAL_CHUNK, "Coal Chunk");
            item(Ic2Items.INDUSTRIAL_DIAMOND, "Industrial Diamond");
            item(Ic2Items.SCRAP, "Scrap");
            item(Ic2Items.SCRAP_BOX, "Scrap Box");
            item(Ic2Items.HYDRATED_COAL_CLUMP, "H. Coal");
            item(Ic2Items.PLANT_BALL, "Plantball");
            item(Ic2Items.COMPRESSED_PLANT_BALL, "Compressed Plants");
            item(Ic2Items.EMPTY_CELL, "Empty Cell");
            item(Ic2Items.LAVA_CELL, "Lava Cell");
            item(Ic2Items.WATER_CELL, "Water Cell");
            item(Ic2Items.AIR_CELL, "Compressed Air Cell");
            item(Ic2Items.COOLANT_CELL_10K, "10k Coolant Cell");
            item(Ic2Items.OVERCLOCKER_UPGRADE, "Overclocker Upgrade");
            item(Ic2Items.TRANSFORMER_UPGRADE, "Transformer Upgrade");
            item(Ic2Items.ENERGY_STORAGE_UPGRADE, "Energy Storage Upgrade");
            item(Ic2Items.RE_BATTERY, "RE-Battery");
            item(Ic2Items.ENERGY_CRYSTAL, "Energy Crystal");
            item(Ic2Items.LAPOTRON_CRYSTAL, "Lapotron Crystal");
            item(Ic2Items.SU_BATTERY, "Single-Use Battery");
            item(Ic2Items.TREETAP, "Treetap");
            item(Ic2Items.WRENCH, "Wrench");
            item(Ic2Items.MINING_DRILL, "Mining Drill");
            item(Ic2Items.DIAMOND_DRILL, "Diamond Drill");
            item(Ic2Items.CHAINSAW, "Chainsaw");
            item(Ic2Items.ELECTRIC_HOE, "Electric Hoe");
            item(Ic2Items.ELECTRIC_TREETAP, "Electric Treetap");
            item(Ic2Items.ELECTRIC_WRENCH, "Electric Wrench");
            item(Ic2Items.MINING_LASER, "Mining Laser");
            item(Ic2Items.NANO_SABER, "Nano Saber");
            item(Ic2Items.OD_SCANNER, "OD Scanner");
            item(Ic2Items.OV_SCANNER, "OV Scanner");
            add(Ic2EntityTypes.MINING_LASER.get(), "Mining Laser");
            add("key.categories.craft.ic2", "IndustrialCraft 2");
            add("key.craft.alt", "ALT Key");
            add("key.craft.boost", "Boost Key");
            add("key.craft.mode_switch", "Mode Switch Key");
            add("key.craft.side_inventory", "Side Inventory Key");
            add("message.craft.wrench.lossless_enabled", "Lossless wrench mode enabled");
            add("message.craft.wrench.lossless_disabled", "Lossless wrench mode disabled");
            add("message.craft.wrench.lossless_no_energy", "Not enough energy for lossless wrench operation");
            add("message.craft.mining_laser.mode", "Laser Mode: %s");
            add("message.craft.mining_laser.mode.mining", "Mining");
            add("message.craft.mining_laser.mode.low_focus", "Low-Focus");
            add("message.craft.mining_laser.mode.long_range", "Long-Range");
            add("message.craft.mining_laser.mode.horizontal", "Horizontal");
            add("message.craft.mining_laser.mode.super_heat", "Super-Heat");
            add("message.craft.mining_laser.mode.scatter", "Scatter");
            add("message.craft.mining_laser.mode.explosive", "Explosive");
            add("message.craft.mining_laser.too_steep", "Mining laser aiming angle too steep");
            add("message.craft.scanner.density", "SCAN RESULT: Ore density in this area is %d");
            add("message.craft.scanner.value", "SCAN RESULT: Ore value in this area is %d");
            add("itemGroup.craft.buildcraft", "BuildCraft");
            add("gui.craft.inventory", "Inventory");
            add("gui.craft.buildcraft.energy", "Energy");
            add("gui.craft.buildcraft.current_output", "Current Output");
            add("gui.craft.buildcraft.stored", "Stored");
            add("gui.craft.buildcraft.heat", "Heat");
            block(BcBlocks.engine(EngineType.REDSTONE), "Redstone Engine");
            block(BcBlocks.engine(EngineType.STIRLING), "Stirling Engine");
            block(BcBlocks.engine(EngineType.COMBUSTION), "Combustion Engine");
            item(BcItems.WOODEN_GEAR, "Wood Gear");
            item(BcItems.STONE_GEAR, "Stone Gear");
            item(BcItems.IRON_GEAR, "Iron Gear");
            item(BcItems.GOLD_GEAR, "Gold Gear");
            item(BcItems.DIAMOND_GEAR, "Diamond Gear");
            item(BcItems.WRENCH, "Wrench");
            block(BcBlocks.OIL, "Oil");
            item(BcItems.OIL_BUCKET, "Oil Bucket");
            item(BcItems.FUEL_BUCKET, "Fuel Bucket");
            block(BcBlocks.MARKER, "Land Mark");
            block(BcBlocks.QUARRY, "Quarry");
            block(BcBlocks.FRAME, "Frame");
            block(BcBlocks.TANK, "Tank");
            block(BcBlocks.PUMP, "Pump");
            block(BcBlocks.REFINERY, "Refinery");
            block(BcBlocks.MINING_WELL, "Mining Well");
            block(BcBlocks.MINING_PIPE, "Mining Pipe");
            add("message.craft.quarry.size", "Quarry size is outside of chunkloading bounds or too small %d %d (%d)");
            add("message.craft.quarry.chunks", "[BUILDCRAFT] The quarry at %d %d %d will keep %d chunks loaded");
            add(BcFluids.OIL_TYPE.get().getDescriptionId(), "Oil");
            add(BcFluids.FUEL_TYPE.get().getDescriptionId(), "Fuel");
            forestry(new String[]{"Forestry", "Thermionic Fabricator", "Carpenter", "Sturdy Casing", "Soldering Iron",
                    "Liquid Glass", "No effect", "Electric Engine", "In Electrical Engines", "Electrical Engine", "Tin Gear",
                    "Empty Socket"},
                    new String[]{"Copper Electron Tube", "Tin Electron Tube", "Bronze Electron Tube",
                            "Iron Electron Tube"},
                    new String[]{"Basic Circuit Board", "Enhanced Circuit Board", "Refined Circuit Board",
                            "Intricate Circuit Board"},
                    new String[]{"Electric Choke", "Electric Boost I", "Electric Boost II", "Electric Efficiency"});
            powerConverters();
        }
    }

    public static final class German extends CraftLanguageProvider {
        public German(PackOutput output) {
            super(output, "de_de");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.craft.ic2", "IndustrialCraft 2");
            add("death.attack.craft.electricity", "%1$s erlitt einen Stromschlag");
            add("death.attack.craft.electricity.player", "%1$s erlitt im Kampf mit %2$s einen Stromschlag");
            add("container.craft.electric_block.level", "Ladung:");
            add("container.craft.electric_block.output", "Ausgang: %1$s EU/t");
            cable(CableType.INSULATED_COPPER, "Kupferkabel");
            cable(CableType.COPPER, "Unisoliertes Kupferkabel");
            cable(CableType.GOLD, "Goldkabel");
            cable(CableType.INSULATED_GOLD, "Isoliertes Goldkabel");
            cable(CableType.DOUBLE_INSULATED_GOLD, "2x isoliertes Goldkabel");
            cable(CableType.HV, "HV-Kabel");
            cable(CableType.INSULATED_HV, "Isoliertes HV-Kabel");
            cable(CableType.DOUBLE_INSULATED_HV, "2x isoliertes HV-Kabel");
            cable(CableType.QUADRUPLE_INSULATED_HV, "4x isoliertes HV-Kabel");
            cable(CableType.GLASS_FIBRE, "Glasfaserkabel");
            cable(CableType.TIN, "Ultra-Low-Current-Kabel");
            cable(CableType.DETECTOR, "EU-Detektorkabel");
            cable(CableType.SPLITTER, "EU-Splitterkabel");
            machine(Ic2Machine.GENERATOR, "Generator");
            machine(Ic2Machine.GEOTHERMAL_GENERATOR, "Geothermiegenerator");
            machine(Ic2Machine.WATER_MILL, "Wassermühle");
            machine(Ic2Machine.SOLAR_PANEL, "Solarpanel");
            machine(Ic2Machine.WIND_MILL, "Windmühle");
            machine(Ic2Machine.IRON_FURNACE, "Eisenofen");
            machine(Ic2Machine.ELECTRIC_FURNACE, "Elektroofen");
            machine(Ic2Machine.MACERATOR, "Macerator");
            machine(Ic2Machine.EXTRACTOR, "Extraktor");
            machine(Ic2Machine.COMPRESSOR, "Kompressor");
            machine(Ic2Machine.RECYCLER, "Recycler");
            machine(Ic2Machine.BATBOX, "BatBox");
            machine(Ic2Machine.MFE, "MFE");
            machine(Ic2Machine.MFSU, "MFSU");
            machine(Ic2Machine.LV_TRANSFORMER, "LV-Transformator");
            machine(Ic2Machine.MV_TRANSFORMER, "MV-Transformator");
            machine(Ic2Machine.HV_TRANSFORMER, "HV-Transformator");
            block(Ic2Blocks.MACHINE_CASING, "Maschinenblock");
            block(Ic2Blocks.TIN_ORE, "Zinnerz");
            block(Ic2Blocks.DEEPSLATE_TIN_ORE, "Tiefenschiefer-Zinnerz");
            block(Ic2Blocks.TIN_BLOCK, "Zinnblock");
            block(Ic2Blocks.BRONZE_BLOCK, "Bronzeblock");
            block(Ic2Blocks.RUBBER_WOOD, "Gummiholz");
            block(Ic2Blocks.RUBBER_LEAVES, "Gummibaumblätter");
            block(Ic2Blocks.RUBBER_SAPLING, "Gummibaumsetzling");
            block(Ic2Blocks.RESIN_SHEET, "Harzschicht");
            item(Ic2Items.RESIN, "Harz");
            item(Ic2Items.RUBBER, "Gummi");
            item(Ic2Items.BRONZE_DUST, "Bronzestaub");
            item(Ic2Items.CLAY_DUST, "Tonstaub");
            item(Ic2Items.COAL_DUST, "Kohlestaub");
            item(Ic2Items.COPPER_DUST, "Kupferstaub");
            item(Ic2Items.GOLD_DUST, "Goldstaub");
            item(Ic2Items.IRON_DUST, "Eisenstaub");
            item(Ic2Items.SMALL_IRON_DUST, "Kleiner Haufen Eisenstaub");
            item(Ic2Items.TIN_DUST, "Zinnstaub");
            item(Ic2Items.HYDRATED_COAL_DUST, "Hydrierter Kohlestaub");
            item(Ic2Items.REFINED_IRON_INGOT, "Raffiniertes Eisen");
            item(Ic2Items.TIN_INGOT, "Zinnbarren");
            item(Ic2Items.BRONZE_INGOT, "Bronzebarren");
            item(Ic2Items.ELECTRONIC_CIRCUIT, "Elektronischer Schaltkreis");
            item(Ic2Items.ADVANCED_CIRCUIT, "Fortgeschrittener Schaltkreis");
            item(Ic2Items.COAL_BALL, "Kohleball");
            item(Ic2Items.COMPRESSED_COAL_BALL, "Komprimierter Kohleball");
            item(Ic2Items.COAL_CHUNK, "Kohleklumpen");
            item(Ic2Items.INDUSTRIAL_DIAMOND, "Industriediamant");
            item(Ic2Items.SCRAP, "Schrott");
            item(Ic2Items.SCRAP_BOX, "Schrottbox");
            item(Ic2Items.HYDRATED_COAL_CLUMP, "Hydrierte Kohle");
            item(Ic2Items.PLANT_BALL, "Pflanzenball");
            item(Ic2Items.COMPRESSED_PLANT_BALL, "Komprimierte Pflanzen");
            item(Ic2Items.EMPTY_CELL, "Leere Zelle");
            item(Ic2Items.LAVA_CELL, "Lavazelle");
            item(Ic2Items.WATER_CELL, "Wasserzelle");
            item(Ic2Items.AIR_CELL, "Druckluftzelle");
            item(Ic2Items.COOLANT_CELL_10K, "10k-Kühlzelle");
            item(Ic2Items.OVERCLOCKER_UPGRADE, "Übertakter-Upgrade");
            item(Ic2Items.TRANSFORMER_UPGRADE, "Transformator-Upgrade");
            item(Ic2Items.ENERGY_STORAGE_UPGRADE, "Energiespeicher-Upgrade");
            item(Ic2Items.RE_BATTERY, "RE-Batterie");
            item(Ic2Items.ENERGY_CRYSTAL, "Energiekristall");
            item(Ic2Items.LAPOTRON_CRYSTAL, "Lapotronkristall");
            item(Ic2Items.SU_BATTERY, "Einwegbatterie");
            item(Ic2Items.TREETAP, "Baumzapfer");
            item(Ic2Items.WRENCH, "Schraubenschlüssel");
            item(Ic2Items.MINING_DRILL, "Bergbaubohrer");
            item(Ic2Items.DIAMOND_DRILL, "Diamantbohrer");
            item(Ic2Items.CHAINSAW, "Kettensäge");
            item(Ic2Items.ELECTRIC_HOE, "Elektrische Hacke");
            item(Ic2Items.ELECTRIC_TREETAP, "Elektrischer Baumzapfer");
            item(Ic2Items.ELECTRIC_WRENCH, "Elektrischer Schraubenschlüssel");
            item(Ic2Items.MINING_LASER, "Bergbaulaser");
            item(Ic2Items.NANO_SABER, "Nanosäbel");
            item(Ic2Items.OD_SCANNER, "OD-Scanner");
            item(Ic2Items.OV_SCANNER, "OV-Scanner");
            add(Ic2EntityTypes.MINING_LASER.get(), "Bergbaulaser");
            add("key.categories.craft.ic2", "IndustrialCraft 2");
            add("key.craft.alt", "ALT-Taste");
            add("key.craft.boost", "Boost-Taste");
            add("key.craft.mode_switch", "Modus-Taste");
            add("key.craft.side_inventory", "Seiteninventar-Taste");
            add("message.craft.wrench.lossless_enabled", "Verlustfreier Modus eingeschaltet");
            add("message.craft.wrench.lossless_disabled", "Verlustfreier Modus ausgeschaltet");
            add("message.craft.wrench.lossless_no_energy", "Nicht genug Energie für verlustfreien Abbau");
            add("message.craft.mining_laser.mode", "Lasermodus: %s");
            add("message.craft.mining_laser.mode.mining", "Abbau");
            add("message.craft.mining_laser.mode.low_focus", "Kurzer Fokus");
            add("message.craft.mining_laser.mode.long_range", "Große Reichweite");
            add("message.craft.mining_laser.mode.horizontal", "Horizontal");
            add("message.craft.mining_laser.mode.super_heat", "Superhitze");
            add("message.craft.mining_laser.mode.scatter", "Streuung");
            add("message.craft.mining_laser.mode.explosive", "Explosiv");
            add("message.craft.mining_laser.too_steep", "Zielwinkel des Bergbaulasers zu steil");
            add("message.craft.scanner.density", "SCAN-ERGEBNIS: Erzdichte in diesem Bereich: %d");
            add("message.craft.scanner.value", "SCAN-ERGEBNIS: Erzwert in diesem Bereich: %d");
            add("itemGroup.craft.buildcraft", "BuildCraft");
            add("gui.craft.inventory", "Inventar");
            add("gui.craft.buildcraft.energy", "Energie");
            add("gui.craft.buildcraft.current_output", "Aktuelle Leistung");
            add("gui.craft.buildcraft.stored", "Gespeichert");
            add("gui.craft.buildcraft.heat", "Hitze");
            block(BcBlocks.engine(EngineType.REDSTONE), "Redstone-Motor");
            block(BcBlocks.engine(EngineType.STIRLING), "Stirlingmotor");
            block(BcBlocks.engine(EngineType.COMBUSTION), "Verbrennungsmotor");
            item(BcItems.WOODEN_GEAR, "Holzzahnrad");
            item(BcItems.STONE_GEAR, "Steinzahnrad");
            item(BcItems.IRON_GEAR, "Eisenzahnrad");
            item(BcItems.GOLD_GEAR, "Goldzahnrad");
            item(BcItems.DIAMOND_GEAR, "Diamantzahnrad");
            item(BcItems.WRENCH, "Schraubenschlüssel");
            block(BcBlocks.OIL, "Öl");
            item(BcItems.OIL_BUCKET, "Öleimer");
            item(BcItems.FUEL_BUCKET, "Treibstoffeimer");
            block(BcBlocks.MARKER, "Landmarke");
            block(BcBlocks.QUARRY, "Steinbruch");
            block(BcBlocks.FRAME, "Rahmen");
            block(BcBlocks.TANK, "Tank");
            block(BcBlocks.PUMP, "Pumpe");
            block(BcBlocks.REFINERY, "Raffinerie");
            block(BcBlocks.MINING_WELL, "Minenbohrer");
            block(BcBlocks.MINING_PIPE, "Minenrohr");
            add("message.craft.quarry.size", "Steinbruch-Bereich liegt außerhalb der Chunkloading-Grenzen oder ist zu klein: %d %d (%d)");
            add("message.craft.quarry.chunks", "[BUILDCRAFT] Der Steinbruch bei %d %d %d hält %d Chunks geladen");
            add(BcFluids.OIL_TYPE.get().getDescriptionId(), "Öl");
            add(BcFluids.FUEL_TYPE.get().getDescriptionId(), "Treibstoff");
            // Forestry de_DE; missing entries (intricate board, layout, circuit descriptions) stay English as in the pack.
            forestry(new String[]{"Forestry", "Glühelektrische Maschine", "Schreinerei", "Robustes Gehäuse",
                    "Lötkolben", "Flüßiges Glass", "Kein Effekt", "Electric Engine", "In Electrical Engines",
                    "Elektrischer Motor", "Zinn Zahnrad", "Leerer Steckplatz"},
                    new String[]{"Elektronenröhre (Kupfer)", "Elektronenröhre (Zinn)", "Elektronenröhre (Bronze)",
                            "Elektronenröhre (Eisen)"},
                    new String[]{"Kleine Leiterplatte", "Mittlere Leiterplatte", "Große Leiterplatte",
                            "Intricate Circuit Board"},
                    new String[]{"Elektrische Drossel", "Boost I", "Boost II", "Effizienz"});
            // Power Converters only registers English names (LanguageRegistry.addName), also in the German pack.
            powerConverters();
        }
    }
}
