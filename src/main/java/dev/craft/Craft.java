package dev.craft;

import com.mojang.logging.LogUtils;
import dev.craft.buildcraft.BuildCraft;
import dev.craft.compactsolars.CompactSolars;
import dev.craft.gregtech.GregTech;
import dev.craft.core.CraftCreativeTabs;
import dev.craft.forestry.Forestry;
import dev.craft.powerconverters.PowerConverters;
import dev.craft.powerplants.PowerPlants;
import dev.craft.portalgun.PortalGun;
import dev.craft.redpower.RedPower;
import dev.craft.ic2.Ic2;
import dev.craft.mining.Mining;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
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
        CraftCreativeTabs.TABS.register(modBus);
    }
}
