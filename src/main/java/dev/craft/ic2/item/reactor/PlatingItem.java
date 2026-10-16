package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemReactorPlating} (spec 06 §3.6). Stacks to 64 like in the original; a stack in one field still
 * counts once.
 */
public class PlatingItem extends PassiveComponentItem {
    private final int maxHeatAdd;
    private final float effectModifier;

    public PlatingItem(Properties properties, int maxHeatAdd, float effectModifier) {
        super(properties);
        this.maxHeatAdd = maxHeatAdd;
        this.effectModifier = effectModifier;
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
        reactor.setMaxHeat(reactor.getMaxHeat() + maxHeatAdd);
        reactor.setHeatEffectModifier(reactor.getHeatEffectModifier() * effectModifier);
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return effectModifier >= 1.0F ? 0.0F : effectModifier;
    }
}
