package dev.craft.mffs.field;

import static dev.craft.mffs.field.ProjectorUpgrade.BLOCK_BREAKER;
import static dev.craft.mffs.field.ProjectorUpgrade.CAMOUFLAGE;
import static dev.craft.mffs.field.ProjectorUpgrade.DEFENSE_STATION;
import static dev.craft.mffs.field.ProjectorUpgrade.FIELD_FUSION;
import static dev.craft.mffs.field.ProjectorUpgrade.FIELD_JAMMER;
import static dev.craft.mffs.field.ProjectorUpgrade.FIELD_MANIPULATOR;
import static dev.craft.mffs.field.ProjectorUpgrade.NPC_DEFENSE;
import static dev.craft.mffs.field.ProjectorUpgrade.SPONGE;
import static dev.craft.mffs.field.ProjectorUpgrade.TOUCH_DAMAGE;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

/**
 * {@code ProjectorTyp} + {@code modules/*} (spec 48 §9.2): the eight field shapes. {@link #getTypeId()} is the original
 * {@code ProTyp} (texture row, power estimate); the loops of {@link #calculate} follow {@code calculateField} 1:1,
 * including the axis swaps per side. Points are relative to the projector; 3D modules also fill the interior.
 */
public enum ProjectorModule {
    WALL(1, "Wall", true, true, true, true, ForceFieldType.DEFAULT,
            EnumSet.of(BLOCK_BREAKER, CAMOUFLAGE, TOUCH_DAMAGE)),
    DEFLECTOR(2, "Deflector", true, false, true, true, ForceFieldType.DEFAULT,
            EnumSet.of(BLOCK_BREAKER, CAMOUFLAGE, TOUCH_DAMAGE)),
    TUBE(3, "Tube", true, true, false, false, ForceFieldType.AREA,
            EnumSet.of(CAMOUFLAGE, FIELD_FUSION, FIELD_MANIPULATOR, FIELD_JAMMER, SPONGE, BLOCK_BREAKER,
                    TOUCH_DAMAGE)),
    CUBE(4, "Cube", true, false, false, false, ForceFieldType.AREA,
            EnumSet.of(CAMOUFLAGE, DEFENSE_STATION, FIELD_FUSION, FIELD_MANIPULATOR, FIELD_JAMMER, NPC_DEFENSE, SPONGE,
                    BLOCK_BREAKER)),
    SPHERE(5, "Sphere", true, true, false, false, ForceFieldType.AREA,
            EnumSet.of(CAMOUFLAGE, DEFENSE_STATION, FIELD_FUSION, FIELD_MANIPULATOR, FIELD_JAMMER, NPC_DEFENSE, SPONGE,
                    BLOCK_BREAKER)),
    CONTAINMENT(6, "Containment", true, true, true, false, ForceFieldType.CONTAINMENT,
            EnumSet.of(CAMOUFLAGE, DEFENSE_STATION, FIELD_FUSION, FIELD_JAMMER, NPC_DEFENSE, SPONGE, BLOCK_BREAKER)),
    ADV_CUBE(7, "Adv.Cube", false, true, true, false, ForceFieldType.AREA,
            EnumSet.of(CAMOUFLAGE, DEFENSE_STATION, FIELD_FUSION, FIELD_MANIPULATOR, FIELD_JAMMER, NPC_DEFENSE, SPONGE,
                    BLOCK_BREAKER)),
    DIAGONAL_WALL(8, "diagonal Wall", true, true, true, true, ForceFieldType.DEFAULT,
            EnumSet.of(BLOCK_BREAKER, CAMOUFLAGE, TOUCH_DAMAGE));

    private final int typeId;
    private final String displayName;
    private final boolean distance;
    private final boolean strength;
    private final boolean matrix;
    private final boolean blockDropper;
    private final ForceFieldType fieldType;
    private final Set<ProjectorUpgrade> upgrades;

    ProjectorModule(int typeId, String displayName, boolean distance, boolean strength, boolean matrix,
                    boolean blockDropper, ForceFieldType fieldType, Set<ProjectorUpgrade> upgrades) {
        this.typeId = typeId;
        this.displayName = displayName;
        this.distance = distance;
        this.strength = strength;
        this.matrix = matrix;
        this.blockDropper = blockDropper;
        this.fieldType = fieldType;
        this.upgrades = upgrades;
    }

    /** {@code ProTyp} 1–8 (0 = no module). */
    public int getTypeId() {
        return typeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean supportsDistance() {
        return distance;
    }

    public boolean supportsStrength() {
        return strength;
    }

    public boolean supportsMatrix() {
        return matrix;
    }

    /** {@code ProjectorTyp.Blockdropper}: the block breaker hands drops to an inventory (spec 48 §9.3). */
    public boolean isBlockDropper() {
        return blockDropper;
    }

    public ForceFieldType getFieldType() {
        return fieldType;
    }

    public boolean supports(ProjectorUpgrade upgrade) {
        return upgrades.contains(upgrade);
    }

    /** 2D modules only fill {@code field}; 3D modules also {@code interior}. */
    public void calculate(FieldParams p, Set<BlockPos> field, Set<BlockPos> interior) {
        switch (this) {
            case WALL -> wall(p, field);
            case DIAGONAL_WALL -> diagonalWall(p, field);
            case DEFLECTOR -> deflector(p, field);
            case TUBE -> tube(p, field, interior);
            case CUBE -> cube(p, field, interior);
            case SPHERE -> sphere(p, field, interior);
            case CONTAINMENT -> box(p, field, interior, p.distance() + 1, p.strength() + 1, false);
            case ADV_CUBE -> box(p, field, interior, -1, p.strength() + 2, p.dome());
        }
    }

    /** Wall orientation: lateral {@code a}, vertical {@code b}, {@code depth} away from the front. */
    private static BlockPos wallPoint(int side, int a, int b, int depth) {
        return switch (side) {
            case 0 -> new BlockPos(a, -depth, -b);
            case 1 -> new BlockPos(a, depth, -b);
            case 2 -> new BlockPos(-a, b, -depth);
            case 3 -> new BlockPos(a, b, depth);
            case 4 -> new BlockPos(-depth, b, a);
            default -> new BlockPos(depth, b, -a);
        };
    }

    /** Deflector/containment/adv. cube orientation (the vertical axis runs the other way). */
    private static BlockPos platePoint(int side, int a, int b, int depth) {
        return switch (side) {
            case 0 -> new BlockPos(a, -depth, b);
            case 1 -> new BlockPos(a, depth, b);
            case 2 -> new BlockPos(-a, -b, -depth);
            case 3 -> new BlockPos(a, -b, depth);
            case 4 -> new BlockPos(-depth, -b, a);
            default -> new BlockPos(depth, -b, -a);
        };
    }

    /** The two coordinates of a wall point that span the wall plane (first: lateral, second: vertical). */
    private static int planeA(int side, BlockPos point) {
        return side <= 3 ? point.getX() : point.getZ();
    }

    private static int planeB(int side, BlockPos point) {
        return side <= 1 ? point.getZ() : point.getY();
    }

    /** {@code ItemProjectorModuleWall}: only the points on the two axes of the plane, strength + 1 layers deep. */
    private static void wall(FieldParams p, Set<BlockPos> field) {
        for (int x1 = -p.focusLeft(); x1 < p.focusRight() + 1; x1++) {
            for (int z1 = -p.focusDown(); z1 < p.focusUp() + 1; z1++) {
                for (int y1 = 1; y1 < p.strength() + 1 + 1; y1++) {
                    BlockPos point = wallPoint(p.side(), x1, z1, y1 + p.distance());
                    if (planeA(p.side(), point) == 0 || planeB(p.side(), point) == 0) {
                        field.add(point);
                    }
                }
            }
        }
    }

    /**
     * {@code ItemProjectorModulediagonallyWall}: the loop bounds come from the focus counts in the original's
     * (partly crossed) way; a point is kept on a diagonal of a quadrant whose focus slot is filled.
     */
    private static void diagonalWall(FieldParams p, Set<BlockPos> field) {
        int up = p.focusUp();
        int down = p.focusDown();
        int right = p.focusRight();
        int left = p.focusLeft();
        int xStart = 0;
        int xEnd = 0;
        int zStart = 0;
        int zEnd = 0;
        if (up > 0) {
            xEnd = Math.max(xEnd, Math.max(up, right));
            zEnd = Math.max(zEnd, Math.max(up, right));
        }
        if (down > 0) {
            xStart = Math.max(xEnd, Math.max(down, left));
            zStart = Math.max(zEnd, Math.max(down, left));
        }
        if (left > 0) {
            xEnd = Math.max(xEnd, Math.max(up, left));
            zStart = Math.max(zStart, Math.max(up, left));
        }
        if (right > 0) {
            zEnd = Math.max(zEnd, Math.max(down, right));
            xStart = Math.max(xStart, Math.max(down, right));
        }
        for (int x1 = -zStart; x1 < zEnd + 1; x1++) {
            for (int z1 = -xStart; z1 < xEnd + 1; z1++) {
                for (int y1 = 1; y1 < p.strength() + 1 + 1; y1++) {
                    BlockPos point = wallPoint(p.side(), x1, z1, y1 + p.distance());
                    int a = planeA(p.side(), point);
                    int b = planeB(p.side(), point);
                    if (Math.abs(a) == Math.abs(b) && onDiagonal(p.side() <= 1, a, b, up, down, right, left)) {
                        field.add(point);
                    }
                }
            }
        }
    }

    /**
     * The quadrant checks of the diagonal wall. Down/up facing: up = (+a, −b), down = (−a, +b), right = (+a, +b),
     * left = (−a, −b); the other sides: up = (+a, +b), down = (−a, −b), right = (+a, −b), left = (−a, +b). Each
     * quadrant also needs a ≤ n and b ≤ n for its focus count n (as in the original, without absolute values).
     */
    private static boolean onDiagonal(boolean vertical, int a, int b, int up, int down, int right, int left) {
        int bUp = vertical ? -b : b;
        return up != 0 && a >= 0 && bUp >= 0 && a <= up && b <= up
                || down != 0 && a <= 0 && bUp <= 0 && a <= down && b <= down
                || right != 0 && a >= 0 && bUp <= 0 && a <= right && b <= right
                || left != 0 && a <= 0 && bUp >= 0 && a <= left && b <= left;
    }

    /** {@code ItemProjectorModuleDeflector}: a plate distance + 1 in front, one block thick. */
    private static void deflector(FieldParams p, Set<BlockPos> field) {
        for (int x1 = -p.focusLeft(); x1 < p.focusRight() + 1; x1++) {
            for (int z1 = -p.focusUp(); z1 < p.focusDown() + 1; z1++) {
                field.add(platePoint(p.side(), x1, z1, p.distance() + 1));
            }
        }
    }

    /**
     * {@code ItemProjectorModuleContainment} ({@code start} = distance + 1, {@code length} = strength + 1) and
     * {@code ItemProjectorModuleAdvCube} ({@code start} = −1, {@code length} = strength + 2): a hollow box along the
     * facing; the adv. cube with field manipulator drops the shell points behind the projector.
     */
    private static void box(FieldParams p, Set<BlockPos> field, Set<BlockPos> interior, int start, int length,
                            boolean dome) {
        int left = p.focusLeft();
        int right = p.focusRight();
        int down = p.focusDown();
        int up = p.focusUp();
        for (int y1 = 0; y1 <= length; y1++) {
            for (int x1 = -left; x1 < right + 1; x1++) {
                for (int z1 = -up; z1 < down + 1; z1++) {
                    int depth = y1 + start;
                    BlockPos point = platePoint(p.side(), x1, z1, depth);
                    if (y1 != 0 && y1 != length && x1 != -left && x1 != right && z1 != -up && z1 != down) {
                        interior.add(point);
                    } else if (!dome || depth >= 0) {
                        field.add(point);
                    }
                }
            }
        }
    }

    /**
     * {@code ItemProjectorModuleTube}: a box around the projector, cross section ±(distance + 2), length ±strength
     * along the facing axis and open at both ends; the field manipulator cuts off one half.
     */
    private static void tube(FieldParams p, Set<BlockPos> field, Set<BlockPos> interior) {
        int distance = p.distance() + 2;
        int strength = p.strength();
        int side = p.side();
        int tpx;
        int tpy;
        int tpz;
        int xs = 0;
        int ys = 0;
        int zs = 0;
        int xe = 0;
        int ye = 0;
        int ze = 0;
        if (side == 0 || side == 1) {
            tpy = strength;
            tpx = distance;
            tpz = distance;
            if (p.dome()) {
                if (side == 0) {
                    ye = strength;
                } else {
                    ys = strength;
                }
            }
        } else if (side == 2 || side == 3) {
            tpy = distance;
            tpz = strength;
            tpx = distance;
            if (p.dome()) {
                if (side == 2) {
                    ze = strength;
                } else {
                    zs = strength;
                }
            }
        } else {
            tpy = distance;
            tpz = distance;
            tpx = strength;
            if (p.dome()) {
                if (side == 4) {
                    xe = strength;
                } else {
                    xs = strength;
                }
            }
        }
        // The ends along the facing axis never count as shell: the tube is open there.
        int ex = tpx == strength && side >= 4 ? tpx + 1 : tpx;
        int ey = tpy == strength && side <= 1 ? tpy + 1 : tpy;
        int ez = tpz == strength && (side == 2 || side == 3) ? tpz + 1 : tpz;
        for (int z1 = -tpz + zs; z1 <= tpz - ze; z1++) {
            for (int x1 = -tpx + xs; x1 <= tpx - xe; x1++) {
                for (int y1 = -tpy + ys; y1 <= tpy - ye; y1++) {
                    BlockPos point = new BlockPos(x1, y1, z1);
                    if ((x1 == -ex || x1 == ex || y1 == -ey || y1 == ey || z1 == -ez || z1 == ez)
                            && p.y() + y1 >= p.minY()) {
                        field.add(point);
                    } else {
                        interior.add(point);
                    }
                }
            }
        }
    }

    /** {@code ItemProjectorModuleCube}: hollow cube of radius distance + 4, top limited by the world height. */
    private static void cube(FieldParams p, Set<BlockPos> field, Set<BlockPos> interior) {
        int radius = p.distance() + 4;
        int yDown = radius;
        int yTop = radius;
        if (p.y() + radius > p.maxY()) {
            yTop = p.maxY() - p.y();
        }
        if (p.dome()) {
            yDown = 0;
        }
        for (int y1 = -yDown; y1 <= yTop; y1++) {
            for (int x1 = -radius; x1 <= radius; x1++) {
                for (int z1 = -radius; z1 <= radius; z1++) {
                    BlockPos point = new BlockPos(x1, y1, z1);
                    if (x1 != -radius && x1 != radius && y1 != -radius && y1 != yTop && z1 != -radius
                            && z1 != radius) {
                        interior.add(point);
                    } else {
                        field.add(point);
                    }
                }
            }
        }
    }

    /** {@code ItemProjectorModuleSphere}: shell of radius distance + 4 and thickness strength + 1 (rounded). */
    private static void sphere(FieldParams p, Set<BlockPos> field, Set<BlockPos> interior) {
        int radius = p.distance() + 4;
        int yDown = p.dome() ? 0 : radius;
        for (int y1 = -yDown; y1 <= radius; y1++) {
            for (int x1 = -radius; x1 <= radius; x1++) {
                for (int z1 = -radius; z1 <= radius; z1++) {
                    int dist = (int) Math.round(Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1));
                    if (dist <= radius && dist > radius - (p.strength() + 1)) {
                        field.add(new BlockPos(x1, y1, z1));
                    } else if (dist <= radius) {
                        interior.add(new BlockPos(x1, y1, z1));
                    }
                }
            }
        }
    }
}
