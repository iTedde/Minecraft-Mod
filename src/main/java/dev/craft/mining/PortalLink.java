package dev.craft.mining;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One portal connection between the overworld and a Craft dimension (spec 18 §4, spec 53 §4): the frame block, the
 * portal block, the POI used to find exit portals and the dimension on the other side.
 *
 * @param frame     block the frame is built from (and the exit frame is generated from)
 * @param portal    portal block filling the frame
 * @param poi       point of interest of the portal block
 * @param dimension the Craft dimension; the other side is always the overworld
 */
public record PortalLink(Block frame, Supplier<? extends Block> portal, ResourceKey<PoiType> poi,
                         ResourceKey<Level> dimension) {

    public boolean isFrame(BlockState state) {
        return state.is(frame);
    }

    public boolean isPortal(BlockState state) {
        return state.is(portal.get());
    }

    /** Overworld and the link's own dimension; a link cannot be lit anywhere else. */
    public boolean connects(ResourceKey<Level> level) {
        return level == Level.OVERWORLD || level == dimension;
    }

    public ResourceKey<Level> destination(ResourceKey<Level> from) {
        return from == dimension ? Level.OVERWORLD : dimension;
    }
}
