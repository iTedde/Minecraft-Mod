package dev.craft.ic2.item.reactor;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorUranium}: single, dual and quad uranium cell (spec 06 §3.1). */
public class UraniumCellItem extends GradualItem implements ReactorComponent {
    private static final Random RANDOM = new Random();

    private final int numberOfCells;

    public UraniumCellItem(Properties properties, int numberOfCells) {
        super(properties);
        this.numberOfCells = numberOfCells;
    }

    public int getNumberOfCells() {
        return numberOfCells;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        if (!reactor.produceEnergy()) {
            return;
        }
        for (int iteration = 0; iteration < numberOfCells; iteration++) {
            int pulses = 1 + numberOfCells / 2;
            for (int i = 0; i < pulses; i++) {
                acceptUraniumPulse(reactor, stack, stack, x, y, x, y);
            }
            pulses += checkPulseable(reactor, x - 1, y, stack, x, y)
                    + checkPulseable(reactor, x + 1, y, stack, x, y)
                    + checkPulseable(reactor, x, y - 1, stack, x, y)
                    + checkPulseable(reactor, x, y + 1, stack, x, y);
            int heat = sumUp(pulses) * 4;
            List<StackCoord> heatAcceptors = new ArrayList<>();
            checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
            checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
            checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
            checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
            while (!heatAcceptors.isEmpty() && heat > 0) {
                int dheat = heat / heatAcceptors.size();
                heat -= dheat;
                StackCoord acceptor = heatAcceptors.remove(0);
                heat += ((ReactorComponent) acceptor.stack.getItem()).alterHeat(reactor, acceptor.stack, acceptor.x,
                        acceptor.y, dheat);
            }
            if (heat > 0) {
                reactor.addHeat(heat);
            }
        }
        if (getDamageOfStack(stack) >= getMaxDamageEx() - 1) {
            if (RANDOM.nextInt(3) == 0) {
                reactor.setItemAt(x, y, new ItemStack(Ic2Items.NEAR_DEPLETED_URANIUM_CELL.get(), numberOfCells));
            } else {
                reactor.setItemAt(x, y, ItemStack.EMPTY);
            }
        } else {
            setDamageForStack(stack, getDamageOfStack(stack) + 1);
        }
    }

    private static int checkPulseable(Reactor reactor, int x, int y, ItemStack me, int meX, int meY) {
        ItemStack other = reactor.getItemAt(x, y);
        return !other.isEmpty() && other.getItem() instanceof ReactorComponent component
                && component.acceptUraniumPulse(reactor, other, me, x, y, meX, meY) ? 1 : 0;
    }

    private static int sumUp(int x) {
        int sum = 0;
        for (int i = 1; i <= x; i++) {
            sum += i;
        }
        return sum;
    }

    private static void checkHeatAcceptor(Reactor reactor, int x, int y, List<StackCoord> heatAcceptors) {
        ItemStack thing = reactor.getItemAt(x, y);
        if (!thing.isEmpty() && thing.getItem() instanceof ReactorComponent component
                && component.canStoreHeat(reactor, thing, x, y)) {
            heatAcceptors.add(new StackCoord(thing, x, y));
        }
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        reactor.addOutput(1);
        return true;
    }

    @Override
    public boolean canStoreHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return false;
    }

    @Override
    public int getMaxHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return 0;
    }

    @Override
    public int getCurrentHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return 0;
    }

    @Override
    public int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat) {
        return heat;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return 2 * numberOfCells;
    }

    private record StackCoord(ItemStack stack, int x, int y) {
    }
}
