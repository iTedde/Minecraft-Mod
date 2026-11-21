package dev.craft.mining;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Entry point of the Craft dimensions: mining (spec 18) and farming (spec 53) – portal blocks, their POIs and portal
 * ignition.
 */
public final class Mining {
    private Mining() {
    }

    public static List<PortalLink> links() {
        return List.of(MiningBlocks.MINING_LINK, MiningBlocks.FARMING_LINK);
    }

    public static void init(IEventBus modBus) {
        MiningBlocks.BLOCKS.register(modBus);
        MiningBlocks.POI_TYPES.register(modBus);
        MiningBlocks.FEATURES.register(modBus);
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) ->
                FarmingPlants.get(event.getServer().registryAccess()));
        NeoForge.EVENT_BUS.addListener(Mining::lightPortal);
    }

    /**
     * Fire placed inside a portal frame (flint and steel, fire charge …) turns into that frame's portal, in the
     * overworld and the link's own dimension only (spec 18 §4.1, spec 53 §4). Runs after the fire block's own nether
     * portal check.
     */
    private static void lightPortal(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !event.getState().is(BlockTags.FIRE) || !(level instanceof Level world)) {
            return;
        }
        for (PortalLink link : links()) {
            if (link.connects(world.dimension())) {
                var shape = CraftPortalShape.findEmpty(link, level, event.getPos(), Direction.Axis.X);
                if (shape.isPresent()) {
                    shape.get().createPortalBlocks();
                    return;
                }
            }
        }
    }
}
