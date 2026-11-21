package dev.craft.mffs.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

/** Field shapes of the projector modules (spec 48 §9.2), hand-counted from the original loops. */
class ProjectorModuleTest {
    private static final int Y = 64;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;

    private final Set<BlockPos> field = new HashSet<>();
    private final Set<BlockPos> interior = new HashSet<>();

    private void calc(ProjectorModule module, int side, int distance, int strength, int up, int down, int right,
                      int left, boolean dome, int y) {
        field.clear();
        interior.clear();
        module.calculate(new FieldParams(side, distance, strength, up, down, right, left, dome, y, MIN_Y, MAX_Y),
                field, interior);
    }

    private void calc(ProjectorModule module, int side, int distance, int strength, int up, int down, int right,
                      int left) {
        calc(module, side, distance, strength, up, down, right, left, false, Y);
    }

    @Test
    void wallIsACrossInFrontOfTheProjector() {
        calc(ProjectorModule.WALL, 3, 0, 0, 0, 0, 0, 0);
        assertEquals(Set.of(new BlockPos(0, 0, 1)), field);
        // South: up 2, right 1, two layers starting 3 blocks in front; only points on the two axes.
        calc(ProjectorModule.WALL, 3, 2, 1, 2, 0, 1, 0);
        assertEquals(8, field.size());
        assertTrue(field.contains(new BlockPos(0, 2, 3)));
        assertTrue(field.contains(new BlockPos(1, 0, 4)));
        assertFalse(field.contains(new BlockPos(1, 1, 3)));
        assertTrue(interior.isEmpty());
        // North mirrors the lateral axis.
        calc(ProjectorModule.WALL, 2, 2, 0, 0, 0, 1, 0);
        assertEquals(Set.of(new BlockPos(0, 0, -3), new BlockPos(-1, 0, -3)), field);
    }

    @Test
    void deflectorIsAPlate() {
        calc(ProjectorModule.DEFLECTOR, 1, 1, 0, 1, 0, 1, 1);
        assertEquals(6, field.size());
        assertTrue(field.stream().allMatch(pos -> pos.getY() == 2));
        assertTrue(field.contains(new BlockPos(-1, 2, -1)));
        assertFalse(field.contains(new BlockPos(0, 2, 1)));
    }

    @Test
    void cubeShellAndDome() {
        calc(ProjectorModule.CUBE, 1, 0, 0, 0, 0, 0, 0);
        assertEquals(9 * 9 * 9 - 7 * 7 * 7, field.size());
        assertEquals(7 * 7 * 7, interior.size());
        calc(ProjectorModule.CUBE, 1, 0, 0, 0, 0, 0, 0, true, Y);
        assertEquals(9 * 9 * 5 - 7 * 7 * 4, field.size());
        assertEquals(7 * 7 * 4, interior.size());
        // The top is cut at the world height.
        calc(ProjectorModule.CUBE, 1, 0, 0, 0, 0, 0, 0, false, MAX_Y - 2);
        assertTrue(field.contains(new BlockPos(0, 2, 0)));
        assertFalse(field.contains(new BlockPos(0, 3, 0)));
    }

    @Test
    void sphereShell() {
        calc(ProjectorModule.SPHERE, 1, 0, 0, 0, 0, 0, 0);
        assertTrue(field.contains(new BlockPos(4, 0, 0)));
        assertTrue(interior.contains(new BlockPos(0, 0, 0)));
        assertFalse(field.contains(new BlockPos(3, 3, 3)) || interior.contains(new BlockPos(3, 3, 3)));
        assertTrue(field.contains(new BlockPos(0, -4, 0)));
        calc(ProjectorModule.SPHERE, 1, 0, 0, 0, 0, 0, 0, true, Y);
        assertFalse(field.contains(new BlockPos(0, -4, 0)));
        assertTrue(field.contains(new BlockPos(0, 4, 0)));
    }

    @Test
    void tubeIsOpenAtTheEnds() {
        calc(ProjectorModule.TUBE, 1, 0, 1, 0, 0, 0, 0);
        assertEquals(16 * 3, field.size());
        assertEquals(9 * 3, interior.size());
        assertTrue(interior.contains(new BlockPos(0, 1, 0)));
        calc(ProjectorModule.TUBE, 1, 0, 1, 0, 0, 0, 0, true, Y);
        assertEquals(16 * 2, field.size());
        // Below the world bottom the shell counts as interior.
        calc(ProjectorModule.TUBE, 1, 0, 1, 0, 0, 0, 0, false, MIN_Y);
        assertEquals(16 * 2, field.size());
        assertTrue(interior.contains(new BlockPos(2, -1, 0)));
    }

    @Test
    void containmentAndAdvancedCube() {
        calc(ProjectorModule.CONTAINMENT, 3, 0, 0, 1, 1, 1, 1);
        assertEquals(18, field.size());
        assertTrue(interior.isEmpty());
        assertTrue(field.contains(new BlockPos(0, 0, 1)) && field.contains(new BlockPos(0, 0, 2)));
        calc(ProjectorModule.CONTAINMENT, 3, 0, 1, 1, 1, 1, 1);
        assertEquals(Set.of(new BlockPos(0, 0, 2)), interior);
        calc(ProjectorModule.ADV_CUBE, 3, 0, 0, 1, 1, 1, 1);
        assertEquals(26, field.size());
        assertEquals(Set.of(new BlockPos(0, 0, 0)), interior);
        assertTrue(field.contains(new BlockPos(0, 0, -1)));
        calc(ProjectorModule.ADV_CUBE, 3, 0, 0, 1, 1, 1, 1, true, Y);
        assertEquals(17, field.size());
        assertFalse(field.contains(new BlockPos(0, 0, -1)));
    }

    @Test
    void diagonalWall() {
        calc(ProjectorModule.DIAGONAL_WALL, 3, 0, 0, 2, 0, 0, 0);
        assertEquals(Set.of(new BlockPos(0, 0, 1), new BlockPos(1, 1, 1), new BlockPos(2, 2, 1)), field);
    }
}
