package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.transport.GenericPipeBlock;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeConnections;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.transport.TravelingItem;
import dev.craft.buildcraft.util.ItemOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the item pipes ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PipeGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private PipeGameTests() {
    }

    private static void pipe(GameTestHelper helper, BlockPos pos, PipeType type) {
        helper.setBlock(pos, BcBlocks.pipe(type).get());
    }

    /** Puts an item at the pipe's edge facing {@code from}, moving into the pipe. */
    private static TravelingItem inject(GameTestHelper helper, BlockPos pos, Direction from, ItemStack stack) {
        BlockPos abs = helper.absolutePos(pos);
        TravelingItem item = new TravelingItem(stack, abs.getX() + 0.5 + from.getStepX() * 0.5,
                abs.getY() + ItemPipeBlockEntity.PIPE_FLOOR + from.getStepY() * 0.5,
                abs.getZ() + 0.5 + from.getStepZ() * 0.5);
        ((ItemPipeBlockEntity) helper.getBlockEntity(pos)).entityEntering(item, from.getOpposite());
        return item;
    }

    @GameTest(template = EMPTY)
    public static void connectionRulesFollowMaterials(GameTestHelper helper) {
        pipe(helper, new BlockPos(1, 1, 1), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(2, 1, 1), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(1, 1, 3), PipeType.COBBLESTONE_TRANSPORT);
        pipe(helper, new BlockPos(2, 1, 3), PipeType.STONE_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 3), PipeType.GOLDEN_TRANSPORT);
        helper.setBlock(new BlockPos(1, 1, 5), Blocks.CHEST);
        pipe(helper, new BlockPos(2, 1, 5), PipeType.SANDSTONE_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 5), PipeType.IRON_TRANSPORT);
        helper.setBlock(new BlockPos(4, 1, 5), Blocks.CHEST);
        helper.setBlock(new BlockPos(1, 1, 6), Blocks.STONE);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(1, 1, 1), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(1, 1, 3), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 3), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(2, 1, 5), PipeBlock.WEST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 5), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 5), PipeBlock.EAST, true);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 800)
    public static void woodenPipeExtractsIntoChest(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos target = new BlockPos(5, 1, 2);
        helper.setBlock(source, Blocks.CHEST);
        helper.setBlock(target, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(source)).setItem(0, new ItemStack(Items.COBBLESTONE, 10));
        pipe(helper, new BlockPos(2, 1, 2), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        pipe(helper, new BlockPos(4, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GenericPipeBlock.SOURCE,
                    Direction.WEST.get3DDataValue());
            ItemPipeBlockEntity wood = helper.getBlockEntity(new BlockPos(2, 1, 2));
            wood.getPowerProvider().setEnergyStored(64.0F);
        });
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(target);
            helper.assertValueEqual(chest.getItem(0).getCount(), 10, "items in target chest");
            helper.assertTrue(((ChestBlockEntity) helper.getBlockEntity(source)).isEmpty(), "source emptied");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void voidPipeDestroysItems(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        pipe(helper, pos, PipeType.VOID_TRANSPORT);
        helper.runAfterDelay(2, () -> inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT, 5)));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(((ItemPipeBlockEntity) helper.getBlockEntity(pos)).getItems().isEmpty(), "pipe empty");
            helper.assertEntityNotPresent(EntityType.ITEM);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void ironPipeSendsOnlyToOutput(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        helper.setBlock(west, Blocks.CHEST);
        helper.setBlock(east, Blocks.CHEST);
        pipe(helper, pos, PipeType.IRON_TRANSPORT);
        helper.runAfterDelay(2, () -> {
            // From DOWN round the sides: up, north, south have nothing, west has the chest.
            PipeConnections.switchIronOutput(helper.getLevel(), helper.absolutePos(pos));
            helper.assertBlockProperty(pos, GenericPipeBlock.OUTPUT, Direction.WEST.get3DDataValue());
            for (int i = 0; i < 4; i++) {
                inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT));
            }
        });
        helper.runAfterDelay(120, () -> {
            helper.assertValueEqual(((ChestBlockEntity) helper.getBlockEntity(west)).getItem(0).getCount(), 4,
                    "west chest");
            helper.assertTrue(((ChestBlockEntity) helper.getBlockEntity(east)).isEmpty(), "east chest empty");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void goldenPipeQuadruplesSpeed(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        pipe(helper, pos, PipeType.GOLDEN_TRANSPORT);
        helper.runAfterDelay(2, () -> {
            TravelingItem item = inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT));
            BlockPos abs = helper.absolutePos(pos);
            double start = item.getZ(1.0F);
            ((ItemPipeBlockEntity) helper.getBlockEntity(pos)).tick();
            helper.assertTrue(Math.abs(item.getZ(1.0F) - start - 0.04) < 1.0E-6, "moved 0.04 per tick");
            helper.assertTrue(abs.getZ() < item.getZ(1.0F), "inside the pipe");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void miningWellOutputsIntoPipe(GameTestHelper helper) {
        BlockPos well = new BlockPos(2, 1, 2);
        BlockPos chest = new BlockPos(4, 1, 2);
        helper.setBlock(well, BcBlocks.MINING_WELL.get());
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        helper.setBlock(chest, Blocks.CHEST);
        helper.runAfterDelay(2, () -> ItemOutput.output(helper.getLevel(), helper.absolutePos(well),
                new ItemStack(Items.COAL, 3)));
        helper.succeedWhen(() -> helper.assertValueEqual(
                ((ChestBlockEntity) helper.getBlockEntity(chest)).getItem(0).getCount(), 3, "coal in chest"));
    }
}
