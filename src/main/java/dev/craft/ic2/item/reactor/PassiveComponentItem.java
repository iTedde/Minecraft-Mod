package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Defaults of an IC2 reactor component that neither takes pulses nor stores heat (plating, component heat vent,
 * heating cell); subclasses override what they use.
 */
public abstract class PassiveComponentItem extends Item implements ReactorComponent {
    protected PassiveComponentItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        return false;
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
        return 0.0F;
    }
}
