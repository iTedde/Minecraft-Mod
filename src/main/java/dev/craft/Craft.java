package dev.craft;

import com.mojang.logging.LogUtils;
import dev.craft.buildcraft.BuildCraft;
import dev.craft.compactsolars.CompactSolars;
import dev.craft.core.CraftCreativeTabs;
import dev.craft.forestry.Forestry;
import dev.craft.powerconverters.PowerConverters;
import dev.craft.ic2.Ic2;
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
        CraftCreativeTabs.TABS.register(modBus);
    }
}
