package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.factory.MiningWellBlockEntity;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.transport.PowerPipeBlockEntity;
import dev.craft.forestry.FrBlocks;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the conductive and structure pipes ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PowerPipeGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private PowerPipeGameTests() {
    }

    private static void pipe(GameTestHelper helper, BlockPos pos, PipeType type) {
        helper.setBlock(pos, BcBlocks.pipe(type).get());
    }

    @GameTest(template = EMPTY)
    public static void powerAndStructurePipeConnections(GameTestHelper helper) {
        // Conductive pipes: machines and pipes of the same transport, no chests, no wood-wood.
        pipe(helper, new BlockPos(1, 1, 1), PipeType.WOODEN_POWER);
        pipe(helper, new BlockPos(2, 1, 1), PipeType.WOODEN_POWER);
        pipe(helper, new BlockPos(3, 1, 1), PipeType.STONE_POWER);
        helper.setBlock(new BlockPos(4, 1, 1), BcBlocks.MINING_WELL.get());
        pipe(helper, new BlockPos(3, 1, 2), PipeType.STONE_TRANSPORT);
        helper.setBlock(new BlockPos(3, 1, 0), Blocks.CHEST);
        // Structure pipes: every block entity and structure pipes, no other pipes.
        pipe(helper, new BlockPos(1, 1, 4), PipeType.COBBLESTONE_STRUCTURE);
        pipe(helper, new BlockPos(2, 1, 4), PipeType.COBBLESTONE_STRUCTURE);
        helper.setBlock(new BlockPos(0, 1, 4), Blocks.CHEST);
        pipe(helper, new BlockPos(1, 1, 5), PipeType.COBBLESTONE_TRANSPORT);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(1, 1, 1), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 1), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 1), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 1), PipeBlock.SOUTH, false);
            helper.assertBlockProperty(new BlockPos(3, 1, 1), PipeBlock.NORTH, false);
            helper.assertBlockProperty(new BlockPos(1, 1, 4), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(1, 1, 4), PipeBlock.WEST, true);
            helper.assertBlockProperty(new BlockPos(1, 1, 4), PipeBlock.SOUTH, false);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void receiversRequestPowerLikeTheOriginal(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), BcBlocks.MINING_WELL.get());
        helper.setBlock(new BlockPos(3, 1, 1), BcBlocks.REFINERY.get());
        helper.setBlock(new BlockPos(5, 1, 1), FrBlocks.THERMIONIC_FABRICATOR.get());
        MiningWellBlockEntity well = helper.getBlockEntity(new BlockPos(1, 1, 1));
        RefineryBlockEntity refinery = helper.getBlockEntity(new BlockPos(3, 1, 1));
        FabricatorBlockEntity fabricator = helper.getBlockEntity(new BlockPos(5, 1, 1));
        helper.runAfterDelay(1, () -> {
            // Mining well digging: min(maxReceived 25, 1000 − stored).
            helper.assertValueEqual(well.powerRequest(), 25, "mining well request");
            well.getPowerProvider().setEnergyStored(990.5F);
            helper.assertValueEqual(well.powerRequest(), 10, "mining well nearly full");
            // Refinery without a recipe is not active.
            helper.assertValueEqual(refinery.powerRequest(), 0, "idle refinery");
            // Fabricator works while not overheated: min(110, 25 / 5 + 1).
            helper.assertValueEqual(fabricator.powerRequest(), 6, "fabricator request");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void woodenPowerPipeFeedsMiningWellThroughStonePipe(GameTestHelper helper) {
        pipe(helper, new BlockPos(1, 2, 1), PipeType.WOODEN_POWER);
        pipe(helper, new BlockPos(2, 2, 1), PipeType.STONE_POWER);
        pipe(helper, new BlockPos(3, 2, 1), PipeType.GOLDEN_POWER);
        helper.setBlock(new BlockPos(4, 2, 1), BcBlocks.MINING_WELL.get());
        PowerPipeBlockEntity wood = helper.getBlockEntity(new BlockPos(1, 2, 1));
        PowerPipeBlockEntity gold = helper.getBlockEntity(new BlockPos(3, 2, 1));
        MiningWellBlockEntity well = helper.getBlockEntity(new BlockPos(4, 2, 1));
        PowerProvider source = wood.getPowerProvider();
        helper.onEachTick(() -> source.setEnergyStored(100.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(gold.getPowerQuery(Direction.EAST) == 25, "mining well request reaches the pipe");
            helper.assertTrue(well.getPowerProvider().isPowerSource(Direction.WEST), "mining well powered");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void woodenPowerPipeOverheatsWhenFull(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        pipe(helper, pos, PipeType.WOODEN_POWER);
        PowerPipeBlockEntity wood = helper.getBlockEntity(pos);
        PowerProvider provider = wood.getPowerProvider();
        helper.assertTrue(provider != null, "provider before overheating");
        helper.onEachTick(() -> provider.setEnergyStored(1000.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(wood.getOverheatTicks() > 0, "overheated");
            helper.assertTrue(wood.getPowerProvider() == null, "no provider while overheated");
            helper.assertTrue(helper.getLevel().getCapability(BcCapabilities.POWER_RECEPTOR,
                    helper.absolutePos(pos), Direction.UP) == null, "engines see no receiver");
        });
    }

    @GameTest(template = EMPTY)
    public static void powerPipeExplodesAtMaxInternalPower(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        pipe(helper, pos, PipeType.STONE_POWER);
        PowerPipeBlockEntity pipe = helper.getBlockEntity(pos);
        // 10 000 · 0.95 stays below the limit, 11 000 · 0.95 does not.
        pipe.receiveEnergy(Direction.WEST, 10000.0);
        helper.assertBlockPresent(BcBlocks.pipe(PipeType.STONE_POWER).get(), pos);
        pipe.receiveEnergy(Direction.EAST, 11000.0);
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.runAfterDelay(2, () -> {
            helper.assertEntityNotPresent(EntityType.ITEM);
            helper.succeed();
        });
    }
}
