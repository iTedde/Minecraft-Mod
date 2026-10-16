package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemReactorVent}: heat vent, reactor/overclocked/advanced heat vent (spec 06 §3.4). The abort check
 * on the hull drain never triggers ({@code alterHeat} of a positive amount is never positive), so the hull is
 * drained even when the vent burns up – kept as in the original.
 */
public class VentItem extends HeatStorageItem {
    private final int selfVent;
    private final int reactorVent;

    public VentItem(Properties properties, int heatStorage, int selfVent, int reactorVent) {
        super(properties, heatStorage);
        this.selfVent = selfVent;
        this.reactorVent = reactorVent;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        if (reactorVent > 0) {
            int rheat = reactor.getHeat();
            int reactorDrain = Math.min(rheat, reactorVent);
            rheat -= reactorDrain;
            if (alterHeat(reactor, stack, x, y, reactorDrain) > 0) {
                return;
            }
            reactor.setHeat(rheat);
        }
        alterHeat(reactor, stack, x, y, -selfVent);
    }
}
