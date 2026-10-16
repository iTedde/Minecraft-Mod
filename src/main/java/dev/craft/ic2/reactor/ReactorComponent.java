package dev.craft.ic2.reactor;

import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code IReactorComponent}, implemented by the item of a reactor component. Neighbours are always the four
 * orthogonal matrix fields; the call order of the original is part of the behaviour.
 */
public interface ReactorComponent {
    /** Action once per reactor cycle. */
    void processChamber(Reactor reactor, ItemStack stack, int x, int y);

    /**
     * Called by a neighbouring (or the own) uranium cell.
     *
     * @return {@code true} if the pulse was accepted (counts towards the cell's heat formula)
     */
    boolean acceptUraniumPulse(Reactor reactor, ItemStack stack, ItemStack pulsingStack, int youX, int youY,
                               int pulseX, int pulseY);

    boolean canStoreHeat(Reactor reactor, ItemStack stack, int x, int y);

    int getMaxHeat(Reactor reactor, ItemStack stack, int x, int y);

    int getCurrentHeat(Reactor reactor, ItemStack stack, int x, int y);

    /** Changes the stored heat; returns the heat that was not taken (may be negative, see the spec). */
    int alterHeat(Reactor reactor, ItemStack stack, int x, int y, int heat);

    /** Contribution to the explosion: values in (0, 1) multiply, everything else is added. */
    float influenceExplosion(Reactor reactor, ItemStack stack);
}
