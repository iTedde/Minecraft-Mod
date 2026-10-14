package dev.craft.ic2.menu;

import java.util.function.IntSupplier;

import net.minecraft.world.inventory.ContainerData;

/**
 * Syncs full ints through container data: vanilla only transfers 16 bits per slot, so every value uses two slots
 * (low and high half), like IC2's {@code fuel & 65535} / {@code fuel >>> 16} updates.
 */
public final class SyncedInts implements ContainerData {
    private final IntSupplier[] sources;
    private final int[] values;

    /** Server side: reads the values from {@code sources}. */
    public SyncedInts(IntSupplier... sources) {
        this.sources = sources;
        this.values = new int[sources.length];
    }

    /** Client side: holds received values. */
    public SyncedInts(int count) {
        this.sources = null;
        this.values = new int[count];
    }

    public int getInt(int index) {
        return sources != null ? sources[index].getAsInt() : values[index];
    }

    @Override
    public int get(int index) {
        int value = getInt(index / 2);
        return index % 2 == 0 ? value & 0xFFFF : value >>> 16;
    }

    @Override
    public void set(int index, int value) {
        int slot = index / 2;
        if (index % 2 == 0) {
            values[slot] = values[slot] & 0xFFFF0000 | value & 0xFFFF;
        } else {
            values[slot] = values[slot] & 0xFFFF | (value & 0xFFFF) << 16;
        }
    }

    @Override
    public int getCount() {
        return values.length * 2;
    }
}
