package dev.craft.core.multiblock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.junit.jupiter.api.Test;

class MultiblockPatternTest {
    /** Asymmetric: controller C at the front centre, an extra block X behind on the east side. */
    private static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("SSX",
                   "SSS",
                   "SCS")
            .where('S', BlockMatcher.blocks(Blocks.STONE))
            .where('X', BlockMatcher.blocks(Blocks.GOLD_BLOCK))
            .controller('C', BlockMatcher.blocks(Blocks.FURNACE))
            .build();

    @Test
    void matchesUnrotatedLayout() {
        FakeLevel level = new FakeLevel();
        BlockPos controller = new BlockPos(10, 64, 10);
        place(level, controller, Direction.NORTH);

        Optional<MultiblockPattern.Match> match = PATTERN.match(level, controller, Direction.NORTH);
        assertTrue(match.isPresent());
        assertEquals(7, match.get().get('S').size());
        assertEquals(new BlockPos(11, 64, 8), match.get().get('X').get(0));
    }

    @Test
    void findsRotatedStructure() {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            FakeLevel level = new FakeLevel();
            BlockPos controller = new BlockPos(-3, 70, 5);
            place(level, controller, facing);

            Optional<MultiblockPattern.Match> match = PATTERN.matchAnyFacing(level, controller);
            assertTrue(match.isPresent(), "facing " + facing);
            assertEquals(facing, match.get().facing());
        }
    }

    @Test
    void missingBlockBreaksMatch() {
        FakeLevel level = new FakeLevel();
        BlockPos controller = new BlockPos(0, 64, 0);
        place(level, controller, Direction.EAST);
        level.blocks.remove(PATTERN.worldPos(controller, Direction.EAST, 0, 0, 0));

        assertFalse(PATTERN.matchAnyFacing(level, controller).isPresent());
    }

    @Test
    void eastFacingTurnsLayoutNorthToEast() {
        // layout north is the controller facing; facing east, the first row (z = 0) lies two blocks east
        BlockPos controller = new BlockPos(0, 0, 0);
        assertEquals(new BlockPos(2, 0, 1), PATTERN.worldPos(controller, Direction.EAST, 2, 0, 0));
    }

    @Test
    void rejectsUndefinedSymbols() {
        assertThrows(IllegalStateException.class, () -> MultiblockPattern.builder()
                .layer("AC").controller('C', BlockMatcher.ANY).build());
    }

    private static void place(FakeLevel level, BlockPos controller, Direction facing) {
        String[] rows = {"SSX", "SSS", "SCS"};
        for (int z = 0; z < 3; z++) {
            for (int x = 0; x < 3; x++) {
                Block block = switch (rows[z].charAt(x)) {
                    case 'X' -> Blocks.GOLD_BLOCK;
                    case 'C' -> Blocks.FURNACE;
                    default -> Blocks.STONE;
                };
                level.blocks.put(PATTERN.worldPos(controller, facing, x, 0, z), block.defaultBlockState());
            }
        }
    }

    private static final class FakeLevel implements BlockGetter {
        final Map<BlockPos, BlockState> blocks = new HashMap<>();

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public int getHeight() {
            return 384;
        }

        @Override
        public int getMinBuildHeight() {
            return -64;
        }
    }
}
