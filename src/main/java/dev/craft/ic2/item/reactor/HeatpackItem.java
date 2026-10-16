package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorHeatpack}: heating cell, warms its neighbours while the hull is cold (spec 06 §3.9). */
public class HeatpackItem extends PassiveComponentItem {
    private final int maxPer;
    private final int heatPer;

    public HeatpackItem(Properties properties, int maxPer, int heatPer) {
        super(properties);
        this.maxPer = maxPer;
        this.heatPer = heatPer;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        heat(reactor, stack.getCount(), x + 1, y);
        heat(reactor, stack.getCount(), x - 1, y);
        heat(reactor, stack.getCount(), x, y + 1);
        heat(reactor, stack.getCount(), x, y - 1);
    }

    private void heat(Reactor reactor, int stackSize, int x, int y) {
        int want = maxPer * stackSize;
        if (reactor.getHeat() >= want) {
            return;
        }
        ItemStack stack = reactor.getItemAt(x, y);
        if (!stack.isEmpty() && stack.getItem() instanceof ReactorComponent component
                && component.canStoreHeat(reactor, stack, x, y)) {
            int add = heatPer * stackSize;
            int current = component.getCurrentHeat(reactor, stack, x, y);
            if (add > want - current) {
                add = want - current;
            }
            if (add > 0) {
                component.alterHeat(reactor, stack, x, y, add);
            }
        }
    }

    @Override
    public int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat) {
        return 0;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return stack.getCount() / 10;
    }
}
