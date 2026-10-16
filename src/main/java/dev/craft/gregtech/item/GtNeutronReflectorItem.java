package dev.craft.gregtech.item;

import dev.craft.ic2.item.reactor.PassiveComponentItem;
import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/**
 * {@code GT_NeutronReflector_Item} (iridium neutron reflector): never wears out (max damage 0), stacks to 64 but
 * only a single item reflects; the pulse is sent back to the pulsing component.
 */
public class GtNeutronReflectorItem extends PassiveComponentItem {
    public GtNeutronReflectorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int x, int y,
                                      int pulseX, int pulseY) {
        if (stack.getCount() > 1) {
            return false;
        }
        ((ReactorComponent) pulsingStack.getItem()).acceptUraniumPulse(reactor, pulsingStack, stack, pulseX, pulseY,
                x, y);
        return true;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return -1.0F;
    }
}
