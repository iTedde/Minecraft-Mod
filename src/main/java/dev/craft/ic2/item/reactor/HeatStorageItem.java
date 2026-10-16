package dev.craft.ic2.item.reactor;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorHeatStorage}: 10k/30k/60k coolant cell (spec 06 §3.2); base of exchangers and vents. */
public class HeatStorageItem extends GradualItem implements ReactorComponent {
    protected final int heatStorage;

    public HeatStorageItem(Properties properties, int heatStorage) {
        this(properties, heatStorage, MAX_DAMAGE);
    }

    protected HeatStorageItem(Properties properties, int heatStorage, int maxDamage) {
        super(properties, maxDamage);
        this.heatStorage = heatStorage;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        return false;
    }

    @Override
    public boolean canStoreHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return true;
    }

    @Override
    public int getMaxHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return heatStorage;
    }

    @Override
    public int getCurrentHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return getHeatOfStack(stack);
    }

    /** Overheating destroys the component and returns a negative rest (original behaviour). */
    @Override
    public int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat) {
        int myHeat = getHeatOfStack(stack) + heat;
        if (myHeat > heatStorage) {
            reactor.setItemAt(x, y, ItemStack.EMPTY);
            heat = heatStorage - myHeat + 1;
        } else {
            if (myHeat < 0) {
                heat = myHeat;
                myHeat = 0;
            } else {
                heat = 0;
            }
            setHeatForStack(stack, myHeat);
        }
        return heat;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return 0.0F;
    }

    public int getHeatStorage() {
        return heatStorage;
    }

    public void setHeatForStack(ItemStack stack, int heat) {
        stack.set(Ic2DataComponents.REACTOR_HEAT.get(), heat);
        if (heatStorage > 0) {
            double p = (double) heat / heatStorage;
            int newDmg = (int) (stack.getMaxDamage() * p);
            if (newDmg >= stack.getMaxDamage()) {
                newDmg = stack.getMaxDamage() - 1;
            }
            stack.setDamageValue(newDmg);
        }
    }

    public static int getHeatOfStack(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.REACTOR_HEAT.get(), 0);
    }
}
