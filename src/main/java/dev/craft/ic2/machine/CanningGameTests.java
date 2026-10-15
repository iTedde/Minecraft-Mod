package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.machine.CannerBlockEntity;
import dev.craft.gregtech.machine.GtBasicMachineBlockEntity;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.FilledFuelCanItem;
import dev.craft.ic2.item.FilledTinCanItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the IC2 canning machine, the GT automatic canning machine and the cans (phase 6b.5a). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CanningGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private CanningGameTests() {
    }

    private static CanningMachineBlockEntity canner(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.CANNING_MACHINE).get());
        return helper.getBlockEntity(POS);
    }

    /** Rotten flesh (food 2) takes 100 ticks and two cans and gives two spoiled cans (meta 1). */
    @GameTest(template = EMPTY, timeoutTicks = 130)
    public static void cansRottenFlesh(GameTestHelper helper) {
        CanningMachineBlockEntity canner = canner(helper);
        canner.setEnergyForTest(CanningMachineBlockEntity.MAX_ENERGY);
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.ROTTEN_FLESH, 2));
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_CONTAINER, new ItemStack(Ic2Items.TIN_CAN.get(), 3));
        helper.succeedWhen(() -> {
            ItemStack output = canner.getInventory().get(CanningMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(Ic2Items.FILLED_TIN_CAN.get()) && output.getCount() == 2, "two filled cans");
            helper.assertValueEqual(FilledTinCanItem.getMeta(output), 1, "meta");
            helper.assertValueEqual(canner.getInventory().get(CanningMachineBlockEntity.SLOT_CONTAINER).getCount(), 1,
                    "cans left");
            // One cans left is too few for the second rotten flesh, so the machine stops.
            helper.assertValueEqual(canner.getProgress(), 0, "progress reset");
        });
    }

    /** The last mushroom stew leaves its bowl in the input slot. */
    @GameTest(template = EMPTY, timeoutTicks = 180)
    public static void mushroomStewLeavesBowl(GameTestHelper helper) {
        CanningMachineBlockEntity canner = canner(helper);
        canner.setEnergyForTest(CanningMachineBlockEntity.MAX_ENERGY);
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.MUSHROOM_STEW));
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_CONTAINER, new ItemStack(Ic2Items.TIN_CAN.get(), 3));
        helper.succeedWhen(() -> {
            helper.assertValueEqual(canner.getInventory().get(CanningMachineBlockEntity.SLOT_OUTPUT).getCount(), 3,
                    "filled cans");
            helper.assertTrue(canner.getInventory().get(CanningMachineBlockEntity.SLOT_INPUT).is(Items.BOWL), "bowl");
        });
    }

    /** Six coalfuel cells fill an empty fuel can with 6 · 2548 in 600 ticks; the can burns 30 576 ticks. */
    @GameTest(template = EMPTY, timeoutTicks = 640)
    public static void fillsFuelCan(GameTestHelper helper) {
        CanningMachineBlockEntity canner = canner(helper);
        canner.setEnergyForTest(CanningMachineBlockEntity.MAX_ENERGY);
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_INPUT,
                new ItemStack(Ic2Items.COALFUEL_CELL.get(), 6));
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_CONTAINER,
                new ItemStack(Ic2Items.EMPTY_FUEL_CAN.get()));
        helper.succeedWhen(() -> {
            ItemStack can = canner.getInventory().get(CanningMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(can.is(Ic2Items.FILLED_FUEL_CAN.get()), "filled fuel can");
            helper.assertValueEqual(FilledFuelCanItem.getValue(can), 6 * CanningMachineBlockEntity.COALFUEL_VALUE,
                    "fuel value");
            helper.assertValueEqual(can.getBurnTime(RecipeType.SMELTING), 30576, "burn time");
            helper.assertTrue(can.getCraftingRemainingItem().is(Ic2Items.EMPTY_FUEL_CAN.get()), "empty can stays");
            helper.assertTrue(canner.getInventory().get(CanningMachineBlockEntity.SLOT_CONTAINER).isEmpty(),
                    "empty can used");
            helper.assertValueEqual(canner.getFuelQuality(), 0, "quality reset");
        });
    }

    /** Six biofuel cells refuel an empty jetpack by 6 · 868. */
    @GameTest(template = EMPTY, timeoutTicks = 640)
    public static void refuelsJetpack(GameTestHelper helper) {
        CanningMachineBlockEntity canner = canner(helper);
        canner.setEnergyForTest(CanningMachineBlockEntity.MAX_ENERGY);
        ItemStack jetpack = new ItemStack(Ic2Items.JETPACK.get());
        jetpack.setDamageValue(18001);
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_INPUT, new ItemStack(Ic2Items.BIOFUEL_CELL.get(), 6));
        canner.getInventory().set(CanningMachineBlockEntity.SLOT_CONTAINER, jetpack);
        helper.succeedWhen(() -> {
            ItemStack output = canner.getInventory().get(CanningMachineBlockEntity.SLOT_OUTPUT);
            helper.assertTrue(output.is(Ic2Items.JETPACK.get()), "jetpack");
            helper.assertValueEqual(output.getDamageValue(), 18001 - 6 * CanningMachineBlockEntity.BIOFUEL_VALUE,
                    "damage");
        });
    }

    /** Food values match the 1.4.7 table ({@code ceil(heal / 2)}, cake 6, filled cans 0). */
    @GameTest(template = EMPTY)
    public static void foodValuesMatchPack(GameTestHelper helper) {
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(new ItemStack(Items.CARROT)), 2, "carrot");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(new ItemStack(Items.BAKED_POTATO)), 3,
                "baked potato");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(new ItemStack(Items.COOKED_BEEF)), 4, "steak");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(new ItemStack(Items.POTATO)), 1, "potato");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(new ItemStack(Items.CAKE)), 6, "cake");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodValue(FilledTinCanItem.create(0, 1)), 0, "can");
        helper.assertValueEqual(CanningMachineBlockEntity.getFoodMeta(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)), 5,
                "enchanted apple");
        helper.succeed();
    }

    /** Eating a spider-eye can poisons, heals and returns the empty can. */
    @GameTest(template = EMPTY)
    public static void eatingCanReturnsEmptyCan(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10);
        player.setHealth(10.0F);
        ItemStack can = FilledTinCanItem.create(2, 1);
        ItemStack rest = can.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(rest.isEmpty(), "can eaten");
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 12, "food");
        helper.assertTrue(player.getHealth() >= 12.0F, "healed");
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "poison");
        helper.assertTrue(player.getInventory().contains(new ItemStack(Ic2Items.TIN_CAN.get())), "empty can");
        helper.succeed();
    }

    /** GT canner, shapeless: the water bucket in slot 2 and the cell in slot 1 give a water cell and the bucket. */
    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void gtCannerFillsWaterCell(GameTestHelper helper) {
        helper.setBlock(POS, GtBlocks.machine(GtMachine.AUTOMATIC_CANNING_MACHINE).get());
        CannerBlockEntity canner = helper.getBlockEntity(POS);
        canner.toggleItemTransfer();
        canner.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_1, new ItemStack(Ic2Items.EMPTY_CELL.get()));
        canner.getInventory().set(GtBasicMachineBlockEntity.SLOT_INPUT_2, new ItemStack(Items.WATER_BUCKET));
        canner.setStoredEnergyForTest(1000);
        helper.succeedWhen(() -> {
            ItemStack cell = canner.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_1);
            ItemStack bucket = canner.getInventory().get(GtBasicMachineBlockEntity.SLOT_OUTPUT_2);
            helper.assertTrue(cell.is(Ic2Items.WATER_CELL.get()) || bucket.is(Ic2Items.WATER_CELL.get()), "water cell");
            helper.assertTrue(cell.is(Items.BUCKET) || bucket.is(Items.BUCKET), "bucket");
        });
    }
}
