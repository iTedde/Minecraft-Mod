package dev.craft.powerplants.block;

import dev.craft.core.multiblock.MultiblockPattern;

/** Generated layouts for the box-shaped power plants (spec 14, spec 23), written for a controller facing north. */
public final class PlantShapes {
    /** Symbol of the controller cell: middle of the front (north) face. */
    public static final char CONTROLLER = 'C';

    private PlantShapes() {
    }

    /** Symbol of a cell of the box; {@code x, y, z} count from the west, bottom and front (controller) side. */
    @FunctionalInterface
    public interface Cell {
        char at(int x, int y, int z);
    }

    /**
     * Layout of a {@code width × height × depth} box with the controller in the middle of the front face; every
     * other cell gets the symbol from {@code cell}.
     */
    public static MultiblockPattern.Builder box(int width, int height, int depth, Cell cell) {
        MultiblockPattern.Builder builder = MultiblockPattern.builder();
        for (int y = 0; y < height; y++) {
            String[] rows = new String[depth];
            for (int z = 0; z < depth; z++) {
                StringBuilder row = new StringBuilder(width);
                for (int x = 0; x < width; x++) {
                    row.append(x == width / 2 && y == height / 2 && z == 0 ? CONTROLLER : cell.at(x, y, z));
                }
                rows[z] = row.toString();
            }
            builder.layer(rows);
        }
        return builder;
    }

    /** Whether the cell lies on the outer shell of the box. */
    public static boolean onShell(int width, int height, int depth, int x, int y, int z) {
        return x == 0 || y == 0 || z == 0 || x == width - 1 || y == height - 1 || z == depth - 1;
    }
}
