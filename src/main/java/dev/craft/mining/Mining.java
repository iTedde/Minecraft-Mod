package dev.craft.mining;

import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Entry point of the mining dimension (spec 18): portal block, its POI and portal ignition. */
public final class Mining {
    private Mining() {
    }

    public static void init(IEventBus modBus) {
        MiningBlocks.BLOCKS.register(modBus);
        MiningBlocks.POI_TYPES.register(modBus);
        NeoForge.EVENT_BUS.addListener(Mining::lightPortal);
    }

    /**
     * Fire placed inside a smooth stone frame (flint and steel, fire charge …) turns into the mining portal, in the
     * overworld and the mining dimension (spec 18 §4.1). Runs after the fire block's own nether portal check.
     */
    private static void lightPortal(BlockEvent.NeighborNotifyEvent event) {
        LevelAccessor level = event.getLevel();
        if (level.isClientSide() || !event.getState().is(BlockTags.FIRE) || !(level instanceof Level world)
                || (world.dimension() != Level.OVERWORLD && world.dimension() != MiningDimension.LEVEL)) {
            return;
        }
        MiningPortalShape.findEmpty(level, event.getPos(), Direction.Axis.X)
                .ifPresent(MiningPortalShape::createPortalBlocks);
    }
}
