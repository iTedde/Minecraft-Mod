package dev.craft.data;

import dev.craft.mffs.MffsBlocks;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import dev.craft.mffs.security.SecurityRight;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeBlocks;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.advancedmachines.AdvancedMachinesItems;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.mining.MiningBlocks;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.transport.PipeType;
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
import dev.craft.chargepads.CpBlocks;
import dev.craft.chargepads.CpItems;
import dev.craft.chargepads.item.PadModule;
import dev.craft.compactsolars.CsBlocks;
import dev.craft.compactsolars.SolarArrayType;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcBlocks;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
import dev.craft.lightshield.LsBlocks;
import dev.craft.lightshield.LsItems;
import dev.craft.portalgun.PgItems;
import dev.craft.enderstorage.EsItems;
import dev.craft.gravisuite.GsItems;
import dev.craft.nuclearcontrol.NcBlocks;
import dev.craft.nuclearcontrol.NcItems;
import dev.craft.mps.MpsBlocks;
import dev.craft.mps.MpsItems;
import dev.craft.mps.module.PowerModule;
import dev.craft.coppergolem.CopperGolems;
import dev.craft.ironchest.ChestUpgradeType;
import dev.craft.ironchest.IronChestBlocks;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestType;
import dev.craft.redpower.RpBlocks;
import dev.craft.redpower.RpGem;
import dev.craft.redpower.RpItems;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Fluids;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.FilledTinCanItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
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

    /** Recipe viewer (EMI) categories and texts (spec 16 §2), English or German. */
    /** Item tag names ({@code tag.item.<namespace>.<path>}), shown by EMI for tag ingredients. */
    protected void itemTags(boolean german) {
        // material path → {English, German stem}
        String[][] materials = {
                {"aluminium", "Aluminium", "Aluminium"}, {"bronze", "Bronze", "Bronze"}, {"calcite", "Calcite", "Calcit"},
                {"chrome", "Chrome", "Chrom"}, {"clay", "Clay", "Ton"}, {"coal", "Coal", "Kohle"},
                {"lead", "Lead", "Blei"}, {"magnesium", "Magnesium", "Magnesium"}, {"manganese", "Manganese", "Mangan"},
                {"nickel", "Nickel", "Nickel"}, {"platinum", "Platinum", "Platin"}, {"silver", "Silver", "Silber"},
                {"steel", "Steel", "Stahl"}, {"tin", "Tin", "Zinn"}, {"titanium", "Titanium", "Titan"},
                {"tungsten", "Tungsten", "Wolfram"}, {"zinc", "Zinc", "Zink"},
                {"tungstensteel", "Tungstensteel", "Wolframstahl"}, {"uranium", "Uranium", "Uran"},
                {"refined_iron", "Refined Iron", "Raffinierteisen"}, {"magnalium", "Magnalium", "Magnalium"},
                {"silicon", "Silicon", "Silizium"}, {"bauxite", "Bauxite", "Bauxit"}, {"cinnabar", "Cinnabar", "Zinnober"},
                {"brass", "Brass", "Messing"}, {"electrum", "Electrum", "Elektrum"},
                {"galena", "Galena", "Galena"}, {"iridium", "Iridium", "Iridium"}, {"olivine", "Olivine", "Olivin"},
                {"pyrite", "Pyrite", "Pyrit"}, {"ruby", "Ruby", "Rubin"}, {"sapphire", "Sapphire", "Saphir"},
                {"sheldonite", "Sheldonite", "Sheldonit"}, {"sodalite", "Sodalite", "Sodalith"},
                {"sphalerite", "Sphalerite", "Sphalerit"}, {"tungstate", "Tungstate", "Tungstat"},
                {"copper", "Copper", "Kupfer"}, {"invar", "Invar", "Invar"}};
        // tag folder → {English suffix, German suffix, materials with that tag}
        String[][] kinds = {
                {"dusts", "Dusts", "staub", "aluminium brass bronze calcite chrome clay coal invar lead magnesium "
                        + "manganese nickel platinum silver steel tin titanium tungsten zinc"},
                {"gears", "Gears", "zahnräder", "copper invar tin"},
                {"ingots", "Ingots", "barren", "aluminium brass bronze chrome electrum invar lead nickel platinum "
                        + "refined_iron silver steel tin titanium tungsten tungstensteel uranium zinc"},
                {"ores", "Ores", "erze", "bauxite cinnabar galena iridium lead nickel olivine pyrite ruby sapphire "
                        + "sheldonite silver sodalite sphalerite tin tungstate uranium"},
                {"plates", "Plates", "platten", "aluminium brass bronze electrum chrome lead magnalium nickel platinum "
                        + "refined_iron silicon silver steel tin titanium tungsten tungstensteel zinc"},
                {"storage_blocks", "Storage Blocks", "blöcke", "bronze steel tin uranium"}};
        for (String[] kind : kinds) {
            for (String path : kind[3].split(" ")) {
                String[] material = Arrays.stream(materials).filter(m -> m[0].equals(path)).findFirst()
                        .orElseThrow(() -> new IllegalStateException("no tag name for " + path));
                add("tag.item.c." + kind[0] + "." + path, german ? material[2] + kind[2] : material[1] + " " + kind[1]);
            }
        }
        String[][] gems = {{"green_sapphire", "Green Sapphires", "Grüne Saphire"}, {"olivine", "Olivines", "Olivine"},
                {"red_garnet", "Red Garnets", "Rote Granate"}, {"ruby", "Rubies", "Rubine"},
                {"sapphire", "Sapphires", "Saphire"}, {"yellow_garnet", "Yellow Garnets", "Gelbe Granate"}};
        for (String[] gem : gems) {
            add("tag.item.c.gems." + gem[0], german ? gem[2] : gem[1]);
        }
        add("tag.item.c.coal_coke", german ? "Koks" : "Coal Coke");
        add("tag.item.craft.crafting_100k_eu_store", german ? "100k-EU-Speicher" : "100k EU Storage");
        add("tag.item.craft.crafting_grinder", german ? "Mahlwerkzeuge" : "Grinders");
        add("tag.item.craft.item_lazurite", german ? "Lazurit" : "Lazurite");
        add("tag.item.craft.chunk_lazurite", german ? "Lazuritbrocken" : "Chunk of Lazurite");
        add("tag.item.craft.metal_boots", german ? "Metallstiefel" : "Metal Boots");
        add("tag.item.craft.raw_machine_tier_01", german ? "Maschinengehäuse (Stufe 1)" : "Machine Hulls (Tier 1)");
        add("tag.item.craft.raw_machine_tier_02", german ? "Maschinengehäuse (Stufe 2)" : "Machine Hulls (Tier 2)");
        add("tag.item.craft.recycler_blacklist", german ? "Recycler-Blacklist" : "Recycler Blacklist");
        add("tag.item.craft.toolbox.boxable", german ? "Werkzeugkasten-tauglich" : "Toolbox Items");
        add("tag.item.craft.toolbox.not_boxable", german ? "Nicht Werkzeugkasten-tauglich" : "Not Toolbox Items");
    }

    /** Mining dimension (spec 18). */
    /** RedPower World gem tools (spec 19 §2), project table and plans (spec 20 §1). */
    protected void redPower(boolean german) {
        add("itemGroup.craft.redpower", "Craft: RedPower");
        block(RpBlocks.PROJECT_TABLE, german ? "Projekttisch" : "Project Table");
        block(RpBlocks.LARGE_PROJECT_TABLE, german ? "Großer Projekttisch" : "Large Project Table");
        add("tooltip.craft.large_project_table", german ? "27 Vorratsplätze" : "27 storage slots");
        item(RpItems.BLANK_PLAN, german ? "Leerer Bauplan" : "Blank Plan");
        item(RpItems.PLAN, german ? "Bauplan" : "Plan");
        add(RpItems.PLAN.get().getDescriptionId() + ".named", german ? "Bauplan: %s" : "%s Plan");
        String[][] gems = {{"Ruby", "Rubin"}, {"Green Sapphire", "Grünsaphir"}, {"Sapphire", "Saphir"}};
        String[][] tools = {{"Pickaxe", "Spitzhacke"}, {"Shovel", "Schaufel"}, {"Axe", "Axt"}, {"Sword", "Schwert"},
                {"Hoe", "Hacke"}, {"Sickle", "Sichel"}, {"Handsaw", "Handsäge"}};
        for (RpGem gem : RpGem.values()) {
            for (RpItems.Tool tool : RpItems.Tool.values()) {
                String[] gemName = gems[gem.ordinal()];
                String[] toolName = tools[tool.ordinal()];
                item(RpItems.tool(gem, tool), german ? gemName[1] + "-" + toolName[1] : gemName[0] + " " + toolName[0]);
            }
        }
    }

    /** Thermal Expansion (spec 26): basics of phase 25.1. */
    protected void thermalExpansion(boolean german) {
        add("itemGroup.craft.thermal_expansion", "Craft: Thermal Expansion");
        block(TeBlocks.SILVER_ORE, german ? "Silbererz" : "Silver Ore");
        block(TeBlocks.DEEPSLATE_SILVER_ORE, german ? "Tiefenschiefer-Silbererz" : "Deepslate Silver Ore");
        block(TeBlocks.LEAD_ORE, german ? "Bleierz" : "Lead Ore");
        block(TeBlocks.DEEPSLATE_LEAD_ORE, german ? "Tiefenschiefer-Bleierz" : "Deepslate Lead Ore");
        block(TeBlocks.NICKEL_ORE, german ? "Nickelerz" : "Ferrous Ore");
        block(TeBlocks.DEEPSLATE_NICKEL_ORE, german ? "Tiefenschiefer-Nickelerz" : "Deepslate Ferrous Ore");
        block(TeBlocks.HARDENED_GLASS, german ? "Gehärtetes Glas" : "Hardened Glass");
        item(TeItems.INVAR_DUST, german ? "Invarstaub" : "Invar Blend");
        item(TeItems.INVAR_INGOT, german ? "Invarbarren" : "Invar Ingot");
        item(TeItems.WOOD_CHIPS, german ? "Holzschnitzel" : "Wood Chips");
        item(TeItems.SAWDUST, german ? "Sägemehl" : "Sawdust");
        item(TeItems.COMPRESSED_SAWDUST, german ? "Gepresstes Sägemehl" : "Compressed Sawdust");
        item(TeItems.SLAG, german ? "Schlacke" : "Slag");
        item(TeItems.RICH_SLAG, german ? "Reiche Schlacke" : "Rich Slag");
        item(TeItems.PNEUMATIC_SERVO, german ? "Pneumatischer Servo" : "Pneumatic Servo");
        item(TeItems.REDSTONE_RECEPTION_COIL, german ? "Redstone-Empfangsspule" : "Redstone Reception Coil");
        item(TeItems.REDSTONE_TRANSMISSION_COIL, german ? "Redstone-Übertragungsspule" : "Redstone Transmission Coil");
        item(TeItems.REDSTONE_CONDUCTANCE_COIL, german ? "Redstone-Leitspule" : "Redstone Conductance Coil");
        item(TeItems.COPPER_GEAR, german ? "Kupferzahnrad" : "Copper Gear");
        item(TeItems.INVAR_GEAR, german ? "Invarzahnrad" : "Invar Gear");
        item(TeItems.MACHINE_FRAME, german ? "Maschinenrahmen" : "Machine Frame");
        item(TeItems.ENERGY_CELL_FRAME, german ? "Energiezellenrahmen (leer)" : "Energy Cell Frame (Empty)");
        item(TeItems.FULL_ENERGY_CELL_FRAME, german ? "Energiezellenrahmen (voll)" : "Energy Cell Frame (Full)");
        item(TeItems.EMPTY_ENERGY_CONDUIT, german ? "Energieleitung (leer)" : "Energy Conduit (Empty)");
        item(TeItems.TESSERACT_FRAME, german ? "Tesserakt-Rahmen" : "Tesseract Frame");
        item(TeItems.UNATTUNED_TESSERACT, german ? "Unabgestimmter Tesserakt" : "Unattuned Tesseract");
        item(TeItems.CRESCENT_HAMMER, german ? "Crescent Hammer" : "Crescent Hammer");
        item(TeItems.MULTIMETER, german ? "Multimeter" : "Multimeter");
        block(TeBlocks.ENERGY_CONDUIT, german ? "Redstone-Energieleitung" : "Redstone Energy Conduit");
        block(TeBlocks.LIQUIDUCT, german ? "Flüssigkeitsleitung" : "Liquiduct");
        add("chat.craft.multimeter.saturation", german ? "Netzauslastung: %s %%" : "Network Saturation: %s%%");
        add("chat.craft.multimeter.monitoring", german ? "Messe 20 Ticks lang …" : "Monitoring for 20 ticks...");
        add("chat.craft.multimeter.average", german ? "Mittlere Abgabe (%s Ticks): %s" : "Average Distribution (%s ticks): %s");
        add("chat.craft.multimeter.liquid_empty", german ? "Flüssigkeit: leer" : "Liquid: Empty");
        add("chat.craft.multimeter.liquid", german ? "Flüssigkeit: %s" : "Liquid: %s");
        add("chat.craft.multimeter.amount", german ? "Menge: %s/%s mB" : "Amount: %s/%s mB");
        add("chat.craft.multimeter.throughput", german ? "Durchsatz: %s mB/t" : "Throughput: %smB/t");
        add("chat.craft.multimeter.requested", german ? "Angeforderte Energie: %s" : "Energy Requested: %s");
        String[] english = {"White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray", "Light Gray",
                "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"};
        String[] germanNames = {"Weiße", "Orange", "Magenta", "Hellblaue", "Gelbe", "Hellgrüne", "Rosa", "Graue",
                "Hellgraue", "Türkise", "Violette", "Blaue", "Braune", "Grüne", "Rote", "Schwarze"};
        for (DyeColor color : DyeColor.values()) {
            add(TeBlocks.rockwool(color).get().getDescriptionId(), german
                    ? germanNames[color.getId()] + " Steinwolle" : english[color.getId()] + " Rockwool");
        }
        add("fluid_type.craft.molten_redstone", german ? "Geschmolzenes Redstone" : "Molten Redstone");
        add("fluid_type.craft.molten_ender", german ? "Geschmolzenes Ender" : "Molten Ender");
        block(TeBlocks.POWERED_FURNACE, german ? "Elektrischer Ofen" : "Powered Furnace");
        block(TeBlocks.PULVERIZER, german ? "Pulverisierer" : "Pulverizer");
        block(TeBlocks.SAWMILL, german ? "Sägewerk" : "Sawmill");
        block(TeBlocks.INDUCTION_SMELTER, german ? "Induktionsschmelze" : "Induction Smelter");
        block(TeBlocks.MAGMA_CRUCIBLE, german ? "Magmatiegel" : "Magma Crucible");
        block(TeBlocks.LIQUID_TRANSPOSER, german ? "Flüssigkeitsumfüller" : "Liquid Transposer");
        block(TeBlocks.GLACIAL_PRECIPITATOR, german ? "Eisbereiter" : "Glacial Precipitator");
        block(TeBlocks.IGNEOUS_EXTRUDER, german ? "Gesteinsextruder" : "Igneous Extruder");
        block(TeBlocks.AQUEOUS_ACCUMULATOR, german ? "Wassersammler" : "Aqueous Accumulator");
        block(TeBlocks.PORTABLE_TANK, german ? "Tragbarer Tank" : "Portable Tank");
        block(TeBlocks.STEAM_ENGINE, german ? "Dampfmotor" : "Steam Engine");
        block(TeBlocks.MAGMATIC_ENGINE, german ? "Magmamotor" : "Magmatic Engine");
        block(TeBlocks.ENERGY_CELL, german ? "Redstone-Energiezelle" : "Redstone Energy Cell");
        block(TeBlocks.ENERGY_TESSERACT, german ? "Energie-Tesserakt" : "Energy Tesseract");
        block(TeBlocks.LIQUID_TESSERACT, german ? "Flüssigkeits-Tesserakt" : "Liquid Tesseract");
        block(TeBlocks.ITEM_TESSERACT, german ? "Item-Tesserakt" : "Item Tesseract");
        add("gui.craft.te.tesseract.inactive", german ? "Gerät inaktiv" : "Device Inactive");
        add("gui.craft.te.tesseract.frequency", german ? "Frequenz: %s" : "Frequency: %s");
        add("gui.craft.te.tesseract.set", german ? "Frequenz setzen" : "Set Frequency");
        add("gui.craft.te.tesseract.disable", german ? "Deaktivieren" : "Disable");
        add("gui.craft.te.tesseract.save", german ? "Frequenz speichern" : "Save Frequency");
        add("gui.craft.te.tesseract.delete", german ? "Frequenz löschen" : "Delete Frequency");
        add("gui.craft.te.access", german ? "Zugriff" : "Access");
        add("gui.craft.te.access_public", german ? "Öffentlich" : "Public");
        add("gui.craft.te.access_friends", german ? "Freunde" : "Friends");
        add("gui.craft.te.access_private", german ? "Privat" : "Private");
        add("gui.craft.te.mode", german ? "Modus" : "Mode");
        add("gui.craft.te.mode_send", german ? "Senden" : "Send");
        add("gui.craft.te.mode_receive", german ? "Empfangen" : "Receive");
        add("gui.craft.te.mode_both", german ? "Senden/Empfangen" : "Send/Receive");
        add("gui.craft.te.info.tesseract", german
                ? "Transportiert Dinge schnell über große Entfernungen.\n\nDie Ender-Frequenz bestimmt die Verbindungen."
                : "Use these to quickly transport things across vast distances.\n\nTune the Ender Frequency to"
                        + " determine links.");
        add("gui.craft.te.tutorial.tesseract", german
                ? "Der Reiter Konfiguration legt Zugriff und Betriebsart dieses Geräts fest."
                : "The Configuration tab determines the privacy settings and operation mode for this device.");
        add("message.craft.tesseract.private", german
                ? "Dieser Tesserakt ist privat und gehört %s! Fast hättest du deine Hand verloren!"
                : "This Tesseract is private and belongs to %s! You could have lost your hand!");
        add("message.craft.tesseract.disabled", german ? "Gerät deaktiviert – Besitzer: %s"
                : "Device Disabled - Owner: %s");
        add("message.craft.tesseract.frequency_private", german ? "Frequenz (privat): %s – Besitzer: %s"
                : "Frequency (Private): %s - Owner: %s");
        add("message.craft.tesseract.frequency", german ? "Frequenz: %s – Besitzer: %s"
                : "Frequency: %s - Owner: %s");
        add("message.craft.tesseract.mode_send", german ? "Modus: Senden – Ausgänge: %s"
                : "Mode: Send - Outputs: %s");
        add("message.craft.tesseract.mode_receive", german ? "Modus: Empfangen – Eingänge: %s"
                : "Mode: Receive - Inputs: %s");
        add("message.craft.tesseract.mode_both", german ? "Modus: Senden/Empfangen – Eingänge: %s – Ausgänge: %s"
                : "Mode: Send/Receive - Inputs: %s - Outputs: %s");
        add("gui.craft.te.power_output", german ? "Leistung" : "Power Output");
        add("gui.craft.te.in", german ? "Ein" : "In");
        add("gui.craft.te.out", german ? "Aus" : "Out");
        add("gui.craft.te.info.steam_engine", german
                ? "Erzeugt MJ aus festem Brennstoff und Wasser.\n\nGeht kaputt, wenn die Energie nicht abgegeben"
                        + " werden kann.\n\nMit dem Schraubenschlüssel reparieren."
                : "Generates MJ using solid fuel and water.\n\nWill break if the energy cannot be transferred."
                        + "\n\nWrench to repair.");
        add("gui.craft.te.info.magmatic_engine", german
                ? "Erzeugt MJ aus viel Lava.\n\nGeht kaputt, wenn die Energie nicht abgegeben werden kann."
                        + "\n\nMit dem Schraubenschlüssel reparieren."
                : "Generates MJ using a lot of lava.\n\nWill break if the energy cannot be transferred."
                        + "\n\nWrench to repair.");
        add("gui.craft.te.info.redstone_energy_cell", german
                ? "Speichert MJ. Rechtsklick ändert die Ein-/Ausgabegrenzen um 1.\n\nSchleichen + Schraubenschlüssel"
                        + " baut die Zelle ab."
                : "Stores MJ. Right click to change input/output limits by 1.\n\nWrench while sneaking to"
                        + " dismantle.");
        add("tooltip.craft.te.cell.charge", german ? "Ladung: %s MJ" : "Charge: %s MJ");
        add("tooltip.craft.te.cell.send_receive", german ? "Senden/Empfangen: %s/%s" : "Send/Receive: %s/%s");
        add("tooltip.craft.te.cell.signal_disabled", german ? "Signal: Aus" : "Signal: Disabled");
        add("tooltip.craft.te.cell.signal_low", german ? "Signal: Ein, Aus" : "Signal: Enabled, Low");
        add("tooltip.craft.te.cell.signal_high", german ? "Signal: Ein, An" : "Signal: Enabled, High");
        add("tooltip.craft.te.tank.liquid", german ? "Flüssigkeit: %s" : "Liquid: %s");
        add("tooltip.craft.te.tank.empty", german ? "Leer" : "Empty");
        add("tooltip.craft.te.tank.level", german ? "Füllstand: %s / %s mB" : "Level: %s / %s mB");
        add("gui.craft.te.info.glacial_precipitator", german
                ? "Gefriert Wasser mit MJ.\n\nDu bekommst das gewählte Item, wenn genug Wasser im Tank ist."
                        + "\n\nMach eine Schneeballschlacht."
                : "Uses MJ to freeze water.\n\nThe selected item is what you'll get, if there is enough water in the"
                        + " tank.\n\nHave a snowball fight.");
        add("gui.craft.te.info.igneous_extruder", german
                ? "Mischt Wasser und Lava zu verschiedenen Arten von Erstarrungsgestein.\n\nDu bekommst das gewählte"
                        + " Item, wenn genug Wasser und Lava in den Tanks sind.\n\nMC-Physik macht Spaß!"
                : "Mixes water and lava to make different types of igneous rock.\n\nThe selected item is what you'll"
                        + " get, if there is enough water and lava in the tanks.\n\nMC physics is fun!");
        add("gui.craft.te.info.aqueous_accumulator", german
                ? "Gewinnt Wasser aus seiner Umgebung.\n\nIn einem Wasserbecken geht es schneller."
                        + "\n\nNicht ertrinken."
                : "Extracts water from its surroundings.\n\nPlace in a pool of water to speed this up."
                        + "\n\nDon't drown.");
        add("gui.craft.te.info.induction_smelter", german
                ? "Schmilzt Metalle und Dinge, die hohe Temperaturen brauchen.\n\nNützlich, um Erze zu verarbeiten und"
                        + " fortgeschrittene Geräte zu bauen.\n\nGart ganz bestimmt kein Essen."
                : "Smelts metals and things that require high temperatures.\n\nUseful for processing ores and building"
                        + " advanced devices.\n\nWill absolutely not cook food.");
        add("gui.craft.te.info.magma_crucible", german
                ? "Macht aus festen Stoffen Flüssigkeiten!\n\nSehr nützlich für fortgeschrittene Geräte."
                        + "\n\nVon kleinen Kindern und Haustieren fernhalten."
                : "Turns solid material into liquids!\n\nVery useful for building advanced devices."
                        + "\n\nKeep away from small children and pets.");
        add("gui.craft.te.info.liquid_transposer", german
                ? "Füllt oder leert Dinge, die Flüssigkeiten fassen.\n\nMit dem Knopf zwischen Füllen und Entleeren"
                        + " wechseln.\n\nMeist umkehrbar."
                : "Fills or empties things that hold liquids.\n\nPress the button to change between Fill and Extract."
                        + "\n\nTypically reversible.");
        add("gui.craft.te.energy", german ? "Energie" : "Energy");
        add("gui.craft.te.power_usage", german ? "Verbrauch" : "Power Usage");
        add("gui.craft.te.max_power", german ? "Maximale Leistung" : "Maximum Power");
        add("gui.craft.te.energy_stored", german ? "Gespeichert" : "Energy Stored");
        add("gui.craft.te.redstone_control", german ? "Redstone-Steuerung" : "Redstone Control");
        add("gui.craft.te.control_status", german ? "Steuerung" : "Control Status");
        add("gui.craft.te.redstone_control_off", german ? "Aus" : "Disabled");
        add("gui.craft.te.redstone_control_on", german ? "Ein" : "Enabled");
        add("gui.craft.te.signal_required", german ? "Benötigtes Signal" : "Signal Required");
        add("gui.craft.te.redstone_state_ignored", german ? "Egal" : "Ignored");
        add("gui.craft.te.redstone_state_high", german ? "An" : "High");
        add("gui.craft.te.redstone_state_low", german ? "Aus" : "Low");
        add("gui.craft.te.configuration", german ? "Konfiguration" : "Configuration");
        add("gui.craft.te.information", german ? "Information" : "Information");
        add("gui.craft.te.tutorial", german ? "Anleitung" : "Tutorial");
        add("gui.craft.te.info.powered_furnace", german
                ? "Schmilzt Dinge mit MJ.\n\nBraucht sehr wenig Energie, um Essen zu garen.\n\nMampf."
                : "Smelt things using MJ.\n\nUses very little energy to cook food.\n\nOm nom nom.");
        add("gui.craft.te.info.pulverizer", german
                ? "Zerschlägt Dinge zu anderen Dingen.\n\nNützlich, um Erze zu verarbeiten oder Items umzuwandeln."
                        + "\n\nMeist nicht umkehrbar."
                : "Smashes things into other things.\n\nUseful for processing ores or converting items."
                        + "\n\nThis is typically not reversible.");
        add("gui.craft.te.info.sawmill", german
                ? "Sägt Stämme zu Brettern und verwertet Dinge aus Holz.\n\nHolt das Meiste aus einem Baum heraus."
                        + "\n\nVorsicht mit den Fingern."
                : "Cuts logs into planks and reprocesses wooden things.\n\nVery useful for making the most out of a"
                        + " tree.\n\nWatch your hands.");
        add("gui.craft.te.tutorial.redstone", german
                ? "Der Reiter Redstone-Steuerung legt fest, wie dieses Gerät auf Redstone-Signale reagiert."
                : "The Redstone Control tab configures how this device reacts to redstone signals.");
        add("gui.craft.te.tutorial.config", german
                ? "Der Reiter Konfiguration legt fest, wie Items in dieses Gerät hinein- und herausgelangen."
                : "The Configuration tab determines how items enter and leave this device.");
        add("gui.craft.te.tutorial.powered", german ? "Benötigt Minecraft Joules (MJ)."
                : "Requires Minecraft Joules to operate.");
        add("emi.category.craft.powered_furnace", german ? "Elektrischer Ofen (TE)" : "Powered Furnace");
        add("emi.category.craft.pulverizer", german ? "Pulverisierer" : "Pulverizer");
        add("emi.category.craft.sawmill", german ? "Sägewerk" : "Sawmill");
        add("emi.category.craft.induction_smelter", german ? "Induktionsschmelze" : "Induction Smelter");
        add("emi.category.craft.magma_crucible", german ? "Magmatiegel" : "Magma Crucible");
        add("emi.category.craft.liquid_transposer_fill", german ? "Flüssigkeitsumfüller: Füllen"
                : "Liquid Transposer: Fill");
        add("emi.category.craft.liquid_transposer_extract", german ? "Flüssigkeitsumfüller: Entleeren"
                : "Liquid Transposer: Extract");
        add("emi.category.craft.glacial_precipitator", german ? "Eisbereiter" : "Glacial Precipitator");
        add("emi.category.craft.igneous_extruder", german ? "Gesteinsextruder" : "Igneous Extruder");
        add("emi.category.craft.te_steam_engine", german ? "Dampfmotor (TE)" : "Steam Engine (TE)");
        add("emi.category.craft.magmatic_engine", german ? "Magmamotor" : "Magmatic Engine");
        add("emi.craft.te.energy", german ? "%1$s MJ · %2$s s bei %3$s MJ/t" : "%1$s MJ · %2$s s at %3$s MJ/t");
        add("emi.craft.te_igneous_extruder.time", german ? "%1$s s, ohne Strom" : "%1$s s, no power");
        add("emi.craft.te_igneous_extruder.start", german ? "Startet erst mit je %1$s mB Lava und Wasser"
                : "Starts only with %1$s mB each of lava and water");
        add("emi.craft.te_steam_engine.fuel", german ? "%1$s MJ" : "%1$s MJ");
        add("emi.craft.te_steam_engine.water", german ? "Dazu Wasser: Leistung · 10 mB je 40 Ticks"
                : "Plus water: power · 10 mB per 40 ticks");
        add("emi.craft.te_magmatic_engine.lava", german ? "%1$s MJ je 1000 mB (%2$s MJ je %3$s mB)"
                : "%1$s MJ per 1000 mB (%2$s MJ per %3$s mB)");
        add("emi.info.craft.te_fallback", german
                ? "Der Elektrische Ofen schmilzt auch alle Ofenrezepte (160 MJ). Pulverisierer und Sägewerk "
                + "übernehmen jedes Macerator-Rezept ohne eigenes TE-Rezept (GregTech): Holzschliff im Sägewerk "
                + "(80 MJ), alles andere im Pulverisierer (400 MJ), ohne Nebenprodukt."
                : "The powered furnace also smelts every furnace recipe (160 MJ). The pulverizer and sawmill take "
                + "every macerator recipe without an own TE recipe (GregTech): wood pulp in the sawmill (80 MJ), "
                + "everything else in the pulverizer (400 MJ), without a secondary output.");
    }


    /** Advanced Machines (spec 25): machines, refined iron dust, tab, GUI and EMI texts. */
    protected void advancedMachines(boolean german) {
        add("itemGroup.craft.advanced_machines", "Craft: Advanced Machines");
        machine(Ic2Machine.ROTARY_MACERATOR, german ? "Rotations-Macerator" : "Rotary Macerator");
        machine(Ic2Machine.SINGULARITY_COMPRESSOR, german ? "Singularitätskompressor" : "Singularity Compressor");
        machine(Ic2Machine.CENTRIFUGE_EXTRACTOR, german ? "Zentrifugalextraktor" : "Centrifuge Extractor");
        item(AdvancedMachinesItems.REFINED_IRON_DUST, german ? "Raffinierter Eisenstaub" : "Refined Iron Dust");
        add("gui.craft.advanced_machine.speed", german ? "Drehzahl:" : "Speed:");
        add("emi.category.craft.rotary_macerator_supplement",
                german ? "Rotations-Macerator: Zusatz" : "Rotary Macerator: Supplement");
        add("emi.craft.rotary_macerator_supplement.uses",
                german ? "Reicht für %s Vorgänge" : "Lasts for %s operations");
        add("emi.info.craft.advanced_machine", german
                ? "Läuft erst hoch: jeder Tick mit Energie und Rezept (oder Redstone-Signal) erhöht die Drehzahl um 1 "
                + "(3 EU/t) bis 7500; der Fortschritt wächst um Drehzahl/30 je Tick, fertig bei 4000. Bei voller "
                + "Drehzahl 16 Ticks je Vorgang für 3 EU/t. Ohne Arbeit sinkt die Drehzahl um 4 je Tick; ein "
                + "Redstone-Signal hält sie (kostet weiter Energie). Overclocker: schneller hoch und höher, aber "
                + "3 · 2^n EU/t beim Hochlaufen. Redstone im Energieslot füllt den ganzen Speicher."
                : "Spins up first: every tick with energy and a recipe (or a redstone signal) raises the speed by 1 "
                + "(3 EU/t) up to 7500; progress grows by speed/30 per tick and is done at 4000. At full speed 16 "
                + "ticks per operation for 3 EU/t. Without work the speed drops by 4 per tick; a redstone signal keeps "
                + "it up (still using energy). Overclockers: faster and higher speed, but 3 · 2^n EU/t while "
                + "spinning up. Redstone in the energy slot fills the whole storage.");
    }

    /** Iron Chests (spec 22): chests and upgrades. */
    protected void ironChests(boolean german) {
        add("itemGroup.craft.iron_chests", "Craft: Iron Chests");
        String[][] chests = {{"Iron Chest", "Eisenkiste"}, {"Gold Chest", "Goldkiste"}, {"Diamond Chest", "Diamantkiste"},
                {"Copper Chest", "Kupferkiste"}, {"Silver Chest", "Silberkiste"}, {"Crystal Chest", "Kristallkiste"},
                {"Obsidian Chest", "Obsidiankiste"}};
        for (IronChestType type : IronChestType.values()) {
            item(IronChestItems.chest(type), chests[type.ordinal()][german ? 1 : 0]);
        }
        // Copper chest stages (spec 35 §1), named like the vanilla copper blocks.
        String[][] stages = {{"", ""}, {"Exposed ", "Angelaufene "}, {"Weathered ", "Verwitterte "},
                {"Oxidized ", "Oxidierte "}};
        for (WeatherState stage : WeatherState.values()) {
            String[] prefix = stages[stage.ordinal()];
            if (stage != WeatherState.UNAFFECTED) {
                block(IronChestBlocks.copperChest(stage, false), german ? prefix[1] + "Kupferkiste"
                        : prefix[0] + "Copper Chest");
            }
            block(IronChestBlocks.copperChest(stage, true), german
                    ? "Gewachste " + prefix[1].toLowerCase(Locale.ROOT) + "Kupferkiste"
                    : "Waxed " + prefix[0] + "Copper Chest");
        }
        String[][] upgrades = {
                {"Iron to Gold Chest Upgrade", "Kisten-Upgrade Eisen → Gold"},
                {"Gold to Diamond Chest Upgrade", "Kisten-Upgrade Gold → Diamant"},
                {"Copper to Silver Chest Upgrade", "Kisten-Upgrade Kupfer → Silber"},
                {"Silver to Gold Chest Upgrade", "Kisten-Upgrade Silber → Gold"},
                {"Copper to Iron Chest Upgrade", "Kisten-Upgrade Kupfer → Eisen"},
                {"Diamond to Crystal Chest Upgrade", "Kisten-Upgrade Diamant → Kristall"},
                {"Normal chest to Iron Chest Upgrade", "Kisten-Upgrade Holz → Eisen"},
                {"Normal chest to Copper Chest Upgrade", "Kisten-Upgrade Holz → Kupfer"},
                {"Diamond to Obsidian Chest Upgrade", "Kisten-Upgrade Diamant → Obsidian"}};
        for (ChestUpgradeType type : ChestUpgradeType.values()) {
            item(IronChestItems.upgrade(type), upgrades[type.ordinal()][german ? 1 : 0]);
        }
        // Copper golem (spec 35 §3).
        addEntityType(CopperGolems.COPPER_GOLEM, german ? "Kupfergolem" : "Copper Golem");
        item(CopperGolems.SPAWN_EGG, german ? "Kupfergolem-Spawn-Ei" : "Copper Golem Spawn Egg");
        add("emi.craft.info.copper_golem_build", german
                ? "Kupfergolem bauen: Kupferblock (jede Stufe, auch gewachst) setzen, dann einen geschnitzten Kürbis "
                + "oder eine Kürbislaterne daran – oben, seitlich oder darunter, der Kürbis muss zuletzt kommen (auch "
                + "per Werfer). Der Kupferblock wird zur Kupferkiste, der Golem erscheint am Kürbis; beide haben die "
                + "Oxidationsstufe des Blocks, ohne Wachs."
                : "Building a copper golem: place a copper block (any stage, waxed too), then a carved pumpkin or "
                + "jack o'lantern next to it – on top, at the side or below; the pumpkin has to come last (dispensers "
                + "work too). The copper block turns into a copper chest, the golem appears at the pumpkin; both get "
                + "the block's oxidation stage, without wax.");
        add("emi.craft.info.copper_golem", german
                ? "Der Kupfergolem sortiert im Umkreis von 32 Blöcken (in jede Richtung): Mit leerer Hand holt er bis "
                + "zu 16 Stück des ersten Items aus der nächsten Kupferkiste. Dann bringt er es zur nächsten Truhe, "
                + "Redstone-Truhe, zum Fass oder zu einer Iron Chest (nicht Kupfer), die leer ist oder das Item schon "
                + "enthält; er schaut jeweils 3 Sekunden hinein. Nach 10 Fehlversuchen läuft er kurz umher und "
                + "beginnt von vorn. Rechtsklick mit leerer Hand nimmt ihm das Item ab. Er oxidiert in vier Stufen "
                + "(je etwa 7 Stunden Spielzeit) und arbeitet trotzdem weiter; Honigwabe wachst, Axt schabt ab, "
                + "Blitz nimmt eine Stufe. 12 Leben, kein Fallschaden, droppt 1–3 Kupferbarren."
                : "The copper golem sorts within 32 blocks (in every direction): with an empty hand it takes up to 16 "
                + "of the first item from the nearest copper chest. It then carries it to the nearest chest, trapped "
                + "chest, barrel or Iron Chest (not copper) that is empty or already holds that item; it looks into "
                + "each for 3 seconds. After 10 failures it wanders for a moment and starts over. Right click with an "
                + "empty hand takes its item. It oxidizes in four stages (about 7 hours of game time each) and keeps "
                + "working; honeycomb waxes, an axe scrapes, lightning takes one stage off. 12 health, no fall damage, "
                + "drops 1–3 copper ingots.");
        add("emi.craft.info.copper_chest", german
                ? "Die Kupferkiste oxidiert wie ein Kupferblock (angelaufen, verwittert, oxidiert); das ist nur "
                + "Optik, Inhalt und Funktion bleiben. Honigwabe wachst, Axt schleichend: Wachs ab bzw. eine Stufe "
                + "zurück, Blitz macht sie wieder frisch. Abgebaut droppt sie immer frisch. Kupfergolems holen aus "
                + "Kupferkisten, legen aber nie hinein."
                : "The copper chest oxidizes like a copper block (exposed, weathered, oxidized); that is only looks, "
                + "contents and function stay. Honeycomb waxes, axe while sneaking: removes wax or one stage, "
                + "lightning makes it fresh again. Broken, it always drops fresh. Copper golems take items from copper "
                + "chests but never put any in.");
    }

    /** IC2 Nuclear Control (spec 46 §1); English names and messages as in the pack's lang file. */
    protected void nuclearControl(boolean german) {
        block(NcBlocks.THERMAL_MONITOR, german ? "Thermomonitor" : "Thermal Monitor");
        block(NcBlocks.INDUSTRIAL_ALARM, german ? "Industriealarm" : "Industrial Alarm");
        block(NcBlocks.HOWLER_ALARM, german ? "Heulalarm" : "Howler Alarm");
        block(NcBlocks.REMOTE_THERMAL_MONITOR, german ? "Fern-Thermomonitor" : "Remote Thermal Monitor");
        item(NcItems.THERMOMETER, "Thermometer");
        item(NcItems.DIGITAL_THERMOMETER, german ? "Digitalthermometer" : "Digital Thermometer");
        item(NcItems.REMOTE_SENSOR_KIT, german ? "Reaktor-Sensorkit" : "Remote Sensor Kit");
        item(NcItems.REACTOR_SENSOR_LOCATION_CARD, german ? "Reaktor-Sensorkarte" : "Reactor Sensor Location Card");
        item(NcItems.RANGE_UPGRADE, german ? "Reichweiten-Upgrade" : "Range Upgrade");
        add("message.craft.nuclearcontrol.thermo", german ? "Hüllentemperatur: %s" : "Hull heat: %s");
        add("message.craft.nuclearcontrol.thermo_digital", german
                ? "Hüllentemperatur: %s (Wasser verdampft: %s / Schmelzen: %s)"
                : "Hull heat: %s (Water evaporate: %s / melting: %s)");
        add("message.craft.nuclearcontrol.sensor_kit", german
                ? "Fernsensor montiert, Sensorkarte erhalten" : "Remote Sensor mounted, Sensor Location Card received");
        add("gui.craft.nuclearcontrol.invert_redstone", german ? "Redstone umkehren" : "Invert redstone");
        add("gui.craft.nuclearcontrol.alarm_range", german ? "Reichweite: %s" : "Sound range: %s");
        add("gui.craft.nuclearcontrol.alarm_sound", german ? "Ton" : "Sound");
        block(NcBlocks.INFORMATION_PANEL, german ? "Industrie-Infotafel" : "Industrial Information Panel");
        block(NcBlocks.INFORMATION_PANEL_EXTENDER, german ? "Infotafel-Erweiterung" : "Information Panel Extender");
        item(NcItems.ENERGY_SENSOR_KIT, german ? "Energie-Sensorkit" : "Energy Sensor Kit");
        item(NcItems.LIQUID_SENSOR_KIT, german ? "Flüssigkeits-Sensorkit" : "Liquid Sensor Kit");
        item(NcItems.ENERGY_SENSOR_LOCATION_CARD, german ? "Energie-Sensorkarte" : "Energy Sensor Location Card");
        item(NcItems.ENERGY_ARRAY_LOCATION_CARD, german ? "Energiefeld-Karte" : "Energy Array Location Card");
        item(NcItems.LIQUID_SENSOR_LOCATION_CARD, german ? "Flüssigkeits-Sensorkarte"
                : "Liquid Sensor Location Card");
        item(NcItems.TEXT_CARD, german ? "Textkarte" : "Text Card");
        item(NcItems.TIME_CARD, german ? "Zeitkarte" : "Time Card");
        item(NcItems.COLOR_UPGRADE, german ? "Farb-Upgrade" : "Color Upgrade");
        add("gui.craft.nuclearcontrol.screen_color", german ? "Bildschirmfarbe" : "Screen Color");
        add("gui.craft.nuclearcontrol.text_color", german ? "Textfarbe" : "Text Color");
        add("tooltip.craft.nuclearcontrol.card_quantity", german ? "Anzahl Karten: %s" : "Cards quantity: %s");
        String panel = "panel.craft.nuclearcontrol.";
        add(panel + "heat", "T: %s");
        add(panel + "max_heat", german ? "Max. Hitze: %s" : "MaxHeat: %s");
        add(panel + "melting", german ? "Schmelzpunkt: %s" : "Melting: %s");
        add(panel + "output", german ? "Ausgabe: %sEU/t" : "Output: %sEU/t");
        add(panel + "time_remaining", german ? "Restzeit: %s" : "Remaining: %s");
        add(panel + "on", german ? "An" : "On");
        add(panel + "off", german ? "Aus" : "Off");
        add(panel + "energy", german ? "Energie: %s" : "Energy: %s");
        add(panel + "energy_free", german ? "Frei: %s" : "Free: %s");
        add(panel + "energy_storage", german ? "Speicher: %s" : "Storage: %s");
        add(panel + "energy_percentage", german ? "Füllstand: %s%%" : "Fill: %s%%");
        add(panel + "energy_n", german ? "#%s Energie: %s" : "#%s Energy: %s");
        add(panel + "energy_free_n", german ? "#%s Frei: %s" : "#%s Free: %s");
        add(panel + "energy_storage_n", german ? "#%s Speicher: %s" : "#%s Storage: %s");
        add(panel + "energy_percentage_n", german ? "#%s Füllstand: %s%%" : "#%s Fill: %s%%");
        add(panel + "liquid_name", "Name: %s");
        add(panel + "liquid_amount", german ? "Menge: %s" : "Amount: %s");
        add(panel + "liquid_free", german ? "Frei: %s" : "Free: %s");
        add(panel + "liquid_capacity", german ? "Kapazität: %s" : "Capacity: %s");
        add(panel + "liquid_percentage", german ? "Füllstand: %s%%" : "Fill: %s%%");
        add(panel + "time", german ? "Zeit: %s" : "Time: %s");
        add(panel + "none", german ? "k. A." : "N/A");
        add(panel + "out_of_range", german ? "Außer Reichweite" : "Out Of Range");
        add(panel + "invalid_card", german ? "Ungültige Karte" : "Invalid Card");
        add(panel + "no_target", german ? "Ziel nicht gefunden" : "Target Not Found");
        String setting = "gui.craft.nuclearcontrol.panel_setting.";
        add(setting + "on_off", german ? "An/Aus-Status" : "On/Off status");
        add(setting + "heat", german ? "Hitze" : "Heat level");
        add(setting + "max_heat", german ? "Max. Hitze" : "Max heat");
        add(setting + "melting", german ? "Schmelzpunkt" : "Melting temp");
        add(setting + "output", german ? "Ausgabe (EU/t)" : "Output (EU/t)");
        add(setting + "time_remaining", german ? "Zeit bis Zyklusende" : "Time to cycle end");
        add(setting + "energy_current", german ? "Energie" : "Energy");
        add(setting + "energy_storage", german ? "Speicher" : "Storage");
        add(setting + "energy_free", german ? "Frei" : "Free");
        add(setting + "energy_percentage", german ? "Füllstand in %" : "Fill percentage");
        add(setting + "energy_each", german ? "Je Karte" : "Each card info");
        add(setting + "energy_total", german ? "Summe" : "Summary");
        add(setting + "liquid_name", german ? "Flüssigkeit" : "Liquid name");
        add(setting + "liquid_amount", german ? "Menge" : "Amount");
        add(setting + "liquid_free", german ? "Frei" : "Free");
        add(setting + "liquid_capacity", german ? "Kapazität" : "Capacity");
        add(setting + "liquid_percentage", german ? "Füllstand in %" : "Fill percentage");
        block(NcBlocks.ENERGY_COUNTER, german ? "Energiezähler" : "Energy Counter");
        block(NcBlocks.AVERAGE_COUNTER, german ? "Durchschnittszähler" : "Average Counter");
        block(NcBlocks.RANGE_TRIGGER, german ? "Bereichsauslöser" : "Range Trigger");
        item(NcItems.COUNTER_SENSOR_KIT, german ? "Zähler-Sensorkit" : "Counter Sensor Kit");
        item(NcItems.COUNTER_SENSOR_LOCATION_CARD, german ? "Zähler-Sensorkarte" : "Counter Sensor Location Card");
        item(NcItems.GT_SENSOR_KIT, german ? "GregTech-Sensorkit" : "GregTech Sensor Kit");
        item(NcItems.GT_SENSOR_CARD, german ? "GregTech-Sensorkarte" : "GregTech Sensor Card");
        add("tooltip.craft.nuclearcontrol.gt_sensor_kit", german ? "An GregTech-Maschinen anbringen"
                : "Attach to GregTech Machines");
        add("tooltip.craft.nuclearcontrol.gt_sensor_card", german ? "In eine Infotafel einsetzen"
                : "Insert into Display Panel");
        add("gui.craft.nuclearcontrol.reset", german ? "Zurücksetzen" : "Reset");
        add("gui.craft.nuclearcontrol.average_period", german ? "Zeitraum: %ss" : "Period: %ssec");
        add(panel + "energy_counter", german ? "Energie: %sEU" : "Energy: %sEU");
        add(setting + "gt_primary", german ? "Primär" : "Primary");
        add(setting + "gt_secondary", german ? "Sekundär" : "Secondary");
        add(setting + "gt_tertiary", german ? "Tertiär" : "Tertiary");
    }

    /** Charge Pads (spec 47 §1); English names and messages as in the pack. */
    /** Modular Force Field System (spec 48); force energy: EN "FE", DE "KE" (Kraftenergie, §11). */
    private void upgrade(boolean german, ProjectorUpgrade upgrade, String name) {
        item(MffsItems.upgrade(upgrade), german ? "Projektor-Upgrade ‹" + name + "›"
                : "MFFS Projector Upgrade <" + name + ">");
    }

    private static String moduleNameDe(ProjectorModule module) {
        return switch (module) {
            case WALL -> "Wand";
            case DEFLECTOR -> "Deflektor";
            case TUBE -> "Röhre";
            case CUBE -> "Würfel";
            case SPHERE -> "Kugel";
            case CONTAINMENT -> "Eindämmung";
            case ADV_CUBE -> "Erweiterter Würfel";
            case DIAGONAL_WALL -> "Diagonale Wand";
        };
    }

    protected void mffs(boolean german) {
        add("itemGroup.craft.mffs", "Craft: Modular Force Field System");
        block(MffsBlocks.MONAZIT_ORE, german ? "Monazit-Erz" : "Monazit Ore");
        block(MffsBlocks.DEEPSLATE_MONAZIT_ORE, german ? "Tiefenschiefer-Monazit-Erz" : "Deepslate Monazit Ore");
        block(MffsBlocks.EXTRACTOR, german ? "MFFS-Extraktor" : "MFFS Extractor");
        block(MffsBlocks.CAPACITOR, german ? "MFFS-Kondensator" : "MFFS Capacitor");
        block(MffsBlocks.CONVERTER, german ? "MFFS-Konverter" : "MFFS Converter");
        block(MffsBlocks.PROJECTOR, german ? "MFFS-Projektor" : "MFFS Projector");
        block(MffsBlocks.FORCE_FIELD, german ? "Kraftfeld" : "Force Field");
        item(MffsItems.module(ProjectorModule.WALL), german ? "Projektormodul Wand" : "MFFS Projector Module Wall");
        item(MffsItems.module(ProjectorModule.DEFLECTOR), german ? "Projektormodul Deflektor"
                : "MFFS Projector Module Deflector");
        item(MffsItems.module(ProjectorModule.TUBE), german ? "Projektormodul Röhre" : "MFFS Projector Module Tube");
        item(MffsItems.module(ProjectorModule.CUBE), german ? "Projektormodul Würfel" : "MFFS Projector Module Cube");
        item(MffsItems.module(ProjectorModule.SPHERE), german ? "Projektormodul Kugel"
                : "MFFS Projector Module Sphere");
        item(MffsItems.module(ProjectorModule.CONTAINMENT), german ? "Projektormodul Eindämmung"
                : "MFFS Projector Module Containment");
        item(MffsItems.module(ProjectorModule.ADV_CUBE), german ? "Projektormodul Erweiterter Würfel"
                : "MFFS Projector Module Adv.Cube");
        item(MffsItems.module(ProjectorModule.DIAGONAL_WALL), german ? "Projektormodul Diagonale Wand"
                : "MFFS Projector Module diagonal Wall");
        item(MffsItems.FORCICIUM, "Forcicium");
        item(MffsItems.FORCICIUM_CELL, german ? "Kompakte Forcicium-Zelle" : "MFFS compact Forcicium Cell");
        item(MffsItems.FORCE_ENERGY_CRYSTAL, german ? "Kraftenergie-Kristall" : "MFFS Force Energy Crystal");
        item(MffsItems.PROJECTOR_FOCUS_MATRIX, german ? "Projektor-Fokusmatrix" : "MFFS Projector Focus Matrix");
        item(MffsItems.FIELD_MODULATOR_DISTANCE, german ? "Feldmodulator ‹Distanz›"
                : "MFFS Projector Field Modulator <distance>");
        item(MffsItems.FIELD_MODULATOR_STRENGTH, german ? "Feldmodulator ‹Stärke›"
                : "MFFS Projector Field Modulator <strength>");
        item(MffsItems.EXTRACTOR_BOOSTER, german ? "Extraktor-Beschleuniger" : "MFFS Extractor Booster");
        item(MffsItems.CAPACITOR_UPGRADE_CAPACITY, german ? "Kondensator-Upgrade ‹Kapazität›"
                : "MFFS Capacitor Upgrade <Capacity>");
        item(MffsItems.CAPACITOR_UPGRADE_RANGE, german ? "Kondensator-Upgrade ‹Reichweite›"
                : "MFFS Capacitor Upgrade <Range>");
        item(MffsItems.CARD_BLANK, german ? "MFFS-Karte ‹leer›" : "MFFS Card <blank>");
        item(MffsItems.CARD_POWER_LINK, german ? "MFFS-Karte ‹Power-Link›" : "MFFS Card <Power Link>");
        item(MffsItems.CARD_INFINITE_POWER, german ? "MFFS-Unendlich-Energie-Karte" : "MFFS Infinite Power Card");
        item(MffsItems.MULTITOOL_WRENCH, german ? "MFFS-Multiwerkzeug ‹Schraubenschlüssel›"
                : "MFFS MultiTool <Wrench>");
        item(MffsItems.MULTITOOL_SWITCH, german ? "MFFS-Multiwerkzeug ‹Schalter›" : "MFFS MultiTool <Switch>");
        item(MffsItems.MULTITOOL_FIELD_TELEPORTER, german ? "MFFS-Multiwerkzeug ‹Feld-Teleporter›"
                : "MFFS MultiTool <Field Teleporter>");
        upgrade(german, ProjectorUpgrade.TOUCH_DAMAGE, german ? "Berührungsschaden" : "touch damage");
        upgrade(german, ProjectorUpgrade.SPONGE, german ? "Schwamm" : "Sponge");
        upgrade(german, ProjectorUpgrade.FIELD_MANIPULATOR, german ? "Feldmanipulator" : "Field Manipulator");
        upgrade(german, ProjectorUpgrade.BLOCK_BREAKER, german ? "Blockbrecher" : "Block Breaker");
        upgrade(german, ProjectorUpgrade.FIELD_JAMMER, german ? "Kraftfeld-Störsender" : "Force Field Jammer");
        upgrade(german, ProjectorUpgrade.CAMOUFLAGE, german ? "Tarnung" : "Camouflage");
        upgrade(german, ProjectorUpgrade.FIELD_FUSION, german ? "Feldfusion" : "Field Fusion");
        upgrade(german, ProjectorUpgrade.NPC_DEFENSE, german ? "NPC-Abwehr" : "NPC Defense");
        upgrade(german, ProjectorUpgrade.DEFENSE_STATION, german ? "Verteidigungsstation" : "Defense Station");
        add("tooltip.craft.mffs.compatible_with_hint", german ? "passt zu: (Umschalt halten)"
                : "compatible with: (Hold Shift)");
        add("tooltip.craft.mffs.compatible_with", german ? "passt zu:" : "compatible with:");
        for (ProjectorModule module : ProjectorModule.values()) {
            add("tooltip.craft.mffs.module." + MffsItems.module(module).getId().getPath(),
                    german ? moduleNameDe(module) : module.getDisplayName());
        }
        add("message.craft.mffs.multitool.projector_repaired", german ? "[Multiwerkzeug] Projektor repariert"
                : "[MultiTool] Projector repaired");
        add("message.craft.mffs.multitool.not_enough_repair", german
                ? "[Multiwerkzeug] Fehler: nicht genug KE, bitte laden, mindestens %s nötig"
                : "[MultiTool] Fail: not enough FP please charge need min %s");
        add("message.craft.mffs.teleporter.granted", german ? "[Feldsicherheit] Erfolg: Zugriff gewährt"
                : "[Field Security] Success: access granted");
        add("message.craft.mffs.teleporter.too_strong", german ? "[Feldsicherheit] Fehler: Feld zu stark >= 5 Blöcke"
                : "[Field Security] Fail: Field to Strong >= 5 Blocks");
        add("message.craft.mffs.teleporter.obstacle", german ? "[Feldsicherheit] Fehler: Hindernis erkannt"
                : "[Field Security] Fail: detected obstacle");
        add("message.craft.mffs.teleporter.void", german
                ? "[Feldsicherheit] Fehler: Übertragung ins Leere nicht erlaubt"
                : "[Field Security] Fail: transmission into Void not allowed");
        add("message.craft.mffs.teleporter.not_enough", german ? "[Feldsicherheit] Fehler: nicht genug KE, bitte laden"
                : "[Field Security] Fail: not enough FP please charge");
        add("message.craft.mffs.teleporter.complete", german ? "[Feldsicherheit] Erfolg: Übertragung abgeschlossen"
                : "[Field Security] Success: transmission complete");
        add("message.craft.mffs.teleporter.unsupported_source", german
                ? "[Feldsicherheit] Fehler: Energiequelle des Projektors unterstützt das nicht"
                : "[Field Security] Fail: Projector Powersource not Support this activities");
        add("message.craft.mffs.area_defence_leave", german ? "!!! [Gebietsverteidigung] Verschwinde oder stirb !!!"
                : "!!! [Area Defence] leave or die !!!");
        add("death.attack.craft.field_defense", german ? "%1$s wurde gegrillt" : "%1$s was fried");
        add("death.attack.craft.field_defense.player", german ? "%1$s wurde gegrillt" : "%1$s was fried");
        add("tooltip.craft.mffs.link_to", german ? "Verknüpft mit: %s" : "Linkto: %s");
        add("tooltip.craft.mffs.world", german ? "Welt: %s" : "World: %s");
        add("tooltip.craft.mffs.coords", german ? "Koordinaten: %s/%s/%s" : "Coords: %s/%s/%s");
        add("tooltip.craft.mffs.valid", german ? "Gültig" : "Valid");
        add("tooltip.craft.mffs.invalid", german ? "Ungültig" : "Invalid");
        add("tooltip.craft.mffs.force_energy", german ? "%s KE/%s KE" : "%s FE/%s FE");
        add("tooltip.craft.mffs.forcicium_cell", "%s / %s Forcicium");
        add("tooltip.craft.mffs.infinite_power_card.1", german ? "Admin-Karte zum Versorgen von Maschinen"
                : "Admin Card to Power Maschines");
        add("tooltip.craft.mffs.infinite_power_card.2", german ? "oder zum unendlichen Laden eines Kondensators"
                : "or use to infinit charge Capactior");
        add("message.craft.mffs.access_denied", german ? "[Feldsicherheit] Fehler: Zugriff verweigert"
                : "[Field Security] Fail: access denied");
        add("message.craft.mffs.card_installed", german ? "Erfolg: %s-Karte eingesetzt" : "Success: %s Card installed");
        add("message.craft.mffs.card_copied", german ? "Erfolg: %s-Kartendaten kopiert" : "Success: %s Card data copied");
        add("message.craft.mffs.slot_not_empty", german ? "Fehler: Platz ist nicht leer" : "Fail: Slot is not empty");
        add("message.craft.mffs.power_link_created", german ? "[Kondensator] Erfolg: <Power-Link>-Karte erstellt"
                : "[Capacitor] Success: <Power-Link> Card create");
        add("message.craft.mffs.cell_active", german ? "[Forcicium-Zelle] Aktiv" : "[Forcicum Cell] Active");
        add("message.craft.mffs.cell_inactive", german ? "[Forcicium-Zelle] Inaktiv" : "[Forcicum Cell] Inactive");
        add("message.craft.mffs.multitool.not_enough", german
                ? "[Multiwerkzeug] Fehler: zu wenig KE, bitte laden, mindestens %s nötig"
                : "[MultiTool] Fail: not enough FP please charge need min %s");
        add("message.craft.mffs.multitool.not_enough_facing", german
                ? "[Multiwerkzeug] Fehler: zu wenig KE, bitte laden, mindestens %s zum Drehen nötig"
                : "[MultiTool] Fail: not enough FE please charge need min %s for change Facing");
        add("message.craft.mffs.multitool.not_enough_remove", german
                ? "[Multiwerkzeug] Fehler: zu wenig KE, bitte laden, mindestens %s zum Abbauen nötig"
                : "[MultiTool] Fail: not enough FE please charge need min %s for remove");
        add("message.craft.mffs.multitool.not_switchable", german
                ? "[Multiwerkzeug] Fehler: Gerät nicht im Schaltermodus" : "[MultiTool] Fail: Object not in switch enable mode");
        add("message.craft.mffs.multitool.not_enough_charge", german
                ? "[Multiwerkzeug] Fehler: zu wenig KE, bitte laden" : "[MultiTool] Fail: not enough FP please charge");
        add("gui.craft.mffs.force_energy", german ? "Kraftenergie" : "Force Energy");
        add("gui.craft.mffs.upgrades", "Upgrades");
        add("gui.craft.mffs.extractor", german ? "Extraktor" : "Extractor");
        add("gui.craft.mffs.capacitor", german ? "Kraftenergie-Kondensator" : "Force Energy Capacitor");
        add("gui.craft.mffs.fe", german ? "KE: %s" : "FE: %s");
        add("gui.craft.mffs.power_uplink", german ? "Power-Uplink: " : "Power Uplink: ");
        add("gui.craft.mffs.transmit_range", german ? "Sendereichweite:" : "transmit range:");
        add("gui.craft.mffs.linked_devices", german ? "verbundene Geräte:" : "linked device:");
        add("gui.craft.mffs.converter", german ? "MFFS-Konverter" : "MFFS Converter");
        add("gui.craft.mffs.packets", german ? "Pakete" : "packets");
        add("gui.craft.mffs.no_link", german ? "KE: Kein Link/OOR" : "FE: No Link/OOR");
        add("gui.craft.mffs.offline", "OFFLINE");
        add("gui.craft.mffs.projector", german ? "MFFS-Projektor" : "MFFS Projector");
        add("gui.craft.mffs.type_mode", german ? "Modultyp" : "Typ-Mode");
        add("gui.craft.mffs.power_link", "PowerLink");
        add("gui.craft.mffs.no_link_oor", german ? "Kein Link/OOR" : "No Link/OOR");
        add("message.craft.mffs.module_installed", german ? "[Projektor] Erfolg: <%s> eingesetzt"
                : "[Projector] Success: <%s> installed");
        add("message.craft.mffs.projector_slot_not_empty", german ? "[Projektor] Fehler: Platz ist nicht leer"
                : "[Projector] Fail: Slot is not empty");
        add("message.craft.mffs.high_energy_field", german ? "[Kraftfeld] Achtung: Hochenergiefeld"
                : "[Force Field] Attention High Energy Field");
        add("death.attack.craft.field_shock", german ? "%1$s wurde von einem Kraftfeld gegrillt"
                : "%1$s was fried by a forcefield");
        add("death.attack.craft.field_shock.player", german ? "%1$s wurde von einem Kraftfeld gegrillt"
                : "%1$s was fried by a forcefield");
        add("gui.craft.mffs.online", "ONLINE");
        mffsSecurity(german);
    }

    /** MFFS security, phase 48.4 (spec 48 §7, §8, §10, §13). */
    private void mffsSecurity(boolean german) {
        block(MffsBlocks.DEFENSE_STATION, german ? "MFFS-Verteidigungsstation" : "MFFS Defence Station");
        block(MffsBlocks.SECURITY_STATION, german ? "MFFS-Sicherheitsstation" : "MFFS Security Station");
        block(MffsBlocks.SECURITY_STORAGE, german ? "MFFS-Sicherheitsspeicher" : "MFFS Security Storage");
        block(MffsBlocks.CONTROL_SYSTEM, german ? "MFFS-Steuerungssystem" : "MFFS Control System");
        item(MffsItems.CARD_PERSONAL_ID, german ? "MFFS-Karte ‹Personalausweis›" : "MFFS Card <Personal ID>");
        item(MffsItems.CARD_SECURITY_LINK, german ? "MFFS-Karte ‹Sicherheitsstations-Link›"
                : "MFFS Card <Security Station Link>");
        item(MffsItems.CARD_ACCESS, german ? "MFFS-Karte ‹Zugangslizenz›" : "MFFS Card <Access license>");
        item(MffsItems.CARD_DATA_LINK, german ? "MFFS-Karte ‹Datenlink›" : "MFFS Card <Data Link>");
        item(MffsItems.MULTITOOL_ID_WRITER, german ? "MFFS-Multiwerkzeug ‹Ausweis- & Datenlink-Kodierer›"
                : "MFFS MultiTool <PersonalID & Data Link Coder>");
        item(MffsItems.MULTITOOL_GUIDE, german ? "MFFS-Multiwerkzeug ‹Handbuch›" : "MFFS MultiTool <Guide>");
        item(MffsItems.MULTITOOL_DEBUGGER, german ? "MFFS-Multiwerkzeug ‹Debugger›" : "MFFS MultiTool <Debugger>");
        String[][] rights = {
                {"FFB", "Kraftfeld-Durchgang"}, {"EB", "MFFS-Block bearbeiten"}, {"CSR", "Sicherheitsrechte vergeben"},
                {"SR", "Im Gebiet bleiben"}, {"OSS", "Sicherheitsspeicher öffnen"}, {"RPB", "Geschützten Block ändern"},
                {"AAI", "Alle Gegenstände erlaubt"}, {"UCS", "Steuerungssystem benutzen"}};
        for (String[] right : rights) {
            SecurityRight value = SecurityRight.byKey(right[0]);
            add(value.translationKey(), german ? right[1] : value.getDisplayName());
        }
        String[][] kinds = {
                {"PROJECTOR", "Projector", "Projektor"}, {"EXTRACTOR", "Extractor", "Extraktor"},
                {"CAPACITOR", "Capacitor", "Kondensator"}, {"CONVERTER", "Converter", "Konverter"},
                {"DEFENSE_STATION", "Defence Station", "Verteidigungsstation"},
                {"SECURITY_STATION", "Security Station", "Sicherheitsstation"},
                {"SECURITY_STORAGE", "Security Storage", "Sicherheitsspeicher"},
                {"CONTROL_SYSTEM", "Control System", "Steuerungssystem"}};
        for (String[] kind : kinds) {
            add(MffsDevices.Kind.byName(kind[0]).translationKey(), german ? kind[2] : kind[1]);
        }
        add("tooltip.craft.mffs.owner", german ? "Besitzer: %s" : "Owner: %s");
        add("tooltip.craft.mffs.rights_hint", german ? "Rechte: (Umschalt halten)" : "Rights: (Hold Shift)");
        add("tooltip.craft.mffs.rights", german ? "Rechte:" : "Rights:");
        add("tooltip.craft.mffs.security_area", german ? "Sicherheitsbereich: %s" : "Security Area: %s");
        add("tooltip.craft.mffs.validity", german ? "Gültigkeit: %s min" : "period of validity: %s min");
        add("tooltip.craft.mffs.device_type", german ? "Gerätetyp: %s" : "DeviceTyp: %s");
        add("tooltip.craft.mffs.device_name", german ? "Gerätename: %s" : "DeviceName: %s");
        add("message.craft.mffs.license_expired", german
                ? "[Sicherheitsstation] Gültigkeit abgelaufen <Zugangslizenz>"
                : "[Security Station] expired validity <Access license>");
        add("message.craft.mffs.station_must_be_active", german
                ? "[Sicherheitsstation] Fehler: Sicherheitsstation muss zum Erstellen aktiv sein"
                : "[Security Station] Fail: Security Station must be Active  for create");
        add("message.craft.mffs.security_link_created", german
                ? "[Sicherheitsstation] Erfolg: <Sicherheitsstations-Link>-Karte erstellt"
                : "[Security Station] Success: <Security Station Link>  Card create");
        add("message.craft.mffs.multitool.need_blank_card", german
                ? "[Multiwerkzeug] Fehler: MFFS-Karte ‹leer› im Inventar nötig"
                : "[MultiTool] Fail: need MFFS Card <blank> in  Inventory");
        add("message.craft.mffs.multitool.id_card_created", german ? "[Multiwerkzeug] Erfolg: Ausweis erstellt"
                : "[MultiTool] Success: ID-Card create");
        add("message.craft.mffs.multitool.data_link_created", german
                ? "[Multiwerkzeug] Erfolg: Datenlink-Karte erstellt" : "[MultiTool] Success: DataLink-Card create");
        add("message.craft.mffs.defence.warning", german
                ? "!!! [Sicherheitsstation][%s] Warnung: du bist in meinem Scanbereich !!!"
                : "!!! [Security Station][%s] Warning you now in me Scanning range!!!");
        add("message.craft.mffs.defence.get_out", german
                ? "!!! [Gebietsverteidigung] Verschwinde sofort, du hast hier kein Recht zu sein !!!"
                : "!!! [Area Defence]  get out immediately you have no right to be here!!!");
        add("message.craft.mffs.defence.bye", german ? "!!! [Gebietsverteidigung] Du wurdest gewarnt – TSCHÜSS !!!"
                : "!!! [Area Defence] you have been warned BYE BYE!!!");
        add("message.craft.mffs.defence.illegal", german
                ? "!!! [Gebietsverteidigung] Du hast verbotene Ware <%s> – sie wird beschlagnahmt !!!"
                : "!!! [Area Defence] You  have illegal goods <%s> will be confiscated!!!");
        add("death.attack.craft.area_defense", german ? "%1$s hat Warnungen missachtet und wurde gegrillt"
                : "%1$s disregarded warnings and was fried");
        add("death.attack.craft.area_defense.player", german ? "%1$s hat Warnungen missachtet und wurde gegrillt"
                : "%1$s disregarded warnings and was fried");
        add("gui.craft.mffs.security_station", german ? "MFFS-Sicherheitsstation:" : "MFFS Security Station:");
        add("gui.craft.mffs.master", german ? "Meister" : "Master");
        add("gui.craft.mffs.rights_allocation", german ? "Rechtevergabe" : "Rights Allocation");
        add("gui.craft.mffs.copy", german ? "Kopie->" : "Copy->");
        add("gui.craft.mffs.validity", german ? "Gültigkeit" : "validity");
        add("gui.craft.mffs.security_storage", german ? "MFFS-Sicherheitsspeicher" : "MFFS Security Storage");
        add("gui.craft.mffs.control_system", german ? "MFFS-Steuerungssystem" : "MFFS Control System");
        add("gui.craft.mffs.data_link", german ? "Datenlink" : "DataLink");
        add("gui.craft.mffs.reader", german ? "Leser" : "Reader");
        add("gui.craft.mffs.remote_name", german ? "Name: %s" : "Name: %s");
        add("gui.craft.mffs.remote_type", german ? "Typ:  %s" : "Type:  %s");
        add("gui.craft.mffs.linked", german ? "verbunden" : "linked");
        add("gui.craft.mffs.power_left", german ? "Energie übrig: %s%%" : "Power left: %s%%");
        add("gui.craft.mffs.open_remote_gui", german ? "Fern-GUI öffnen" : "Open Remote Gui");
        add("gui.craft.mffs.defense_station", german ? "MFFS-Verteidigungsstation" : "MFFS Defence Station");
        add("gui.craft.mffs.action_desc", german ? "Aktion:" : "Action desc:");
        add("gui.craft.mffs.items", german ? "Gegenst." : "items");
        add("gui.craft.mffs.allowed", german ? "erlaubt" : "allowed");
        add("gui.craft.mffs.banned", german ? "verboten" : "banned");
        add("gui.craft.mffs.fe_percent", german ? "KE: %s %%" : "FE: %s %%");
        add("gui.craft.mffs.warning", german ? "Warn-" : "warning");
        add("gui.craft.mffs.action", german ? "Aktions-" : "action");
        add("gui.craft.mffs.perimeter", german ? "bereich: %s" : "perimeter: %s");
        add("gui.craft.mffs.inventory", german ? "Lager" : "inventory ");
        String[][] modes = german ? new String[][] {
                {"informieren", " sendet Info", " an Spieler", " ohne (SR)", " Bleiberecht"},
                {"töten", " tötet Spieler", " ohne (SR),", " sammelt seine", " Ausrüstung"},
                {"durchsuchen", "durchsucht", "Spieler ohne", "(AAI), nimmt", "Verbotenes"},
                {"NPCs töten", "tötet jeden NPC", "freundlich oder", "feindlich"},
                {"NPCs töten", "tötet nur", "feindliche NPCs"},
                {"NPCs töten", "tötet nur", "freundliche NPCs"}}
                : new String[][] {
                {"inform", " send Info", " to player ", " without (SR)", " Stay Right"},
                {"kill", " kill player", " without (SR)", " gathers his", " equipment"},
                {"search", "scans player", "without (AAI)", "and remove", "banned items"},
                {"NPC kill", "kill any NPC", "friendly or", "hostile"},
                {"NPC kill", "kill only", "hostile NPCs"},
                {"NPC kill", "kill only", "friendly NPCs"}};
        for (int mode = 0; mode < modes.length; mode++) {
            add("gui.craft.mffs.action_mode." + mode, modes[mode][0]);
            for (int line = 1; line < modes[mode].length; line++) {
                add("gui.craft.mffs.action_mode." + mode + "." + (line - 1), modes[mode][line]);
            }
        }
        add("gui.craft.mffs.guide", german ? "Modular-Force-Field-System-Handbuch" : "ModularForceFieldSystem Guide");
        add("gui.craft.mffs.guide.page", german ? "Seite [%s] :%s" : "Page [%s] :%s");
        add("gui.craft.mffs.guide.contents", german ? "Inhaltsverzeichnis" : "Table of Contents");
        add("gui.craft.mffs.guide.title.toc", german ? "Inhalt" : "Table of Content");
        add("gui.craft.mffs.guide.title.monazit", german ? "Monazit/Forcicium/-Zelle" : "Monazit/Forcicium/-Cell");
        add("gui.craft.mffs.guide.title.cards_1", german ? "Kartenübersicht(1)" : "Card Overview(1)");
        add("gui.craft.mffs.guide.title.cards_2", german ? "Kartenübersicht(2)" : "Card Overview(2)");
        String[] monazit = german ? new String[] {
                "Monazit-Erz (Block/Weltgen)", "Forcicium (Item/zum Craften)", "Forcicium-Zelle (Item/gecraftet)",
                "Monazit liegt zwischen Höhe 80 und 0", "Im Ofen gibt es 4 Forcicium", "Im IC-Macerator 8 Forcicium",
                "Die Zelle speichert 1k Forcicium", "in der Hand Rechtsklick: aktivieren", "aktiv zieht sie Forcicium aus",
                "dem Spielerinventar und speichert es"}
                : new String[] {
                "Monazit Ore (Block/WorldGen)", "Forcicium (Item/for Crafting)", "Forcicium Cell (Item/from Crafting)",
                "Monazite can be found between 80 and 0", "Use furnace to get 4 Forcicium",
                "Use IC Macerator to get 8 Forcicium", "Forcicium Cell can store 1kForcicium",
                "if in hand right click to activate", "when active remove Forcicium from  ",
                "Player Inventory and stores it"};
        String[] cards1 = german ? new String[] {
                "Karte ‹leer› aus der Werkbank, stapelbar", "bis 16, nötig für alle Karten",
                "Karte ‹Power-Link› per Rechtsklick auf", "Kondensator mit einer", "Karte ‹Sicherheitsstations-Link›",
                "per Rechtsklick auf", "Sicherheitsstation mit"}
                : new String[] {
                "Card <blank> get from Crafting stackable ", "up to 16 need for create all Cards ",
                "Card <Power Link> get from right click", "Capacitor with a ", "Card <Security Station Link> get from",
                "right click", "SecurityStation with a "};
        String[] cards2 = german ? new String[] {
                "Karte ‹Zugangslizenz› entsteht in einer", "Sicherheitsstation mit", "Karte ‹Personalausweis› mit dem",
                "Multiwerkzeug: Rechtsklick für sich", "oder Linksklick für einen Spieler",
                "Karte ‹Datenlink› mit dem", "Multiwerkzeug: Rechtsklick auf eine", "MFFS-Maschine",
                "Admin-Karte für unendliche Kraftenergie"}
                : new String[] {
                "Card <Access license> create inside a  ", "SecurityStation with a", "Card <Personal ID> create with help  ",
                "MultiTool right click create self", "or left click for another Player",
                "Card <Data Link> create with help  ", "MultiTool right click any ", "MFFS Maschine ",
                "Admin Infinite Force Energy Card"};
        String[][] pages = {monazit, cards1, cards2};
        String[] pageNames = {"monazit", "cards_1", "cards_2"};
        for (int page = 0; page < pages.length; page++) {
            for (int line = 0; line < pages[page].length; line++) {
                add("gui.craft.mffs.guide." + pageNames[page] + "." + line, pages[page][line]);
            }
        }
    }

    protected void chargePads(boolean german) {
        block(CpBlocks.STATIC_CHARGE_PAD, german ? "Statisches Ladepad" : "Static Charge Pad");
        block(CpBlocks.CRYSTALIZOR_CHARGE_PAD, german ? "Kristallisator-Ladepad" : "Crystalizor Charge Pad");
        block(CpBlocks.LAPOTRONIC_CHARGE_PAD, german ? "Lapotron-Ladepad" : "Lapotronic Charge Pad");
        item(CpItems.CRYSTALIZOR_UPGRADE_KIT, german ? "Kristallisator-Ausbausatz" : "Crystalizor Upgrade Kit");
        item(CpItems.MFS_UNIT_UPGRADE, german ? "MFSU-Aufrüstung" : "MFS Unit Upgrade");
        item(CpItems.LAPOTRONIC_UPGRADE_KIT, german ? "Lapotron-Ausbausatz" : "Lapotronic Upgrade Kit");
        item(CpItems.EFFICIENCY_UPGRADE, german ? "Effizienz-Upgrade" : "Efficiency Upgrade");
        item(CpItems.module(PadModule.DRAIN_CONVERSION), german ? "Entladungs-Umwandlungsmodul"
                : "Drain Conversion Module");
        item(CpItems.module(PadModule.DAMAGE_CONVERSION), german ? "Schadens-Umwandlungsmodul"
                : "Damage Conversion Module");
        item(CpItems.module(PadModule.ARMOUR_PRIORITY), german ? "Rüstungsvorrang-Modul" : "Armour Priority Module");
        item(CpItems.module(PadModule.PROXIMITY_BOOSTER), german ? "Nahbereichs-Verstärkermodul"
                : "Proximity Booster Module");
        item(CpItems.module(PadModule.WIDE_BAND_BOOSTER), german ? "Breitband-Verstärkermodul"
                : "Wide-Band Booster Module");
        item(CpItems.module(PadModule.FIELD_EXPANSION_ALPHA), german ? "Feldausdehnungsmodul (Alpha)"
                : "Field Expansion Module (Alpha)");
        item(CpItems.module(PadModule.FIELD_EXPANSION_DELTA), german ? "Feldausdehnungsmodul (Delta)"
                : "Field Expansion Module (Delta)");
        item(CpItems.module(PadModule.FIELD_EXPANSION_THETA), german ? "Feldausdehnungsmodul (Theta)"
                : "Field Expansion Module (Theta)");
        add("tooltip.craft.chargepads.restricted", german ? "Beschränkt: " : "Restricted: ");
        add("tooltip.craft.chargepads.emitter_slot", german ? "Emitterplatz" : "Emitter Slot");
        add("tooltip.craft.chargepads.disabled", german ? "DEAKTIVIERT" : "DISABLED");
        add("tooltip.craft.chargepads.modulation", german ? "Modulation: " : "Modulation: ");
        add("tooltip.craft.chargepads.strength", german ? "Stärke: " : "Strength: ");
        add("tooltip.craft.chargepads.strength.alpha", "Alpha");
        add("tooltip.craft.chargepads.strength.delta", "Delta");
        add("tooltip.craft.chargepads.strength.theta", "Theta");
        add("message.craft.chargepads.upgrade_zap", german ? "Das Pad ist aktiv und verpasst dir einen Stromschlag!"
                : "The pad is active and electrocutes you!");
        add("gui.craft.chargepads.field", german ? "Feld" : "Field");
        add("gui.craft.chargepads.projector", german ? "Projektor" : "Projector");
        add("death.attack.craft.electrocution", german ? "%1$s starb an einem Stromschlag" : "%1$s died from shock");
        add("death.attack.craft.electrocution.player", german
                ? "%1$s starb im Kampf mit %2$s an einem Stromschlag" : "%1$s died from shock whilst fighting %2$s");
        add("death.attack.craft.emitter_radiation", german ? "%1$s starb an Emitterstrahlung"
                : "%1$s died from emitter radiation");
        add("death.attack.craft.emitter_radiation.player", german
                ? "%1$s starb im Kampf mit %2$s an Emitterstrahlung"
                : "%1$s died from emitter radiation whilst fighting %2$s");
    }

    /** GraviSuite (spec 28 §1); English names as in the pack. */
    protected void graviSuite(boolean german) {
        item(GsItems.SUPERCONDUCTOR_COVER, german ? "Supraleiter-Hülle" : "Superconductor Cover");
        item(GsItems.SUPERCONDUCTOR, german ? "Supraleiter" : "Superconductor");
        item(GsItems.COOLING_CORE, german ? "Kühlkern" : "Cooling Core");
        item(GsItems.GRAVITATION_ENGINE, german ? "Gravitationsantrieb" : "Gravitation Engine");
        item(GsItems.MAGNETRON, "Magnetron");
        item(GsItems.VAJRA_CORE, german ? "Vajra-Kern" : "Vajra Core");
        item(GsItems.ENGINE_BOOSTER, german ? "Triebwerksverstärker" : "Engine Booster");
        item(GsItems.ADVANCED_LAPPACK, german ? "Fortgeschrittenes LapPack" : "Advanced Lappack");
        item(GsItems.ULTIMATE_LAPPACK, german ? "Ultimatives LapPack" : "Ultimate Lappack");
        item(GsItems.ADVANCED_JETPACK, german ? "Fortgeschrittenes elektrisches Jetpack" : "Advanced Electric Jetpack");
        item(GsItems.ADVANCED_NANO_CHESTPLATE, german ? "Fortgeschrittene Nano-Brustplatte" : "Advanced NanoChestPlate");
        item(GsItems.ADVANCED_DIAMOND_DRILL, german ? "Fortgeschrittener Diamantbohrer" : "Advanced Diamond Drill");
        item(GsItems.ADVANCED_CHAINSAW, german ? "Fortgeschrittene Kettensäge" : "Advanced Chainsaw");
        item(GsItems.VAJRA, "Vajra");
        item(GsItems.GRAVI_CHESTPLATE, german ? "Gravitations-Brustplatte" : "GraviChestPlate");
        item(GsItems.ULTIMATE_SOLAR_HELMET, german ? "Ultimativer Solarhelm" : "Ultimate Solar Helmet");
        item(GsItems.GRAVITOOL, "GraviTool");
        String tool = GsItems.GRAVITOOL.get().getDescriptionId();
        add(tool + ".hoe", german ? "GraviTool (Hacke)" : "GraviTool (Hoe)");
        add(tool + ".treetap", german ? "GraviTool (Baumzapfer)" : "GraviTool (TreeTap)");
        add(tool + ".wrench", german ? "GraviTool (Schraubenschlüssel)" : "GraviTool (Wrench)");
        add(tool + ".screwdriver", german ? "GraviTool (Schraubendreher)" : "GraviTool (Screwdriver)");
        String gt = "message.craft.gravisuite.gravitool.";
        add(gt + "hoe_activated", german ? "§2Hacke §aaktiviert" : "§2Hoe §ftool §aactivated");
        // "TreeTrap" (sic) as in the pack.
        add(gt + "treetap_activated", german ? "§6Baumzapfer §aaktiviert" : "§6TreeTrap §ftool §aactivated");
        add(gt + "wrench_activated", german ? "§bSchraubenschlüssel §aaktiviert" : "§bWrench §ftool §aactivated");
        add(gt + "screwdriver_activated", german ? "§dSchraubendreher §aaktiviert" : "§dScrewdriver §ftool §aactivated");
        add(gt + "no_energy", german ? "Nicht genug Energie für diesen Vorgang!"
                : "Not enough energy for this operation !");
        add(gt + "no_energy_operation", german ? "Nicht genug Energie für diesen Vorgang"
                : "Not enough energy for this operation");
        add(gt + "no_energy_lossless", german ? "Nicht genug Energie für verlustfreies Abbauen"
                : "Not enough energy for lossless wrench operation");
        add("hud.craft.gravisuite.gravi_on", german ? "§aGravitationsantrieb AN" : "§aGravitation engine ON");
        add("message.craft.gravisuite.gravi_on", german ? "§aGravitationsantrieb AN" : "§aGravitation engine ON");
        add("message.craft.gravisuite.gravi_off", german ? "§cGravitationsantrieb AUS" : "§cGravitation engine OFF");
        add("message.craft.gravisuite.gravi_no_energy", german ? "Nicht genug Energie für den Gravitationsantrieb!"
                : "Not enough energy to run Gravitation engine !");
        add("message.craft.gravisuite.gravi_depleted", german
                ? "§cWarnung! Die Energiezelle ist leer! Gravitationsantrieb abgeschaltet!"
                : "§cWarning ! Your's energy cell is depleted ! Gravitation engine shutdown !");
        add("hud.craft.gravisuite.magnet_on", german ? "§bGravitationsmagnet AN" : "§bGravitation magnet ON");
        add("message.craft.gravisuite.magnet_on", german ? "§bGravitationsmagnet AN" : "§bGravitation magnet ON");
        add("message.craft.gravisuite.magnet_off", german ? "§cGravitationsmagnet AUS" : "§cGravitation magnet OFF");
        add("hud.craft.gravisuite.energy_level", german ? "Energiestand: %s" : "Energy level: %s");
        add("hud.craft.gravisuite.jetpack_on", german ? "§eJetpack §aTriebwerk AN" : "§eJetpack §aengine ON");
        add("hud.craft.gravisuite.hover_mode", german ? " §e(Schwebemodus)" : " §e(Hover mode)");
        add("message.craft.gravisuite.jetpack_on", german ? "§eJetpack §aTriebwerk AN" : "§eJetpack §aengine ON");
        add("message.craft.gravisuite.jetpack_off", german ? "§eJetpack §cTriebwerk AUS" : "§eJetpack §cengine OFF");
        add("message.craft.gravisuite.hover_enabled", german ? "§eSchwebemodus §aaktiviert." : "§eHover Mode §aenabled.");
        add("message.craft.gravisuite.hover_disabled",
                german ? "§eSchwebemodus §cdeaktiviert." : "§eHover Mode §cdisabled.");
        add("emi.craft.info.gravi_chestplate", german
                ? "Gravi-Flugtaste (Standard G) schaltet den Flug ein, ab 80 000 EU. Verbraucht 416 EU/t in der Luft, "
                + "am Boden nichts; unter 416 EU schaltet sie ab. Flug so schnell wie Sprinten, kein Boost. Kein "
                + "Fallschaden. Schützt wie die Quanten-Brustplatte (900 EU je Schaden) und lädt Werkzeuge. "
                + "Gravitationsmagnet: Boost-Taste (Strg) halten + Modus-Taste (M) schaltet ihn; zieht Items (50 EU je "
                + "Stapel, nur bei Platz im Inventar) und Erfahrung im Umkreis 8 heran, auch am Boden."
                : "Gravi Fly Key (default G) turns flight on, from 80,000 EU. Uses 416 EU/t in the air, nothing on the ground; below "
                + "416 EU it shuts down. Flight is as fast as sprinting, no boost. No fall damage. "
                + "Protects like the quantum chestplate (900 EU per damage) and charges tools. Gravitation magnet: hold "
                + "the boost key (Ctrl) + mode key (M) to switch it; pulls items (50 EU per stack, only with room in "
                + "the inventory) and experience within 8 blocks, also on the ground.");
        add("emi.craft.info.advanced_jetpack", german
                ? "Gravi-Flugtaste (Standard G) schaltet das Triebwerk ein/aus, Springen gibt Schub (6 EU/t), "
                + "höchstens so schnell wie Sprinten, kein Boost. Modus-Taste (M) beim Springen: Schwebemodus (langsames Sinken). Lädt "
                + "Werkzeuge wie ein LapPack. Die Nano-Brustplatte schützt zusätzlich (800 EU je Schaden)."
                : "Gravi Fly Key (default G) switches the engine, jumping thrusts (6 EU/t), at most "
                + "as fast as sprinting, no boost. "
                + "Mode key (M) while jumping: hover mode (slow descent). Charges tools like a lappack. The nano "
                + "chestplate also protects (800 EU per damage).");
        add("emi.craft.info.gravisuite_lappack", german
                ? "Energiespeicher für den Rücken: lädt gehaltene elektrische Werkzeuge bis Stufe 2 nach. "
                + "Fortgeschritten 1 000 000 EU, Ultimativ 10 000 000 EU."
                : "Energy storage for the back: recharges held electric tools up to tier 2. Advanced 1,000,000 EU, "
                + "ultimate 10,000,000 EU.");
        add("emi.craft.info.ultimate_solar_helmet", german
                ? "Im Pack ohne Rezept, in Craft mit eigenem Rezept (Quantenhelm + Solarhelm). Erzeugt getragen 512 EU/t bei Sonne und 64 EU/t "
                + "unter freiem Himmel; lädt zuerst die Rüstung (auch sich selbst), dann das Inventar. Rüstungswert 3."
                : "No recipe in the pack, Craft adds one (quantum helmet + solar helmet). Worn it makes 512 EU/t in the sun and 64 EU/t under open "
                + "sky; charges the armor first (itself too), then the inventory. Armor value 3.");
        add("message.craft.gravisuite.drill_area_on", german ? "§e3×3-Modus §aAN" : "§e3×3 mode §aON");
        add("message.craft.gravisuite.drill_area_off", german ? "§e3×3-Modus §cAUS" : "§e3×3 mode §cOFF");
        add("tooltip.craft.gravisuite.drill_area_on", german ? "3×3-Modus: an (M + Rechtsklick)"
                : "3×3 mode: on (M + right click)");
        add("tooltip.craft.gravisuite.drill_area_off", german ? "3×3-Modus: aus (M + Rechtsklick)"
                : "3×3 mode: off (M + right click)");
        add("emi.craft.info.advanced_diamond_drill", german
                ? "Wie der Diamantbohrer, Effizienz 35, 160 EU je Block, Speicher 1 000 000 EU (im Pack 15 000). Modus-Taste "
                + "(M) + Rechtsklick schaltet den 3×3-Modus: Jeder Abbau nimmt die 8 Blöcke darum in der Ebene quer zur "
                + "getroffenen Seite mit (je 160 EU), sofern der Bohrer sie abbaut und sie nicht härter sind als der "
                + "Mittelblock (mindestens bis Erzhärte)."
                : "Like the diamond drill, efficiency 35, 160 EU per block, storage 1,000,000 EU (pack: 15,000). Mode key "
                + "(M) + right click switches the 3×3 mode: every break also takes the 8 blocks around it in the plane "
                + "across the hit face (160 EU each), if the drill mines them and they are not harder than the centre "
                + "block (at least up to ore hardness).");
        add("emi.craft.info.vajra", german
                ? "Baut jeden Block sofort mit Drops ab, 3 333 EU je Block. Rechtsklick auf einen Block "
                + "(Präzisionsmodus): Block wird entfernt und droppt, auch Endportalrahmen – nicht Grundgestein. "
                + "Schlag: 25 Schaden für 6 666 EU."
                : "Mines every block instantly with drops, 3,333 EU per block. Right click on a block (precision "
                + "mode): removes it with its drops, also end portal frames – not bedrock. Hit: 25 damage for 6,666 EU.");
        add("emi.craft.info.gravitool", german
                ? "Modus-Taste (M) + Rechtsklick wechselt: Hacke (50 EU), Baumzapfer (50 EU), Schraubenschlüssel "
                + "(drehen 50 EU, abbauen 500 EU, immer verlustfrei, bei Maschinen mit Verlust 10 000 EU; auch BuildCraft-Schlüssel für "
                + "500 EU), Schraubendreher (Verstärker, Werfer, Kolben drehen, 500 EU)."
                : "Mode key (M) + right click switches: hoe (50 EU), treetap (50 EU), wrench (turn 50 EU, remove 500 EU, "
                + "always lossless, 10,000 EU for machines that would lose; also a BuildCraft wrench for 500 EU), screwdriver (turns repeaters, "
                + "dispensers, pistons, 500 EU).");
        add("key.categories.craft", "Craft");
        add("key.craft.gravi_fly", german ? "Gravi-Flugtaste" : "Gravi Fly Key");
        add("key.craft.mps_down", german ? "Power-Rüstung: Runter (Flugsteuerung)" : "Power Armor: Down (Flight Control)");
        add("emi.craft.info.project_table", german
                ? "Werkbank mit Vorrat: Projekttisch 18 Plätze, Großer Projekttisch 27. Rezept mit je einem Item ins "
                + "Raster legen, Nachschub in den Vorrat – nach jedem Handwerk füllt sich das Raster daraus nach. Mit "
                + "beschriebenem Bauplan bleibt das Raster leer und alles kommt aus dem Vorrat. Rohre und Trichter "
                + "erreichen von allen Seiten nur den Vorrat. Der „+“-Knopf in EMI füllt aus Vorrat und Inventar."
                : "Crafting table with storage: project table 18 slots, large project table 27. Put the recipe into "
                + "the grid with one item each and the refill into the storage – after each craft the grid refills "
                + "from it. With a written plan the grid stays empty and everything comes from the storage. Pipes and "
                + "hoppers reach only the storage, from every side. EMI's \"+\" button fills from storage and "
                + "inventory.");
        add("emi.craft.info.tinker_table", german
                ? "Rechtsklick öffnet die Werkbank für Power-Rüstung und Power-Werkzeug im Inventar. Module kosten "
                + "Bauteile (Kreativ: kostenlos), Regler stellen ihre Werte ein. Ausbauen gibt jedes Bauteil mit 90 % "
                + "zurück, Kondensatoren gehen verloren. Ohne Module sind die Teile nutzlos."
                : "Right click opens the bench for power armor and power tools in the inventory. Modules cost "
                + "components (creative: free), sliders tune their values. Salvaging returns each component with 90 %, "
                + "capacitors are lost. Without modules the items are useless.");
        add("emi.craft.info.power_armor", german
                ? "Modulare Rüstung: Schutz, Energie und Fähigkeiten kommen aus Modulen (Tinker Table). Energie in Joule "
                + "(50 J = 1 EU) aus Batteriemodulen, gemeinsamer Vorrat aller getragenen Teile; lädt in IC2-Speichern "
                + "(Stufe 1). Gewicht über 25 kg bremst. Tasten: Z = runter (Flugsteuerung), K = Tastenbelegung zum "
                + "Ein-/Ausschalten von Modulen."
                : "Modular armor: protection, energy and abilities come from modules (Tinker Table). Energy in joules "
                + "(50 J = 1 EU) from battery modules, one pool for all worn parts; charges in IC2 storage (tier 1). "
                + "Weight above 25 kg slows you. Keys: Z = down (flight control), K = keybinds to switch modules on/off.");
        add("emi.craft.info.power_tool", german
                ? "Spitzhacke/Schaufel/Axt-Module zum Abbauen (Energie je Block; schon die Spitzhacke baut Obsidian). "
                + "Schleichen + Mausrad wählt den Modus, Rechtsklick führt ihn aus: Railgun, Blink Drive (Teleport), "
                + "Plasmakanone (aufladen wie ein Bogen) oder In-Place Assembler (Werkbank). Nahkampfhilfe verstärkt "
                + "Schläge."
                : "Pickaxe/shovel/axe modules for mining (energy per block; the pickaxe alone mines obsidian). Sneak + "
                + "mouse wheel selects the mode, right click runs it: railgun, blink drive (teleport), plasma cannon "
                + "(charge like a bow) or in-place assembler (crafting grid). Melee assist strengthens hits.");
        add("emi.craft.info.mps_unused_component", german
                ? "Kein Rezept und keine Verwendung – auch im Pack nicht (Modular Powersuits 0.3.2). Nur im Kreativmodus."
                : "No recipe and no use – also not in the pack (Modular Powersuits 0.3.2). Creative only.");
        add("entity.craft.plasma_bolt", german ? "Plasmageschoss" : "Plasma Bolt");
        add("key.craft.mps_keybinds", german ? "Power-Rüstung: Tastenbelegung" : "Power Armor: Keybindings");
    }

    /** Modular Powersuits (spec 29); English texts as in the pack, module names in German translated. */
    protected void modularPowersuits(boolean german) {
        add("itemGroup.craft.modular_powersuits", "Craft: Modular Powersuits");
        String[][] components = {
                {"wiring", "Wiring", "Verdrahtung", "A special type of wiring with high voltaic capacity and precision, necessary for the sensitive electronics in power armor.", "Eine besondere Verdrahtung mit hoher Spannungsfestigkeit und Präzision, nötig für die empfindliche Elektronik der Power-Rüstung."},
                {"solenoid", "Solenoid", "Magnetspule", "Wires wound around a ferromagnetic core produces a basic electromagnet.", "Um einen ferromagnetischen Kern gewickelter Draht ergibt einen einfachen Elektromagneten."},
                {"servo", "Servo Motor", "Servomotor", "A special type of motor which uses a pulse-modulated signal to enact very precise movements.", "Ein besonderer Motor, der mit einem pulsmodulierten Signal sehr präzise Bewegungen ausführt."},
                {"glider_wing", "Glider Wing", "Gleitflügel", "A lightweight aerodynamic wing with an electromagnet for quick deployment and retraction.", "Ein leichter, aerodynamischer Flügel mit Elektromagnet zum schnellen Aus- und Einfahren."},
                {"ion_thruster", "Ion Thruster", "Ionentriebwerk", "Essentially a miniature particle accelerator. Accelerates ions to near-light speed to produce thrust.", "Im Grunde ein Miniatur-Teilchenbeschleuniger. Beschleunigt Ionen nahezu auf Lichtgeschwindigkeit, um Schub zu erzeugen."},
                {"lv_capacitor", "LV Capacitor", "NS-Kondensator", "A simple capacitor can store and discharge small amounts of energy rapidly.", "Ein einfacher Kondensator kann kleine Energiemengen speichern und schnell abgeben."},
                {"mv_capacitor", "MV Capacitor", "MS-Kondensator", "A more advanced capacitor which can store more energy at higher voltages.", "Ein fortgeschrittener Kondensator, der mehr Energie bei höheren Spannungen speichert."},
                {"hv_capacitor", "HV Capacitor", "HS-Kondensator", "A synthetic crystal device which can store and release massive amounts of energy.", "Ein synthetischer Kristall, der gewaltige Energiemengen speichern und abgeben kann."},
                {"parachute", "Parachute", "Fallschirm", "A simple reusable parachute which can be deployed and recovered in midair.", "Ein einfacher, wiederverwendbarer Fallschirm, der in der Luft geöffnet und wieder eingeholt werden kann."},
                {"plating_basic", "Basic Plating", "Einfache Panzerung", "Some carefully-arranged metal armor plates.", "Sorgfältig angeordnete Panzerplatten aus Metall."},
                {"plating_advanced", "Advanced Plating", "Fortgeschrittene Panzerung", "Some carefully-arranged armor plates of a rare and stronger material", "Sorgfältig angeordnete Panzerplatten aus einem seltenen, stärkeren Material."},
                {"field_emitter", "Force Field Emitter", "Kraftfeldemitter", "An advanced device which directly manipulates electromagnetic and gravitational fields in an area.", "Ein fortgeschrittenes Gerät, das elektromagnetische und Gravitationsfelder in einem Bereich direkt beeinflusst."},
                {"laser_emitter", "Hologram Emitter", "Hologrammemitter", "A multicoloured laser array which can cheaply alter the appearance of something.", "Eine mehrfarbige Laseranordnung, die das Aussehen von etwas günstig verändern kann."},
                {"carbon_myofiber", "Carbon Myofiber", "Kohlenstoff-Myofaser", "A small bundle of carbon fibers, refined for use in artificial muscles.", "Ein kleines Bündel Kohlenstofffasern, veredelt für künstliche Muskeln."},
                {"control_circuit", "Control Circuit", "Steuerschaltung", "A simple networkable microcontroller for coordinating an individual component.", "Ein einfacher, vernetzbarer Mikrocontroller zur Steuerung eines einzelnen Bauteils."},
                {"myofiber_gel", "Myofiber Gel", "Myofaser-Gel", "A thick, conductive paste, perfect for fitting between myofibers in an artificial muscle.", "Eine dicke, leitfähige Paste, ideal zwischen den Myofasern eines künstlichen Muskels."},
                {"artificial_muscle", "Artificial Muscle", "Künstlicher Muskel", "An electrical, artificial muscle, with less range of movement than human muscle but orders of magnitude more strength.", "Ein elektrischer, künstlicher Muskel mit weniger Bewegungsumfang als ein menschlicher, aber um Größenordnungen mehr Kraft."},
                {"solar_panel", "Solar Panel", "Solarzelle", "A light sensitive device that will generate electricity from the sun.", "Ein lichtempfindliches Bauteil, das aus Sonnenlicht Strom erzeugt."},
        };
        for (String[] component : components) {
            add("item.craft.component_" + component[0], german ? component[2] : component[1]);
            add("item.craft.component_" + component[0] + ".desc", german ? component[4] : component[3]);
        }
        item(MpsItems.POWER_ARMOR_HEAD, german ? "Power-Rüstungshelm" : "Power Armor Head");
        item(MpsItems.POWER_ARMOR_TORSO, german ? "Power-Rüstungsbrustteil" : "Power Armor Torso");
        item(MpsItems.POWER_ARMOR_LEGS, german ? "Power-Rüstungshose" : "Power Armor Legs");
        item(MpsItems.POWER_ARMOR_FEET, german ? "Power-Rüstungsstiefel" : "Power Armor Feet");
        item(MpsItems.POWER_TOOL, german ? "Power-Werkzeug" : "Power Tool");
        block(MpsBlocks.TINKER_TABLE, german ? "Power-Rüstungs-Werkbank" : "Power Armor Tinker Table");
        String[][] modules = {
                {"Night Vision", "Nachtsicht", "A pair of augmented vision goggles to help you see at night and underwater.", "Eine Brille mit erweiterter Sicht, mit der du nachts und unter Wasser siehst."},
                {"Active Camouflage", "Aktive Tarnung", "Emit a hologram of your surroundings to make yourself almost imperceptible.", "Projiziert ein Hologramm der Umgebung und macht dich fast unsichtbar."},
                {"Flight Control", "Flugsteuerung", "An integrated control circuit to help you fly better. Press Z to go down.", "Eine eingebaute Steuerschaltung für besseres Fliegen. Z drücken, um zu sinken."},
                {"Plasma Cannon", "Plasmakanone", "Use electrical arcs in a containment field to superheat air to a plasma and launch it at enemies.", "Lichtbögen in einem Eindämmungsfeld erhitzen Luft zu Plasma, das auf Gegner geschleudert wird."},
                {"Melee Assist", "Nahkampfhilfe", "A much simpler addon, makes your powertool punches hit harder.", "Eine viel einfachere Erweiterung: Schläge mit dem Power-Werkzeug treffen härter."},
                {"Railgun", "Railgun", "An assembly which accelerates a projectile to supersonic speeds using magnetic force. Heavy recoil.", "Beschleunigt ein Geschoss mit Magnetkraft auf Überschall. Starker Rückstoß."},
                {"In-Place Assembler", "Mobile Werkbank", "A larger crafting grid, on the go.", "Ein größeres Handwerksraster für unterwegs."},
                {"Blink Drive", "Blink-Antrieb", "Get from point A to point C via point B, where point B is a fold in space & time.", "Von Punkt A über Punkt B nach Punkt C, wobei Punkt B eine Falte in Raum und Zeit ist."},
                {"Kinetic Generator", "Kinetischer Generator", "Generate power with your movement.", "Erzeugt Strom aus deiner Bewegung."},
                {"Solar Generator", "Solargenerator", "Let the sun power your adventures.", "Lass die Sonne deine Abenteuer antreiben."},
                {"Auto-Feeder", "Auto-Fütterer", "Whenever you're hungry, this module will grab the bottom-left-most food item from your inventory and feed it to you, storing the rest for later.", "Wenn du hungrig bist, nimmt dieses Modul Nahrung aus deinem Inventar, füttert dich damit und speichert den Rest für später."},
                {"Iron Plating", "Eisenpanzerung", "Basic plating is heavy but protective.", "Einfache Panzerung ist schwer, schützt aber."},
                {"Diamond Plating", "Diamantpanzerung", "Advanced plating is lighter, harder, and more protective than Basic but much harder to make.", "Fortgeschrittene Panzerung ist leichter, härter und schützt besser als die einfache, ist aber viel schwerer herzustellen."},
                {"Energy Shield", "Energieschild", "Energy shields are much lighter than plating, but consume energy.", "Energieschilde sind viel leichter als Panzerung, verbrauchen aber Energie."},
                {"Shovel", "Schaufel", "Shovels are good for soft materials like dirt and sand.", "Schaufeln eignen sich für weiche Materialien wie Erde und Sand."},
                {"Pickaxe", "Spitzhacke", "Picks are good for harder materials like stone and ore.", "Spitzhacken eignen sich für härtere Materialien wie Stein und Erz."},
                {"Axe", "Axt", "Axes are mostly for chopping trees.", "Äxte sind vor allem zum Bäumefällen."},
                {"Diamond Drill Upgrade", "Diamantbohrer-Upgrade", "Add diamonds to allow your pickaxe module to mine Obsidian. *REQUIRES PICKAXE MODULE TO WORK*", "Diamanten lassen das Spitzhacken-Modul Obsidian abbauen. *BRAUCHT DAS SPITZHACKEN-MODUL*"},
                {"Aqua Affinity", "Wasseraffinität", "Reduces the speed penalty for using your tool underwater.", "Verringert die Verlangsamung beim Abbauen unter Wasser."},
                {"Basic Battery", "Einfache Batterie", "Integrate a battery to allow the item to store energy.", "Eine eingebaute Batterie lässt das Item Energie speichern."},
                {"Advanced Battery", "Fortgeschrittene Batterie", "Integrate a more advanced battery to store more energy.", "Eine fortgeschrittenere Batterie speichert mehr Energie."},
                {"Elite Battery", "Elite-Batterie", "Integrate a the most advanced battery to store an extensive amount of energy.", "Die fortschrittlichste Batterie speichert gewaltige Energiemengen."},
                {"Sprint Assist", "Sprinthilfe", "Servo motors carry the load while you sprint (double-tap forward): more carry capacity, less exhaustion. No extra speed.", "Servomotoren tragen die Last beim Sprinten (zweimal vorwärts): mehr Tragkraft, weniger Erschöpfung. Kein Zusatztempo."},
                {"Jump Assist", "Sprunghilfe", "Another set of servo motors to help you jump higher.", "Weitere Servomotoren, mit denen du höher springst."},
                {"Shock Absorber", "Stoßdämpfer", "With some servos, springs, and padding, you should be able to negate a portion of fall damage.", "Mit Servos, Federn und Polstern lässt sich ein Teil des Fallschadens aufheben."},
                {"Glider", "Gleiter", "Tack on some wings to turn downward into forward momentum, at most sprinting speed. Press sneak+forward while falling to activate.", "Flügel wandeln Fallen in Vorwärtsbewegung um, höchstens Sprinttempo. Im Fallen Schleichen + Vorwärts drücken."},
                {"Parachute", "Fallschirm", "Add a parachute to slow your descent. Activate by pressing sneak (defaults to Shift) in midair.", "Ein Fallschirm bremst den Fall. In der Luft Schleichen (Standard: Shift) drücken."},
                {"Jetpack", "Jetpack", "A jetpack should allow you to jump indefinitely, or at least until you run out of power.", "Mit einem Jetpack springst du endlos – oder zumindest, bis die Energie ausgeht."},
                {"Jet Boots", "Düsenstiefel", "Jet boots are not as strong as a jetpack, but they should at least be strong enough to counteract gravity.", "Düsenstiefel sind schwächer als ein Jetpack, sollten aber wenigstens die Schwerkraft ausgleichen."},
                {"Water Electrolyzer", "Wasser-Elektrolyseur", "When you run out of air, this module will jolt the water around you, electrolyzing a small bubble to breathe from.", "Geht dir die Luft aus, zerlegt das Modul das Wasser um dich per Stromstoß in eine kleine Atemblase."},
                {"Swim Boost", "Schwimmhilfe", "Diver servos steady your arms underwater: mining underwater (even while swimming) is as fast as on land. No thrust.", "Taucher-Servos halten unter Wasser die Arme ruhig: Abbauen unter Wasser (auch schwimmend) so schnell wie an Land. Kein Schub."},
                {"Uphill Step Assist", "Stufenhilfe", "A pair of dedicated servos allow you to effortlessly step up 1m-high ledges.", "Zwei Servos lassen dich mühelos 1 m hohe Stufen hinaufsteigen."},
                {"Custom Colour Module", "Farbmodul", "Give your armor some coloured tinting to customize your armor's appearance.", "Färbt deine Rüstung nach Wunsch ein."},
                {"Transparent Armor", "Durchsichtige Rüstung", "Make the item transparent, so you can show off your skin without losing armor.", "Macht das Teil durchsichtig, damit dein Skin zu sehen ist, ohne Schutz zu verlieren."},
                {"Citizen Joe Style", "Citizen-Joe-Stil", "An alternative armor texture, c/o CitizenJoe of IC2 forums.", "Eine andere Rüstungstextur, von CitizenJoe aus den IC2-Foren."},
        };
        for (String[] module : modules) {
            String key = "mps.module." + PowerModule.key(module[0]);
            add(key, german ? module[1] : module[0]);
            add(key + ".desc", german ? module[3] : module[2]);
        }
        String[][] categories = {{"Armor", "Rüstung"}, {"Energy", "Energie"}, {"Tool", "Werkzeug"}, {"Weapon", "Waffe"},
                {"Movement", "Bewegung"}, {"Cosmetic", "Kosmetik"}, {"Environment", "Umwelt"}, {"Special", "Spezial"}};
        for (String[] category : categories) {
            add("mps.category." + PowerModule.key(category[0]), german ? category[1] : category[0]);
        }
        String[][] tweaks = {{"Amperage", "Stromstärke"}, {"Voltage", "Spannung"}, {"Impact", "Wucht"},
                {"Carry-through", "Durchschlag"}, {"Range", "Reichweite"}, {"Energy Generated", "Erzeugte Energie"},
                {"Efficiency", "Effizienz"}, {"Plating Thickness", "Panzerungsdicke"}, {"Field Strength", "Feldstärke"},
                {"Overclock", "Übertaktung"}, {"Power", "Leistung"}, {"Battery Size", "Batteriegröße"},
                {"Compensation", "Ausgleich"}, {"Thrust", "Schub"},
                {"Red Intensity", "Rotanteil"}, {"Green Intensity", "Grünanteil"}, {"Blue Intensity", "Blauanteil"}};
        for (String[] tweak : tweaks) {
            add("mps.tweak." + PowerModule.key(tweak[0]), german ? tweak[1] : tweak[0]);
        }
        String[][] properties = {
                {"Armor (Physical)", "Rüstung (physisch)"}, {"Armor (Energy)", "Rüstung (Energie)"},
                {"Energy Per Damage", "Energie je Schaden"}, {"Weight", "Gewicht"}, {"Maximum Energy", "Max. Energie"},
                {"Plasma Energy Per Tick", "Plasma-Energie je Tick"},
                {"Plasma Damage At Full Charge", "Plasma-Schaden voll geladen"},
                {"Plasma Explosiveness", "Plasma-Sprengkraft"}, {"Punch Energy Consumption", "Schlag-Energie"},
                {"Melee Damage", "Nahkampfschaden"}, {"Melee Knockback", "Nahkampf-Rückstoß"},
                {"Railgun Total Impulse", "Railgun-Impuls"}, {"Railgun Energy Cost", "Railgun-Energie"},
                {"Blink Drive Energy Consuption", "Blink-Energie"}, {"Blink Drive Range", "Blink-Reichweite"},
                {"Energy Generation Per 5 Blocks", "Energie je 5 Blöcke"},
                {"Daytime Solar Energy Generation", "Solarenergie am Tag"},
                {"Nighttime Solar Energy Generation", "Solarenergie in der Nacht"},
                {"Eating Energy Consumption", "Energie je Essen"}, {"Auto-Feeder Efficiency", "Fütterer-Effizienz"},
                {"Shovel Energy Consumption", "Schaufel-Energie"}, {"Shovel Harvest Speed", "Schaufel-Tempo"},
                {"Pickaxe Energy Consumption", "Spitzhacken-Energie"}, {"Pickaxe Harvest Speed", "Spitzhacken-Tempo"},
                {"Axe Energy Consumption", "Axt-Energie"}, {"Axe Harvest Speed", "Axt-Tempo"},
                {"Underwater Energy Consumption", "Energie unter Wasser"},
                {"Underwater Harvest Speed", "Tempo unter Wasser"},
                {"Sprint Energy Consumption", "Sprint-Energie"}, {"Carry Capacity", "Tragkraft"},
                {"Sprint Exhaustion Compensation", "Sprint-Erschöpfungsausgleich"},
                {"Jump Energy Consumption", "Sprung-Energie"}, {"Jump Boost", "Sprungkraft"},
                {"Jump Exhaustion Compensation", "Sprung-Erschöpfungsausgleich"},
                {"Impact Energy consumption", "Aufprall-Energie"}, {"Distance Reduction", "Fallhöhen-Minderung"},
                {"Jet Energy Consumption", "Düsen-Energie"}, {"Jet Thrust", "Düsenschub"},
                {"Jolt Energy", "Stromstoß-Energie"}, {"Swim Boost Energy Consumption", "Schwimm-Energie"},
                {"Red Tint", "Rot"}, {"Green Tint", "Grün"},
                {"Blue Tint", "Blau"}};
        for (String[] property : properties) {
            add("mps.property." + PowerModule.key(property[0]), german ? property[1] : property[0]);
        }
        add("gui.craft.mps.install", german ? "Einbauen" : "Install");
        add("gui.craft.mps.keybinds", german ? "Power-Rüstung: Tastenbelegung" : "Power Armor Keybindings");
        add("gui.craft.mps.keybinds.new", german ? "Neu" : "New");
        add("gui.craft.mps.keybinds.trash", german ? "Müll" : "Trash");
        add("gui.craft.mps.keybinds.press_key", german ? "Taste drücken" : "Press Key");
        add("gui.craft.mps.keybinds.taken", german ? "Belegt!" : "Taken!");
        add("gui.craft.mps.keybinds.help.0", german ? "Mit „Neu“ neue Tasten belegen." : "Use 'new' to bind new keys.");
        add("gui.craft.mps.keybinds.help.1", german ? "Module per Ziehen auf Tasten legen." : "Drag and drop modules to bind them to keys.");
        add("gui.craft.mps.keybinds.help.2", german ? "Tasten auf „Müll“ ziehen zum Löschen." : "Drop keys on 'trash' to unbind them.");
        add("message.craft.mps.toggled_on", german ? "%s eingeschaltet" : "Toggled %s on");
        add("message.craft.mps.toggled_off", german ? "%s ausgeschaltet" : "Toggled %s off");
        add("message.craft.mps.feeder_ate", german ? "Auto-Feeder: %s gegessen" : "Feeder module: Ate a %s");
        add("gui.craft.mps.salvage", german ? "Ausbauen" : "Salvage");
        add("gui.craft.mps.tinker", german ? "Einstellen" : "Tinker");
        add("gui.craft.mps.summary", german ? "Übersicht" : "Detailed Summary");
        add("gui.craft.mps.armor", german ? "Rüstung" : "Armor");
        add("gui.craft.mps.energy_storage", german ? "Energiespeicher" : "Energy Storage");
        add("gui.craft.mps.weight", german ? "Gewicht" : "Weight");
        add("gui.craft.mps.no_items.0", german ? "Keine Power-Rüstungsteile" : "No modular powersuit items");
        add("gui.craft.mps.no_items.1", german ? "im Inventar. Bau welche!" : "found in inventory. Make some!");
        add("gui.craft.mps.no_items.2", german ? "Wir empfehlen, mit Eisen" : "We recommend experimenting");
        add("gui.craft.mps.no_items.3", german ? "und Verdrahtung zu experimentieren." : "with steel plates and basic circuits.");
        add("tooltip.craft.mps.mode", german ? "Modus: %s" : "Mode:%s");
        add("tooltip.craft.mps.energy", german ? "Energie: %s/%s" : "Energy: %s/%s");
        add("tooltip.craft.mps.shift", german ? "Shift drücken für mehr Informationen." : "Press SHIFT for more information.");
        add("tooltip.craft.mps.no_modules", german
                ? "Keine Module eingebaut! Ohne Module an einer Power-Rüstungs-Werkbank ist dieses Item nutzlos."
                : "No installed modules! This item is useless until you add some modules at a Tinker Table.");
        add("tooltip.craft.mps.installed", german ? "Eingebaute Module:" : "Installed Modules:");
        add("tooltip.craft.mps.for_tinker_table", german ? "Für die Power-Rüstungs-Werkbank." : "For use in Tinker Table.");
        add("message.craft.mps.module_disallowed", german ? "Der Server hat dieses Modul verboten. Sorry!" : "Server has disallowed this module. Sorry!");
    }

    /** EnderStorage (spec 27); the chest keeps the original name, German marks it apart from the vanilla one. */
    protected void enderStorage(boolean german) {
        add("itemGroup.craft.ender_storage", "Craft: EnderStorage");
        item(EsItems.ENDER_CHEST, german ? "Ender-Truhe (farbig)" : "Ender Chest");
        add("container.craft.ender_chest", german ? "Ender-Truhe" : "Ender Chest");
        item(EsItems.ENDER_POUCH, german ? "Enderbeutel" : "Ender Pouch");
        add("container.craft.ender_pouch", german ? "Enderbeutel" : "Ender Pouch");
        item(EsItems.ENDER_TANK, german ? "Ender-Tank" : "Ender Tank");
        add("tooltip.craft.ender_frequency", german ? "Farben: %s / %s / %s" : "Colours: %s / %s / %s");
        add("emi.craft.info.ender_chest", german
                ? "Alle Ender-Truhen und Enderbeutel mit denselben drei Farben teilen sich 27 Plätze – überall, auch in "
                + "anderen Dimensionen. Farbstoff auf einen der drei Knöpfe auf dem Deckel ändert diese Farbe (oder "
                + "Farbstoffe über die Truhe in die Werkbank legen). Diamant auf das Schloss: persönlich, nur für dich "
                + "(eigener Speicher). Schleichen + Rechtsklick auf das Schloss mit leerer Hand: wieder global, der "
                + "Diamant kommt zurück. Abbauen behält Farben und Inhalt."
                : "All ender chests and ender pouches with the same three colours share 27 slots – anywhere, also in "
                + "other dimensions. Dye on one of the three buttons on the lid changes that colour (or put dyes above "
                + "the chest in a crafting grid). Diamond on the latch: personal, just for you (own storage). Sneak + "
                + "right click the latch with an empty hand: global again, the diamond comes back. Breaking keeps "
                + "colours and contents.");
        add("emi.craft.info.ender_tank", german
                ? "Alle Ender-Tanks mit denselben drei Farben teilen sich 16 000 mB einer Flüssigkeit (getrennt von "
                + "den Truhen). Eimer/Zellen per Rechtsklick füllen oder leeren; Rohre an jeder Seite. Farbknöpfe oben "
                + "und Diamant wie bei der Truhe, aber auf dem Ventilknauf vorne. Klick auf den Knauf dreht das Ventil: "
                + "bei Druck (Redstone-Signal, bzw. ohne Signal wenn gedreht) gibt der Tank je Tick bis 100 mB an jeden "
                + "angrenzenden Tank/jede Maschine ab. Leuchtet mit leuchtenden Flüssigkeiten."
                : "All ender tanks with the same three colours share 16,000 mB of one liquid (apart from the chests). "
                + "Fill or empty buckets/cells with a right click; pipes on every side. Colour buttons on top and "
                + "diamond as on the chest, but on the valve knob at the front. Clicking the knob turns the valve: "
                + "with pressure (redstone signal, or no signal when turned) the tank pushes up to 100 mB per tick "
                + "into every adjacent tank/machine. Glows with glowing liquids.");
        add("emi.craft.info.ender_pouch", german
                ? "Öffnet per Rechtsklick den Speicher der Ender-Truhen mit seinen Farben. Schleichen + Rechtsklick auf "
                + "eine Ender-Truhe übernimmt deren Farben und Besitzer."
                : "Right click opens the storage of the ender chests with its colours. Sneak + right click an ender "
                + "chest to take over its colours and owner.");
    }

    /** Portal Gun long fall boots (spec 21). */
    protected void portalGun(boolean german) {
        item(PgItems.LONG_FALL_BOOT, german ? "Einzelner Weitfallstiefel" : "Long Fall Boot");
        item(PgItems.LONG_FALL_BOOTS, german ? "Weitfallstiefel" : "Long Fall Boots");
    }

    /** Custom music discs (user 2026-11-13): same item name as vanilla, title as jukebox song description. */
    protected void musicDiscs(boolean german) {
        for (dev.craft.music.MusicDiscs.Track track : dev.craft.music.MusicDiscs.tracks()) {
            item(track.disc(), german ? "Schallplatte" : "Music Disc");
            add("jukebox_song.craft." + track.name(), track.title());
        }
    }

    /** Phase 50 small mods (spec 50). */
    protected void kleinkram(boolean german) {
        add("itemGroup.craft.kleinkram", german ? "Craft: Kleinkram" : "Craft: Odds and Ends");
        add(dev.craft.obsidiplates.ObsidiPlates.OBSIDIAN_PRESSURE_PLATE.get(), german ? "Obsidian-Druckplatte"
                : "Obsidian Pressure Plate");
        item(dev.craft.omnitools.OmniTools.OMNIWRENCH, german ? "Prototyp-OmniWrench" : "Prototype OmniWrench");
        add(dev.craft.omnitools.OmniTools.MYSTICAL_PLINTH.get(), german ? "Mystischer Sockel" : "Mystical Plinth");
        item(dev.craft.omnitools.OmniTools.FORGE_LEXICON, german ? "Forge-Lexikon" : "Forge Lexicon");
        add("gui.craft.forge_lexicon.no_conversion", german ? "Keine Umwandlung nötig." : "No conversion necessary.");
        item(dev.craft.gravitygun.GravityGun.GRAVITY_GUN, german ? "Gravitationskanone" : "Gravity Gun");
        add("itemGroup.craft.secret_rooms", "Craft: Secret Rooms");
        item(dev.craft.secretrooms.SecretRooms.CAMO_PASTE, german ? "Tarnpaste" : "Camo Paste");
        add(dev.craft.secretrooms.SecretRooms.GHOST_BLOCK.get(), german ? "Geisterblock" : "Ghost Block");
        add(dev.craft.secretrooms.SecretRooms.ONE_WAY_GLASS.get(), german ? "Einwegglas" : "One-Way Glass");
        add("key.craft.one_way_facing", german ? "Einwegglas-Richtung umschalten" : "Toggle one-way glass facing");
        add("message.craft.secret_rooms.facing_towards", german ? "Einwegglas: Tarnseite zu dir"
                : "One-way glass: camouflage towards you");
        add("message.craft.secret_rooms.facing_away", german ? "Einwegglas: Tarnseite von dir weg"
                : "One-way glass: camouflage away from you");
        add("emi.craft.info.camo_paste", german
                ? "Zutat aller Secret-Rooms-Blöcke. Getarnte Blöcke sehen beim Setzen aus wie ihre Nachbarn: ein "
                + "gleiches gegenüberliegendes Nachbarpaar (unten/oben, West/Ost, Nord/Süd) hat Vorrang, sonst der "
                + "erste Nachbar (unten, oben, West, Ost, Nord, Süd); es zählen nur volle Blöcke."
                : "Ingredient of all Secret Rooms blocks. Camouflaged blocks take on the look of their neighbours when "
                + "placed: a matching pair of opposite neighbours (down/up, west/east, north/south) wins, otherwise the "
                + "first neighbour (down, up, west, east, north, south); only full blocks count.");
        add("emi.craft.info.ghost_block", german
                ? "Getarnter Block, durch den man hindurchlaufen kann. Mit Redstone-Signal am Block oder am Block "
                + "darüber wird er fest."
                : "Camouflaged block you can walk through. A redstone signal at the block or the block above makes "
                + "it solid.");
        add("emi.craft.info.one_way_glass", german
                ? "Eine Seite ist getarnt wie die umliegenden Blöcke, alle anderen sind Glas – von innen sieht man "
                + "hinaus, von außen nicht hinein. Die Tarnseite zeigt beim Setzen von dir weg; die Taste "
                + "„Einwegglas-Richtung umschalten“ (Standard: \\) dreht das um."
                : "One side is camouflaged like the blocks around it, all others are glass – from inside you see out, "
                + "from outside not in. The camouflaged side faces away from you when placed; the key \"Toggle "
                + "one-way glass facing\" (default: \\) turns that round.");
        item(dev.craft.gravitygun.GravityGun.CHARGED_GRAVITY_GUN, german ? "Aufgeladene Gravitationskanone"
                : "Charged Gravity Gun");
        add(dev.craft.gravitygun.GravityGun.GRABBED_BLOCK.get(), german ? "Gegriffener Block" : "Grabbed Block");
        add("tooltip.craft.gravity_gun", german ? "Benutzen: greifen/loslassen · Angriff: werfen"
                : "Use: grab/drop · Attack: throw");
        add("tooltip.craft.gravity_gun.charged", german
                ? "Benutzen: greifen/loslassen (auch Blöcke aus 15 Blöcken) · Angriff: werfen"
                : "Use: grab/drop (blocks from 15 blocks away too) · Attack: throw");
        add("emi.craft.info.gravity_gun", german
                ? "Benutzen-Taste: Entity oder Block in bis zu 4 Blöcken greifen und 3,5 Blöcke vor sich halten, erneut "
                + "drücken lässt los; gedrückt halten zieht Entities aus 10 Blöcken heran. Angriffstaste wirft das "
                + "Gehaltene bzw. stößt ein Ziel an. Geworfenes richtet beim Aufprall Schaden an (2,5 je Block/Tick "
                + "Tempo). Greift volle Blöcke, Truhen (mit Inhalt), Glas, Glowstone, Kuchen, TNT, Eis, Laub, Ambosse; "
                + "losgelassene Blöcke fallen und setzen sich ab. Ein Blitz in der Nähe lädt die Kanone auf."
                : "Use key: grab an entity or block within 4 blocks and hold it 3.5 blocks ahead, press again to drop; "
                + "holding the key pulls entities in from 10 blocks. Attack throws what you hold or punts a target. "
                + "Thrown things deal impact damage (2.5 per block/tick of speed). Grabs full blocks, chests (with "
                + "contents), glass, glowstone, cake, TNT, ice, leaves, anvils; released blocks fall and set down. "
                + "Lightning nearby charges the gun.");
        add("emi.craft.info.charged_gravity_gun", german
                ? "Wie die Gravitationskanone, aber Reichweite 15, zieht doppelt so stark, wirft stärker (12 statt 10) "
                + "und reißt beim Heranziehen auch Blöcke aus 15 Blöcken los. Von ihr geworfene Creeper explodieren "
                + "beim Aufprall."
                : "Like the gravity gun, but range 15, pulls twice as hard, throws harder (12 instead of 10) and also "
                + "tears blocks loose from 15 blocks away while pulling. Creepers it throws explode on impact.");
        add("emi.craft.info.forge_lexicon", german
                ? "Wandelt Erze, Staub, kleine Stäube und Barren zwischen gleichwertigen Varianten verschiedener Mods "
                + "um (gemeinsamer c:-Tag, z. B. Craft-Zinnbarren ↔ Mekanism-Zinnbarren; Gruppen nach der Whitelist des "
                + "Packs). Schleichen + Rechtsklick öffnet es überall; auf einem Mystischen Sockel abgelegt (Rechtsklick "
                + "auf den Sockel) bleibt es dort aufgeschlagen. Pfeile wählen die Zielvariante, der Knopf wandelt den "
                + "ganzen Stapel um."
                : "Converts ores, dusts, small dusts and ingots between equivalent variants of different mods (shared "
                + "c: tag, e.g. Craft tin ingot ↔ Mekanism tin ingot; groups follow the pack's whitelist). Sneak + right "
                + "click opens it anywhere; placed on a Mystical Plinth (right click the plinth) it stays there open. "
                + "The arrows pick the target variant, the button converts the whole stack.");
        add("emi.craft.info.mystical_plinth", german
                ? "Sockel für das Forge-Lexikon: Rechtsklick mit dem Lexikon legt es mit Blitz und Knall (harmlos) "
                + "darauf, dann leuchtet er (15) und öffnet per Rechtsklick das Lexikon. Schleichen + Schraubenschlüssel "
                + "nimmt das Lexikon samt Inhalt wieder ab bzw. baut den leeren Sockel ab."
                : "Plinth for the Forge Lexicon: right click with the lexicon puts it on with a (harmless) lightning "
                + "strike and bang; then it glows (15) and right click opens the lexicon. Sneak + wrench takes the "
                + "lexicon and its contents off again, or dismantles the empty plinth.");
        add("emi.craft.info.omniwrench", german
                ? "Unzerstörbarer Universal-Schraubenschlüssel ohne Strom. Dreht Vanilla-Blöcke (Schleichen: "
                + "andersherum), dreht IC2-Maschinen auf die geklickte Seite (Schleichen: Gegenseite) und baut sie per "
                + "Klick auf die Front ab – immer verlustfrei. Gilt auch als BuildCraft-/Thermal-Expansion-Schlüssel."
                : "Unbreakable universal wrench without power. Rotates vanilla blocks (sneaking: the other way), turns "
                + "IC2 machines to the clicked side (sneaking: opposite side) and removes them by clicking the front – "
                + "always without loss. Also works as a BuildCraft/Thermal Expansion wrench.");
        add("emi.craft.info.obsidian_pressure_plate", german
                ? "Druckplatte, die nur Spieler auslösen – Mobs, Tiere und Gegenstände nicht. Fest wie Obsidian."
                : "Pressure plate that only players trigger – not mobs, animals or items. As tough as obsidian.");
    }

    /** Electric light shield (spec 30). */
    protected void lightShield(boolean german) {
        item(LsItems.ELECTRIC_LIGHT_SHIELD, german ? "Elektrischer Leuchtschild" : "Electric Light Shield");
        add(LsBlocks.SHIELD_LIGHT.get(), german ? "Schildlicht" : "Shield Light");
        add("message.craft.light_shield.on", german ? "Licht: an" : "Light: on");
        add("message.craft.light_shield.off", german ? "Licht: aus" : "Light: off");
        add("tooltip.craft.light_shield.on", german ? "Licht an (Modus-Taste + Rechtsklick)"
                : "Light on (mode key + right click)");
        add("tooltip.craft.light_shield.off", german ? "Licht aus (Modus-Taste + Rechtsklick)"
                : "Light off (mode key + right click)");
        add("tooltip.craft.light_shield.flashlight", german ? "Reichweite %s · Kegel %s° · %s EU/t"
                : "Range %s · cone %s° · %s EU/t");
        add("tooltip.craft.light_shield.settings", german ? "Einstellen: Modus-Taste + Schleichen + Rechtsklick"
                : "Adjust: mode key + sneak + right click");
        add("gui.craft.flashlight", german ? "Taschenlampe" : "Flashlight");
        add("gui.craft.flashlight.range", german ? "Reichweite: %s Blöcke" : "Range: %s blocks");
        add("gui.craft.flashlight.width", german ? "Kegel: %s°" : "Cone: %s°");
        add("gui.craft.flashlight.cost", german ? "%s EU/t · Laufzeit %s:%s:%s" : "%s EU/t · runtime %s:%s:%s");
        add("emi.craft.info.distribution_transport_pipe", german
                ? "Item-Rohr, das Items nach Anteilen auf die Seiten verteilt. Rechtsklick: je Seite 0–100 % "
                + "(Umschalt-Klick: 0 bzw. 100). Die Werte sind Gewichte unter den gerade erreichbaren Seiten, "
                + "0 % sperrt eine Seite, alle 0 = normales Rohr. Exakt: jedes Item wird gezählt, Stapel werden "
                + "geteilt (64 bei 50/30/20 → 32/19/13). Volle Ziele werden übersprungen, ohne später nachzuholen; "
                + "ohne freies Ziel gehen Items zurück."
                : "Item pipe that splits items between its sides by share. Right click: 0–100 % per side "
                + "(shift-click: 0 or 100). The values are weights among the sides that can take items right now, "
                + "0 % closes a side, all 0 = plain pipe. Exact: every item is counted, stacks are split "
                + "(64 at 50/30/20 → 32/19/13). Full targets are skipped without catching up later; with no free "
                + "target items go back.");
        add("emi.craft.info.electric_light_shield", german
                ? "Elektrischer Schild (100 000 EU, Stufe 1), lädt wie LV-Werkzeuge. In Haupt- oder Nebenhand ist er eine "
                + "Taschenlampe mit echtem Licht: heller Kegel in Blickrichtung (Licht 15, am Ende 11) plus Schein 14 "
                + "um den Träger; im Licht spawnen keine Monster. Reichweite 16/32/48/64 Blöcke und Kegel 15/30/45/60° "
                + "im GUI (Modus-Taste + Schleichen + Rechtsklick); kostet Reichweitenstufe × Kegelstufe EU je 2 Ticks "
                + "(0,5–8 EU/t). Modus-Taste + Rechtsklick schaltet das Licht. Geblockte Treffer kosten 100 EU je "
                + "Haltbarkeitspunkt statt Haltbarkeit; leer lässt er sich nicht heben. Haltbarkeit spart EU, "
                + "Reparatur lädt mit Erfahrung (1 XP = 200 EU)."
                : "Electric shield (100,000 EU, tier 1), charges like LV tools. In main or off hand it is a flashlight "
                + "with real light: a bright cone where you look (light 15, 11 at the end) plus a glow of 14 around "
                + "the holder; no monsters spawn in the light. Range 16/32/48/64 blocks and cone 15/30/45/60° in the "
                + "GUI (mode key + sneak + right click); costs range stage × cone stage EU per 2 ticks (0.5–8 EU/t). "
                + "Mode key + right click toggles the light. Blocked hits cost 100 EU per durability point instead of "
                + "durability; empty it cannot be raised. Unbreaking saves EU, Mending charges it with experience "
                + "(1 XP = 200 EU).");
        add("emi.craft.info.fe_producer", german
                ? "Bauteil der Power Converters: gibt Strom aus einer angrenzenden Energy Bridge als FE ab "
                + "(1 EU = 4 FE, 1 MJ ≈ 9,7 FE). Aufbau: EU-Kabel → IC2 Consumer → Energy Bridge → FE Producer → "
                + "FE-Gerät (z. B. AE2 Energy Acceptor). Schiebt an alle angrenzenden FE-Empfänger und lässt sich von "
                + "FE-Kabeln anderer Mods anzapfen; Reste unter 1 FE bleiben in der Bridge. Nur EU → FE, kein Rückweg. "
                + "Beispiel: 128 EU/t → 512 FE/t."
                : "Power Converters component: outputs power from an adjacent Energy Bridge as FE "
                + "(1 EU = 4 FE, 1 MJ ≈ 9.7 FE). Setup: EU cable → IC2 Consumer → Energy Bridge → FE Producer → "
                + "FE device (e.g. AE2 Energy Acceptor). Pushes into every adjacent FE receiver and can be drained by "
                + "other mods' FE cables; remainders below 1 FE stay in the bridge. EU → FE only, no way back. "
                + "Example: 128 EU/t → 512 FE/t.");
    }

    protected void mining(boolean german) {
        add("biome.craft.mining", german ? "Mining-Welt" : "Mining World");
        add(MiningBlocks.MINING_PORTAL.get(), german ? "Mining-Portal" : "Mining Portal");
    }

    protected void recipeViewer(boolean german) {
        itemTags(german);
        add("gui.craft.show_recipes", german ? "Rezepte anzeigen" : "Show Recipes");
        add("emi.category.craft.macerator", "Macerator");
        add("emi.category.craft.extractor", german ? "Extraktor" : "Extractor");
        add("emi.category.craft.compressor", german ? "Kompressor" : "Compressor");
        add("emi.category.craft.recycler", "Recycler");
        add("emi.category.craft.scrap_box", german ? "Schrottbox" : "Scrap Box");
        add("emi.category.craft.mass_fabricator", german ? "Massefabrikator" : "Mass Fabricator");
        add("emi.category.craft.canning_machine", german ? "Konservierungsmaschine" : "Canning Machine");
        add("emi.category.craft.electrolyzer", german ? "Elektrolyseur" : "Electrolyzer");
        add("emi.category.craft.generator", german ? "Generator-Brennstoffe" : "Generator Fuels");
        add("emi.category.craft.geothermal_generator", german ? "Geothermiegenerator" : "Geothermal Generator");
        // GregTech machines have English names in both languages
        add("emi.category.craft.plate_bending_machine", "Plate Bending Machine");
        add("emi.category.craft.industrial_electrolyzer", "Industrial Electrolyzer");
        add("emi.category.craft.industrial_blast_furnace", "Industrial Blast Furnace");
        add("emi.category.craft.assembling_machine", "Assembling Machine");
        add("emi.category.craft.automatic_canning_machine", "Automatic Canning Machine");
        add("emi.category.craft.implosion_compressor", "Implosion Compressor");
        add("emi.category.craft.industrial_centrifuge", "Industrial Centrifuge");
        add("emi.category.craft.industrial_grinder", "Industrial Grinder");
        add("emi.category.craft.vacuum_freezer", "Vacuum Freezer");
        add("emi.craft.energy", "%1$s EU/t · %2$s s · %3$s EU");
        add("emi.craft.gt.heat", german ? "Benötigte Hitzekapazität: %1$s K" : "Required heat capacity: %1$s K");
        add("emi.craft.gt.shapeless", german ? "Eingaben in beliebiger Reihenfolge" : "Inputs in any order");
        add("emi.craft.compressor.pump", german ? "Leerer Kompressor neben Pumpe über Wasser"
                : "Empty compressor next to a pump over water");
        add("emi.craft.recycler.any", german ? "Beliebiges Item → 12,5 % Schrott" : "Any item → 12.5% scrap");
        add("emi.craft.recycler.blacklist", german ? "Wird verbraucht, gibt nie Schrott" : "Consumed, never gives scrap");
        add("emi.craft.mass_fabricator.energy", german ? "%1$s EU je UU-Materie" : "%1$s EU per UU-Matter");
        add("emi.craft.mass_fabricator.amplifier", german ? "Verstärker: %1$s" : "Amplifier: %1$s");
        add("emi.craft.mass_fabricator.saves", german ? "Spart bis zu %1$s EU" : "Saves up to %1$s EU");
        add("emi.craft.canning_machine.fuel", german ? "Brennwert %1$s" : "Fuel value %1$s");
        add("emi.craft.canning_machine.cf_pack", german ? "1 Pellet je %1$s Ticks, 1 EU/t"
                : "1 pellet per %1$s ticks, 1 EU/t");
        add("emi.craft.canning_machine.boosters", german ? "Danach Redstone/Glowstone/Schwarzpulver: +20/30/40 %"
                : "Then redstone/glowstone/gunpowder: +20/30/40%");
        add("emi.craft.electrolyzer.store", german ? "Speichert %1$s EU aus BatBox/MFE/MFSU"
                : "Stores %1$s EU from a BatBox/MFE/MFSU");
        add("emi.craft.electrolyzer.release", german ? "Gibt %1$s/%2$s/%3$s EU zurück (BatBox/MFE/MFSU)"
                : "Gives back %1$s/%2$s/%3$s EU (BatBox/MFE/MFSU)");
        add("emi.craft.generator.output", "%1$s EU (%2$s EU/t, %3$s s)");
        add("emi.craft.geothermal_generator.pump", german ? "Lava aus der IC2-Pumpe zählt nur zur Hälfte"
                : "Lava from an IC2 pump counts only half");
        buildCraftForestryPowerPlantViewer(german);
        multiblockInfo(german);
        sourceTexts(german);
    }

    /** Where the raw materials come from (EMI info page, shift tooltip, wiki; spec 24). */
    private void sourceTexts(boolean german) {
        add("tooltip.craft.source.hint", german ? "[Shift] Herkunft" : "[Shift] Where to get it");
        add("emi.craft.source.title", german ? "Herkunft" : "Where to get it");
        for (SourceTexts.Entry entry : SourceTexts.entries()) {
            for (String id : entry.ids()) {
                ResourceLocation location = ResourceLocation.parse(id);
                add("source." + location.getNamespace() + "." + location.getPath(),
                        german ? entry.german() : entry.english());
            }
        }
    }

    /** EMI categories of BuildCraft, Forestry and the power plants (spec 16 §2, phase 11.3). */
    private void buildCraftForestryPowerPlantViewer(boolean german) {
        add("emi.category.craft.refinery", german ? "Raffinerie" : "Refinery");
        add("emi.category.craft.combustion_engine", german ? "Verbrennungsmotor" : "Combustion Engine");
        add("emi.category.craft.fabricator", german ? "Glühelektrische Maschine" : "Thermionic Fabricator");
        add("emi.category.craft.carpenter", german ? "Schreinerei" : "Carpenter");
        add("emi.category.craft.coke_oven", german ? "Koksofen" : "Coke Oven");
        add("emi.category.craft.boiler", german ? "Kessel" : "Boiler");
        add("emi.category.craft.steam_turbine", german ? "Dampfturbine" : "Steam Turbine");
        add("emi.craft.refinery.energy", german ? "%1$s MJ je 1000 mB (%2$s MJ je mB)"
                : "%1$s MJ per 1000 mB (%2$s MJ per mB)");
        add("emi.craft.refinery.rate", german ? "Höchstens %1$s mB je %2$s Tick" : "At most %1$s mB per %2$s tick");
        add("emi.craft.combustion_engine.fuel", german ? "%1$s MJ/t · %2$s s · %3$s MJ je 1000 mB"
                : "%1$s MJ/t · %2$s s · %3$s MJ per 1000 mB");
        add("emi.craft.combustion_engine.coolant", german ? "Kühlmittel: %1$s Hitze je mB, ab %2$s Hitze"
                : "Coolant: %1$s heat per mB, above %2$s heat");
        add("emi.craft.combustion_engine.ice", german ? "Eis im Slot wird zu 1000 mB Wasser, sobald es heiß ist"
                : "Ice in the slot turns into 1000 mB water once hot");
        add("emi.craft.fabricator.melting_point", german ? "Schmelzpunkt: %1$s Hitze" : "Melting point: %1$s heat");
        add("emi.craft.fabricator.heating", german ? "Je Arbeitsschritt %1$s MJ → +25 Hitze (max. %2$s)"
                : "Each work step %1$s MJ → +25 heat (max. %2$s)");
        add("emi.craft.fabricator.plan", german ? "Bauplan wird nicht verbraucht" : "The plan is not consumed");
        add("emi.craft.carpenter.steps", german ? "%1$s Arbeitsschritte · %2$s MJ" : "%1$s work steps · %2$s MJ");
        add("emi.craft.coke_oven.time", german ? "%1$s s, ohne Strom" : "%1$s s, no power needed");
        add("emi.craft.boiler.burn", german ? "Brenndauer %1$s s · %2$s mB/t Dampf bei %3$s °C"
                : "Burns %1$s s · %2$s mB/t steam at %3$s °C");
        add("emi.craft.boiler.energy", german ? "= %1$s EU in der Dampfturbine" : "= %1$s EU in the steam turbine");
        add("emi.craft.boiler.cold", german ? "Kalter Kessel: weniger Dampf, solange er aufheizt"
                : "Cold boiler: less steam while it heats up");
        add("emi.craft.steam_turbine.output", german ? "%1$s EU (1 mB Dampf = 1 EU)" : "%1$s EU (1 mB steam = 1 EU)");
        add("emi.craft.steam_turbine.max", german ? "Höchstens %1$s mB/t → %2$s EU/t" : "At most %1$s mB/t → %2$s EU/t");
    }

    /** Building plans of the multiblocks (EMI info pages, spec 16 §2.3). */
    private void multiblockInfo(boolean german) {
        add("emi.craft.info.booze_barrel", german
                ? "Auf ein Holzgerüst setzen. Rechtsklick mit Wasser (Eimer/Zellen), Weizen und Hopfen braut Bier, mit "
                + "Zuckerrohr Rum (nur junges Fass). Bier reift: Brew → Youngster (1 Tag) → Beer (3) → Ale (9) → "
                + "Dragonblood (27), nach weiteren 729 Tagen verdorben; spätere Zutaten können es verderben. Rum braucht "
                + "1200 · n · 0,95^(n−1) Ticks für n Liter. Ein unbenutzter Baumzapfer an der Seite stoppt die Reifung; "
                + "dort mit dem Steinkrug zapfen. Schlagen entfernt den Zapfer (1 L geht verloren) bzw. nimmt das Fass "
                + "mit Inhalt auf."
                : "Place on a wooden scaffold. Right-click with water (bucket/cells), wheat and hops to brew beer, or "
                + "sugar cane for rum (young barrel only). Beer matures: Brew → Youngster (1 day) → Beer (3) → Ale (9) → "
                + "Dragonblood (27), spoiled after another 729 days; adding solids later may spoil it. Rum needs "
                + "1200 · n · 0.95^(n−1) ticks for n litres. An unused treetap on a side stops ageing; draw there with a "
                + "stone mug. Punching removes the treetap (1 L is lost) or picks up the barrel with its contents.");
        add("emi.craft.info.booze_mug", german
                ? "Hopfen/Weizen bestimmt die Stärke, (Weizen + Hopfen)/Wasser die Sättigung und Dauer, das Alter die "
                + "Intensität. Bier gibt Abbaulähmung, bei wiederholtem Trinken Stärke, Langsamkeit, Resistenz, Übelkeit "
                + "und Schaden. Fertiger Rum gibt Feuer- und Schadensresistenz; verdorbene Getränke zufällige Übel."
                : "Hops/wheat sets the strength, (wheat + hops)/water the food and duration, the age the intensity. Beer "
                + "gives mining fatigue, when drunk repeatedly strength, slowness, resistance, nausea and damage. "
                + "Finished rum gives fire resistance and resistance; spoiled drinks random ailments.");
        add("emi.craft.info.quantum_leggings", german
                ? "Macht nicht schneller. Solange geladen (ab 1 000 EU): Sprinten und Sprint-Springen kosten keinen "
                + "Hunger, kein Rückstoß bei Treffern. Sprinten verbraucht 1 000 EU je 10 Ticks."
                : "Does not make you faster. While charged (from 1,000 EU): sprinting and sprint jumping cost no "
                + "hunger, no knockback from hits. Sprinting uses 1,000 EU per 10 ticks.");
        add("emi.craft.info.quantum_boots", german
                ? "Boost-Taste + Springen: hoher Sprung (nur Höhe, kein Weitsprung). Hebt Fallschaden auf, solange "
                + "die Ladung reicht."
                : "Boost key + jump: high jump (height only, no long jump). Cancels fall damage while the charge "
                + "lasts.");
        add("emi.craft.info.jetpack", german
                ? "Springen halten gibt Schub nach oben; vorwärts höchstens so schnell wie Sprinten. Modus-Taste "
                + "+ Springen: Schwebemodus."
                : "Hold jump to thrust upwards; forward at most as fast as sprinting. Mode key + jump: hover mode.");
        add("emi.craft.info.coffee", german
                ? "Gibt Sättigung und Eile und steigert beide bei jedem Schluck (kalt bis II, schwarz bis VI, mit Milch "
                + "bis VII). Ab Stufe IV (Milchkaffee: VI) Übelkeit, ab V (VII) zusätzlich Schaden."
                : "Gives saturation and haste and raises both with every mug (cold up to II, dark up to VI, with milk up "
                + "to VII). From level IV (milk coffee: VI) nausea, from V (VII) also damage.");
        add("emi.craft.info.cultivation_blueprint", german
                ? "Terraformer (4000 EU je Schritt, Reichweite 40): Sand wird zu Erde, Erde zu Gras, auf Gras wachsen "
                + "Gras, Blumen, Setzlinge, Weizen, Pilze, Kürbisse und Gummisetzlinge."
                : "Terraformer (4000 EU per step, range 40): sand turns into dirt, dirt into grass; grass, flowers, "
                + "saplings, wheat, mushrooms, pumpkins and rubber saplings grow on grass.");
        add("emi.craft.info.irrigation_blueprint", german
                ? "Terraformer (3000 EU je Schritt, Reichweite 60): selten Regen; Sand wird zu Erde, Setzlinge und "
                + "Weizen wachsen sofort, Baumstämme wachsen mit Blättern, Feuer erlischt, Gras breitet sich aus."
                : "Terraformer (3000 EU per step, range 60): rarely starts rain; sand turns into dirt, saplings and "
                + "wheat grow at once, logs grow with leaves, fire goes out, grass spreads.");
        add("emi.craft.info.chilling_blueprint", german
                ? "Terraformer (2000 EU je Schritt, Reichweite 50): Wasser gefriert, Eis wächst nach unten, Schnee "
                + "fällt und wird zu Schneeblöcken."
                : "Terraformer (2000 EU per step, range 50): water freezes, ice grows downwards, snow is laid and turns "
                + "into snow blocks.");
        add("emi.craft.info.desertification_blueprint", german
                ? "Terraformer (2500 EU je Schritt, Reichweite 40): Erde, Gras und Ackerboden werden zu Sand; Wasser, "
                + "Schnee, Blätter und Pflanzen verschwinden, Eis schmilzt, Holz fängt Feuer."
                : "Terraformer (2500 EU per step, range 40): dirt, grass and farmland turn into sand; water, snow, "
                + "leaves and plants vanish, ice melts, wood catches fire.");
        add("emi.craft.info.flatification_blueprint", german
                ? "Terraformer (4000 EU je Schritt, Reichweite 40): ebnet das Gelände auf die Höhe des Terraformers "
                + "ein – natürliche Blöcke darüber werden entfernt, Senken mit Erde aufgefüllt."
                : "Terraformer (4000 EU per step, range 40): levels the ground to the terraformer's height – natural "
                + "blocks above are removed, dips are filled with dirt.");
        add("emi.craft.info.mushroom_blueprint", german
                ? "Terraformer (8000 EU je Schritt, Reichweite 25): Myzel breitet sich aus (Biom Pilzland), darauf "
                + "wachsen Pilze und Riesenpilze."
                : "Terraformer (8000 EU per step, range 25): mycelium spreads (mushroom fields biome), mushrooms and "
                + "huge mushrooms grow on it.");
        add("emi.craft.info.coke_oven", german
                ? "Koksofen: Würfel 3×3×3 aus 25 Koksofenziegeln, innen Luft. Der Koksofen sitzt in der Mitte einer "
                + "Seitenfläche (nicht oben/unten). Kohle oben oder seitlich einfüllen, Koks unten entnehmen."
                : "Coke oven: 3×3×3 cube of 25 coke oven bricks with air inside. The coke oven sits in the middle of a "
                + "side face (not top/bottom). Insert coal from the top or sides, extract coke at the bottom.");
        add("emi.craft.info.boiler", german
                ? "Kessel: Würfel 3×3×3 aus 25 Kesselgehäusen, innen Luft. Die Kesselfeuerung sitzt in der Mitte einer "
                + "Seitenfläche. Wasser über Behälter oder Rohre einfüllen; der Dampf wird an angrenzende Tanks und "
                + "Turbinen abgegeben. Wasser auf einen trockenen, heißen Kessel bringt ihn zur Explosion."
                : "Boiler: 3×3×3 cube of 25 boiler casings with air inside. The boiler firebox sits in the middle of a "
                + "side face. Fill water via containers or pipes; steam is pushed into adjacent tanks and turbines. "
                + "Water on a dry, hot boiler makes it explode.");
        add("emi.craft.info.steam_turbine", german
                ? "Dampfturbine: Quader 3×3 × 5 lang. Die Dampfturbine sitzt in der Mitte einer 3×3-Stirnseite, "
                + "dahinter drei Turbinenrotoren in einer Reihe, der Rest sind 41 Turbinengehäuse. Dampf über jeden "
                + "Block einspeisen; bis zu 3 Kessel werden voll genutzt."
                : "Steam turbine: 3×3 × 5 long. The steam turbine sits in the middle of a 3×3 end face, three turbine "
                + "rotors in a row behind it, the remaining 41 blocks are turbine casings. Steam enters through any "
                + "block; up to 3 boilers are used fully.");
        add("emi.craft.info.water_turbine", german
                ? "Wasserturbine: massiver Würfel 3×3×3 aus 26 Wasserturbinengehäusen, die Wasserturbine in der Mitte "
                + "einer Seitenfläche. Gezählt wird fließendes Wasser bis 5 Blöcke um den Würfel herum und bis 20 "
                + "Blöcke darüber: ab 16 Blöcken 0,25 EU/t je Block, höchstens 32 EU/t; im Fluss-Biom ×1,5 "
                + "und höchstens 48 EU/t (weiter in LV-Paketen bis 32)."
                : "Water turbine: solid 3×3×3 cube of 26 water turbine casings, the water turbine in the middle of a "
                + "side face. Flowing water up to 5 blocks around the cube and up to 20 blocks above it counts: from "
                + "16 blocks 0.25 EU/t per block, at most 32 EU/t; in river biomes ×1.5 and at most 48 EU/t (still "
                + "in LV packets of up to 32).");
        add("emi.craft.info.high_pressure_boiler", german
                ? "Hochdruckkessel: Würfel 5×5×5 aus 97 Hochdruck-Kesselgehäusen, innen 3×3×3 Luft. Die "
                + "Hochdruck-Kesselfeuerung sitzt in der Mitte einer Seitenfläche. Bis 1500 °C, dann 160 mB/t Dampf "
                + "(braucht 160 mB/t Wasser). Wasser auf einen trockenen, heißen Kessel: Explosion der Stärke 10."
                : "High-pressure boiler: 5×5×5 cube of 97 high-pressure boiler casings with 3×3×3 air inside. The "
                + "high-pressure boiler firebox sits in the middle of a side face. Up to 1500 °C, then 160 mB/t steam "
                + "(needs 160 mB/t water). Water on a dry, hot boiler: explosion of power 10.");
        add("emi.craft.info.large_steam_turbine", german
                ? "Große Dampfturbine: Quader 5×5 × 9 lang. Die Große Dampfturbine sitzt in der Mitte einer "
                + "5×5-Stirnseite, dahinter sieben Große Turbinenrotoren in einer Reihe mit Luft drumherum, die Hülle "
                + "sind 161 Große Turbinengehäuse. Bis 512 mB/t Dampf → 512 EU/t (HV); 3 Hochdruckkessel werden "
                + "voll genutzt."
                : "Large steam turbine: 5×5 × 9 long. The large steam turbine sits in the middle of a 5×5 end face, "
                + "seven large turbine rotors in a row behind it with air around them, the shell is 161 large turbine "
                + "casings. Up to 512 mB/t steam → 512 EU/t (HV); 3 high-pressure boilers are used fully.");
        add("emi.craft.info.large_water_turbine", german
                ? "Große Wasserturbine: massiver Würfel 5×5×5 aus 124 Großen Wasserturbinengehäusen, die Große "
                + "Wasserturbine in der Mitte einer Seitenfläche. Gezählt wird fließendes Wasser bis 7 Blöcke um den "
                + "Würfel und bis 24 Blöcke darüber: ab 64 Blöcken 1 EU/t je Block, höchstens 256 EU/t (HV); im "
                + "Fluss-Biom ×1,5 und höchstens 384 EU/t."
                : "Large water turbine: solid 5×5×5 cube of 124 large water turbine casings, the large water turbine "
                + "in the middle of a side face. Flowing water up to 7 blocks around the cube and up to 24 blocks "
                + "above it counts: from 64 blocks 1 EU/t per block, at most 256 EU/t (HV); in river biomes ×1.5 "
                + "and at most 384 EU/t.");
        add("emi.craft.info.large_wind_turbine", german
                ? "Große Windturbine: die Große Windturbine auf 24 Windturbinenmasten übereinander. Rotor per "
                + "Rechtsklick einsetzen (Schleichen + leere Hand nimmt ihn heraus). Leistung 12× Windmühle × "
                + "Rotorfaktor (Eisen 50 %, Stahl 75 %, Carbon 100 %), höchstens 192 EU/t (HV): y 128 ≈ 38 EU/t, "
                + "y 200 ≈ 82 EU/t, y 319 ≈ 153 EU/t, Gewitter ×3. Blöcke im Bereich ±5 um die Gondel senken die Leistung."
                : "Large wind turbine: the large wind turbine on top of 24 wind turbine masts. Right-click to insert "
                + "a rotor (sneak with an empty hand to take it out). Output 12× wind mill × rotor factor (iron 50 %, "
                + "steel 75 %, carbon 100 %), at most 192 EU/t (HV): y 128 ≈ 38 EU/t, y 200 ≈ 82 EU/t, y 319 ≈ 153 EU/t, "
                + "thunder ×3. "
                + "Blocks within ±5 of the nacelle lower the output.");
        add("emi.craft.info.industrial_blast_furnace", german
                ? "Hochofen: Quader 3×3, 4 hoch, direkt hinter dem Controller (dieser sitzt vor der Mitte der unteren "
                + "Lage). Die zwei Mittelblöcke der Lagen 2 und 3 sind Luft oder Lava, die übrigen 34 "
                + "GT-Maschinengehäuse. Hitze: Standard 30 K, verstärkt 50 K, fortgeschritten 70 K je Block, Lava "
                + "250 K, je Heizspulen-Stufe 500 K (4 Kanthal-, dann 4 Nichrom-Spulen per Rechtsklick)."
                : "Blast furnace: 3×3, 4 high, directly behind the controller (which sits in front of the middle of "
                + "the bottom layer). The two centre blocks of layers 2 and 3 are air or lava, the other 34 GT machine "
                + "casings. Heat: standard 30 K, reinforced 50 K, advanced 70 K per block, lava 250 K, 500 K per "
                + "heating coil tier (4 Kanthal, then 4 Nichrome coils by right-click).");
        add("emi.craft.info.industrial_grinder", german
                ? "Industrial Grinder: Würfel 3×3×3 direkt hinter dem Controller (dieser mittig an der Würfelseite). "
                + "Mitte Wasser, mittlere Ebene 8 verstärkte Gehäuse, obere und untere Ebene je 9 Standard-Gehäuse."
                : "Industrial grinder: 3×3×3 cube directly behind the controller (centred on a cube side). Centre "
                + "water, middle layer 8 reinforced casings, top and bottom layer 9 standard casings each.");
        add("emi.craft.info.implosion_compressor", german
                ? "Implosionskompressor: Würfel 3×3×3 unter dem Controller (dieser mittig obenauf). Mitte Luft, die 8 "
                + "Ecken Standard-Gehäuse, die übrigen 18 Blöcke verstärkte Gehäuse."
                : "Implosion compressor: 3×3×3 cube below the controller (centred on top). Centre air, the 8 corners "
                + "standard casings, the other 18 blocks reinforced casings.");
        add("emi.craft.info.vacuum_freezer", german
                ? "Vakuumgefrierer: Würfel 3×3×3 unter dem Controller (dieser mittig obenauf). Mitte Luft, die 6 "
                + "Flächenmitten fortgeschrittene Gehäuse, die übrigen 20 Blöcke verstärkte Gehäuse."
                : "Vacuum freezer: 3×3×3 cube below the controller (centred on top). Centre air, the 6 face centres "
                + "advanced casings, the other 20 blocks reinforced casings.");
        add("emi.craft.info.solar_field", german
                ? "Solarfeld: Jedes Solarpanel liefert bei Sonne 2 EU/t. Liegt es in einem lückenlosen Quadrat aus "
                + "9×9 Solarpanels auf derselben Höhe, liefert es 3 EU/t (×1,5). Ein 9×9-Feld "
                + "bringt so 243 EU/t statt 162. Panels leiten untereinander verlustfrei: ein "
                + "Kabel an einem beliebigen Panel sammelt den Strom des ganzen Felds (nur Panels speisen ein). Die alten LV/MV/HV-Solar-Arrays liefern nichts mehr; in der Werkbank "
                + "zerfallen sie in 8 Teile der Stufe darunter."
                : "Solar field: every solar panel delivers 2 EU/t in sunlight. If it lies in a complete square of "
                + "9×9 solar panels on the same level, it delivers 3 EU/t (×1.5). A 9×9 field "
                + "thus yields 243 EU/t instead of 162. Panels pass power to each other without loss: one "
                + "cable at any panel collects the whole field (only panels feed in). The old LV/MV/HV solar arrays no longer deliver anything; at "
                + "the crafting table they come apart into 8 of the tier below.");
        nuclearControlInfo(german);
        chargePadInfo(german);
        mffsInfo(german);
    }

    /** EMI info pages of the Modular Force Field System (spec 48). */
    private void mffsInfo(boolean german) {
        add("emi.craft.info.mffs_forcicium", german
                ? "Forcicium aus Monazit-Erz ist der Brennstoff von MFFS: Im Extraktor gibt 1 Forcicium 250 Takte "
                + "à 12 000 KE = 3 Mio. Kraftenergie (KE). Kompakte Forcicium-Zelle: hält 1 000 Forcicium; "
                + "Rechtsklick in der Luft schaltet aktiv, dann zieht sie Forcicium aus dem Inventar. Im Extraktor "
                + "liefert sie 230 Takte je Forcicium. Kraftenergie-Kristall: tragbarer Speicher (5 Mio. KE), im "
                + "Kondensator laden/entladen oder direkt als Energiequelle in Projektor & Co. einsetzen."
                : "Forcicium from monazit ore is the fuel of MFFS: in the extractor 1 forcicium gives 250 cycles of "
                + "12,000 FE = 3 million force energy (FE). Compact forcicium cell: holds 1,000 forcicium; right "
                + "click in the air makes it active, then it pulls forcicium from the inventory. In the extractor it "
                + "gives 230 cycles per forcicium. Force energy crystal: portable storage (5 million FE), charge/"
                + "discharge it in the capacitor or use it directly as power source in the projector etc.");
        add("emi.craft.info.mffs_extractor", german
                ? "Wandelt EU (alle Seiten, bis 2 048 EU/Paket, sonst Explosion) oder MJ (1 MJ = 2,5 EU) + Forcicium "
                + "in Kraftenergie: 4 000 EU → 12 000 KE je Takt, Takt alle 20 Ticks, je Beschleuniger 1 Tick "
                + "schneller (max. 19). Power-Link-Karte einlegen, dann schickt er die KE an den Kondensator. "
                + "Kapazitäts-Upgrade +100 000 KE Puffer. Mit einer Forcicium-Zelle rechts füllt er stattdessen die "
                + "Zelle (200 000 EU je Forcicium)."
                : "Turns EU (all sides, up to 2,048 EU/packet, else explosion) or MJ (1 MJ = 2.5 EU) + forcicium into "
                + "force energy: 4,000 EU → 12,000 FE per cycle, one cycle every 20 ticks, 1 tick faster per booster "
                + "(max. 19). Insert a power link card and it sends the FE to the capacitor. Capacity upgrade +100,000 "
                + "FE buffer. With a forcicium cell in the right slot it fills the cell instead (200,000 EU per "
                + "forcicium).");
        add("emi.craft.info.mffs_capacitor", german
                ? "Speichert 10 Mio. KE (+2 Mio. je Kapazitäts-Upgrade, max. 9) und versorgt drahtlos alle Geräte mit "
                + "seiner Power-Link-Karte in Reichweite: 8 Blöcke, +8 je Reichweiten-Upgrade (max. 80). Mittlerer "
                + "Platz: Kristall/Multiwerkzeug laden oder entladen, oder Power-Link zu einem anderen Kondensator "
                + "(bei ≥ 95 % weitergeben, ausgleichen, alles weitergeben)."
                : "Stores 10 million FE (+2 million per capacity upgrade, max. 9) and wirelessly powers every device "
                + "holding its power link card within range: 8 blocks, +8 per range upgrade (max. 80). Middle slot: "
                + "charge or discharge a crystal/multitool, or a power link to another capacitor (pass on at ≥ 95 %, "
                + "balance, pass on everything).");
        add("emi.craft.info.mffs_converter", german
                ? "Wandelt Kraftenergie zurück in EU: 3 KE je EU. Power-Link-Karte einlegen, Paketgröße (1/32/128/512/"
                + "2 048 EU) und Pakete je Tick (1–9) bei ausgeschaltetem Ausgang wählen, dann „IC 2“ einschalten. "
                + "Gibt an alle Seiten ab. Extraktor + Konverter übertragen EU drahtlos und verlustfrei (bis 80 "
                + "Blöcke)."
                : "Turns force energy back into EU: 3 FE per EU. Insert a power link card, choose packet size (1/32/"
                + "128/512/2,048 EU) and packets per tick (1–9) while the output is off, then switch \"IC 2\" on. "
                + "Outputs to all sides. Extractor + converter move EU wirelessly and without loss (up to 80 blocks).");
        add("emi.craft.info.mffs_machine", german
                ? "MFFS-Maschinen sind unzerstörbar: Abbau mit dem Multiwerkzeug ‹Schraubenschlüssel› (Vorderseite "
                + "anklicken; andere Seite dreht) oder dem IC2-Schraubenschlüssel, nur ausgeschaltet. Schaltmodus "
                + "(Knopf oben rechts): Redstone, Schalter (Multiwerkzeug ‹Schalter›) oder Steuerungssystem. Jede "
                + "Maschine hält ihren Chunk geladen. Name per Tastatur im GUI. Karten mit Daten fallen beim Abbau "
                + "als leere Karte heraus."
                : "MFFS machines are unbreakable: remove them with the multitool <wrench> (click the front; another "
                + "side rotates) or the IC2 wrench, only while off. Switch mode (button top right): redstone, switch "
                + "(multitool <switch>) or control system. Every machine keeps its chunk loaded. Name by typing in "
                + "the GUI. Cards with data drop as blank cards when removed.");
        add("emi.craft.info.mffs_cards", german
                ? "Leere Karte auf einen Kondensator → Power-Link-Karte, auf eine aktive Sicherheitsstation → "
                + "Sicherheits-Link-Karte. Rechtsklick mit der Karte auf eine Maschine setzt sie ein (liegt dort eine "
                + "leere Karte, wird kopiert). Gültig, solange das Ziel existiert und in Reichweite ist (folgt ihm "
                + "auch nach dem Verschieben). Jede Datenkarte allein in der Werkbank → leere Karte. "
                + "Unendlich-Energie-Karte: nur Kreativ, immer 10 Mio. KE."
                : "Blank card on a capacitor → power link card, on an active security station → security station link "
                + "card. Right clicking a machine with the card inserts it (a blank card already there gets a copy). "
                + "Valid while the target exists and is in range (follows it even after moving). Any data card alone "
                + "in the crafting grid → blank card. Infinite power card: creative only, always 10 million FE.");
        add("emi.craft.info.mffs_projector", german
                ? "Erzeugt das Kraftfeld: Power-Link-Karte (oder Kristall), Modul in der Mitte, dann Distanz-/"
                + "Stärke-Modulatoren und Fokusmatrizen (oben/unten/rechts/links) je nach Modul. Einschalten kostet "
                + "10 KE je Feldblock, Unterhalt 1 KE je Feldblock alle 20 Ticks (Wand/Deflektor ohne Upgrade ×2). "
                + "Feldblöcke ersetzen nur Luft und Flüssigkeit, höchstens 5 000 je Durchlauf. Zugriffsart: kein "
                + "Zutritt, frei, Station des Kondensators oder eigene Station (Sicherheits-Link-Karte)."
                : "Creates the force field: power link card (or crystal), module in the middle, then distance/strength "
                + "modulators and focus matrices (up/down/right/left) depending on the module. Switching on costs 10 "
                + "FE per field block, upkeep 1 FE per field block every 20 ticks (wall/deflector without upgrade ×2). "
                + "Field blocks only replace air and liquids, at most 5,000 per pass. Access: none, free, capacitor's "
                + "station or own station (security station link card).");
        add("emi.craft.info.mffs_modules", german
                ? "D = Distanz-, S = Stärke-Modulatoren. Wand/Diagonale Wand: Kreuz bzw. Diagonalen aus den "
                + "Fokusmatrizen, S+1 tief, ab D+1 vor dem Projektor. Deflektor: Fläche aus den Fokusmatrizen im "
                + "Abstand D+1. Röhre: offener Hohlquader um den Projektor (±(D+2), Länge ±S). Würfel: Hohlwürfel "
                + "Radius D+4. Kugel: Schale Radius D+4, Dicke S+1. Eindämmung: Hohlquader vor dem Projektor. "
                + "Erw. Würfel: Hohlquader um den Projektor aus den Fokusmatrizen und S."
                : "D = distance, S = strength modulators. Wall/diagonal wall: cross or diagonals from the focus "
                + "matrices, S+1 deep, from D+1 in front of the projector. Deflector: area from the focus matrices at "
                + "distance D+1. Tube: open hollow box around the projector (±(D+2), length ±S). Cube: hollow cube "
                + "radius D+4. Sphere: shell radius D+4, thickness S+1. Containment: hollow box in front of the "
                + "projector. Adv. cube: hollow box around the projector from the focus matrices and S.");
        add("emi.craft.info.mffs_upgrades", german
                ? "Bis zu 3 Upgrades, nur bei ausgeschaltetem Projektor und passendem Modul. Berührungsschaden: 10 "
                + "Schaden je Berührung. Schwamm: entfernt Flüssigkeiten im Innenraum. Feldmanipulator: Kuppel/"
                + "Halbform. Blockbrecher: räumt Blöcke auf Feldplätzen ab. Störsender: andere Projektoren mit "
                + "Feldpunkten im Innenraum brennen aus (Reparatur mit Multiwerkzeug ‹Schraubenschlüssel›). Tarnung: "
                + "Feld sieht aus wie der Block im Tarnplatz. Feldfusion: Felder am selben Kondensator verschmelzen. "
                + "NPC-Abwehr: 10 Schaden an Monstern im Feld. Verteidigungsstation: 10 Schaden an Spielern ohne "
                + "Recht „Im Gebiet bleiben“. Abwehr je 10 000 KE."
                : "Up to 3 upgrades, only while the projector is off and the module supports them. Touch damage: 10 "
                + "damage per touch. Sponge: removes liquids inside. Field manipulator: dome/half shape. Block "
                + "breaker: clears blocks on field positions. Jammer: other projectors with field points inside burn "
                + "out (repair with multitool <wrench>). Camouflage: the field looks like the block in the camouflage "
                + "slot. Field fusion: fields on the same capacitor merge. NPC defense: 10 damage to monsters in the "
                + "field. Defense station: 10 damage to players without the \"Stay in Area\" right. Defense costs "
                + "10,000 FE each.");
        add("emi.craft.info.mffs_security_station", german
                ? "Aktiv, sobald ein Personalausweis im Master-Platz liegt (dessen Besitzer hat alle Rechte). Ausweis "
                + "in den Bearbeitungsplatz: Rechte-Knöpfe schalten die 8 Rechte. Leere Karte dort + „+“ → "
                + "Zugangslizenz (5 min, ±5 min je Klick). Ausweise in der Ablage zählen. Leere Karte + Ausweis "
                + "rechts unten → Kopie. Ausweise macht das Multiwerkzeug ‹Kodierer›. Inaktive Station = alles erlaubt."
                : "Active as soon as a personal ID card lies in the master slot (its owner has every right). ID card "
                + "in the edit slot: the right buttons toggle the 8 rights. Blank card there + \"+\" → access license "
                + "(5 min, ±5 min per click). ID cards in storage count. Blank card + ID card bottom right → copy. ID "
                + "cards come from the multitool <coder>. Inactive station = everything allowed.");
        add("emi.craft.info.mffs_security_storage", german
                ? "54 Plätze, gesichert über eine Sicherheits-Link-Karte; öffnen nur mit Recht „Sicherheitsspeicher "
                + "öffnen“. Eingeschaltet (Schalter/Steuerungssystem) sperrt er Trichter und Rohre, ausgeschaltet "
                + "nehmen und geben sie wie bei einer Truhe."
                : "54 slots, secured by a security station link card; opening needs the \"Open Secure Storage\" "
                + "right. Switched on (switch/control system) it blocks hoppers and pipes, switched off they insert "
                + "and extract like a chest.");
        add("emi.craft.info.mffs_control_system", german
                ? "Fernsteuerung: Sicherheits-Link-Karte einlegen, Datenlink-Karten in die Ablage, die aktive in den "
                + "Leser. Zeigt Name, Typ, Zustand, Energie und Schaltmodus des Ziels; schaltet Modus und Wert um und "
                + "öffnet dessen GUI (bis 61 Blöcke, das GUI bleibt nur bis 8 Blöcke offen). Datenlink-Karte: "
                + "Multiwerkzeug ‹Kodierer› auf eine Maschine (1 000 KE + leere Karte)."
                : "Remote control: insert a security station link card, data link cards into storage, the active one "
                + "into the reader. Shows name, type, state, energy and switch mode of the target; toggles mode and "
                + "value and opens its GUI (up to 61 blocks, the GUI only stays open within 8 blocks). Data link card: "
                + "multitool <coder> on a machine (1,000 FE + blank card).");
        add("emi.craft.info.mffs_defense_station", german
                ? "Braucht Power-Link- und Sicherheits-Link-Karte. Distanz-Modulatoren: Aktionsbereich und "
                + "Warnbereich (Zusatz: Aktionsbereich + diese + 3). Kostet 10 KE × Warnradius je Tick. Im Warnbereich: "
                + "Warnung + 1 Schaden. Im Aktionsbereich je Modus: informieren, Spieler töten (Inventar ins Lager), "
                + "durchsuchen (Gegenstände der Liste bzw. nicht auf der Liste beschlagnahmen) oder alle/feindliche/"
                + "freundliche NPCs töten. Recht „Im Gebiet bleiben“ schützt."
                : "Needs a power link and a security station link card. Distance modulators: action range and "
                + "warning range (extra: action range + these + 3). Costs 10 FE × warning radius per tick. In the warning "
                + "range: warning + 1 damage. In the action range depending on mode: inform, kill players (inventory "
                + "into storage), search (confiscate items on or not on the list) or kill all/hostile/friendly NPCs. "
                + "The \"Stay in Area\" right protects.");
        add("emi.craft.info.mffs_multitool", german
                ? "Ein Werkzeug, mehrere Modi: Schleichen + Rechtsklick wechselt Schraubenschlüssel → Schalter → "
                + "Kodierer → Feld-Teleporter → Handbuch (Ladung bleibt, 1 Mio. KE, im Kondensator laden). "
                + "Schraubenschlüssel dreht/baut ab (1 000 KE) und repariert ausgebrannte Projektoren (10 000 KE). "
                + "Schalter schaltet Maschinen im Schaltermodus. Kodierer: Luft → eigener Ausweis, Linksklick auf "
                + "Spieler → seiner, Maschine → Datenlink. Feld-Teleporter: durch ein Feld bis 5 dick (10 000 KE). "
                + "Debugger nur Kreativ, gilt als Freibrief."
                : "One tool, several modes: sneak + right click cycles wrench → switch → coder → field teleporter → "
                + "guide (charge is kept, 1 million FE, charge in the capacitor). Wrench rotates/removes (1,000 FE) "
                + "and repairs burnt out projectors (10,000 FE). Switch toggles machines in switch mode. Coder: air → "
                + "own ID card, left click a player → theirs, machine → data link. Field teleporter: through a field up "
                + "to 5 thick (10,000 FE). Debugger creative only, counts as full access.");
    }

    /** EMI info pages of ChargePads (spec 47). */
    private void chargePadInfo(boolean german) {
        add("emi.craft.info.cp_charge_pad", german
                ? "Lädt Elektro-Items der Spieler, die darauf stehen (bis zur Pad-Stufe 1/2/3). EU nur von unten, "
                + "Speicher 40 000 / 600 000 / 10 000 000 EU, Laderate = Eingang 32 / 128 / 512 EU/t; zu großes "
                + "Paket → Explosion. Aktiv kostet er Grundverbrauch (Stufe EU/t) und gibt Redstone 15 nach unten und "
                + "zur Seite. Zuerst die gehaltene Hand, nur wenn dort nichts zu laden ist die Rüstung. Batterieslot "
                + "lädt den Pad (Redstone 500 EU, Einweg-Batterie 1 000 EU)."
                : "Charges the electric items of players standing on it (up to the pad tier 1/2/3). EU only from "
                + "below, storage 40,000 / 600,000 / 10,000,000 EU, charge rate = input 32 / 128 / 512 EU/t; oversized "
                + "packet → explosion. While active it uses a base drain (tier EU/t) and emits redstone 15 downwards "
                + "and to the sides. First the held item, the armour only if nothing in hand can be charged. The "
                + "battery slot charges the pad (redstone 500 EU, single-use battery 1,000 EU).");
        add("emi.craft.info.cp_hazard", german
                ? "Vorsicht, Strom! Nur der Schraubenschlüssel baut den Pad ab (ohne Energie). Abbauen ohne Schlüssel: "
                + "Stromschlag in der Umgebung (je nach Speicher bis 10 / 12 / 20 Schaden, ignoriert Rüstung), ein Teil "
                + "des Inventars geht verloren, der Pad droppt nicht. Wer aus mehr als 3,5 / 2,5 / 1,5 Blöcken auf "
                + "den Pad fällt, bekommt 3 / 6 / 9 Schaden extra. Mit Entladungs- oder Schadensmodul öffnen GUI und "
                + "Schlüssel nur von unten."
                : "Danger, electricity! Only the wrench removes the pad (without energy). Breaking it otherwise: "
                + "electric shock around it (up to 10 / 12 / 20 damage depending on storage, ignores armour), part of "
                + "the inventory is lost, the pad drops nothing. Falling more than 3.5 / 2.5 / 1.5 blocks onto the pad "
                + "deals 3 / 6 / 9 extra damage. With a drain or damage module, GUI and wrench only work from below.");
        add("emi.craft.info.cp_upgrades", german
                ? "Upgrade-Plätze 1 / 2 / 3. Übertakter (max. 16): Laderate × 1,05 je Stück, +1 EU/t Grundverbrauch. "
                + "Effizienz-Upgrade (max. 16): hebt den Übertakter-Mehrverbrauch auf, Ladekosten × 0,995 je Stück; "
                + "im Rezept nur ein voll geladenes RE-Akku. Energiespeicher: + Stufe × 10 000 EU. Transformer "
                + "(max. 4): Eingang × 4, Batterieslot eine Stufe höher."
                : "Upgrade slots 1 / 2 / 3. Overclocker (max. 16): charge rate × 1.05 each, +1 EU/t base drain. "
                + "Efficiency upgrade (max. 16): cancels the overclocker drain, charging cost × 0.995 each; its recipe "
                + "needs a fully charged RE battery. Energy storage: + tier × 10,000 EU. Transformer (max. 4): input "
                + "× 4, battery slot one tier higher.");
        add("emi.craft.info.cp_upgrade_kit", german
                ? "Statisch → Kristallisator → Lapotron: in der Werkbank mit dem Pad oder per Rechtsklick auf die "
                + "Unterseite des gesetzten Pads – Speicher und Inhalt bleiben. Ist der Pad gerade aktiv, gibt es "
                + "einen Stromschlag. Die MFSU-Aufrüstung ist nur ein Bauteil des Lapotron-Ausbausatzes."
                : "Static → Crystalizor → Lapotronic: at the crafting table with the pad or by right clicking the "
                + "bottom of the placed pad – storage and contents stay. If the pad is active you get a shock. The MFS "
                + "unit upgrade is only a part of the lapotronic upgrade kit.");
        add("emi.craft.info.cp_field_module", german
                ? "Feld-Platz (Kristallisator/Lapotron). Entladung: das Feld entlädt statt zu laden. Schaden: Lebewesen "
                + "im Feld bekommen laufend Schaden und Vergiftung, nichts wird geladen. Rüstungsvorrang: zuerst die "
                + "Rüstung, dann die Hand."
                : "Field slot (crystalizor/lapotronic). Drain: the field discharges instead of charging. Damage: living "
                + "beings in the field take continuous damage and poison, nothing is charged. Armour priority: armour "
                + "first, then the hand.");
        add("emi.craft.info.cp_projector_module", german
                ? "Projektor-Platz (Kristallisator/Lapotron). Nahbereich: auch die Hotbar-Fächer neben der Hand. "
                + "Breitband: die ganze Hotbar. Feldausdehnung Alpha/Delta/Theta: Feld +1/+2/+3 Blöcke zu jeder "
                + "Seite, Grundverbrauch +2/+4/+6 EU/t; Delta und Theta nur im Lapotron-Pad."
                : "Projector slot (crystalizor/lapotronic). Proximity: also the hotbar slots next to the hand. Wide "
                + "band: the whole hotbar. Field expansion alpha/delta/theta: field +1/+2/+3 blocks to each side, base "
                + "drain +2/+4/+6 EU/t; delta and theta only in the lapotronic pad.");
    }

    /** EMI info pages of IC2 Nuclear Control (spec 46). */
    private void nuclearControlInfo(boolean german) {
        add("emi.craft.info.nc_thermal_monitor", german
                ? "Direkt an einen Reaktor oder eine Reaktorkammer setzen (braucht festen Halt). Ab der eingestellten "
                + "Hitze (Standard 500, GUI ±1 … ±10 000) gibt er ein Redstone-Signal 15 nach allen Seiten außer zum "
                + "Reaktor; „Invertieren“ kehrt das um. Front grün/rot, grau ohne Reaktor."
                : "Place directly on a reactor or reactor chamber (needs a solid block). From the set heat (default "
                + "500, GUI ±1 … ±10,000) it emits redstone 15 to all sides except towards the reactor; \"Invert\" "
                + "reverses it. Front green/red, grey without a reactor.");
        add("emi.craft.info.nc_remote_thermal_monitor", german
                ? "Wie der Thermomonitor, aber aus der Ferne: Reaktor-Sensorkarte einlegen. Braucht 1 EU/t (Batterie-"
                + "Slot oder Kabel, 600 EU Speicher; zu großes Paket → Explosion). Reichweite 8 Blöcke je Achse, mit "
                + "Reichweiten-Upgrades ×2 je Stück (max. 7 → 1024). Transformator-/Energiespeicher-Upgrades passen."
                : "Like the thermal monitor, but remote: insert a reactor sensor location card. Needs 1 EU/t (battery "
                + "slot or cable, 600 EU storage; oversized packet → explosion). Range 8 blocks per axis, ×2 per range "
                + "upgrade (max. 7 → 1024). Transformer/energy storage upgrades fit.");
        add("emi.craft.info.nc_thermometer", german
                ? "Rechtsklick auf Reaktor oder Kammer zeigt die Hüllentemperatur. Thermometer: 11 Messungen, dann "
                + "kaputt. Digitalthermometer: 50 EU je Messung (12 000 EU, LV), zeigt dazu die Grenzen für "
                + "Wasserverdampfen (50 %) und Schmelzen (85 %)."
                : "Right click a reactor or chamber to show the hull heat. Thermometer: 11 readings, then it breaks. "
                + "Digital thermometer: 50 EU per reading (12,000 EU, LV), also shows the limits for water evaporating "
                + "(50 %) and melting (85 %).");
        add("emi.craft.info.nc_alarm", german
                ? "Heult, solange Redstone anliegt (am Block oder an angrenzendem Draht). GUI: Ton (Standard/Sci-Fi) "
                + "und Reichweite 0–128 Blöcke (Standard 64). Der Industriealarm blinkt zusätzlich mit Licht. "
                + "Braucht festen Halt."
                : "Sounds while powered by redstone (at the block or adjacent wire). GUI: sound (default/sci-fi) and "
                + "range 0–128 blocks (default 64). The industrial alarm also flashes light. Needs a solid block.");
        add("emi.craft.info.nc_information_panel", german
                ? "Zeigt den Inhalt einer Karte, aber nur mit Redstone-Signal (dann leuchtet die Tafel). "
                + "Infotafel-Erweiterungen in gleicher Ausrichtung vergrößern den Bildschirm (je Richtung bis 20). "
                + "Reichweiten-Upgrade: Fernkarten-Reichweite 8 × 2ⁿ (max. 7). Farb-Upgrade: Farben wählen („T“). "
                + "GUI: Zeilen ein/aus, Kartentitel. Shift + Schraubenschlüssel dreht den Text."
                : "Shows a card's content, but only with a redstone signal (then the panel lights up). Information "
                + "panel extenders facing the same way enlarge the screen (up to 20 each direction). Range upgrade: "
                + "remote card range 8 × 2ⁿ (max. 7). Color upgrade: choose colours (\"T\"). GUI: lines on/off, card "
                + "title. Shift + wrench rotates the text.");
        add("emi.craft.info.nc_sensor_kit", german
                ? "Sensorkarten haben kein Rezept: Mit dem passenden Kit auf das Ziel rechtsklicken, dann wird das Kit "
                + "zur Karte mit dessen Koordinaten. Reaktor-Kit → Reaktor/Kammer, Energie-Kit → BatBox/MFE/MFSU, "
                + "GT-Maschine (EU) oder BC-Empfänger (MJ), Flüssigkeits-Kit → Tank, Zähler-Kit → Energie-/"
                + "Durchschnittszähler, GregTech-Kit → Hochofen, Grinder, Zentrifuge, Elektrolyseur, "
                + "Implosionskompressor, Vakuumgefrierer."
                : "Sensor cards have no recipe: right click the target with the matching kit and the kit turns into "
                + "the card with its coordinates. Reactor kit → reactor/chamber, energy kit → BatBox/MFE/MFSU, GT "
                + "machine (EU) or BC receiver (MJ), liquid kit → tank, counter kit → energy/average counter, "
                + "GregTech kit → blast furnace, grinder, centrifuge, electrolyzer, implosion compressor, vacuum "
                + "freezer.");
        add("emi.craft.info.nc_energy_array_card", german
                ? "Werkbank, formlos: 2–6 Energie-Sensorkarten → Energiefeld-Karte; Feldkarte + weitere Energiekarten "
                + "→ mehr Ziele (höchstens 6). Zeigt je Speicher und/oder die Summe. Feldkarte allein → 2 "
                + "Elektronische Schaltungen je Ziel."
                : "Crafting, shapeless: 2–6 energy sensor location cards → energy array location card; array card + "
                + "more energy cards → more targets (at most 6). Shows each storage and/or the total. Array card alone "
                + "→ 2 electronic circuits per target.");
        add("emi.craft.info.nc_text_time_card", german
                ? "Textkarte: 10 Zeilen freier Text, bearbeitet über „...“ in der Infotafel. Zeitkarte: Tageszeit "
                + "hh:mm. Beide brauchen keinen Sensor."
                : "Text card: 10 lines of free text, edited with \"...\" in the information panel. Time card: time of "
                + "day hh:mm. Neither needs a sensor.");
        add("emi.craft.info.nc_counter", german
                ? "EU rein an der Front (zeigt beim Setzen zum Spieler), raus an allen anderen Seiten, ein volles Paket "
                + "je Tick. Paket 32 EU, ×4 je Transformator-Upgrade (max. 4); größere Pakete → Explosion. "
                + "Energiezähler zählt die durchgeleiteten EU (Reset), Durchschnittszähler zeigt EU/t über 1/3/5/10 s."
                : "EU in at the front (faces the player when placed), out at all other sides, one full packet per "
                + "tick. Packet 32 EU, ×4 per transformer upgrade (max. 4); larger packets → explosion. The energy "
                + "counter counts the EU passed (reset), the average counter shows EU/t over 1/3/5/10 s.");
        add("emi.craft.info.nc_range_trigger", german
                ? "Energie- oder Energiefeld-Karte einlegen und zwei Grenzen einstellen: ab der oberen Redstone an, "
                + "unter der unteren aus, dazwischen bleibt es (Hysterese). „Invertieren“ kehrt das Signal um. "
                + "Reichweiten-Upgrade wie bei der Infotafel."
                : "Insert an energy or energy array card and set two limits: redstone on from the upper, off below the "
                + "lower, unchanged in between (hysteresis). \"Invert\" reverses the signal. Range upgrade as on the "
                + "information panel.");
    }

    /** IC2 crops (spec 05 §9): blocks, items, crop names, Cropnalyzer texts. */
    protected void crops(boolean german) {
        block(Ic2Blocks.CROP, german ? "Crop-Stab" : "Crop");
        item(Ic2Items.CROP_SEED_BAG, german ? "Saatbeutel" : "Seed Bag");
        add("item.craft.crop_seed_bag.unknown", german ? "Unbekannte Samen" : "Unknown Seeds");
        add("item.craft.crop_seed_bag.invalid", german ? "Samen ohne Daten – Fehler?" : "Seed is missing data - bug?");
        add("item.craft.crop_seed_bag.named", german ? "%s-Samen" : "%s Seeds");
        item(Ic2Items.CROPNALYZER, "Cropnalyzer");
        item(Ic2Items.FERTILIZER, german ? "Dünger" : "Fertilizer");
        item(Ic2Items.HYDRATING_CELL, german ? "Hydrierzelle" : "Hydration Cell");
        item(Ic2Items.WEED_EX, "Weed-EX");
        item(Ic2Items.GRIN_POWDER, german ? "Grinpulver" : "Grin Powder");
        add("crop.craft.weed", german ? "Unkraut" : "Weed");
        add("crop.craft.wheat", german ? "Weizen" : "Wheat");
        add("crop.craft.pumpkin", german ? "Kürbis" : "Pumpkin");
        add("crop.craft.melon", german ? "Melone" : "Melon");
        add("crop.craft.dandelion", german ? "Löwenzahn" : "Dandelion");
        add("crop.craft.rose", "Rose");
        add("crop.craft.blackthorn", german ? "Schwarzdorn" : "Blackthorn");
        add("crop.craft.tulip", german ? "Tulpe" : "Tulip");
        add("crop.craft.cyazint", "Cyazint");
        add("crop.craft.venomilia", "Venomilia");
        add("crop.craft.reed", german ? "Schilf" : "Reed");
        add("crop.craft.stickreed", german ? "Klebschilf" : "Stickreed");
        add("crop.craft.cocoa", german ? "Kakao" : "Cocoa");
        add("crop.craft.ferru", "Ferru");
        add("crop.craft.aurelia", "Aurelia");
        add("crop.craft.redwheat", german ? "Rotweizen" : "Redwheat");
        add("crop.craft.nether_wart", german ? "Netherwarze" : "Nether Wart");
        add("crop.craft.terra_wart", german ? "Terra-Warze" : "Terra Wart");
        add("crop.craft.coffee", german ? "Kaffee" : "Coffee");
        add("crop.craft.hops", german ? "Hopfen" : "Hops");
        add("crop.craft.carrots", german ? "Karotten" : "Carrots");
        add("crop.craft.potato", german ? "Kartoffel" : "Potato");
        add("emi.category.craft.crops", "Crops");
        add("emi.craft.crop.planted", german ? "%s: Größe %s, Gr %s Ga %s Re %s" : "%s: size %s, Gr %s Ga %s Re %s");
        add("emi.craft.crop.tier", german ? "%s – Stufe %s" : "%s - tier %s");
        add("emi.craft.crop.wheat", german ? "bis 7 (Licht ≥ 9), Ernte bei 7" : "to 7 (light ≥ 9), harvest at 7");
        add("emi.craft.crop.pumpkin", german ? "bis 4, Ernte bei 4" : "to 4, harvest at 4");
        add("emi.craft.crop.melon", german ? "4: Melone (1/3) oder 2–5 Scheiben" : "4: melon (1/3) or 2-5 slices");
        for (String flower : new String[]{"dandelion", "rose", "blackthorn", "tulip", "cyazint"}) {
            add("emi.craft.crop." + flower, german ? "bis 4 (Licht ≥ 12), Ernte bei 4" : "to 4 (light ≥ 12), harvest at 4");
        }
        add("emi.craft.crop.venomilia", german ? "4: Farbstoff, 5: Grinpulver; vergiftet bei 5"
                : "4: dye, 5: grin powder; poisons at 5");
        add("emi.craft.crop.reed", german ? "bis 3, Größe − 1 Zuckerrohr" : "to 3, size - 1 sugar cane");
        add("emi.craft.crop.stickreed", german ? "bis 3 Zuckerrohr, 4: Harz" : "to 3 sugar cane, 4: resin");
        add("emi.craft.crop.cocoa", german ? "bis 4 (Nährstoffe ≥ 3)" : "to 4 (nutrients ≥ 3)");
        add("emi.craft.crop.ferru", german ? "Stufe 4 nur über Eisenerz" : "size 4 only above iron ore");
        add("emi.craft.crop.aurelia", german ? "Stufe 4 nur über Golderz" : "size 4 only above gold ore");
        add("emi.craft.crop.redwheat", german ? "bis 7 (Licht 5–10); leuchtet, Redstone-Signal"
                : "to 7 (light 5-10); glows, redstone signal");
        add("emi.craft.crop.nether_wart", german ? "schneller auf Seelensand; auf Schnee → Terra-Warze"
                : "faster on soul sand; on snow → Terra Wart");
        add("emi.craft.crop.terra_wart", german ? "schneller auf Schnee; auf Seelensand → Netherwarze"
                : "faster on snow; on soul sand → Nether Wart");
        add("emi.craft.crop.coffee", german ? "bis 5 (Licht ≥ 9), Bohnen nur bei 5" : "to 5 (light ≥ 9), beans at 5 only");
        add("emi.craft.crop.hops", german ? "bis 7 (Licht ≥ 9), Ernte bei 7" : "to 7 (light ≥ 9), harvest at 7");
        add("emi.craft.crop.carrots", german ? "bis 3 (Licht ≥ 9), Ernte bei 3" : "to 3 (light ≥ 9), harvest at 3");
        add("emi.craft.crop.potato", german ? "bis 3 (Licht ≥ 9); 1/50 giftig" : "to 3 (light ≥ 9); 1/50 poisonous");
        item(Ic2Items.TERRA_WART, german ? "Terra-Warze" : "Terra Wart");
        item(Ic2Items.COFFEE_BEANS, german ? "Kaffeebohnen" : "Coffee Beans");
        item(Ic2Items.HOPS, german ? "Hopfen" : "Hops");
        item(Ic2Items.COFFEE_POWDER, german ? "Kaffeepulver" : "Coffee Powder");
        item(Ic2Items.STONE_MUG, german ? "Steinkrug" : "Stone Mug");
        item(Ic2Items.COLD_COFFEE, german ? "Kalter Kaffee" : "Cold Coffee");
        item(Ic2Items.DARK_COFFEE, german ? "Schwarzer Kaffee" : "Dark Coffee");
        item(Ic2Items.COFFEE, german ? "Kaffee" : "Coffee");
        // ItemBarrel/ItemBooze names are hard-coded English in IC2; the brew names stay English (proper names).
        item(Ic2Items.BOOZE_BARREL, german ? "Leeres Fass" : "Empty Booze Barrel");
        add(Ic2Items.BOOZE_BARREL.get().getDescriptionId() + ".filled", german ? "Fass (%s L)" : "%sL Booze Barrel");
        add(Ic2Blocks.BARREL.get().getDescriptionId(), german ? "Fass" : "Booze Barrel");
        String booze = Ic2Items.BOOZE_MUG.get().getDescriptionId();
        add(booze, "Booze");
        add(booze + ".zero", "Zero");
        add(booze + ".rum", "Rum");
        add(booze + ".black_stuff", "Black Stuff");
        add(booze + ".beer", "%s%s%s");
        String[] solid = {"Watery ", "Clear ", "Lite ", "", "Strong ", "Thick ", "Stodge "};
        String[] hops = {"Soup ", "Alcfree ", "White ", "", "Dark ", "Full ", "Black "};
        String[] time = {"Brew", "Youngster", "Beer", "Ale", "Dragonblood"};
        for (int i = 0; i < solid.length; i++) {
            add(booze + ".solid." + i, solid[i]);
            add(booze + ".hops." + i, hops[i]);
        }
        for (int i = 0; i < time.length; i++) {
            add(booze + ".time." + i, time[i]);
        }
        add("message.craft.crop.unknown", german ? "Unbekannte Pflanze" : "Unknown Crop");
        add("message.craft.crop.scanned", "%s - Gr: %s Ga: %s Re: %s");
        add("gui.craft.cropnalyzer.unknown", german ? "UNBEKANNT" : "UNKNOWN");
        add("gui.craft.cropnalyzer.tier", german ? "Stufe: %s" : "Tier: %s");
        add("gui.craft.cropnalyzer.discovered", german ? "Entdeckt von:" : "Discovered by:");
        add("gui.craft.cropnalyzer.growth", german ? "Wachstum:" : "Growth:");
        add("gui.craft.cropnalyzer.gain", german ? "Ertrag:" : "Gain:");
        add("gui.craft.cropnalyzer.resistance", german ? "Resist.:" : "Resis.:");
    }

    /** Names of the new power plants (spec 14), English or German. */
    protected void powerPlants(boolean german) {
        add("itemGroup.craft.power_plants", german ? "Craft: Kraftwerke" : "Craft: Power Plants");
        add("gui.craft.structure_incomplete", german ? "Struktur unvollständig" : "Structure incomplete");
        add("gui.craft.output", german ? "Erzeugt: %s %s" : "Output: %s %s");
        add("gui.craft.consumption", german ? "Verbraucht: %s %s" : "Consumes: %s %s");
        add("tooltip.craft.charge", "%s / %s EU");
        add("tooltip.craft.fuel", german ? "Treibstoff: %s %%" : "Fuel: %s %%");
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
        block(PpBlocks.WATER_TURBINE_CASING, german ? "Wasserturbinengehäuse" : "Water Turbine Casing");
        block(PpBlocks.WATER_TURBINE, german ? "Wasserturbine" : "Water Turbine");
        add("message.craft.water_turbine.status", german ? "Fließendes Wasser: %s, Leistung %s EU/t"
                : "Flowing water: %s, output %s EU/t");
        add("message.craft.water_turbine.incomplete", german ? "Struktur unvollständig" : "Structure incomplete");
        block(PpBlocks.HIGH_PRESSURE_BOILER_CASING, german ? "Hochdruck-Kesselgehäuse" : "High-Pressure Boiler Casing");
        block(PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX, german ? "Hochdruck-Kesselfeuerung"
                : "High-Pressure Boiler Firebox");
        block(PpBlocks.LARGE_TURBINE_CASING, german ? "Großes Turbinengehäuse" : "Large Turbine Casing");
        block(PpBlocks.LARGE_TURBINE_ROTOR, german ? "Großer Turbinenrotor" : "Large Turbine Rotor");
        block(PpBlocks.LARGE_STEAM_TURBINE, german ? "Große Dampfturbine" : "Large Steam Turbine");
        block(PpBlocks.LARGE_WATER_TURBINE_CASING, german ? "Großes Wasserturbinengehäuse"
                : "Large Water Turbine Casing");
        block(PpBlocks.LARGE_WATER_TURBINE, german ? "Große Wasserturbine" : "Large Water Turbine");
        block(PpBlocks.WIND_TURBINE_MAST, german ? "Windturbinenmast" : "Wind Turbine Mast");
        block(PpBlocks.LARGE_WIND_TURBINE, german ? "Große Windturbine" : "Large Wind Turbine");
        item(PpItems.IRON_WIND_ROTOR, german ? "Eisen-Windrotor" : "Iron Wind Rotor");
        item(PpItems.STEEL_WIND_ROTOR, german ? "Stahl-Windrotor" : "Steel Wind Rotor");
        item(PpItems.CARBON_WIND_ROTOR, german ? "Carbon-Windrotor" : "Carbon Wind Rotor");
        add("tooltip.craft.wind_rotor.factor", german ? "Leistung %s %%" : "Output %s %%");
        add("message.craft.large_wind_turbine.status", german ? "Hindernisse: %s, Leistung %s EU/t"
                : "Obstructions: %s, output %s EU/t");
        add("message.craft.large_wind_turbine.no_rotor", german ? "Kein Rotor eingesetzt" : "No rotor inserted");
    }

    /** Compact Solars names ({@code CompactSolarType.friendlyName}, English only). */
    protected void compactSolars() {
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
        item(GtItems.TUNGSTEN_CELL, "Wolframium Cell");
        item(GtItems.SULFUR_CELL, "Sulfur Cell");
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
        item(GtItems.FLINT_DUST, "Flint Dust");
        item(GtItems.SMALL_FLINT_DUST, "Tiny Pile of Flint Dust");
        item(GtItems.WOOD_PULP, "Wood Pulp");
        item(GtItems.SMALL_WOOD_PULP, "Tiny Pile of Wood Pulp");
        item(GtItems.WOOD_PLATE, "Wood Plate");
        item(GtItems.ELECTRUM_DUST, "Electrum Dust");
        item(GtItems.OBSIDIAN_DUST, "Obsidian Dust");
        item(GtItems.CHARCOAL_DUST, "Charcoal Dust");
        item(GtItems.LAZURITE_CHUNK, "Chunk of Lazurite");
        item(GtItems.ELECTRUM_PLATE, "Electrum Plate");
        item(GtItems.BASIC_CIRCUIT_BOARD, "Basic Circuit Board");
        add("tooltip.craft.gt_basic_circuit_board", "Just a simple Circuit Plate");
        item(GtItems.ADVANCED_CIRCUIT_BOARD, "Advanced Circuit Board");
        add("tooltip.craft.gt_advanced_circuit_board", "Standard Circuit Plate");
        item(GtItems.ADVANCED_CIRCUIT_PARTS, "Advanced Circuit Parts");
        add("tooltip.craft.advanced_circuit_parts", "Part of advanced Circuitry");
        item(GtItems.BRASS_DUST, "Brass Dust");
        item(GtItems.BRASS_INGOT, "Brass Ingot");
        item(GtItems.BRASS_PLATE, "Brass Plate");
        item(GtItems.FLOUR, "Flour");
        item(GtItems.IRON_MORTAR, "Iron Mortar");
        add("tooltip.craft.iron_mortar", "Can pulverize Ingots to Dust");
        item(GtItems.FLINT_MORTAR, "Flint Mortar");
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
        block(PcBlocks.converter(ConverterType.FE_PRODUCER), "FE Producer");
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
            machine(Ic2Machine.MINER, "Miner");
            block(Ic2Blocks.MINING_TIP, "Mining Pipe");
            machine(Ic2Machine.TELEPORTER, "Teleporter");
            machine(Ic2Machine.TESLA_COIL, "Tesla Coil");
            machine(Ic2Machine.CROP_MATRON, "Crop-Matron");
            machine(Ic2Machine.TERRAFORMER, "Terraformer");
            machine(Ic2Machine.PERSONAL_SAFE, "Personal Safe");
            machine(Ic2Machine.TRADE_O_MAT, "Trade-O-Mat");
            machine(Ic2Machine.ENERGY_O_MAT, "Energy-O-Mat");
            add("gui.craft.o_mat.offer", "Offer:");
            add("gui.craft.o_mat.want", "Want:");
            add("gui.craft.trade_o_mat.total_trades0", "Performed");
            add("gui.craft.trade_o_mat.total_trades1", "Trades:");
            add("gui.craft.trade_o_mat.stock", "Stock:");
            add("gui.craft.energy_o_mat.paid_for", "Paid For: %1$s EU");
            add("message.craft.personal_safe.owned", "This safe is owned by %1$s");
            add("message.craft.personal_safe.not_empty", "Can't wrench non-empty safe");
            item(Ic2Items.TERRAFORMER_BLUEPRINT, "TFBP - Empty");
            item(Ic2Items.CULTIVATION_BLUEPRINT, "TFBP - Cultivation");
            item(Ic2Items.IRRIGATION_BLUEPRINT, "TFBP - Irrigation");
            item(Ic2Items.CHILLING_BLUEPRINT, "TFBP - Chilling");
            item(Ic2Items.DESERTIFICATION_BLUEPRINT, "TFBP - Desertification");
            item(Ic2Items.FLATIFICATION_BLUEPRINT, "TFBP - Flatification");
            item(Ic2Items.MUSHROOM_BLUEPRINT, "TFBP - Mushroom");
            item(Ic2Items.FREQUENCY_TRANSMITTER, "Frequency Transmitter");
            add("message.craft.frequency_transmitter.unlinked", "Frequency Transmitter unlinked");
            add("message.craft.frequency_transmitter.linked", "Frequency Transmitter linked to Teleporter.");
            add("message.craft.frequency_transmitter.self", "Can't link Teleporter to itself.");
            add("message.craft.frequency_transmitter.unchanged", "Teleportation link unchanged.");
            add("message.craft.frequency_transmitter.established", "Teleportation link established.");
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
            block(Ic2Blocks.REINFORCED_STONE, "Reinforced Stone");
            block(Ic2Blocks.REINFORCED_DOOR, "Reinforced Door");
            block(Ic2Blocks.RUBBER_MAT, "Rubber Sheet");
            block(Ic2Blocks.CONSTRUCTION_FOAM, "Construction Foam");
            block(Ic2Blocks.CONSTRUCTION_FOAM_WALL, "Construction Foam Wall");
            block(Ic2Blocks.WOODEN_SCAFFOLD, "Scaffold");
            block(Ic2Blocks.IRON_SCAFFOLD, "Iron Scaffold");
            block(Ic2Blocks.LUMINATOR, "Luminator");
            block(Ic2Blocks.INDUSTRIAL_TNT, "Industrial TNT");
            add(Ic2EntityTypes.INDUSTRIAL_TNT.get(), "Industrial TNT");
            item(Ic2Items.DYNAMITE, "Dynamite");
            item(Ic2Items.STICKY_DYNAMITE, "Sticky Dynamite");
            item(Ic2Items.DYNAMITE_O_MOTE, "Dynamite-O-Mote");
            add("item.craft.dynamite_o_mote.linked", "Linked to %s dynamite");
            add("message.craft.dynamite_o_mote.not_linked",
                    "This dynamite stick is not linked to this remote, cannot unlink.");
            block(Ic2Blocks.DYNAMITE_STICK, "Dynamite");
            block(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE, "Dynamite (linked)");
            add(Ic2EntityTypes.DYNAMITE.get(), "Dynamite");
            add(Ic2EntityTypes.STICKY_DYNAMITE.get(), "Sticky Dynamite");
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
            item(Ic2Items.COIN, "Industrial Credit");
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
            item(Ic2Items.INSULATION_CUTTER, "Insulation Cutter");
            item(Ic2Items.EU_METER, "EU-Reader");
            item(Ic2Items.TOOLBOX, "Tool Box");
            item(Ic2Items.FOAM_SPRAYER, "CF Sprayer");
            item(Ic2Items.CONSTRUCTION_FOAM_PELLET, "CF Pellet");
            item(Ic2Items.PAINTER, "Painter");
            for (int color = 0; color < 16; color++) {
                item(Ic2Items.painter(color), "Painter");
            }
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
            item(Ic2Items.COMPOSITE_VEST, "Composite Vest");
            add(Ic2Fluids.COOLANT_TYPE.get().getDescriptionId(), "Coolant");
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
            item(Ic2Items.CF_PACK, "CF Backpack");
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
            add("message.craft.painter.auto_refill_on", "Painter automatic refill mode enabled");
            add("message.craft.painter.auto_refill_off", "Painter automatic refill mode disabled");
            add("message.craft.eu_meter.start", "Starting new measurement");
            add("message.craft.eu_meter.result", "Measured power [EU/t]: %1$s in %2$s out %3$s gain (avg. over %4$s ticks)");
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
            add("gui.craft.forestry.energy", "Energy");
            add("gui.craft.forestry.stored", "Stored");
            add("gui.craft.forestry.max_energy", "Maximum Energy");
            add("gui.craft.forestry.max_input", "Maximum Input");
            add("gui.craft.forestry.current_output", "Current Output");
            add("gui.craft.forestry.heat", "Heat");
            add("gui.craft.forestry.did_you_know", "Did you know");
            add("gui.craft.forestry.error.no_fuel", "No fuel");
            add("gui.craft.forestry.error.no_fuel.help", "Replenish this machine's fuel supplies.");
            add("gui.craft.forestry.error.no_resource", "Missing resources");
            add("gui.craft.forestry.error.no_resource.help", "More resources need to be supplied for operation.");
            add("gui.craft.forestry.error.no_space", "No inventory space");
            add("gui.craft.forestry.error.no_space.help", "Empty this machine's inventory.");
            add("gui.craft.forestry.error.no_recipe", "No recipe");
            add("gui.craft.forestry.error.no_recipe.help", "No matching recipe was found for the supplied resources.");
            add("gui.craft.forestry.error.forced_cooldown", "Forced Cooldown");
            add("gui.craft.forestry.error.forced_cooldown.help", "Engine has overheated and is forced into cooldown.");
            add("gui.craft.forestry.hint.energyunit.tag", "Say Minecraft Joule!");
            add("gui.craft.forestry.hint.energyunit",
                    "An engine's energy output is measured in MJ/t, which stands for \"Minecraft Joule per tick\". Each second has 20 ticks.");
            add("gui.craft.forestry.hint.energybuffer.tag", "Buffering Energy!");
            add("gui.craft.forestry.hint.energybuffer",
                    "Engines are capable of buffering a lesser amount of energy if it cannot be transmitted to the target.");
            add("gui.craft.forestry.hint.forcedcooldown.tag", "Beware Overheating!");
            add("gui.craft.forestry.hint.forcedcooldown",
                    "Engines can overheat if the energy they produce is not consumed. Reaching melting point will force them into cooldown.");
            add("gui.craft.forestry.hint.cooldownloss.tag", "Cooldown means energy loss!");
            add("gui.craft.forestry.hint.cooldownloss",
                    "Engines going into forced cooldown will also lose a significant chunk of their stored energy.");
            add("gui.craft.forestry.hint.enginetinbatteries.tag", "Charge those batteries!");
            add("gui.craft.forestry.hint.enginetinbatteries",
                    "You can use batteries to run your electrical engines without needing to wire them up.");
            add("gui.craft.forestry.hint.enginetinchoke.tag", "Regulate Output");
            add("gui.craft.forestry.hint.enginetinchoke",
                    "Electrical engines can be outfitted with circuit boards to choke their power output to 1 MJ/t and also reduce their energy demand.");
            add("gui.craft.forestry.hint.powerrequired.tag", "Requires Energy!");
            add("gui.craft.forestry.hint.powerrequired",
                    "This machine requires energy from an engine to function. Attach an engine and start it, to allow production.");
            add("gui.craft.forestry.hint.crating.tag", "Crate your Surplus!");
            add("gui.craft.forestry.hint.crating",
                    "Main task of carpenters is the compression of your surplus resources. Many bulk blocks and items can be packaged into crates.");
            block(BcBlocks.engine(EngineType.REDSTONE), "Redstone Engine");
            block(BcBlocks.engine(EngineType.STIRLING), "Stirling Engine");
            block(BcBlocks.engine(EngineType.COMBUSTION), "Combustion Engine");
            item(BcItems.WOODEN_GEAR, "Wood Gear");
            item(BcItems.STONE_GEAR, "Stone Gear");
            item(BcItems.IRON_GEAR, "Iron Gear");
            item(BcItems.GOLD_GEAR, "Gold Gear");
            item(BcItems.DIAMOND_GEAR, "Diamond Gear");
            item(BcItems.WRENCH, "Wrench");
            block(BcBlocks.pipe(PipeType.WOODEN_TRANSPORT), "Wooden Transport Pipe");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_TRANSPORT), "Cobblestone Transport Pipe");
            block(BcBlocks.pipe(PipeType.STONE_TRANSPORT), "Stone Transport Pipe");
            block(BcBlocks.pipe(PipeType.SANDSTONE_TRANSPORT), "Sandstone Transport Pipe");
            block(BcBlocks.pipe(PipeType.IRON_TRANSPORT), "Iron Transport Pipe");
            block(BcBlocks.pipe(PipeType.GOLDEN_TRANSPORT), "Golden Transport Pipe");
            block(BcBlocks.pipe(PipeType.DIAMOND_TRANSPORT), "Diamond Transport Pipe");
            block(BcBlocks.pipe(PipeType.OBSIDIAN_TRANSPORT), "Obsidian Transport Pipe");
            block(BcBlocks.pipe(PipeType.EMERALD_TRANSPORT), "Emerald Transport Pipe");
            add("container.craft.pipe_filters", "Filters");
            block(BcBlocks.pipe(PipeType.VOID_TRANSPORT), "Void Transport Pipe");
            block(BcBlocks.pipe(PipeType.DISTRIBUTION_TRANSPORT), "Distribution Transport Pipe");
            add("gui.craft.distribution_pipe.side.down", "Down");
            add("gui.craft.distribution_pipe.side.up", "Up");
            add("gui.craft.distribution_pipe.side.north", "North");
            add("gui.craft.distribution_pipe.side.south", "South");
            add("gui.craft.distribution_pipe.side.west", "West");
            add("gui.craft.distribution_pipe.side.east", "East");
            add("gui.craft.distribution_pipe.sum", "Sum: %s");
            add("gui.craft.distribution_pipe.hint", "All 0 = split evenly like a plain pipe");
            add("tooltip.craft.distribution_pipe", "Splits items by the share set per side (right-click)");
            add("tooltip.craft.fe_producer", "Outputs power from an adjacent Energy Bridge as FE (1 EU = 4 FE)");
            item(BcItems.PIPE_WATERPROOF, "Pipe Waterproof");
            block(BcBlocks.pipe(PipeType.WOODEN_FLUID), "Wooden Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_FLUID), "Cobblestone Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.STONE_FLUID), "Stone Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.SANDSTONE_FLUID), "Sandstone Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.IRON_FLUID), "Iron Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.GOLDEN_FLUID), "Golden Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.EMERALD_FLUID), "Emerald Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.VOID_FLUID), "Void Waterproof Pipe");
            block(BcBlocks.pipe(PipeType.WOODEN_POWER), "Wooden Conductive Pipe");
            block(BcBlocks.pipe(PipeType.STONE_POWER), "Stone Conductive Pipe");
            block(BcBlocks.pipe(PipeType.GOLDEN_POWER), "Golden Conductive Pipe");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_STRUCTURE), "Cobblestone Structure Pipe");
            block(BcBlocks.OIL, "Oil");
            item(BcItems.OIL_BUCKET, "Oil Bucket");
            item(BcItems.FUEL_BUCKET, "Fuel Bucket");
            block(BcBlocks.MARKER, "Land Mark");
            block(BcBlocks.QUARRY, "Quarry");
            block(BcBlocks.FRAME, "Frame");
            block(BcBlocks.TANK, "Tank");
            block(BcBlocks.PUMP, "Pump");
            block(BcBlocks.REFINERY, "Refinery");
            block(BcBlocks.AUTO_WORKBENCH, "Automatic Crafting Table");
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
            crops(false);
            recipeViewer(false);
            mining(false);
            redPower(false);
            portalGun(false);
            lightShield(false);
            kleinkram(false);
            musicDiscs(false);
            ironChests(false);
            enderStorage(false);
            graviSuite(false);
            nuclearControl(false);
            chargePads(false);
            mffs(false);
            modularPowersuits(false);
            advancedMachines(false);
            thermalExpansion(false);
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
            machine(Ic2Machine.MINER, "Miner");
            block(Ic2Blocks.MINING_TIP, "Bergbaurohr");
            machine(Ic2Machine.TELEPORTER, "Teleporter");
            machine(Ic2Machine.TESLA_COIL, "Teslaspule");
            machine(Ic2Machine.CROP_MATRON, "Crop-Matron");
            machine(Ic2Machine.TERRAFORMER, "Terraformer");
            machine(Ic2Machine.PERSONAL_SAFE, "Persönlicher Safe");
            machine(Ic2Machine.TRADE_O_MAT, "Trade-O-Mat");
            machine(Ic2Machine.ENERGY_O_MAT, "Energy-O-Mat");
            add("gui.craft.o_mat.offer", "Angebot:");
            add("gui.craft.o_mat.want", "Preis:");
            add("gui.craft.trade_o_mat.total_trades0", "Getätigte");
            add("gui.craft.trade_o_mat.total_trades1", "Handel:");
            add("gui.craft.trade_o_mat.stock", "Vorrat:");
            add("gui.craft.energy_o_mat.paid_for", "Bezahlt: %1$s EU");
            add("message.craft.personal_safe.owned", "Dieser Safe gehört %1$s");
            add("message.craft.personal_safe.not_empty", "Ein nicht leerer Safe kann nicht abgebaut werden");
            item(Ic2Items.TERRAFORMER_BLUEPRINT, "TFBP - Leer");
            item(Ic2Items.CULTIVATION_BLUEPRINT, "TFBP - Kultivierung");
            item(Ic2Items.IRRIGATION_BLUEPRINT, "TFBP - Bewässerung");
            item(Ic2Items.CHILLING_BLUEPRINT, "TFBP - Kühlung");
            item(Ic2Items.DESERTIFICATION_BLUEPRINT, "TFBP - Wüstenbildung");
            item(Ic2Items.FLATIFICATION_BLUEPRINT, "TFBP - Einebnung");
            item(Ic2Items.MUSHROOM_BLUEPRINT, "TFBP - Pilze");
            item(Ic2Items.FREQUENCY_TRANSMITTER, "Frequenzsender");
            add("message.craft.frequency_transmitter.unlinked", "Frequenzsender entkoppelt");
            add("message.craft.frequency_transmitter.linked", "Frequenzsender mit dem Teleporter verbunden.");
            add("message.craft.frequency_transmitter.self", "Ein Teleporter kann nicht mit sich selbst verbunden werden.");
            add("message.craft.frequency_transmitter.unchanged", "Teleporter-Verbindung unverändert.");
            add("message.craft.frequency_transmitter.established", "Teleporter-Verbindung hergestellt.");
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
            block(Ic2Blocks.REINFORCED_STONE, "Verstärkter Stein");
            block(Ic2Blocks.REINFORCED_DOOR, "Verstärkte Tür");
            block(Ic2Blocks.RUBBER_MAT, "Gummimatte");
            block(Ic2Blocks.CONSTRUCTION_FOAM, "Bauschaum");
            block(Ic2Blocks.CONSTRUCTION_FOAM_WALL, "Bauschaumwand");
            block(Ic2Blocks.WOODEN_SCAFFOLD, "Gerüst");
            block(Ic2Blocks.IRON_SCAFFOLD, "Eisengerüst");
            block(Ic2Blocks.LUMINATOR, "Luminator");
            block(Ic2Blocks.INDUSTRIAL_TNT, "Industrie-TNT");
            add(Ic2EntityTypes.INDUSTRIAL_TNT.get(), "Industrie-TNT");
            item(Ic2Items.DYNAMITE, "Dynamit");
            item(Ic2Items.STICKY_DYNAMITE, "Klebedynamit");
            item(Ic2Items.DYNAMITE_O_MOTE, "Fernzünder");
            add("item.craft.dynamite_o_mote.linked", "Mit %s Dynamit verbunden");
            add("message.craft.dynamite_o_mote.not_linked",
                    "Dieses Dynamit ist nicht mit diesem Fernzünder verbunden und kann nicht getrennt werden.");
            block(Ic2Blocks.DYNAMITE_STICK, "Dynamit");
            block(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE, "Dynamit (verbunden)");
            add(Ic2EntityTypes.DYNAMITE.get(), "Dynamit");
            add(Ic2EntityTypes.STICKY_DYNAMITE.get(), "Klebedynamit");
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
            item(Ic2Items.COIN, "Industrie-Münze");
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
            item(Ic2Items.INSULATION_CUTTER, "Isolierungsschneider");
            item(Ic2Items.EU_METER, "EU-Messgerät");
            item(Ic2Items.TOOLBOX, "Werkzeugkasten");
            item(Ic2Items.FOAM_SPRAYER, "Bauschaumsprüher");
            item(Ic2Items.CONSTRUCTION_FOAM_PELLET, "Bauschaum-Pellet");
            item(Ic2Items.PAINTER, "Farbroller");
            for (int color = 0; color < 16; color++) {
                item(Ic2Items.painter(color), "Farbroller");
            }
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
            item(Ic2Items.COMPOSITE_VEST, "Kompositweste");
            add(Ic2Fluids.COOLANT_TYPE.get().getDescriptionId(), "Kühlmittel");
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
            item(Ic2Items.CF_PACK, "CF-Rucksack");
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
            add("message.craft.painter.auto_refill_on", "Automatisches Nachfüllen eingeschaltet");
            add("message.craft.painter.auto_refill_off", "Automatisches Nachfüllen ausgeschaltet");
            add("message.craft.eu_meter.start", "Neue Messung gestartet");
            add("message.craft.eu_meter.result",
                    "Gemessene Leistung [EU/t]: %1$s rein, %2$s raus, %3$s Gewinn (Mittel über %4$s Ticks)");
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
            add("gui.craft.forestry.energy", "Energie");
            add("gui.craft.forestry.stored", "Speicher");
            add("gui.craft.forestry.max_energy", "Maximale Energie");
            add("gui.craft.forestry.max_input", "Maximale Aufnahme");
            add("gui.craft.forestry.current_output", "Aktuelle Leistung");
            add("gui.craft.forestry.heat", "Temperatur");
            add("gui.craft.forestry.did_you_know", "Wusstest du schon");
            add("gui.craft.forestry.error.no_fuel", "Kein Treibstoff");
            add("gui.craft.forestry.error.no_fuel.help", "Diese Maschine benötigt mehr Treibstoff.");
            add("gui.craft.forestry.error.no_resource", "Rohstoffe fehlen");
            add("gui.craft.forestry.error.no_resource.help", "Es müssen mehr Rohstoffe herangeschafft werden.");
            add("gui.craft.forestry.error.no_space", "Kein Lagerplatz");
            add("gui.craft.forestry.error.no_space.help", "Das Lager dieser Maschine muss geleert werden.");
            add("gui.craft.forestry.error.no_recipe", "Kein Rezept");
            add("gui.craft.forestry.error.no_recipe.help",
                    "Es wurde kein passendes Rezept gefunden. Benötigt andere Ressourcen.");
            add("gui.craft.forestry.error.forced_cooldown", "Abkühlphase");
            add("gui.craft.forestry.error.forced_cooldown.help",
                    "Motor ist überhitzt und muss eine zwangsweise Abkühlphase durchlaufen.");
            add("gui.craft.forestry.hint.energyunit.tag", "Kennst du Minecraft Joule?");
            add("gui.craft.forestry.hint.energyunit",
                    "Der Energieausstoß eines Motors wird in MJ/t (\"Minecraft Joule pro Tick\") gemessen. Jede Sekunde hat ca. 20 Ticks.");
            add("gui.craft.forestry.hint.energybuffer.tag", "Energiepuffer");
            add("gui.craft.forestry.hint.energybuffer",
                    "Motoren sind in der Lage, eine gewisse Menge Energie zu puffern, falls eine Weiterleitung nicht sofort möglich ist.");
            add("gui.craft.forestry.hint.forcedcooldown.tag", "Vermeide Überhitzung!");
            add("gui.craft.forestry.hint.forcedcooldown",
                    "Motoren können überhitzen, falls die produzierte Energie nicht abgeführt wird. Falls das passiert, werden sie zwangsweise abgekühlt.");
            add("gui.craft.forestry.hint.cooldownloss.tag", "Abkühlphase bedeutet Energieverlust!");
            add("gui.craft.forestry.hint.cooldownloss",
                    "Motoren in erzwungener Abkühlphase verlieren einen bedeutenden Teil der gespeicherten Energie.");
            add("gui.craft.forestry.hint.enginetinbatteries.tag", "Nutze Batterien!");
            add("gui.craft.forestry.hint.enginetinbatteries",
                    "Du kannst Batterien verwenden, um deine elektrischen Motoren auch ohne Netz zu unterhalten.");
            add("gui.craft.forestry.hint.enginetinchoke.tag", "Reguliere die Energie");
            add("gui.craft.forestry.hint.enginetinchoke",
                    "Elektrische Motoren können mit Leiterplatten ausgestattet werden, um ihre Eigenschaften zu verändern.");
            add("gui.craft.forestry.hint.powerrequired.tag", "Benötigt Energie!");
            add("gui.craft.forestry.hint.powerrequired",
                    "Diese Maschine benötigt Energie von einem Motor. Bringe einen solchen an und fahre ihn hoch, um mit der Produktion zu beginnen.");
            add("gui.craft.forestry.hint.crating.tag", "Komprimier Überschuss!");
            add("gui.craft.forestry.hint.crating",
                    "Hauptaufgabe eines Schreiners ist die Kompression von überschüssigen Ressourcen. Viele Massenprodukte können in Kisten verpackt werden.");
            block(BcBlocks.engine(EngineType.REDSTONE), "Redstone-Motor");
            block(BcBlocks.engine(EngineType.STIRLING), "Stirlingmotor");
            block(BcBlocks.engine(EngineType.COMBUSTION), "Verbrennungsmotor");
            item(BcItems.WOODEN_GEAR, "Holzzahnrad");
            item(BcItems.STONE_GEAR, "Steinzahnrad");
            item(BcItems.IRON_GEAR, "Eisenzahnrad");
            item(BcItems.GOLD_GEAR, "Goldzahnrad");
            item(BcItems.DIAMOND_GEAR, "Diamantzahnrad");
            item(BcItems.WRENCH, "Schraubenschlüssel");
            block(BcBlocks.pipe(PipeType.WOODEN_TRANSPORT), "Holz-Transportrohr");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_TRANSPORT), "Bruchstein-Transportrohr");
            block(BcBlocks.pipe(PipeType.STONE_TRANSPORT), "Stein-Transportrohr");
            block(BcBlocks.pipe(PipeType.SANDSTONE_TRANSPORT), "Sandstein-Transportrohr");
            block(BcBlocks.pipe(PipeType.IRON_TRANSPORT), "Eisen-Transportrohr");
            block(BcBlocks.pipe(PipeType.GOLDEN_TRANSPORT), "Gold-Transportrohr");
            block(BcBlocks.pipe(PipeType.DIAMOND_TRANSPORT), "Diamant-Transportrohr");
            block(BcBlocks.pipe(PipeType.OBSIDIAN_TRANSPORT), "Obsidian-Transportrohr");
            block(BcBlocks.pipe(PipeType.EMERALD_TRANSPORT), "Smaragd-Transportrohr");
            add("container.craft.pipe_filters", "Filter");
            block(BcBlocks.pipe(PipeType.VOID_TRANSPORT), "Leere-Transportrohr");
            block(BcBlocks.pipe(PipeType.DISTRIBUTION_TRANSPORT), "Verteiler-Transportrohr");
            add("gui.craft.distribution_pipe.side.down", "Unten");
            add("gui.craft.distribution_pipe.side.up", "Oben");
            add("gui.craft.distribution_pipe.side.north", "Norden");
            add("gui.craft.distribution_pipe.side.south", "Süden");
            add("gui.craft.distribution_pipe.side.west", "Westen");
            add("gui.craft.distribution_pipe.side.east", "Osten");
            add("gui.craft.distribution_pipe.sum", "Summe: %s");
            add("gui.craft.distribution_pipe.hint", "Alle 0 = gleichmäßig wie ein normales Rohr");
            add("tooltip.craft.distribution_pipe", "Teilt Items nach eingestellten Anteilen auf (Rechtsklick)");
            add("tooltip.craft.fe_producer", "Gibt Strom aus einer angrenzenden Energy Bridge als FE ab (1 EU = 4 FE)");
            item(BcItems.PIPE_WATERPROOF, "Rohrdichtmittel");
            block(BcBlocks.pipe(PipeType.WOODEN_FLUID), "Wasserdichtes Holzrohr");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_FLUID), "Wasserdichtes Bruchsteinrohr");
            block(BcBlocks.pipe(PipeType.STONE_FLUID), "Wasserdichtes Steinrohr");
            block(BcBlocks.pipe(PipeType.SANDSTONE_FLUID), "Wasserdichtes Sandsteinrohr");
            block(BcBlocks.pipe(PipeType.IRON_FLUID), "Wasserdichtes Eisenrohr");
            block(BcBlocks.pipe(PipeType.GOLDEN_FLUID), "Wasserdichtes Goldrohr");
            block(BcBlocks.pipe(PipeType.EMERALD_FLUID), "Wasserdichtes Smaragdrohr");
            block(BcBlocks.pipe(PipeType.VOID_FLUID), "Wasserdichtes Leererohr");
            block(BcBlocks.pipe(PipeType.WOODEN_POWER), "Leitendes Holzrohr");
            block(BcBlocks.pipe(PipeType.STONE_POWER), "Leitendes Steinrohr");
            block(BcBlocks.pipe(PipeType.GOLDEN_POWER), "Leitendes Goldrohr");
            block(BcBlocks.pipe(PipeType.COBBLESTONE_STRUCTURE), "Bruchstein-Strukturrohr");
            block(BcBlocks.OIL, "Öl");
            item(BcItems.OIL_BUCKET, "Öleimer");
            item(BcItems.FUEL_BUCKET, "Treibstoffeimer");
            block(BcBlocks.MARKER, "Landmarke");
            block(BcBlocks.QUARRY, "Steinbruch");
            block(BcBlocks.FRAME, "Rahmen");
            block(BcBlocks.TANK, "Tank");
            block(BcBlocks.PUMP, "Pumpe");
            block(BcBlocks.REFINERY, "Raffinerie");
            block(BcBlocks.AUTO_WORKBENCH, "Automatische Werkbank");
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
            crops(true);
            recipeViewer(true);
            mining(true);
            redPower(true);
            portalGun(true);
            lightShield(true);
            kleinkram(true);
            musicDiscs(true);
            ironChests(true);
            enderStorage(true);
            graviSuite(true);
            nuclearControl(true);
            chargePads(true);
            mffs(true);
            modularPowersuits(true);
            advancedMachines(true);
            thermalExpansion(true);
        }
    }
}
