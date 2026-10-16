package dev.craft.ic2.item.reactor;

import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemReactorCondensator}: RSH and LZH condensator (spec 06 §3.8). Takes heat until full, never cools
 * itself; repaired in the crafting grid ({@code GradualRepairRecipe}).
 */
public class CondensatorItem extends GradualIntItem implements ReactorComponent {
    public CondensatorItem(Properties properties, int capacity) {
        super(properties, capacity + 1);
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
        return getDamageOfStack(stack) + 1 < maxDmg;
    }

    @Override
    public int getMaxHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return maxDmg;
    }

    @Override
    public int getCurrentHeat(Reactor reactor, ItemStack stack, int x, int y) {
        return 0;
    }

    /** Negative heat is "absorbed" as well and lowers the wear (original behaviour). */
    @Override
    public int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat) {
        int can = maxDmg - (getDamageOfStack(stack) + 1);
        if (can > heat) {
            can = heat;
        }
        heat -= can;
        setDamageForStack(stack, getDamageOfStack(stack) + can);
        return heat;
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return 0.0F;
    }
}
