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
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.compactsolars.CsBlocks;
import dev.craft.compactsolars.SolarArrayType;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcBlocks;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.FilledTinCanItem;
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

    /** Names of the new power plants (spec 14), English or German. */
    protected void powerPlants(boolean german) {
        add("itemGroup.craft.power_plants", german ? "Craft: Kraftwerke" : "Craft: Power Plants");
        add("gui.craft.structure_incomplete", german ? "Struktur unvollständig" : "Structure incomplete");
        item(PpItems.COKE, german ? "Koks" : "Coal Coke");
        block(PpBlocks.COKE_BLOCK, german ? "Koksblock" : "Block of Coal Coke");
        block(PpBlocks.COKE_OVEN_BRICKS, german ? "Koksofenziegel" : "Coke Oven Bricks");
        block(PpBlocks.COKE_OVEN, german ? "Koksofen" : "Coke Oven");
        block(PpBlocks.BOILER_CASING, german ? "Kesselgehäuse" : "Boiler Casing");
        block(PpBlocks.BOILER_FIREBOX, german ? "Kesselfeuerung" : "Boiler Firebox");
        add(PpFluids.STEAM_TYPE.get().getDescriptionId(), german ? "Dampf" : "Steam");
        block(PpBlocks.TURBINE_CASING, german ? "Turbinengehäuse" : "Turbine Casing");
        block(PpBlocks.TURBINE_ROTOR, german ? "Turbinenrotor" : "Turbine Rotor");
        block(PpBlocks.STEAM_TURBINE, german ? "Dampfturbine" : "Steam Turbine");
    }

    /** Compact Solars names ({@code CompactSolarType.friendlyName}, English only). */
    protected void compactSolars() {
        add("itemGroup.craft.compact_solars", "Compact Solars");
        block(CsBlocks.array(SolarArrayType.LV), "Low Voltage Solar Array");
        block(CsBlocks.array(SolarArrayType.MV), "Medium Voltage Solar Array");
        block(CsBlocks.array(SolarArrayType.HV), "High Voltage Solar Array");
    }

    /** GregTech names and tooltips (GT 2.90 registers English only, also in the German pack). */
    protected void gregTech() {
        add("itemGroup.craft.gregtech", "GregTech");
        block(GtBlocks.machine(GtMachine.PLATE_BENDING_MACHINE), "Plate Bending Machine");
        item(GtItems.IRON_PLATE, "Iron Plate");
        item(GtItems.GOLD_PLATE, "Gold Plate");
        item(GtItems.REFINED_IRON_PLATE, "Refined Iron Plate");
        item(GtItems.TIN_PLATE, "Tin Plate");
        item(GtItems.COPPER_PLATE, "Copper Plate");
        item(GtItems.BRONZE_PLATE, "Bronze Plate");
        item(GtItems.CONVEYOR_MODULE, "Conveyor Module");
        add("tooltip.craft.conveyor_module", "Moves Items around");
        item(GtItems.MACHINE_PARTS, "Machine Parts");
        add("tooltip.craft.machine_parts", "Random Machine Parts");
        block(GtBlocks.machine(GtMachine.INDUSTRIAL_ELECTROLYZER), "Industrial Electrolyzer");
        add("container.craft.gt.insufficient_energy", "Insufficient Energy Line!");
        item(GtItems.HYDROGEN_CELL, "Hydrogen Cell");
        item(GtItems.LITHIUM_CELL, "Lithium Cell");
        item(GtItems.SILICON_CELL, "Silicon Cell");
        item(GtItems.CARBON_CELL, "Carbon Cell");
        item(GtItems.CALCIUM_CELL, "Calcium Cell");
        item(GtItems.SODIUM_CELL, "Sodium Cell");
        item(GtItems.SULFUR_DUST, "Sulfur Dust");
        item(GtItems.ALUMINIUM_DUST, "Aluminium Dust");
        item(GtItems.DARK_ASH, "Dark Ashes");
        item(GtItems.WATER, "Water");
        block(GtBlocks.machine(GtMachine.INDUSTRIAL_BLAST_FURNACE), "Industrial Blast Furnace");
        add("container.craft.gt.heat_capacity", "Heat Capacity: %s K");
        add("container.craft.gt.incomplete_casing", "Incomplete Machine Casing!");
        block(GtBlocks.casing(GtCasingBlock.Type.STANDARD), "Standard Machine Casing");
        block(GtBlocks.casing(GtCasingBlock.Type.REINFORCED), "Reinforced Machine Casing");
        block(GtBlocks.casing(GtCasingBlock.Type.ADVANCED), "Advanced Machine Casing");
        item(GtItems.KANTHAL_HEATING_COIL, "Kanthal Heating Coil");
        add("tooltip.craft.kanthal_heating_coil", "Standard Heating Coil");
        item(GtItems.NICHROME_HEATING_COIL, "Nichrome Heating Coil");
        add("tooltip.craft.nichrome_heating_coil", "Advanced Heating Coil");
        item(GtItems.CUPRONICKEL_HEATING_COIL, "Cupronickel Heating Coil");
        add("tooltip.craft.cupronickel_heating_coil", "Cheap and simple Heating Coil");
        block(GtBlocks.machine(GtMachine.ASSEMBLING_MACHINE), "Assembling Machine");
        block(GtBlocks.machine(GtMachine.AUTOMATIC_CANNING_MACHINE), "Automatic Canning Machine");
        item(GtItems.PNEUMATIC_GENERATOR_UPGRADE, "Pneumatic Generator Upgrade");
        add("tooltip.craft.pneumatic_generator_upgrade", "Lets Machines accept MJ");
        item(GtItems.LITHIUM_BATTERY_UPGRADE, "Lithium Battery Upgrade");
        add("tooltip.craft.lithium_battery_upgrade", "Adds 100000 EU to the Energy Capacity");
        item(GtItems.HV_TRANSFORMER_UPGRADE, "HV-Transformer Upgrade");
        add("tooltip.craft.hv_transformer_upgrade", "Higher Tier of Transformer Upgrade");
        item(GtItems.ALUMINIUM_MACHINE_HULL, "Aluminium Machine Hull");
        add("tooltip.craft.aluminium_machine_hull", "Machine Block");
        item(GtItems.BRONZE_MACHINE_HULL, "Bronze Machine Hull");
        add("tooltip.craft.bronze_machine_hull", "Machine Block");
        item(GtItems.STEEL_MACHINE_HULL, "Steel Machine Hull");
        add("tooltip.craft.steel_machine_hull", "Advanced Machine Block");
        item(GtItems.LITHIUM_BATTERY, "Lithium-Battery");
        item(GtItems.LITHIUM_BATPACK, "Lithium-Batpack");
        item(GtItems.NICKEL_PLATE, "Nickel Plate");
        item(GtItems.ALUMINIUM_PLATE, "Aluminium Plate");
        item(GtItems.STEEL_PLATE, "Steel Plate");
        item(GtItems.ALUMINIUM_INGOT, "Aluminium Ingot");
        item(GtItems.STEEL_INGOT, "Steel Ingot");
        item(GtItems.NICKEL_INGOT, "Nickel Ingot");
        item(GtItems.SILICON_PLATE, "Silicon Plate");
        item(GtItems.IRIDIUM_INGOT, "Iridium Ingot");
        item(GtItems.IRIDIUM_ALLOY_INGOT, "Iridium Alloy Ingot");
        item(GtItems.DIAMOND_DUST, "Diamond Dust");
        item(GtItems.EMERALD_DUST, "Emerald Dust");
        block(GtBlocks.machine(GtMachine.IMPLOSION_COMPRESSOR), "Implosion Compressor");
        item(GtItems.STEEL_DUST, "Steel Dust");
        item(GtItems.NICKEL_DUST, "Nickel Dust");
        block(GtBlocks.machine(GtMachine.INDUSTRIAL_CENTRIFUGE), "Industrial Centrifuge");
        add("container.craft.gt.centrifuge_1", "Industrial");
        add("container.craft.gt.centrifuge_2", "Centrifuge");
        item(GtItems.LAVA, "Lava");
        item(GtItems.DEUTERIUM_CELL, "Deuterium Cell");
        item(GtItems.TRITIUM_CELL, "Tritium Cell");
        item(GtItems.HELIUM_CELL, "Helium Cell");
        item(GtItems.HELIUM_3_CELL, "Helium-3 Cell");
        item(GtItems.METHANE_CELL, "Methane Cell");
        item(GtItems.MERCURY_CELL, "Mercury Cell");
        item(GtItems.OIL_CELL, "Oil Cell");
        item(GtItems.LAZURITE_DUST, "Lazurite Dust");
        item(GtItems.PYRITE_DUST, "Pyrite Dust");
        item(GtItems.SALTPETER_DUST, "Saltpeter Dust");
        item(GtItems.RUBY_DUST, "Ruby Dust");
        item(GtItems.ASH, "Ashes");
        item(GtItems.SMALL_PYRITE_DUST, "Tiny Pile of Pyrite Dust");
        item(GtItems.SMALL_CALCITE_DUST, "Tiny Pile of Calcite Dust");
        item(GtItems.SMALL_SODALITE_DUST, "Tiny Pile of Sodalite Dust");
        item(GtItems.SMALL_TUNGSTEN_DUST, "Tiny Pile of Tungsten Dust");
        item(GtItems.THORIUM_INGOT, "Thorium Ingot");
        item(GtItems.PLUTONIUM_INGOT, "Plutonium Ingot");
        item(GtItems.URANIUM_DUST, "Uranium Dust");
        item(GtItems.THORIUM_DUST, "Thorium Dust");
        item(GtItems.PLUTONIUM_DUST, "Plutonium Dust");
        item(GtItems.SMALL_PLUTONIUM_DUST, "Tiny Pile of Plutonium Dust");
        item(GtItems.TUNGSTEN_DUST, "Tungsten Dust");
        item(GtItems.BERYLLIUM_CELL, "Berylium Cell");
        item(GtItems.HELIUM_COOLANT_CELL_60K, "60k Helium Coolant Cell");
        item(GtItems.HELIUM_COOLANT_CELL_180K, "180k Helium Coolant Cell");
        item(GtItems.HELIUM_COOLANT_CELL_360K, "360k Helium Coolant Cell");
        add("tooltip.craft.stored_heat", "Stored Heat: %s");
        item(GtItems.IRIDIUM_NEUTRON_REFLECTOR, "Iridium Neutron Reflector");
        item(GtItems.THORIUM_CELL, "Thorium Cell");
        item(GtItems.DUAL_THORIUM_CELL, "Double Thorium Cell");
        item(GtItems.QUAD_THORIUM_CELL, "Quad Thorium Cell");
        item(GtItems.PLUTONIUM_CELL, "Plutonium Cell");
        item(GtItems.DUAL_PLUTONIUM_CELL, "Double Plutonium Cell");
        item(GtItems.QUAD_PLUTONIUM_CELL, "Quad Plutonium Cell");
        add("tooltip.craft.time_left", "Time left: %s secs");
        item(GtItems.ENDER_PEARL_DUST, "Ender Pearl Dust");
        item(GtItems.SODALITE_DUST, "Sodalite Dust");
        item(GtItems.LEAD_DUST, "Lead Dust");
        item(GtItems.SMALL_SILVER_DUST, "Tiny Pile of Silver Dust");
        item(GtItems.LEAD_INGOT, "Lead Ingot");
        item(GtItems.LEAD_PLATE, "Lead Plate");
        item(GtItems.CHLORITE_CELL, "Chlorite Cell");
        item(GtItems.POTASSIUM_CELL, "Potassium Cell");
        item(GtItems.NITROGEN_CELL, "Nitrogen Cell");
        item(GtItems.NAK_COOLANT_CELL_60K, "60k NaK Coolantcell");
        item(GtItems.NAK_COOLANT_CELL_180K, "180k NaK Coolantcell");
        item(GtItems.NAK_COOLANT_CELL_360K, "360k NaK Coolantcell");
        item(GtItems.SMALL_LEAD_DUST, "Tiny Pile of Lead Dust");
        item(GtItems.SMALL_ZINC_DUST, "Tiny Pile of Zinc Dust");
        item(GtItems.SMALL_NICKEL_DUST, "Tiny Pile of Nickel Dust");
        item(GtItems.SMALL_GOLD_DUST, "Tiny Pile of Gold Dust");
        item(GtItems.SMALL_COPPER_DUST, "Tiny Pile of Copper Dust");
        item(GtItems.SMALL_TIN_DUST, "Tiny Pile of Tin Dust");
        item(GtItems.ELECTRUM_INGOT, "Electrum Ingot");
        for (GtOre ore : GtOre.values()) {
            block(GtBlocks.ore(ore), ore.getEnglishName());
            if (ore.hasDeepslateVariant()) {
                block(GtBlocks.deepslateOre(ore), "Deepslate " + ore.getEnglishName());
            }
        }
        item(GtItems.RUBY, "Ruby");
        item(GtItems.SAPPHIRE, "Sapphire");
        item(GtItems.GREEN_SAPPHIRE, "Green Sapphire");
        item(GtItems.OLIVINE, "Olivine");
        item(GtItems.RED_GARNET, "Red Garnet");
        item(GtItems.YELLOW_GARNET, "Yellow Garnet");
        item(GtItems.PLATINUM_INGOT, "Platinum Ingot");
        item(GtItems.GALENA_DUST, "Galena Dust");
        item(GtItems.CINNABAR_DUST, "Cinnabar Dust");
        item(GtItems.SPHALERITE_DUST, "Sphalerite Dust");
        item(GtItems.BAUXITE_DUST, "Bauxite Dust");
        item(GtItems.ZINC_DUST, "Zinc Dust");
        item(GtItems.PLATINUM_DUST, "Platinum Dust");
        item(GtItems.SAPPHIRE_DUST, "Sapphire Dust");
        item(GtItems.GREEN_SAPPHIRE_DUST, "Green Sapphire Dust");
        item(GtItems.OLIVINE_DUST, "Olivine Dust");
        item(GtItems.RED_GARNET_DUST, "Red Garnet Dust");
        item(GtItems.YELLOW_GARNET_DUST, "Yellow Garnet Dust");
        item(GtItems.CALCITE_DUST, "Calcite Dust");
        item(GtItems.CALCIUM_CARBONATE_CELL, "Calcium Carbonate Cell");
        item(GtItems.SMALL_SULFUR_DUST, "Tiny Pile of Sulfur Dust");
        block(GtBlocks.machine(GtMachine.INDUSTRIAL_GRINDER), "Industrial Grinder");
        block(GtBlocks.machine(GtMachine.VACUUM_FREEZER), "Vacuum Freezer");
        item(GtItems.NETHERRACK_DUST, "Netherrack Dust");
        item(GtItems.SMALL_GLOWSTONE_DUST, "Tiny Pile of Glowstone Dust");
        item(GtItems.SMALL_REDSTONE_DUST, "Tiny Pile of Redstone Dust");
        item(GtItems.SMALL_DIAMOND_DUST, "Tiny Pile of Diamond Dust");
        item(GtItems.SMALL_EMERALD_DUST, "Tiny Pile of Emerald Dust");
        item(GtItems.SMALL_OLIVINE_DUST, "Tiny Pile of Olivine Dust");
        item(GtItems.SMALL_RUBY_DUST, "Tiny Pile of Ruby Dust");
        item(GtItems.SMALL_SAPPHIRE_DUST, "Tiny Pile of Sapphire Dust");
        item(GtItems.SMALL_GREEN_SAPPHIRE_DUST, "Tiny Pile of Green Sapphire Dust");
        item(GtItems.SMALL_RED_GARNET_DUST, "Tiny Pile of Red Garnet Dust");
        item(GtItems.SMALL_YELLOW_GARNET_DUST, "Tiny Pile of Yellow Garnet Dust");
        item(GtItems.SMALL_THORIUM_DUST, "Tiny Pile of Thorium Dust");
        item(GtItems.SMALL_MANGANESE_DUST, "Tiny Pile of Manganese Dust");
        item(GtItems.SMALL_PLATINUM_DUST, "Tiny Pile of Platinum Dust");
        item(GtItems.IRIDIUM_NUGGET, "Iridium Nugget");
        item(GtItems.DIAMOND_GRINDER, "Diamond Grinder");
        item(GtItems.SILVER_INGOT, "Silver Ingot");
        item(GtItems.SILVER_PLATE, "Silver Plate");
        item(GtItems.ZINC_INGOT, "Zinc Ingot");
        item(GtItems.ZINC_PLATE, "Zinc Plate");
        item(GtItems.TITANIUM_INGOT, "Titanium Ingot");
        item(GtItems.TITANIUM_PLATE, "Titanium Plate");
        item(GtItems.CHROME_INGOT, "Chrome Ingot");
        item(GtItems.CHROME_PLATE, "Chrome Plate");
        item(GtItems.PLATINUM_PLATE, "Platinum Plate");
        item(GtItems.MAGNALIUM_PLATE, "Magnalium Plate");
        item(GtItems.MAGNESIUM_DUST, "Magnesium Dust");
        item(GtItems.MANGANESE_DUST, "Manganese Dust");
        item(GtItems.TITANIUM_DUST, "Titanium Dust");
        item(GtItems.CHROME_DUST, "Chrome Dust");
        item(GtItems.SMALL_MAGNESIUM_DUST, "Tiny Pile of Magnesium Dust");
        item(GtItems.SMALL_TITANIUM_DUST, "Tiny Pile of Titanium Dust");
        item(GtItems.SMALL_CHROME_DUST, "Tiny Pile of Chrome Dust");
        item(GtItems.PYROPE_DUST, "Pyrope Dust");
        item(GtItems.ALMANDINE_DUST, "Almandine Dust");
        item(GtItems.SPESSARTINE_DUST, "Spessartine Dust");
        item(GtItems.ANDRADITE_DUST, "Andradite Dust");
        item(GtItems.GROSSULAR_DUST, "Grossular Dust");
        item(GtItems.UVAROVITE_DUST, "Uvarovite Dust");
        add("tooltip.craft.diamond_grinder", "Fancy Grinding Head");
        item(GtItems.ENERGY_FLOW_CIRCUIT, "Energy Flow Circuit");
        add("tooltip.craft.energy_flow_circuit", "Manages large amounts of Energy");
        item(GtItems.DATA_CONTROL_CIRCUIT, "Data Control Circuit");
        add("tooltip.craft.data_control_circuit", "Basic Computer Processor");
        item(GtItems.DATA_STORAGE_CIRCUIT, "Data Storage Circuit");
        add("tooltip.craft.data_storage_circuit", "Stores Data");
        item(GtItems.TITANIUM_MACHINE_HULL, "Titanium Machine Hull");
        add("tooltip.craft.titanium_machine_hull", "Very Advanced Machine Block");
        item(GtItems.ELITE_CIRCUIT_BOARD, "Elite Circuit Board");
        add("tooltip.craft.elite_circuit_board", "Highly advanced Circuit Plate");
        block(GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK, "Highly Advanced Machineblock");
        item(GtItems.TUNGSTEN_INGOT, "Tungsten Ingot");
        item(GtItems.TUNGSTEN_PLATE, "Tungsten Plate");
        item(GtItems.HOT_TUNGSTENSTEEL_INGOT, "Hot Tungstensteel Ingot");
        item(GtItems.TUNGSTENSTEEL_INGOT, "Tungstensteel Ingot");
        item(GtItems.TUNGSTENSTEEL_PLATE, "Tungstensteel Plate");
        item(GtItems.WOLFRAMIUM_GRINDER, "Wolframium Grinder");
        add("tooltip.craft.wolframium_grinder", "Regular Grinding Head");
        item(GtItems.ICE_CELL, "Ice Cell");
        block(GtBlocks.STEEL_BLOCK, "Block of Steel");
        add("tooltip.craft.gt.max_input", "Max EU/p IN: %s");
        add("tooltip.craft.gt.possible_upgrades", "Possible Upgrades: %s");
        add("tooltip.craft.gt.mj_converter", "has MJ-Converter");
        add("tooltip.craft.gt.overclockers", "%s Overclocker Upgrades");
        add("tooltip.craft.gt.transformers", "%s Transformer Upgrades");
        add("tooltip.craft.gt.batteries", "%s Battery Upgrades");
        add("tooltip.craft.gt.li_batteries", "%s Lithium Battery Upgrades");
        add("tooltip.craft.gt.energy_cells", "%s Energy Cell Upgrades");
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
            add("death.attack.craft.radiation", "%1$s died from radiation");
            add("death.attack.craft.radiation.player", "%1$s died from radiation whilst fighting %2$s");
            add("death.attack.craft.nuke", "%1$s was nuked");
            add("death.attack.craft.nuke.player", "%1$s was nuked whilst fighting %2$s");
            add("container.craft.electric_block.level", "Power Level:");
            add("container.craft.electric_block.output", "Out: %1$s EU/t");
            add("container.craft.mass_fabricator.progress", "Progress:");
            add("container.craft.mass_fabricator.amplifier", "Amplifier:");
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
            machine(Ic2Machine.NUCLEAR_REACTOR, "Nuclear Reactor");
            block(Ic2Blocks.REACTOR_CHAMBER, "Reactor Chamber");
            block(Ic2Blocks.URANIUM_ORE, "Uranium Ore");
            block(Ic2Blocks.DEEPSLATE_URANIUM_ORE, "Deepslate Uranium Ore");
            block(Ic2Blocks.URANIUM_BLOCK, "Uranium Block");
            machine(Ic2Machine.IRON_FURNACE, "Iron Furnace");
            machine(Ic2Machine.ELECTRIC_FURNACE, "Electric Furnace");
            machine(Ic2Machine.MACERATOR, "Macerator");
            machine(Ic2Machine.EXTRACTOR, "Extractor");
            machine(Ic2Machine.COMPRESSOR, "Compressor");
            machine(Ic2Machine.RECYCLER, "Recycler");
            machine(Ic2Machine.MAGNETIZER, "Magnetizer");
            machine(Ic2Machine.ELECTROLYZER, "Electrolyzer");
            machine(Ic2Machine.INDUCTION_FURNACE, "Induction Furnace");
            machine(Ic2Machine.CANNING_MACHINE, "Canning Machine");
            machine(Ic2Machine.PUMP, "Pump");
            block(Ic2Blocks.MINING_PIPE, "Mining Pipe");
            add("gui.craft.induction_furnace.heat", "Heat:");
            machine(Ic2Machine.MASS_FABRICATOR, "Mass Fabricator");
            machine(Ic2Machine.BATBOX, "BatBox");
            machine(Ic2Machine.MFE, "MFE");
            machine(Ic2Machine.MFSU, "MFSU");
            machine(Ic2Machine.LV_TRANSFORMER, "LV-Transformer");
            machine(Ic2Machine.MV_TRANSFORMER, "MV-Transformer");
            machine(Ic2Machine.HV_TRANSFORMER, "HV-Transformer");
            block(Ic2Blocks.MACHINE_CASING, "Machine Block");
            block(Ic2Blocks.ADVANCED_MACHINE_CASING, "Advanced Machine Block");
            block(Ic2Blocks.TIN_ORE, "Tin Ore");
            block(Ic2Blocks.DEEPSLATE_TIN_ORE, "Deepslate Tin Ore");
            block(Ic2Blocks.TIN_BLOCK, "Tin Block");
            block(Ic2Blocks.BRONZE_BLOCK, "Bronze Block");
            block(Ic2Blocks.IRON_FENCE, "Iron Fence");
            block(Ic2Blocks.REINFORCED_GLASS, "Reinforced Glass");
            block(Ic2Blocks.INDUSTRIAL_TNT, "Industrial TNT");
            add(Ic2EntityTypes.INDUSTRIAL_TNT.get(), "Industrial TNT");
            block(Ic2Blocks.NUKE, "Nuke");
            add(Ic2EntityTypes.NUKE.get(), "Nuke");
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
            item(Ic2Items.UU_MATTER, "UU-Matter");
            item(Ic2Items.IRIDIUM_ORE, "Iridium Ore");
            item(Ic2Items.IRIDIUM_PLATE, "Iridium Plate");
            item(Ic2Items.SILVER_DUST, "Silver Dust");
            item(Ic2Items.HYDRATED_COAL_CLUMP, "H. Coal");
            item(Ic2Items.PLANT_BALL, "Plantball");
            item(Ic2Items.COMPRESSED_PLANT_BALL, "Compressed Plants");
            item(Ic2Items.EMPTY_CELL, "Empty Cell");
            item(Ic2Items.LAVA_CELL, "Lava Cell");
            item(Ic2Items.WATER_CELL, "Water Cell");
            item(Ic2Items.AIR_CELL, "Compressed Air Cell");
            item(Ic2Items.ELECTROLYZED_WATER_CELL, "Electrolyzed Water Cell");
            item(Ic2Items.COOLANT_CELL_10K, "10k Coolant Cell");
            item(Ic2Items.COOLANT_CELL_30K, "30k Coolant Cell");
            item(Ic2Items.COOLANT_CELL_60K, "60k Coolant Cell");
            item(Ic2Items.URANIUM_DROP, "Uranium Ore");
            item(Ic2Items.URANIUM_INGOT, "Refined Uranium");
            item(Ic2Items.URANIUM_CELL, "Uranium Cell");
            item(Ic2Items.DUAL_URANIUM_CELL, "Dual Uranium Cell");
            item(Ic2Items.QUAD_URANIUM_CELL, "Quad Uranium Cell");
            item(Ic2Items.ISOTOPE_CELL, "Depleted Isotope Cell");
            item(Ic2Items.RE_ENRICHED_URANIUM_CELL, "Re-Enriched Uranium Cell");
            item(Ic2Items.NEAR_DEPLETED_URANIUM_CELL, "Near-depleted Uranium Cell");
            item(Ic2Items.REACTOR_PLATING, "Reactor Plating");
            item(Ic2Items.HEAT_CAPACITY_REACTOR_PLATING, "Heat-Capacity Reactor Plating");
            item(Ic2Items.CONTAINMENT_REACTOR_PLATING, "Containment Reactor Plating");
            item(Ic2Items.HEAT_EXCHANGER, "Heat Exchanger");
            item(Ic2Items.REACTOR_HEAT_EXCHANGER, "Reactor Heat Exchanger");
            item(Ic2Items.COMPONENT_HEAT_EXCHANGER, "Component Heat Exchanger");
            item(Ic2Items.ADVANCED_HEAT_EXCHANGER, "Advanced Heat Exchanger");
            item(Ic2Items.HEAT_VENT, "Heat Vent");
            item(Ic2Items.REACTOR_HEAT_VENT, "Reactor Heat Vent");
            item(Ic2Items.OVERCLOCKED_HEAT_VENT, "Overclocked Heat Vent");
            item(Ic2Items.COMPONENT_HEAT_VENT, "Component Heat Vent");
            item(Ic2Items.ADVANCED_HEAT_VENT, "Advanced Heat Vent");
            item(Ic2Items.HEATING_CELL, "Heating Cell");
            item(Ic2Items.NEUTRON_REFLECTOR, "Neutron Reflector");
            item(Ic2Items.THICK_NEUTRON_REFLECTOR, "Thick Neutron Reflector");
            item(Ic2Items.RSH_CONDENSATOR, "RSH-Condensator");
            item(Ic2Items.LZH_CONDENSATOR, "LZH-Condensator");
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
            item(Ic2Items.CARBON_FIBER, "Raw Carbon Fibre");
            item(Ic2Items.CARBON_MESH, "Raw Carbon Mesh");
            item(Ic2Items.CARBON_PLATE, "Carbon Plate");
            item(Ic2Items.MIXED_METAL_INGOT, "Mixed Metal Ingot");
            item(Ic2Items.ADVANCED_ALLOY, "Advanced Alloy");
            item(Ic2Items.DENSE_COPPER_PLATE, "Dense Copper Plate");
            item(Ic2Items.EMPTY_FUEL_CAN, "(Empty) Fuel Can");
            item(Ic2Items.FILLED_FUEL_CAN, "Filled Fuel Can");
            item(Ic2Items.TIN_CAN, "Tin Can");
            item(Ic2Items.FILLED_TIN_CAN, "(Filled) Tin Can");
            add(FilledTinCanItem.BAD_KEY, "This looks bad...");
            item(Ic2Items.HYDRATED_COAL_CELL, "H. Coal Cell");
            item(Ic2Items.COALFUEL_CELL, "Coalfuel Cell");
            item(Ic2Items.BIO_CELL, "Bio Cell");
            item(Ic2Items.BIOFUEL_CELL, "Biofuel Cell");
            item(Ic2Items.HAZMAT_HELMET, "Scuba Helmet");
            item(Ic2Items.HAZMAT_CHESTPLATE, "Hazmat Suit");
            item(Ic2Items.HAZMAT_LEGGINGS, "Hazmat Suit Leggings");
            item(Ic2Items.RUBBER_BOOTS, "Rubber Boots");
            item(Ic2Items.NANO_HELMET, "NanoSuit Helmet");
            item(Ic2Items.NANO_CHESTPLATE, "NanoSuit Bodyarmor");
            item(Ic2Items.NANO_LEGGINGS, "NanoSuit Leggings");
            item(Ic2Items.NANO_BOOTS, "NanoSuit Boots");
            item(Ic2Items.QUANTUM_HELMET, "QuantumSuit Helmet");
            item(Ic2Items.QUANTUM_CHESTPLATE, "QuantumSuit Bodyarmor");
            item(Ic2Items.QUANTUM_LEGGINGS, "QuantumSuit Leggings");
            item(Ic2Items.QUANTUM_BOOTS, "QuantumSuit Boots");
            item(Ic2Items.JETPACK, "Jetpack");
            item(Ic2Items.ELECTRIC_JETPACK, "Electric Jetpack");
            item(Ic2Items.BATPACK, "BatPack");
            item(Ic2Items.LAPPACK, "Lappack");
            item(Ic2Items.SOLAR_HELMET, "Solar Helmet");
            item(Ic2Items.STATIC_BOOTS, "Static Boots");
            item(Ic2Items.NIGHTVISION_GOGGLES, "Nightvision Goggles");
            add(Ic2MobEffects.RADIATION.get(), "Radiation");
            add("message.craft.jetpack.hover_enabled", "Hover Mode enabled.");
            add("message.craft.jetpack.hover_disabled", "Hover Mode disabled.");
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
            compactSolars();
            gregTech();
            powerPlants(false);
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
            add("death.attack.craft.radiation", "%1$s starb an Strahlung");
            add("death.attack.craft.radiation.player", "%1$s starb im Kampf mit %2$s an Strahlung");
            add("death.attack.craft.nuke", "%1$s wurde atomisiert");
            add("death.attack.craft.nuke.player", "%1$s wurde im Kampf mit %2$s atomisiert");
            add("container.craft.electric_block.level", "Ladung:");
            add("container.craft.electric_block.output", "Ausgang: %1$s EU/t");
            add("container.craft.mass_fabricator.progress", "Fortschritt:");
            add("container.craft.mass_fabricator.amplifier", "Verstärker:");
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
            machine(Ic2Machine.NUCLEAR_REACTOR, "Kernreaktor");
            block(Ic2Blocks.REACTOR_CHAMBER, "Reaktorkammer");
            block(Ic2Blocks.URANIUM_ORE, "Uranerz");
            block(Ic2Blocks.DEEPSLATE_URANIUM_ORE, "Tiefenschiefer-Uranerz");
            block(Ic2Blocks.URANIUM_BLOCK, "Uranblock");
            machine(Ic2Machine.IRON_FURNACE, "Eisenofen");
            machine(Ic2Machine.ELECTRIC_FURNACE, "Elektroofen");
            machine(Ic2Machine.MACERATOR, "Macerator");
            machine(Ic2Machine.EXTRACTOR, "Extraktor");
            machine(Ic2Machine.COMPRESSOR, "Kompressor");
            machine(Ic2Machine.RECYCLER, "Recycler");
            machine(Ic2Machine.MAGNETIZER, "Magnetisierer");
            machine(Ic2Machine.ELECTROLYZER, "Elektrolyseur");
            machine(Ic2Machine.INDUCTION_FURNACE, "Induktionsofen");
            machine(Ic2Machine.CANNING_MACHINE, "Konservierungsmaschine");
            machine(Ic2Machine.PUMP, "Pumpe");
            block(Ic2Blocks.MINING_PIPE, "Bergbaurohr");
            add("gui.craft.induction_furnace.heat", "Hitze:");
            machine(Ic2Machine.MASS_FABRICATOR, "Massefabrikator");
            machine(Ic2Machine.BATBOX, "BatBox");
            machine(Ic2Machine.MFE, "MFE");
            machine(Ic2Machine.MFSU, "MFSU");
            machine(Ic2Machine.LV_TRANSFORMER, "LV-Transformator");
            machine(Ic2Machine.MV_TRANSFORMER, "MV-Transformator");
            machine(Ic2Machine.HV_TRANSFORMER, "HV-Transformator");
            block(Ic2Blocks.MACHINE_CASING, "Maschinenblock");
            block(Ic2Blocks.ADVANCED_MACHINE_CASING, "Fortgeschrittener Maschinenblock");
            block(Ic2Blocks.TIN_ORE, "Zinnerz");
            block(Ic2Blocks.DEEPSLATE_TIN_ORE, "Tiefenschiefer-Zinnerz");
            block(Ic2Blocks.TIN_BLOCK, "Zinnblock");
            block(Ic2Blocks.BRONZE_BLOCK, "Bronzeblock");
            block(Ic2Blocks.IRON_FENCE, "Eisenzaun");
            block(Ic2Blocks.REINFORCED_GLASS, "Verstärktes Glas");
            block(Ic2Blocks.INDUSTRIAL_TNT, "Industrie-TNT");
            add(Ic2EntityTypes.INDUSTRIAL_TNT.get(), "Industrie-TNT");
            block(Ic2Blocks.NUKE, "Atombombe");
            add(Ic2EntityTypes.NUKE.get(), "Atombombe");
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
            item(Ic2Items.UU_MATTER, "UU-Materie");
            item(Ic2Items.IRIDIUM_ORE, "Iridiumerz");
            item(Ic2Items.IRIDIUM_PLATE, "Iridiumplatte");
            item(Ic2Items.SILVER_DUST, "Silberstaub");
            item(Ic2Items.HYDRATED_COAL_CLUMP, "Hydrierte Kohle");
            item(Ic2Items.PLANT_BALL, "Pflanzenball");
            item(Ic2Items.COMPRESSED_PLANT_BALL, "Komprimierte Pflanzen");
            item(Ic2Items.EMPTY_CELL, "Leere Zelle");
            item(Ic2Items.LAVA_CELL, "Lavazelle");
            item(Ic2Items.WATER_CELL, "Wasserzelle");
            item(Ic2Items.AIR_CELL, "Druckluftzelle");
            item(Ic2Items.ELECTROLYZED_WATER_CELL, "Elektrolysierte Wasserzelle");
            item(Ic2Items.COOLANT_CELL_10K, "10k-Kühlzelle");
            item(Ic2Items.COOLANT_CELL_30K, "30k-Kühlzelle");
            item(Ic2Items.COOLANT_CELL_60K, "60k-Kühlzelle");
            item(Ic2Items.URANIUM_DROP, "Uranerz-Brocken");
            item(Ic2Items.URANIUM_INGOT, "Raffiniertes Uran");
            item(Ic2Items.URANIUM_CELL, "Uranzelle");
            item(Ic2Items.DUAL_URANIUM_CELL, "Doppel-Uranzelle");
            item(Ic2Items.QUAD_URANIUM_CELL, "Vierfach-Uranzelle");
            item(Ic2Items.ISOTOPE_CELL, "Abgereicherte Isotopenzelle");
            item(Ic2Items.RE_ENRICHED_URANIUM_CELL, "Wiederangereicherte Uranzelle");
            item(Ic2Items.NEAR_DEPLETED_URANIUM_CELL, "Fast erschöpfte Uranzelle");
            item(Ic2Items.REACTOR_PLATING, "Reaktorplatte");
            item(Ic2Items.HEAT_CAPACITY_REACTOR_PLATING, "Wärmekapazitive Reaktorplatte");
            item(Ic2Items.CONTAINMENT_REACTOR_PLATING, "Containment-Reaktorplatte");
            item(Ic2Items.HEAT_EXCHANGER, "Wärmetauscher");
            item(Ic2Items.REACTOR_HEAT_EXCHANGER, "Reaktor-Wärmetauscher");
            item(Ic2Items.COMPONENT_HEAT_EXCHANGER, "Komponenten-Wärmetauscher");
            item(Ic2Items.ADVANCED_HEAT_EXCHANGER, "Fortgeschrittener Wärmetauscher");
            item(Ic2Items.HEAT_VENT, "Wärmeableiter");
            item(Ic2Items.REACTOR_HEAT_VENT, "Reaktor-Wärmeableiter");
            item(Ic2Items.OVERCLOCKED_HEAT_VENT, "Vergoldeter Wärmeableiter");
            item(Ic2Items.COMPONENT_HEAT_VENT, "Komponenten-Wärmeableiter");
            item(Ic2Items.ADVANCED_HEAT_VENT, "Fortgeschrittener Wärmeableiter");
            item(Ic2Items.HEATING_CELL, "Heizzelle");
            item(Ic2Items.NEUTRON_REFLECTOR, "Neutronenreflektor");
            item(Ic2Items.THICK_NEUTRON_REFLECTOR, "Dicker Neutronenreflektor");
            item(Ic2Items.RSH_CONDENSATOR, "RSH-Kondensator");
            item(Ic2Items.LZH_CONDENSATOR, "LZH-Kondensator");
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
            item(Ic2Items.CARBON_FIBER, "Rohe Kohlefaser");
            item(Ic2Items.CARBON_MESH, "Rohes Kohlenetz");
            item(Ic2Items.CARBON_PLATE, "Kohleplatte");
            item(Ic2Items.MIXED_METAL_INGOT, "Mischmetallbarren");
            item(Ic2Items.ADVANCED_ALLOY, "Fortgeschrittene Legierung");
            item(Ic2Items.DENSE_COPPER_PLATE, "Dichte Kupferplatte");
            item(Ic2Items.EMPTY_FUEL_CAN, "(Leerer) Brennstoffkanister");
            item(Ic2Items.FILLED_FUEL_CAN, "Gefüllter Brennstoffkanister");
            item(Ic2Items.TIN_CAN, "Zinndose");
            item(Ic2Items.FILLED_TIN_CAN, "(Gefüllte) Zinndose");
            add(FilledTinCanItem.BAD_KEY, "Das sieht nicht gut aus ...");
            item(Ic2Items.HYDRATED_COAL_CELL, "Hydr. Kohlezelle");
            item(Ic2Items.COALFUEL_CELL, "Kohle-Brennstoffzelle");
            item(Ic2Items.BIO_CELL, "Biozelle");
            item(Ic2Items.BIOFUEL_CELL, "Bio-Brennstoffzelle");
            item(Ic2Items.HAZMAT_HELMET, "Taucherhelm");
            item(Ic2Items.HAZMAT_CHESTPLATE, "Schutzanzug");
            item(Ic2Items.HAZMAT_LEGGINGS, "Schutzanzug-Hose");
            item(Ic2Items.RUBBER_BOOTS, "Gummistiefel");
            item(Ic2Items.NANO_HELMET, "Nanoanzug-Helm");
            item(Ic2Items.NANO_CHESTPLATE, "Nanoanzug-Körperpanzer");
            item(Ic2Items.NANO_LEGGINGS, "Nanoanzug-Hose");
            item(Ic2Items.NANO_BOOTS, "Nanoanzug-Stiefel");
            item(Ic2Items.QUANTUM_HELMET, "Quantenanzug-Helm");
            item(Ic2Items.QUANTUM_CHESTPLATE, "Quantenanzug-Körperpanzer");
            item(Ic2Items.QUANTUM_LEGGINGS, "Quantenanzug-Hose");
            item(Ic2Items.QUANTUM_BOOTS, "Quantenanzug-Stiefel");
            item(Ic2Items.JETPACK, "Jetpack");
            item(Ic2Items.ELECTRIC_JETPACK, "Elektrisches Jetpack");
            item(Ic2Items.BATPACK, "BatPack");
            item(Ic2Items.LAPPACK, "LapPack");
            item(Ic2Items.SOLAR_HELMET, "Solarhelm");
            item(Ic2Items.STATIC_BOOTS, "Statikstiefel");
            item(Ic2Items.NIGHTVISION_GOGGLES, "Nachtsichtbrille");
            add(Ic2MobEffects.RADIATION.get(), "Strahlung");
            add("message.craft.jetpack.hover_enabled", "Schwebemodus aktiviert.");
            add("message.craft.jetpack.hover_disabled", "Schwebemodus deaktiviert.");
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
            compactSolars();
            gregTech();
            powerPlants(true);
        }
    }
}
