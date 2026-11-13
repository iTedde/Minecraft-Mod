package dev.craft.secretrooms;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 50.5 (spec 50 §4): camouflaged redstone, switches, gate, stairs, torch lever. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class SecretRoomsRedstoneGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private SecretRoomsRedstoneGameTests() {
    }

    private static void use(GameTestHelper helper, BlockPos pos) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absolute = helper.absolutePos(pos);
        helper.getBlockState(pos).useWithoutItem(helper.getLevel(), player,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    /** Two camouflaged redstone blocks carry a redstone block's signal to a lamp, and off again. */
    @GameTest(template = EMPTY)
    public static void camoRedstoneCarriesSignal(GameTestHelper helper) {
        BlockPos a = new BlockPos(2, 1, 3);
        BlockPos b = a.east();
        BlockPos lamp = b.east();
        helper.setBlock(lamp, Blocks.REDSTONE_LAMP);
        helper.setBlock(a, SecretRooms.CAMO_REDSTONE.get());
        helper.setBlock(b, SecretRooms.CAMO_REDSTONE.get());
        helper.setBlock(a.west(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(a, CamoRedstoneBlock.SOURCE, CamoRedstoneBlock.Source.WEST);
            helper.assertBlockProperty(b, CamoRedstoneBlock.SOURCE, CamoRedstoneBlock.Source.WEST);
            helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true);
            helper.setBlock(a.west(), Blocks.AIR);
        });
        helper.runAfterDelay(10, () -> {
            helper.assertBlockProperty(a, CamoRedstoneBlock.SOURCE, CamoRedstoneBlock.Source.NONE);
            helper.assertBlockProperty(b, CamoRedstoneBlock.SOURCE, CamoRedstoneBlock.Source.NONE);
            helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, false);
            helper.succeed();
        });
    }

    /** Lever toggles, button springs back after 20 ticks. */
    @GameTest(template = EMPTY)
    public static void camoLeverAndButton(GameTestHelper helper) {
        BlockPos lever = new BlockPos(2, 1, 2);
        BlockPos button = new BlockPos(5, 1, 5);
        helper.setBlock(lever, SecretRooms.CAMO_LEVER.get());
        helper.setBlock(button, SecretRooms.CAMO_BUTTON.get());
        use(helper, lever);
        use(helper, button);
        helper.assertBlockProperty(lever, CamoSwitchBlock.POWERED, true);
        helper.assertBlockProperty(button, CamoSwitchBlock.POWERED, true);
        helper.assertTrue(helper.getLevel().getSignal(helper.absolutePos(lever), Direction.WEST) == 15,
                "lever gives 15");
        use(helper, lever);
        helper.assertBlockProperty(lever, CamoSwitchBlock.POWERED, false);
        helper.runAfterDelay(CamoSwitchBlock.DELAY + 2, () -> {
            helper.assertBlockProperty(button, CamoSwitchBlock.POWERED, false);
            helper.succeed();
        });
    }

    /** Signal pushes out extensions upwards into air; without signal they go again. */
    @GameTest(template = EMPTY)
    public static void camoGateBuildsAndRetracts(GameTestHelper helper) {
        BlockPos gate = new BlockPos(3, 1, 3);
        helper.setBlock(gate, SecretRooms.CAMO_GATE.get().defaultBlockState()
                .setValue(CamoGateBlock.FACING, Direction.UP));
        helper.setBlock(gate.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(3, () -> {
            for (int i = 1; i <= 2; i++) {
                helper.assertBlockPresent(SecretRooms.CAMO_GATE_EXTENSION.get(), gate.above(i));
            }
            helper.setBlock(gate.east(), Blocks.AIR);
        });
        helper.runAfterDelay(6, () -> {
            helper.assertBlockPresent(Blocks.AIR, gate.above());
            helper.assertBlockPresent(Blocks.AIR, gate.above(2));
            helper.succeed();
        });
    }

    /** Camouflaged stairs: full outline, stair-shaped collision. */
    @GameTest(template = EMPTY)
    public static void camoStairsCollision(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, SecretRooms.CAMO_STAIRS.get());
        var level = helper.getLevel();
        BlockPos absolute = helper.absolutePos(pos);
        var state = level.getBlockState(absolute);
        helper.assertTrue(state.getCollisionShape(level, absolute, CollisionContext.empty()).bounds().maxY == 1.0
                && !state.isCollisionShapeFullBlock(level, absolute), "stair collision");
        helper.assertTrue(state.getShape(level, absolute).bounds().getSize() > 0.99, "full outline");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void torchLeverSwitches(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, SecretRooms.TORCH_LEVER.get());
        use(helper, pos);
        helper.assertBlockProperty(pos, TorchLeverBlock.POWERED, true);
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).getLightEmission() == 14,
                "torch light");
        helper.succeed();
    }
}
