package dev.craft.core.multiblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fixed block layout relative to a controller, rotatable around the Y axis.
 * <p>
 * The layout is written for a controller facing {@link Direction#NORTH}: layers go from bottom to top, each layer is
 * a list of rows from north to south, each row a string of cells from west to east; north is the direction the
 * controller faces. A space means "don't care".
 * Exactly one cell must hold the controller symbol.
 */
public final class MultiblockPattern {
    public static final char IGNORED = ' ';

    private final char[][][] cells; // [y][z][x]
    private final Map<Character, BlockMatcher> matchers;
    private final BlockPos controllerOffset; // controller cell in layout coordinates (x, y, z)
    private final int width;
    private final int height;
    private final int depth;

    private MultiblockPattern(char[][][] cells, Map<Character, BlockMatcher> matchers, BlockPos controllerOffset) {
        this.cells = cells;
        this.matchers = matchers;
        this.controllerOffset = controllerOffset;
        this.height = cells.length;
        this.depth = cells[0].length;
        this.width = cells[0][0].length;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    /** Matches the structure with the controller at {@code controllerPos} facing {@code facing}. */
    public Optional<Match> match(BlockGetter level, BlockPos controllerPos, Direction facing) {
        Map<Character, List<BlockPos>> positions = new LinkedHashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    char symbol = cells[y][z][x];
                    if (symbol == IGNORED) {
                        continue;
                    }
                    offset(controllerPos, facing, x, y, z, pos);
                    BlockState state = level.getBlockState(pos);
                    if (!matchers.get(symbol).matches(level, pos, state)) {
                        return Optional.empty();
                    }
                    positions.computeIfAbsent(symbol, k -> new ArrayList<>()).add(pos.immutable());
                }
            }
        }
        return Optional.of(new Match(facing, positions));
    }

    /** Tries all horizontal facings in the order north, east, south, west. */
    public Optional<Match> matchAnyFacing(BlockGetter level, BlockPos controllerPos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            Optional<Match> match = match(level, controllerPos, facing);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    /** World position of layout cell (x, y, z) for the given controller position and facing. */
    public BlockPos worldPos(BlockPos controllerPos, Direction facing, int x, int y, int z) {
        return offset(controllerPos, facing, x, y, z, new BlockPos.MutableBlockPos()).immutable();
    }

    private BlockPos.MutableBlockPos offset(BlockPos controllerPos, Direction facing, int x, int y, int z,
                                            BlockPos.MutableBlockPos out) {
        int dx = x - controllerOffset.getX();
        int dy = y - controllerOffset.getY();
        int dz = z - controllerOffset.getZ();
        int rx;
        int rz;
        switch (facing) {
            case EAST -> { rx = -dz; rz = dx; }
            case SOUTH -> { rx = -dx; rz = -dz; }
            case WEST -> { rx = dz; rz = -dx; }
            default -> { rx = dx; rz = dz; }
        }
        return out.set(controllerPos.getX() + rx, controllerPos.getY() + dy, controllerPos.getZ() + rz);
    }

    /** Result of a successful match: facing and world positions per symbol in layout order. */
    public record Match(Direction facing, Map<Character, List<BlockPos>> positions) {
        public List<BlockPos> get(char symbol) {
            return positions.getOrDefault(symbol, List.of());
        }

        public List<BlockPos> all() {
            List<BlockPos> all = new ArrayList<>();
            positions.values().forEach(all::addAll);
            return all;
        }
    }

    public static final class Builder {
        private final List<String[]> layers = new ArrayList<>();
        private final Map<Character, BlockMatcher> matchers = new HashMap<>();
        private Character controller;

        /** Adds the next layer (bottom to top), rows from north to south. */
        public Builder layer(String... rows) {
            layers.add(rows);
            return this;
        }

        /** Adds the same layer {@code count} times. */
        public Builder layers(int count, String... rows) {
            for (int i = 0; i < count; i++) {
                layer(rows);
            }
            return this;
        }

        public Builder where(char symbol, BlockMatcher matcher) {
            if (symbol == IGNORED) {
                throw new IllegalArgumentException("space is reserved for ignored cells");
            }
            matchers.put(symbol, matcher);
            return this;
        }

        public Builder controller(char symbol, BlockMatcher matcher) {
            controller = symbol;
            return where(symbol, matcher);
        }

        public MultiblockPattern build() {
            if (layers.isEmpty() || controller == null) {
                throw new IllegalStateException("pattern needs layers and a controller");
            }
            int depth = layers.get(0).length;
            int width = layers.get(0)[0].length();
            char[][][] cells = new char[layers.size()][depth][width];
            BlockPos controllerOffset = null;
            for (int y = 0; y < layers.size(); y++) {
                String[] rows = layers.get(y);
                if (rows.length != depth) {
                    throw new IllegalStateException("layer " + y + " has " + rows.length + " rows, expected " + depth);
                }
                for (int z = 0; z < depth; z++) {
                    if (rows[z].length() != width) {
                        throw new IllegalStateException("row " + z + " of layer " + y + " has wrong width");
                    }
                    for (int x = 0; x < width; x++) {
                        char symbol = rows[z].charAt(x);
                        if (symbol != IGNORED && !matchers.containsKey(symbol)) {
                            throw new IllegalStateException("no matcher for symbol '" + symbol + "'");
                        }
                        if (symbol == controller) {
                            if (controllerOffset != null) {
                                throw new IllegalStateException("controller symbol used more than once");
                            }
                            controllerOffset = new BlockPos(x, y, z);
                        }
                        cells[y][z][x] = symbol;
                    }
                }
            }
            if (controllerOffset == null) {
                throw new IllegalStateException("controller symbol not placed");
            }
            return new MultiblockPattern(cells, Map.copyOf(matchers), controllerOffset);
        }
    }
}
