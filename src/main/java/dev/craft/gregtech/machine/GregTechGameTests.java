package dev.craft.gregtech.machine;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the GT basic machine base and the plate bender (phase 6b.1). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GregTechGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GregTechGameTests() {
    }

    private static GtBasicMachineBlockEntity bender(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, GtBlocks.machine(GtMachine.PLATE_BENDING_MACHINE).get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void bendsIngotIntoPlate(GameTestHelper helper) {
        GtBasicMachineBlockEntity bender = bender(helper, new BlockPos(2, 1, 2));
        bender.toggleItemTransfer();
        bender.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_1, new ItemStack(Items.IRON_INGOT, 2));
        bender.setStoredEnergyForTest(1500);
        // Starts on tick 21 (needs > 900 EU), 50 ticks at 20 EU/t; the second ingot finds only 500 EU.
        helper.succeedWhen(() -> {
            ItemStack plate = bender.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_1);
            helper.assertTrue(plate.is(GtItems.IRON_PLATE.get()) && plate.getCount() == 1, "one iron plate");
            helper.assertValueEqual(bender.getStoredEU(), 500, "energy left");
            helper.assertValueEqual(bender.getInventory().get(GtBasicMachineBlockEntity.SLOT_INPUT_2).getCount(), 1,
                    "ingot moved to the second input");
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GtMachineBlock.ACTIVE, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GtMachineBlock.FRONT, GtMachineBlock.Front.NORTH);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void overclockerDoublesSpeed(GameTestHelper helper) {
        GtBasicMachineBlockEntity bender = bender(helper, new BlockPos(2, 1, 2));
        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack overclocker = new ItemStack(Ic2Items.OVERCLOCKER_UPGRADE.get());
        helper.assertTrue(bender.installUpgrade(player, overclocker), "overclocker installed");
        bender.toggleItemTransfer();
        bender.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_2, new ItemStack(Items.GOLD_INGOT));
        bender.setStoredEnergyForTest(2000);
        // Tick 21 starts, every tick +2 progress for 80 EU: done after 25 ticks with 2000 − 25 · 80 = 0 EU.
        helper.succeedWhen(() -> {
            ItemStack plate = bender.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_1);
            helper.assertTrue(plate.is(GtItems.GOLD_PLATE.get()), "gold plate");
            helper.assertValueEqual(bender.getStoredEU(), 0, "energy used");
        });
    }

    @GameTest(template = EMPTY)
    public static void transformerUpgradesStopAt512(GameTestHelper helper) {
        GtBasicMachineBlockEntity bender = bender(helper, new BlockPos(2, 1, 2));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack transformers = new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get(), 5);
        helper.assertTrue(bender.installUpgrade(player, transformers), "first transformer");
        helper.assertValueEqual(bender.getMaxSafeInput(), 128, "input after one");
        helper.assertTrue(bender.installUpgrade(player, transformers), "second transformer");
        helper.assertValueEqual(bender.getMaxSafeInput(), 512, "input after two");
        helper.assertFalse(bender.installUpgrade(player, transformers), "third needs the HV transformer upgrade");
        helper.assertValueEqual(transformers.getCount(), 3, "two used up");
        ItemStack storage = new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get());
        helper.assertTrue(bender.installUpgrade(player, storage), "battery upgrade");
        helper.assertValueEqual(bender.getCapacity(), 12000, "capacity");
        ItemStack drop = bender.getWrenchDrop(player);
        helper.assertTrue(drop.is(GtItems.machine(GtMachine.PLATE_BENDING_MACHINE).get()), "wrench drop is the machine");
        helper.assertValueEqual(drop.get(DataComponents.BLOCK_ENTITY_DATA).copyTag().getByte("mTransformers"),
                (byte) 2, "transformers kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void pushesOutputIntoChestBehind(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        GtBasicMachineBlockEntity bender = bender(helper, pos);
        // Facing north: the output side is the south face.
        helper.setBlock(pos.south(), Blocks.CHEST);
        bender.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_1, new ItemStack(Items.COPPER_INGOT));
        bender.setStoredEnergyForTest(2000);
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(pos.south());
            helper.assertTrue(chest.getItem(0).is(GtItems.COPPER_PLATE.get()), "plate pushed into the chest");
            helper.assertTrue(bender.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_1).isEmpty(),
                    "output empty");
            // 1000 EU for the recipe, 1 EU for the moved item.
            helper.assertValueEqual(bender.getStoredEU(), 999, "energy");
        });
    }

    @GameTest(template = EMPTY)
    public static void benderRecipesAndEnergyRules(GameTestHelper helper) {
        GtRecipe recipe = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BENDER,
                new ItemStack(Ic2Items.REFINED_IRON_INGOT.get()), ItemStack.EMPTY, false);
        helper.assertTrue(recipe != null && recipe.getOutput(0).is(GtItems.REFINED_IRON_PLATE.get())
                && recipe.getDuration() == 50 && recipe.getEuPerTick() == 20, "refined iron recipe");
        GtBasicMachineBlockEntity bender = bender(helper, new BlockPos(2, 1, 2));
        helper.assertFalse(bender.doEnergyExplosion(), "no explosion below a fifth of the capacity");
        helper.assertFalse(bender.acceptsEnergyFrom(bender, Direction.NORTH), "no input through the facing side");
        helper.assertTrue(bender.acceptsEnergyFrom(bender, Direction.SOUTH), "input from the back without output");
        bender.toggleOutput();
        helper.assertFalse(bender.acceptsEnergyFrom(bender, Direction.SOUTH), "back side emits with output on");
        helper.assertValueEqual(bender.getOutput(), 32, "output packet");
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "gt_overvoltage")
    public static void explodesOnOvervoltage(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        GtBasicMachineBlockEntity bender = bender(helper, pos);
        helper.assertValueEqual(bender.injectEnergy(Direction.EAST, 32), 0, "32 EU accepted");
        helper.assertValueEqual(bender.injectEnergy(Direction.EAST, 33), 0, "overvoltage");
        helper.assertBlockNotPresent(GtBlocks.machine(GtMachine.PLATE_BENDING_MACHINE).get(), pos);
        helper.succeed();
    }
}
