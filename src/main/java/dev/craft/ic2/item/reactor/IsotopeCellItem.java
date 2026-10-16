package dev.craft.ic2.item.reactor;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.reactor.Reactor;
import dev.craft.ic2.reactor.ReactorComponent;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemReactorDepletedUranium}: depleted isotope cell, crafted at damage 9999 and re-enriched by uranium
 * pulses, faster in a hot reactor (spec 06 §3.10). Counts as an accepted pulse but adds no output itself.
 */
public class IsotopeCellItem extends GradualItem implements ReactorComponent {
    public IsotopeCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public void processChamber(Reactor reactor, ItemStack stack, int x, int y) {
    }

    @Override
    public boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                                      int pulseX, int pulseY) {
        int myLevel = stack.getDamageValue() - 1 - reactor.getHeat() / 3000;
        if (myLevel <= 0) {
            reactor.setItemAt(youX, youY, new ItemStack(Ic2Items.RE_ENRICHED_URANIUM_CELL.get()));
        } else {
            stack.setDamageValue(myLevel);
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
        return 0.0F;
    }
}
