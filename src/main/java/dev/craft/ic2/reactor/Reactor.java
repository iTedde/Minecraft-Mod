package dev.craft.ic2.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * IC2 {@code IReactor}: the view reactor components get of the reactor while it runs a cycle
 * (see {@code docs/spec/06-reaktor.md}).
 */
public interface Reactor {
    Level getReactorLevel();

    BlockPos getReactorPos();

    int getHeat();

    void setHeat(int heat);

    /** Adds to the hull heat and returns the new value. */
    int addHeat(int amount);

    int getMaxHeat();

    void setMaxHeat(int maxHeat);

    /** Heat effect modifier ({@code hem}), the product of the plating factors. */
    float getHeatEffectModifier();

    void setHeatEffectModifier(float hem);

    int getOutput();

    /** One accepted pulse = +1 output (= 5 EU/t); returns the new output. */
    int addOutput(int energy);

    /** Item in the matrix (column {@code x} 0–8, row {@code y} 0–5), empty outside. */
    ItemStack getItemAt(int x, int y);

    void setItemAt(int x, int y, ItemStack stack);

    /** Redstone signal present (uranium cells only work then). */
    boolean produceEnergy();
}
