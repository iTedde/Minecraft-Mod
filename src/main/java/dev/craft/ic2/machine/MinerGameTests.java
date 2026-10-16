package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the IC2 miner (phase 12.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MinerGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final BlockPos MINER = new BlockPos(7, 6, 7);
    private static final BlockPos CHEST = MINER.east();

    private MinerGameTests() {
    }

    private static MinerBlockEntity place(GameTestHelper helper, Item drill) {
        helper.setBlock(MINER, Ic2Blocks.machine(Ic2Machine.MINER).get());
        helper.setBlock(CHEST, Blocks.CHEST);
        MinerBlockEntity miner = helper.getBlockEntity(MINER);
        if (drill != null) {
            miner.getInventory().set(MinerBlockEntity.SLOT_DRILL, ElectricItems.charged(new ItemStack(drill)));
        }
        helper.onEachTick(() -> miner.setEnergyForTest(1000));
        return miner;
    }

    private static int count(GameTestHelper helper, Item item) {
        ChestBlockEntity chest = helper.getBlockEntity(CHEST);
        int count = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            if (chest.getItem(i).is(item)) {
                count += chest.getItem(i).getCount();
            }
        }
        return count;
    }

    /**
     * The diamond drill mines a block every 50 ticks. Drops go to the chest; the tip follows down and only the pipe
     * above the tip costs an item (original quirk: the tip is free).
     */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void minesDownWithPipe(GameTestHelper helper) {
        helper.setBlock(MINER.below(), Blocks.STONE);
        helper.setBlock(MINER.below(2), Blocks.STONE);
        MinerBlockEntity miner = place(helper, Ic2Items.DIAMOND_DRILL.get());
        miner.getInventory().set(MinerBlockEntity.SLOT_PIPE, new ItemStack(Ic2Items.MINING_PIPE.get(), 10));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Ic2Blocks.MINING_PIPE.get(), MINER.below());
            helper.assertBlockPresent(Ic2Blocks.MINING_TIP.get(), MINER.below(2));
            helper.assertValueEqual(count(helper, Items.COBBLESTONE), 2, "cobblestone in chest");
            helper.assertValueEqual(miner.getInventory().get(MinerBlockEntity.SLOT_PIPE).getCount(), 9, "pipes left");
        });
    }

    /** Without a drill the tip is pulled up every 20 ticks; a block in the pipe slot fills the hole. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void withdrawsPipeAndFillsHole(GameTestHelper helper) {
        helper.setBlock(MINER.below(), Ic2Blocks.MINING_PIPE.get());
        helper.setBlock(MINER.below(2), Ic2Blocks.MINING_TIP.get());
        MinerBlockEntity miner = place(helper, null);
        miner.getInventory().set(MinerBlockEntity.SLOT_PIPE, new ItemStack(Items.DIRT, 2));
        helper.runAfterDelay(25, () -> {
            helper.assertBlockPresent(Blocks.DIRT, MINER.below(2));
            helper.assertBlockPresent(Ic2Blocks.MINING_TIP.get(), MINER.below());
        });
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.DIRT, MINER.below());
            helper.assertValueEqual(count(helper, Ic2Items.MINING_PIPE.get()), 2, "pipes returned");
            helper.assertTrue(miner.getInventory().get(MinerBlockEntity.SLOT_PIPE).isEmpty(), "dirt used");
        });
    }

    /** The mining drill (harvest level 2) cannot mine obsidian: the miner gets stuck and stops. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void miningDrillStuckOnObsidian(GameTestHelper helper) {
        helper.setBlock(MINER.below(), Blocks.OBSIDIAN);
        MinerBlockEntity miner = place(helper, Ic2Items.MINING_DRILL.get());
        miner.getInventory().set(MinerBlockEntity.SLOT_PIPE, new ItemStack(Ic2Items.MINING_PIPE.get(), 4));
        helper.succeedWhen(() -> {
            helper.assertTrue(miner.isStuck(), "stuck");
            helper.assertFalse(miner.isOperating(), "stopped");
            helper.assertBlockPresent(Blocks.OBSIDIAN, MINER.below());
        });
    }

    /** With a scanner the miner picks the first valuable block of the tip's layer and keeps its tip there. */
    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void scannerFindsOreOnTipLayer(GameTestHelper helper) {
        BlockPos tip = MINER.below();
        BlockPos ore = tip.west(2);
        helper.setBlock(tip, Ic2Blocks.MINING_TIP.get());
        helper.setBlock(ore, Blocks.IRON_ORE);
        helper.setBlock(tip.below(), Blocks.STONE);
        MinerBlockEntity miner = place(helper, Ic2Items.DIAMOND_DRILL.get());
        miner.getInventory().set(MinerBlockEntity.SLOT_PIPE, new ItemStack(Ic2Items.MINING_PIPE.get(), 4));
        miner.getInventory().set(MinerBlockEntity.SLOT_SCANNER,
                ElectricItems.charged(new ItemStack(Ic2Items.OD_SCANNER.get())));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.AIR, ore);
            helper.assertValueEqual(count(helper, Items.RAW_IRON), 1, "raw iron");
            helper.assertBlockPresent(Ic2Blocks.MINING_TIP.get(), tip);
            helper.assertBlockPresent(Blocks.STONE, tip.below());
        });
    }
}
