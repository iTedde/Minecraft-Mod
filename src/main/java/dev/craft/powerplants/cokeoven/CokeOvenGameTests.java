package dev.craft.powerplants.cokeoven;

import dev.craft.Craft;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests for the coke oven (phase 9.2, spec 14 §2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CokeOvenGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Controller in the middle of the north side, facing north; the cube spans x 2–4, y 1–3, z 1–3. */
    private static final BlockPos CONTROLLER = new BlockPos(3, 2, 1);
    private static final BlockPos CORE = new BlockPos(3, 2, 2);

    private CokeOvenGameTests() {
    }

    private static CokeOvenBlockEntity build(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(4, 3, 3))) {
            if (!pos.equals(CORE) && !pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.COKE_OVEN_BRICKS.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.COKE_OVEN.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    /** One coal becomes one coke after exactly 900 ticks. */
    @GameTest(template = EMPTY, timeoutTicks = 1000)
    public static void cokesCoalIn900Ticks(GameTestHelper helper) {
        CokeOvenBlockEntity oven = build(helper);
        oven.getInventory().set(CokeOvenBlockEntity.SLOT_INPUT, new ItemStack(Items.COAL, 2));
        helper.runAtTickTime(CokeOvenBlockEntity.OPERATION_LENGTH - 5, () -> {
            helper.assertTrue(oven.isFormed(), "structure formed");
            helper.assertTrue(oven.getInventory().get(CokeOvenBlockEntity.SLOT_OUTPUT).isEmpty(), "no coke yet");
        });
        helper.runAtTickTime(CokeOvenBlockEntity.OPERATION_LENGTH + 5, () -> {
            helper.assertTrue(oven.getInventory().get(CokeOvenBlockEntity.SLOT_OUTPUT)
                    .is(PpItems.COKE.get()), "coke produced");
            helper.assertValueEqual(oven.getInventory().get(CokeOvenBlockEntity.SLOT_OUTPUT).getCount(), 1, "coke");
            helper.assertValueEqual(oven.getInventory().get(CokeOvenBlockEntity.SLOT_INPUT).getCount(), 1, "coal");
            helper.succeed();
        });
    }

    /** A block in the core breaks the structure; the oven makes no progress. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void filledCoreStopsTheOven(GameTestHelper helper) {
        CokeOvenBlockEntity oven = build(helper);
        helper.setBlock(CORE, Blocks.STONE);
        oven.getInventory().set(CokeOvenBlockEntity.SLOT_INPUT, new ItemStack(Items.COAL));
        helper.runAtTickTime(40, () -> {
            helper.assertFalse(oven.isFormed(), "not formed");
            helper.assertValueEqual(oven.getProgress(), 0, "progress");
            helper.succeed();
        });
    }

    /** Through the bricks: coal goes in from the side, charcoal is refused, coke comes out at the bottom. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void bricksForwardItemAccess(GameTestHelper helper) {
        CokeOvenBlockEntity oven = build(helper);
        oven.getInventory().set(CokeOvenBlockEntity.SLOT_OUTPUT, new ItemStack(PpItems.COKE.get(), 3));
        helper.runAtTickTime(5, () -> {
            BlockPos side = helper.absolutePos(new BlockPos(4, 2, 2));
            BlockPos bottom = helper.absolutePos(new BlockPos(3, 1, 2));
            IItemHandler sideAccess = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, side,
                    Direction.EAST);
            IItemHandler bottomAccess = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, bottom,
                    Direction.DOWN);
            helper.assertTrue(sideAccess != null && bottomAccess != null, "brick capabilities");
            helper.assertValueEqual(sideAccess.insertItem(0, new ItemStack(Items.CHARCOAL), false).getCount(), 1,
                    "charcoal refused");
            helper.assertTrue(sideAccess.insertItem(0, new ItemStack(Items.COAL), false).isEmpty(), "coal taken");
            helper.assertValueEqual(bottomAccess.extractItem(0, 64, false).getCount(), 3, "coke extracted");
            helper.assertValueEqual(oven.getInventory().get(CokeOvenBlockEntity.SLOT_INPUT).getCount(), 1, "coal");
            helper.succeed();
        });
    }
}
