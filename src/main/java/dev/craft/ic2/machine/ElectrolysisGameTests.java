package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.IronFenceBlock;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the iron fence, the magnetizer and the IC2 electrolyzer (phase 6b.2a). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ElectrolysisGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private ElectrolysisGameTests() {
    }

    private static <T extends MachineBlockEntity> T place(GameTestHelper helper, BlockPos pos, Ic2Machine machine) {
        helper.setBlock(pos, Ic2Blocks.machine(machine).get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY)
    public static void ironFenceConnectsOnlyToIronFences(GameTestHelper helper) {
        BlockPos fence = new BlockPos(2, 1, 2);
        helper.setBlock(fence, Ic2Blocks.IRON_FENCE.get());
        helper.setBlock(fence.east(), Ic2Blocks.IRON_FENCE.get());
        helper.setBlock(fence.south(), Blocks.STONE);
        helper.setBlock(fence.west(), Blocks.OAK_FENCE);
        helper.assertBlockProperty(fence, FenceBlock.EAST, true);
        helper.assertBlockProperty(fence, FenceBlock.SOUTH, false);
        helper.assertBlockProperty(fence, FenceBlock.WEST, false);
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void magnetizerChargesFenceColumn(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.above(), Ic2Blocks.IRON_FENCE.get());
        helper.setBlock(pos.above(2), Ic2Blocks.IRON_FENCE.get().defaultBlockState()
                .setValue(IronFenceBlock.MAGNETIZATION, 10));
        MagnetizerBlockEntity magnetizer = place(helper, pos, Ic2Machine.MAGNETIZER);
        magnetizer.setEnergyForTest(100);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(pos.above(), IronFenceBlock.MAGNETIZATION, 15);
            helper.assertBlockProperty(pos.above(2), IronFenceBlock.MAGNETIZATION, 15);
            helper.assertValueEqual(magnetizer.getEnergy(), 100 - 15 - 5, "energy");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void magnetizerChargesForFreeWhenShort(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.above(), Ic2Blocks.IRON_FENCE.get());
        MagnetizerBlockEntity magnetizer = place(helper, pos, Ic2Machine.MAGNETIZER);
        magnetizer.setEnergyForTest(1);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(pos.above(), IronFenceBlock.MAGNETIZATION, 15);
            helper.assertValueEqual(magnetizer.getEnergy(), 0, "energy");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void electrolyzerStoresSurplusOfFullBatBox(GameTestHelper helper) {
        ElectricStorageBlockEntity batBox = place(helper, new BlockPos(2, 1, 2), Ic2Machine.BATBOX);
        ElectrolyzerBlockEntity electrolyzer = place(helper, new BlockPos(2, 2, 2), Ic2Machine.ELECTROLYZER);
        batBox.setEnergy(40000);
        electrolyzer.setEnergyForTest(19998);
        electrolyzer.getInventory().set(ElectrolyzerBlockEntity.SLOT_WATER, new ItemStack(Ic2Items.WATER_CELL.get()));
        // One drain step of 2 EU completes the cell; then there is no water cell left.
        helper.succeedWhen(() -> {
            helper.assertTrue(electrolyzer.getInventory().get(ElectrolyzerBlockEntity.SLOT_ELECTROLYZED)
                    .is(Ic2Items.ELECTROLYZED_WATER_CELL.get()), "electrolyzed cell");
            helper.assertTrue(electrolyzer.getInventory().get(ElectrolyzerBlockEntity.SLOT_WATER).isEmpty(),
                    "water cell used");
            helper.assertValueEqual(electrolyzer.getEnergy(), 0, "buffer");
            helper.assertValueEqual(batBox.getEnergy(), 39998, "BatBox energy");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void electrolyzerFeedsEmptyBatBox(GameTestHelper helper) {
        ElectricStorageBlockEntity batBox = place(helper, new BlockPos(2, 1, 2), Ic2Machine.BATBOX);
        ElectrolyzerBlockEntity electrolyzer = place(helper, new BlockPos(2, 2, 2), Ic2Machine.ELECTROLYZER);
        electrolyzer.getInventory().set(ElectrolyzerBlockEntity.SLOT_ELECTROLYZED,
                new ItemStack(Ic2Items.ELECTROLYZED_WATER_CELL.get()));
        // The cell turns into 14 000 EU (BatBox tier 1), handed back at 2 EU per tick.
        helper.succeedWhen(() -> {
            helper.assertTrue(electrolyzer.getInventory().get(ElectrolyzerBlockEntity.SLOT_WATER)
                    .is(Ic2Items.WATER_CELL.get()), "water cell back");
            helper.assertTrue(batBox.getEnergy() >= 10, "BatBox charging");
            helper.assertValueEqual(batBox.getEnergy() + electrolyzer.getEnergy(), 14000, "energy conserved");
            helper.assertBlockProperty(new BlockPos(2, 2, 2), MachineBlock.ACTIVE, true);
        });
    }
}
