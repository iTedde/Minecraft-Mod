package dev.craft.enderstorage;

import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * EnderStorage {@code EnderLiquidStorage} (spec 27 §5): one tank of 16 buckets for any liquid, shared by all ender
 * tanks of one frequency.
 */
public class EnderLiquidStorage extends FluidTank {
    public static final int CAPACITY = 16_000;

    private final EnderStorageData data;

    public EnderLiquidStorage(EnderStorageData data) {
        super(CAPACITY);
        this.data = data;
    }

    @Override
    protected void onContentsChanged() {
        data.setDirty();
    }
}
