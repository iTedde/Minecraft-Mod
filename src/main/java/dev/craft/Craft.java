package dev.craft;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Craft.MODID)
public final class Craft {
    public static final String MODID = "craft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Craft(IEventBus modBus, ModContainer container) {
    }
}
