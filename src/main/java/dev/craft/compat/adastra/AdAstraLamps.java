package dev.craft.compat.adastra;

import dev.craft.Craft;
import dev.craft.mixin.BlockStateBaseAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Ad Astra's industrial lamps only emit light 12 (small ones 8), which barely lights a room (user 2026-11-06).
 * All of them (every colour, big and small) are raised to full light 15. Does nothing without Ad Astra.
 */
public final class AdAstraLamps {
    private static final String MOD_ID = "ad_astra";
    private static final int LIGHT = 15;

    private AdAstraLamps() {
    }

    public static void init(IEventBus modBus) {
        if (ModList.get().isLoaded(MOD_ID)) {
            modBus.addListener(AdAstraLamps::commonSetup);
        }
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int count = 0;
            for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
                var id = entry.getKey().location();
                if (!id.getNamespace().equals(MOD_ID) || !id.getPath().endsWith("industrial_lamp")) {
                    continue;
                }
                for (BlockState state : entry.getValue().getStateDefinition().getPossibleStates()) {
                    ((BlockStateBaseAccessor) state).craft$setLightEmission(LIGHT);
                }
                count++;
            }
            Craft.LOGGER.info("Raised light level of {} Ad Astra lamps to {}", count, LIGHT);
        });
    }
}
