package dev.craft.gregtech.block;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the connected casing textures (phase 11.5). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtCasingGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final BlockPos CASING = new BlockPos(5, 1, 5);

    private GtCasingGameTests() {
    }

    /** Only casings of the same type connect; the side variants follow {@code getBlockTexture}. */
    @GameTest(template = EMPTY)
    public static void casingsConnectToSameType(GameTestHelper helper) {
        GtCasingBlock standard = GtBlocks.casing(GtCasingBlock.Type.STANDARD).get();
        place(helper, CASING, standard);
        place(helper, CASING.east(), standard);
        place(helper, CASING.above(), GtBlocks.casing(GtCasingBlock.Type.REINFORCED).get());
        BlockState state = helper.getBlockState(CASING);
        helper.assertTrue(connected(state, Direction.EAST), "east casing connects");
        helper.assertFalse(connected(state, Direction.UP), "reinforced casing does not connect");
        helper.assertTrue(connected(helper.getBlockState(CASING.east()), Direction.WEST), "connection is mutual");
        helper.assertValueEqual(GtCasingBlock.textureVariant(state, Direction.EAST), 7, "covered side");
        // Only the left edge (east) of the north side is connected: original picks the top/bottom frame (0).
        helper.assertValueEqual(GtCasingBlock.textureVariant(state, Direction.NORTH), 0, "north side");
        helper.assertValueEqual(GtCasingBlock.textureVariant(state, Direction.WEST), 7, "lone side");

        helper.setBlock(CASING.east(), Blocks.AIR);
        helper.assertFalse(connected(helper.getBlockState(CASING), Direction.EAST), "disconnects on removal");
        helper.succeed();
    }

    /** A casing in the middle of a wall shows the frameless variant on its open faces. */
    @GameTest(template = EMPTY)
    public static void wallCenterIsFrameless(GameTestHelper helper) {
        GtCasingBlock standard = GtBlocks.casing(GtCasingBlock.Type.STANDARD).get();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                place(helper, CASING.offset(dx, dy + 1, 0), standard);
            }
        }
        BlockState center = helper.getBlockState(CASING.above());
        helper.assertValueEqual(GtCasingBlock.textureVariant(center, Direction.NORTH), 6, "north face");
        helper.assertValueEqual(GtCasingBlock.textureVariant(center, Direction.SOUTH), 6, "south face");
        BlockState topCenter = helper.getBlockState(CASING.offset(0, 2, 0));
        helper.assertValueEqual(GtCasingBlock.textureVariant(topCenter, Direction.NORTH), 3, "top edge framed");
        helper.succeed();
    }

    /** Places like a player does ({@code getStateForPlacement}); a plain {@code setBlock} leaves sides unset. */
    private static void place(GameTestHelper helper, BlockPos pos, GtCasingBlock casing) {
        helper.setBlock(pos, casing.connectedState(helper.getLevel(), helper.absolutePos(pos)));
    }

    private static boolean connected(BlockState state, Direction side) {
        return state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side));
    }
}
