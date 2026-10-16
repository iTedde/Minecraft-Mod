package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorVentSpread}: component heat vent, cools each neighbour by 4 (spec 06 §3.5). */
public class VentSpreadItem extends PassiveComponentItem {
    private final int sideVent;

    public VentSpreadItem(Properties properties, int sideVent) {
        super(properties.stacksTo(1));
        this.sideVent = sideVent;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        cool(reactor, x - 1, y);
        cool(reactor, x + 1, y);
        cool(reactor, x, y - 1);
        cool(reactor, x, y + 1);
    }

    private void cool(Reactor reactor, int x, int y) {
        ItemStack stack = reactor.getItemAt(x, y);
        if (!stack.isEmpty() && stack.getItem() instanceof ReactorComponent component
                && component.canStoreHeat(reactor, stack, x, y)) {
            component.alterHeat(reactor, stack, x, y, -sideVent);
        }
    }
}
