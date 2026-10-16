package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorHeatSwitch}: the four heat exchangers (spec 06 §3.3). */
public class HeatSwitchItem extends HeatStorageItem {
    private final int switchSide;
    private final int switchReactor;

    public HeatSwitchItem(Properties properties, int heatStorage, int switchSide, int switchReactor) {
        super(properties, heatStorage);
        this.switchSide = switchSide;
        this.switchReactor = switchReactor;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        int myHeat = 0;
        List<StackCoord> heatAcceptors = new ArrayList<>();
        double med = (double) getCurrentHeat(reactor, stack, x, y) / getMaxHeat(reactor, stack, x, y);
        int c = 1;
        if (switchReactor > 0) {
            c++;
            med += (double) reactor.getHeat() / reactor.getMaxHeat();
        }
        if (switchSide > 0) {
            med += checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
            med += checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
            med += checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
            med += checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
        }
        med /= c + heatAcceptors.size();
        if (switchSide > 0) {
            for (StackCoord coord : heatAcceptors) {
                ReactorComponent heatable = (ReactorComponent) coord.stack().getItem();
                int add = (int) (med * heatable.getMaxHeat(reactor, coord.stack(), coord.x(), coord.y()))
                        - heatable.getCurrentHeat(reactor, coord.stack(), coord.x(), coord.y());
                add = Math.max(-switchSide, Math.min(switchSide, add));
                myHeat -= add;
                myHeat += heatable.alterHeat(reactor, coord.stack(), coord.x(), coord.y(), add);
            }
        }
        if (switchReactor > 0) {
            int add = (int) (med * reactor.getMaxHeat()) - reactor.getHeat();
            add = Math.max(-switchReactor, Math.min(switchReactor, add));
            myHeat -= add;
            reactor.setHeat(reactor.getHeat() + add);
        }
        alterHeat(reactor, stack, x, y, myHeat);
    }

    private static double checkHeatAcceptor(Reactor reactor, int x, int y, List<StackCoord> heatAcceptors) {
        ItemStack thing = reactor.getItemAt(x, y);
        if (!thing.isEmpty() && thing.getItem() instanceof ReactorComponent component
                && component.canStoreHeat(reactor, thing, x, y)) {
            heatAcceptors.add(new StackCoord(thing, x, y));
            double max = component.getMaxHeat(reactor, thing, x, y);
            if (max <= 0.0) {
                return 0.0;
            }
            return component.getCurrentHeat(reactor, thing, x, y) / max;
        }
        return 0.0;
    }

    private record StackCoord(ItemStack stack, int x, int y) {
    }
}
