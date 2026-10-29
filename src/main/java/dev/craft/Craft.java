package dev.craft;

import com.mojang.logging.LogUtils;
import dev.craft.advancedmachines.AdvancedMachines;
import dev.craft.buildcraft.BuildCraft;
import dev.craft.compactsolars.CompactSolars;
import dev.craft.enderstorage.EnderStorage;
import dev.craft.gravisuite.GraviSuite;
import dev.craft.mps.Mps;
import dev.craft.gregtech.GregTech;
import dev.craft.coppergolem.CopperGolems;
import dev.craft.core.CraftCreativeTabs;
import dev.craft.forestry.Forestry;
import dev.craft.powerconverters.PowerConverters;
import dev.craft.powerplants.PowerPlants;
import dev.craft.ironchest.IronChests;
import dev.craft.lightshield.LightShield;
import dev.craft.portalgun.PortalGun;
import dev.craft.redpower.RedPower;
import dev.craft.thermalexpansion.ThermalExpansion;
import dev.craft.ic2.Ic2;
import dev.craft.mining.Mining;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

@Mod(Craft.MODID)
public final class Craft {
    public static final String MODID = "craft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Craft(IEventBus modBus, ModContainer container) {
        Ic2.init(modBus);
        BuildCraft.init(modBus);
        Forestry.init(modBus);
        PowerConverters.init(modBus);
        CompactSolars.init(modBus);
        GregTech.init(modBus);
        PowerPlants.init(modBus);
        Mining.init(modBus);
        RedPower.init(modBus);
        PortalGun.init(modBus);
        IronChests.init(modBus);
        CopperGolems.init(modBus);
        AdvancedMachines.init(modBus);
        ThermalExpansion.init(modBus);
        EnderStorage.init(modBus);
        GraviSuite.init(modBus);
        Mps.init(modBus);
        LightShield.init(modBus);
        CraftCreativeTabs.TABS.register(modBus);
        modBus.addListener(Craft::addPackFinders);
    }

    /**
     * Built-in data pack that replaces Create Encased's wooden shaft/cogwheel recipes, which clash with Create's
     * cogwheel recipes (pack fix, user 2026-10-23); its recipes only load with Create Encased installed.
     * A second one replaces the Mechanical Extruder's andesite/granite recipes (catalyst instead of height limit) and
     * adds tuff (user 2026-10-26); only loads with the extruder installed.
     */
    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath(MODID, "packs/createcasing_fix"),
                    PackType.SERVER_DATA, Component.literal("Craft: Create Encased recipe fix"), PackSource.BUILT_IN,
                    true, Pack.Position.TOP);
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath(MODID, "packs/extruder_tweaks"),
                    PackType.SERVER_DATA, Component.literal("Craft: Mechanical Extruder recipes"), PackSource.BUILT_IN,
                    true, Pack.Position.TOP);
        }
    }
}
