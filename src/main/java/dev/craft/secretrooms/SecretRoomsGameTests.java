package dev.craft.secretrooms;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 50.4 (spec 50 §4): camouflage choice, ghost block, one-way glass. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class SecretRoomsGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 2, 3);

    private SecretRoomsGameTests() {
    }

    private static CamoBlockEntity camo(GameTestHelper helper, BlockPos pos) {
        return (CamoBlockEntity) helper.getBlockEntity(pos);
    }

    /** A matching opposite pair beats a single neighbour; alone, the only neighbour; nothing: stone. */
    @GameTest(template = EMPTY)
    public static void camoChoosesPairThenFirst(GameTestHelper helper) {
        helper.setBlock(POS.west(), Blocks.OAK_PLANKS);
        helper.setBlock(POS.north(), Blocks.BRICKS);
        helper.setBlock(POS.south(), Blocks.BRICKS);
        helper.setBlock(POS, SecretRooms.GHOST_BLOCK.get());
        helper.assertTrue(camo(helper, POS).getCamo().is(Blocks.BRICKS), "north/south pair wins");
        BlockPos lone = new BlockPos(6, 2, 6);
        helper.setBlock(lone.below(), Blocks.SAND);
        helper.setBlock(lone, SecretRooms.GHOST_BLOCK.get());
        helper.assertTrue(camo(helper, lone).getCamo().is(Blocks.SAND), "single neighbour");
        BlockPos chained = new BlockPos(3, 2, 4).east();
        helper.setBlock(chained, SecretRooms.GHOST_BLOCK.get());
        helper.assertTrue(CamoSelection.lookOf(helper.getLevel(), helper.absolutePos(POS)).is(Blocks.BRICKS),
                "a camouflaged neighbour passes on its camouflage");
        helper.succeed();
    }

    /** Walk-through without signal, solid with a redstone block next to it. */
    @GameTest(template = EMPTY)
    public static void ghostBlockSolidWhenPowered(GameTestHelper helper) {
        helper.setBlock(POS, SecretRooms.GHOST_BLOCK.get());
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(POS);
        helper.assertTrue(level.getBlockState(absolute).getCollisionShape(level, absolute, CollisionContext.empty())
                .isEmpty(), "no collision");
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.assertBlockProperty(POS, GhostBlock.POWERED, true);
        helper.assertFalse(level.getBlockState(absolute).getCollisionShape(level, absolute, CollisionContext.empty())
                .isEmpty(), "solid when powered");
        helper.setBlock(POS.east(), Blocks.AIR);
        helper.assertBlockProperty(POS, GhostBlock.POWERED, false);
        helper.succeed();
    }

    /** One-way glass: camouflage from the four blocks around the facing axis, duplicates first. */
    @GameTest(template = EMPTY)
    public static void oneWayGlassCamouflage(GameTestHelper helper) {
        helper.setBlock(POS.above(), Blocks.STONE_BRICKS);
        helper.setBlock(POS.below(), Blocks.DIRT);
        helper.setBlock(POS.east(), Blocks.DIRT);
        helper.setBlock(POS.north(), Blocks.GOLD_BLOCK);
        helper.setBlock(POS, SecretRooms.ONE_WAY_GLASS.get().defaultBlockState()
                .setValue(OneWayGlassBlock.FACING, Direction.NORTH));
        helper.assertTrue(camo(helper, POS).getCamo().is(Blocks.DIRT), "dirt appears twice around the north axis");
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(POS);
        helper.assertTrue(level.getBlockState(absolute).getLightBlock(level, absolute) == 15, "blocks light");
        helper.succeed();
    }
}
