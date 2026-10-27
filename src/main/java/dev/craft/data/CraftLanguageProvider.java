package dev.craft.data;

import java.util.Arrays;
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
import dev.craft.mps.MpsBlocks;
import dev.craft.mps.MpsItems;
import dev.craft.mps.module.PowerModule;
import dev.craft.ironchest.ChestUpgradeType;
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
        add("message.craft.gravisuite.gravi_no_boost", german ? "Nicht genug Energie für den Boost!"
                : "Not enough energy to boost !");
        add("hud.craft.gravisuite.energy_level", german ? "Energiestand: %s" : "Energy level: %s");
        add("hud.craft.gravisuite.jetpack_on", german ? "§eJetpack §aTriebwerk AN" : "§eJetpack §aengine ON");
        add("hud.craft.gravisuite.hover_mode", german ? " §e(Schwebemodus)" : " §e(Hover mode)");
        add("message.craft.gravisuite.jetpack_on", german ? "§eJetpack §aTriebwerk AN" : "§eJetpack §aengine ON");
        add("message.craft.gravisuite.jetpack_off", german ? "§eJetpack §cTriebwerk AUS" : "§eJetpack §cengine OFF");
        add("message.craft.gravisuite.hover_enabled", german ? "§eSchwebemodus §aaktiviert." : "§eHover Mode §aenabled.");
        add("message.craft.gravisuite.hover_disabled",
                german ? "§eSchwebemodus §cdeaktiviert." : "§eHover Mode §cdisabled.");
        add("emi.craft.info.gravi_chestplate", german
                ? "Gravi-Flugtaste (Standard G) schaltet den Flug ein, ab 80 000 EU. Verbraucht 416 EU/t, auch am "
                + "Boden; unter 416 EU schaltet sie ab. Boost-Taste (Strg) im Flug: schneller, 2 × 1 248 EU/t. Kein "
                + "Fallschaden. Schützt wie die Quanten-Brustplatte (900 EU je Schaden) und lädt Werkzeuge."
                : "Gravi Fly Key (default G) turns flight on, from 80,000 EU. Uses 416 EU/t, also on the ground; below "
                + "416 EU it shuts down. Boost key (Ctrl) while flying: faster, 2 × 1,248 EU/t. No fall damage. "
                + "Protects like the quantum chestplate (900 EU per damage) and charges tools.");
        add("emi.craft.info.advanced_jetpack", german
                ? "Gravi-Flugtaste (Standard G) schaltet das Triebwerk ein/aus, Springen gibt Schub (12 EU/t), "
                + "Boost-Taste (Strg) +60 EU/t. Modus-Taste (M) beim Springen: Schwebemodus (langsames Sinken). Lädt "
                + "Werkzeuge wie ein LapPack. Die Nano-Brustplatte schützt zusätzlich (800 EU je Schaden)."
                : "Gravi Fly Key (default G) switches the engine, jumping thrusts (12 EU/t), boost key (Ctrl) +60 EU/t. "
                + "Mode key (M) while jumping: hover mode (slow descent). Charges tools like a lappack. The nano "
                + "chestplate also protects (800 EU per damage).");
        add("emi.craft.info.gravisuite_lappack", german
                ? "Energiespeicher für den Rücken: lädt gehaltene elektrische Werkzeuge bis Stufe 2 nach. "
                + "Fortgeschritten 1 000 000 EU, Ultimativ 10 000 000 EU."
                : "Energy storage for the back: recharges held electric tools up to tier 2. Advanced 1,000,000 EU, "
                + "ultimate 10,000,000 EU.");
        add("emi.craft.info.ultimate_solar_helmet", german
                ? "Nur im Kreativmodus (kein Rezept, wie im Pack). Erzeugt getragen 512 EU/t bei Sonne und 64 EU/t "
                + "unter freiem Himmel; lädt zuerst die Rüstung (auch sich selbst), dann das Inventar. Rüstungswert 3."
                : "Creative only (no recipe, as in the pack). Worn it makes 512 EU/t in the sun and 64 EU/t under open "
                + "sky; charges the armor first (itself too), then the inventory. Armor value 3.");
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
                {"Sprint Assist", "Sprinthilfe", "A set of servo motors to help you sprint (double-tap forward) and walk faster.", "Servomotoren, mit denen du schneller sprintest (zweimal vorwärts) und gehst."},
                {"Jump Assist", "Sprunghilfe", "Another set of servo motors to help you jump higher.", "Weitere Servomotoren, mit denen du höher springst."},
                {"Shock Absorber", "Stoßdämpfer", "With some servos, springs, and padding, you should be able to negate a portion of fall damage.", "Mit Servos, Federn und Polstern lässt sich ein Teil des Fallschadens aufheben."},
                {"Glider", "Gleiter", "Tack on some wings to turn downward into forward momentum. Press sneak+forward while falling to activate.", "Flügel wandeln Fallen in Vorwärtsbewegung um. Im Fallen Schleichen + Vorwärts drücken."},
                {"Parachute", "Fallschirm", "Add a parachute to slow your descent. Activate by pressing sneak (defaults to Shift) in midair.", "Ein Fallschirm bremst den Fall. In der Luft Schleichen (Standard: Shift) drücken."},
                {"Jetpack", "Jetpack", "A jetpack should allow you to jump indefinitely, or at least until you run out of power.", "Mit einem Jetpack springst du endlos – oder zumindest, bis die Energie ausgeht."},
                {"Jet Boots", "Düsenstiefel", "Jet boots are not as strong as a jetpack, but they should at least be strong enough to counteract gravity.", "Düsenstiefel sind schwächer als ein Jetpack, sollten aber wenigstens die Schwerkraft ausgleichen."},
                {"Water Electrolyzer", "Wasser-Elektrolyseur", "When you run out of air, this module will jolt the water around you, electrolyzing a small bubble to breathe from.", "Geht dir die Luft aus, zerlegt das Modul das Wasser um dich per Stromstoß in eine kleine Atemblase."},
                {"Swim Boost", "Schwimmhilfe", "By refitting an ion thruster for underwater use, you may be able to add extra forward (or backward) thrust when underwater.", "Ein umgebautes Ionentriebwerk gibt unter Wasser zusätzlichen Schub nach vorn (oder hinten)."},
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
                {"Compensation", "Ausgleich"}, {"Walking Assist", "Gehhilfe"}, {"Thrust", "Schub"},
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
                {"Sprint Energy Consumption", "Sprint-Energie"}, {"Sprint Speed Multiplier", "Sprint-Tempo"},
                {"Sprint Exhaustion Compensation", "Sprint-Erschöpfungsausgleich"},
                {"Walking Energy Consumption", "Geh-Energie"}, {"Walking Speed Multiplier", "Geh-Tempo"},
                {"Jump Energy Consumption", "Sprung-Energie"}, {"Jump Boost", "Sprungkraft"},
                {"Jump Exhaustion Compensation", "Sprung-Erschöpfungsausgleich"},
                {"Impact Energy consumption", "Aufprall-Energie"}, {"Distance Reduction", "Fallhöhen-Minderung"},
                {"Jet Energy Consumption", "Düsen-Energie"}, {"Jet Thrust", "Düsenschub"},
                {"Jolt Energy", "Stromstoß-Energie"}, {"Swim Boost Energy Consumption", "Schwimm-Energie"},
                {"Underwater Movement Boost", "Schwimmschub"}, {"Red Tint", "Rot"}, {"Green Tint", "Grün"},
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
                ? "Elektrischer Schild (10 000 EU, Stufe 1), lädt wie LV-Werkzeuge. In Haupt- oder Nebenhand leuchtet er "
                + "wie eine Fackel in der Hand (Licht 14 ringsum; nur sichtbar mit LambDynamicLights, rein optisch, "
                + "Mobs spawnen weiter). Kostet 1 EU je 4 Ticks; "
                + "Modus-Taste + Rechtsklick schaltet das Licht. Geblockte Treffer kosten 100 EU je Haltbarkeitspunkt statt Haltbarkeit; leer lässt er "
                + "sich nicht heben. Haltbarkeit spart EU, Reparatur lädt mit Erfahrung (1 XP = 200 EU)."
                : "Electric shield (10,000 EU, tier 1), charges like LV tools. In main or off hand it shines like a "
                + "held torch (light 14 all around; visible with LambDynamicLights only, purely visual, mobs still "
                + "spawn). Costs 1 EU per 4 ticks; mode key + right click toggles the "
                + "light. Blocked hits cost 100 EU per durability point instead of durability; empty it cannot be raised. Unbreaking "
                + "saves EU, Mending charges it with experience (1 XP = 200 EU).");
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
        add("emi.craft.info.coffee", german
                ? "Gibt Tempo und Eile und steigert beide bei jedem Schluck (kalt bis II, schwarz bis VI, mit Milch "
                + "bis VII). Ab Stufe IV (Milchkaffee: VI) Übelkeit, ab V (VII) zusätzlich Schaden."
                : "Gives speed and haste and raises both with every mug (cold up to II, dark up to VI, with milk up "
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
                ? "Solarfeld: Jedes Solarpanel liefert bei Sonne 1 EU/t. Liegt es in einem lückenlosen Quadrat aus "
                + "9×9 Solarpanels auf derselben Höhe, liefert es 1,2 EU/t (alle 5 Ticks 1 EU extra). Ein 9×9-Feld "
                + "bringt so rund 97 EU/t statt 81. Die alten LV/MV/HV-Solar-Arrays liefern nichts mehr; in der Werkbank "
                + "zerfallen sie in 8 Teile der Stufe darunter."
                : "Solar field: every solar panel delivers 1 EU/t in sunlight. If it lies in a complete square of "
                + "9×9 solar panels on the same level, it delivers 1.2 EU/t (1 extra EU every 5 ticks). A 9×9 field "
                + "thus yields about 97 EU/t instead of 81. The old LV/MV/HV solar arrays no longer deliver anything; at "
                + "the crafting table they come apart into 8 of the tier below.");
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
            block(Ic2Blocks.NUKE, "Nuke");
            add(Ic2EntityTypes.NUKE.get(), "Nuke");
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
            ironChests(false);
            enderStorage(false);
            graviSuite(false);
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
            block(Ic2Blocks.NUKE, "Atombombe");
            add(Ic2EntityTypes.NUKE.get(), "Atombombe");
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
            ironChests(true);
            enderStorage(true);
            graviSuite(true);
            modularPowersuits(true);
            advancedMachines(true);
            thermalExpansion(true);
        }
    }
}
