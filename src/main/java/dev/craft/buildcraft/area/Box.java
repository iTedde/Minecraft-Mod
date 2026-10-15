package dev.craft.buildcraft.area;

import net.minecraft.nbt.CompoundTag;

/** BuildCraft {@code Box}: an inclusive block box, uninitialised until a size is set. */
public final class Box {
    public int xMin;
    public int yMin;
    public int zMin;
    public int xMax;
    public int yMax;
    public int zMax;
    private boolean initialized;

    public Box() {
        reset();
    }

    public void reset() {
        initialized = false;
        xMin = yMin = zMin = xMax = yMax = zMax = Integer.MAX_VALUE;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.zMin = zMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.zMax = zMax;
        initialized = true;
    }

    public void initialize(AreaProvider area) {
        initialize(area.xMin(), area.yMin(), area.zMin(), area.xMax(), area.yMax(), area.zMax());
    }

    public int sizeX() {
        return xMax - xMin + 1;
    }

    public int sizeY() {
        return yMax - yMin + 1;
    }

    public int sizeZ() {
        return zMax - zMin + 1;
    }

    public double centerX() {
        return xMin + sizeX() / 2.0;
    }

    public double centerY() {
        return yMin + sizeY() / 2.0;
    }

    public double centerZ() {
        return zMin + sizeZ() / 2.0;
    }

    public boolean contains(int x, int y, int z) {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax;
    }

    /** BuildCraft {@code Box.expand}: moves the box (the original shifts instead of growing). */
    public Box shifted(int amount) {
        Box box = new Box();
        box.initialize(xMin + amount, yMin + amount, zMin + amount, xMax + amount, yMax + amount, zMax + amount);
        return box;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("xMin", xMin);
        tag.putInt("yMin", yMin);
        tag.putInt("zMin", zMin);
        tag.putInt("xMax", xMax);
        tag.putInt("yMax", yMax);
        tag.putInt("zMax", zMax);
        return tag;
    }

    public void load(CompoundTag tag) {
        initialize(tag.getInt("xMin"), tag.getInt("yMin"), tag.getInt("zMin"), tag.getInt("xMax"), tag.getInt("yMax"),
                tag.getInt("zMax"));
    }
}
