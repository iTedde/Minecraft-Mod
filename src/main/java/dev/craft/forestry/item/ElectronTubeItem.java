package dev.craft.forestry.item;

import dev.craft.forestry.circuit.ElectronTube;
import net.minecraft.world.item.Item;

/** One electron tube of Forestry's {@code ItemOverlay thermionicTubes}. */
public class ElectronTubeItem extends Item {
    private final ElectronTube tube;

    public ElectronTubeItem(ElectronTube tube, Properties properties) {
        super(properties);
        this.tube = tube;
    }

    public ElectronTube getTube() {
        return tube;
    }
}
