package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.area.Box;
import dev.craft.buildcraft.builders.MarkerBlock;
import dev.craft.buildcraft.builders.MarkerBlockEntity;
import dev.craft.buildcraft.factory.FrameBlock;
import dev.craft.buildcraft.factory.QuarryBlock;
import dev.craft.buildcraft.factory.QuarryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for landmarks and the quarry ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class QuarryGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final String LARGE = "empty_16x8x16";

    private QuarryGameTests() {
    }

    private static MarkerBlockEntity marker(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, BcBlocks.MARKER.get().defaultBlockState().setValue(MarkerBlock.FACING, Direction.UP));
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY)
    public static void landmarksSpanBoxAndDissolve(GameTestHelper helper) {
        MarkerBlockEntity origin = marker(helper, new BlockPos(1, 2, 1));
        MarkerBlockEntity xPartner = marker(helper, new BlockPos(5, 2, 1));
        MarkerBlockEntity zPartner = marker(helper, new BlockPos(1, 2, 5));
        origin.tryConnection();
        BlockPos min = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos max = helper.absolutePos(new BlockPos(5, 2, 5));
        helper.assertValueEqual(origin.xMin(), Math.min(min.getX(), max.getX()), "xMin");
        helper.assertValueEqual(origin.xMax(), Math.max(min.getX(), max.getX()), "xMax");
        helper.assertValueEqual(origin.zMin(), Math.min(min.getZ(), max.getZ()), "zMin");
        helper.assertValueEqual(origin.zMax(), Math.max(min.getZ(), max.getZ()), "zMax");
        helper.assertValueEqual(origin.yMin(), min.getY(), "yMin");
        helper.assertValueEqual(xPartner.xMax(), origin.xMax(), "partners share the box");
        helper.assertTrue(zPartner.isLinked(), "z partner linked");
        helper.destroyBlock(new BlockPos(5, 2, 1));
        helper.assertFalse(origin.isLinked(), "origin unlinked after breaking a partner");
        helper.assertFalse(zPartner.isLinked(), "partner unlinked after breaking a partner");
        helper.succeed();
    }

    @GameTest(template = LARGE)
    public static void quarryDefaultBoxLiesBehindIt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 1, 8);
        helper.setBlock(pos, BcBlocks.QUARRY.get().defaultBlockState().setValue(QuarryBlock.FACING, Direction.NORTH));
        QuarryBlockEntity quarry = helper.getBlockEntity(pos);
        helper.runAfterDelay(2, () -> {
            BlockPos abs = helper.absolutePos(pos);
            Box box = quarry.getBox();
            helper.assertTrue(box.isInitialized(), "box initialised");
            // Front north → box to the south of the quarry, centred on it (the absolute directions depend on the
            // test rotation, so compare sizes and the distance to the quarry).
            helper.assertValueEqual(box.sizeX(), 11, "size x");
            helper.assertValueEqual(box.sizeY(), 5, "size y");
            helper.assertValueEqual(box.sizeZ(), 11, "size z");
            helper.assertValueEqual(box.yMin, abs.getY(), "box starts at the quarry");
            helper.assertFalse(box.contains(abs.getX(), abs.getY(), abs.getZ()), "quarry outside the box");
            helper.succeed();
        });
    }

    @GameTest(template = LARGE, timeoutTicks = 2400)
    public static void quarryBuildsFrameAndMinesIntoChest(GameTestHelper helper) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
        BlockPos originPos = new BlockPos(3, 1, 3);
        MarkerBlockEntity origin = marker(helper, originPos);
        marker(helper, new BlockPos(7, 1, 3));
        marker(helper, new BlockPos(3, 1, 7));
        origin.tryConnection();
        BlockPos quarryPos = new BlockPos(2, 1, 3);
        BlockPos chestPos = new BlockPos(1, 1, 3);
        helper.setBlock(chestPos, Blocks.CHEST);
        helper.setBlock(quarryPos, BcBlocks.QUARRY.get().defaultBlockState()
                .setValue(QuarryBlock.FACING, Direction.WEST));
        QuarryBlockEntity quarry = helper.getBlockEntity(quarryPos);
        helper.onEachTick(() -> quarry.getPowerProvider().setEnergyStored(QuarryBlockEntity.MAX_ENERGY));
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(BcBlocks.MARKER.get(), originPos);
            helper.assertBlockPresent(BcBlocks.FRAME.get(), originPos);
            helper.assertBlockPresent(BcBlocks.FRAME.get(), new BlockPos(7, 5, 7));
            ChestBlockEntity chest = helper.getBlockEntity(chestPos);
            boolean cobble = false;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                cobble |= chest.getItem(i).is(Items.COBBLESTONE);
            }
            helper.assertTrue(cobble, "mined stone ends up in the chest");
            // breaking the quarry lets the frame decay
            helper.destroyBlock(quarryPos);
            BlockState frame = helper.getBlockState(new BlockPos(7, 5, 7));
            helper.assertTrue(frame.is(BcBlocks.FRAME.get()) && frame.getValue(FrameBlock.DECAYING),
                    "frame marked for decay");
        });
    }
}
