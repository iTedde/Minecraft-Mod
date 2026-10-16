package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemReactorReflector}: neutron reflector and thick neutron reflector (spec 06 §3.7). */
public class ReflectorItem extends GradualIntItem implements ReactorComponent {
    public ReflectorItem(Properties properties, int maxDamage) {
        super(properties, maxDamage);
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        reactor.addOutput(1);
        if (getDamageOfStack(stack) + 1 >= maxDmg) {
            reactor.setItemAt(youX, youY, ItemStack.EMPTY);
        } else {
            setDamageForStack(stack, getDamageOfStack(stack) + 1);
        }
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
        return -1.0F;
    }
}
