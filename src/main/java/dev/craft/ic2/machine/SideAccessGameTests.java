package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.advancedmachines.AdvancedMachineType;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.machine.VacuumFreezerBlockEntity;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** Machine sides that were output-only in the original sort inserted items and never fill an output slot. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class SideAccessGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private SideAccessGameTests() {
    }

    private static <T extends BlockEntity> T place(GameTestHelper helper, Ic2Machine machine) {
        helper.setBlock(POS, Ic2Blocks.machine(machine).get());
        return helper.getBlockEntity(POS);
    }

    /** The item capability of the machine side {@code side}, as a pipe or hopper sees it. */
    private static IItemHandler side(GameTestHelper helper, Direction side) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(POS), side);
    }

    /** Inserts like a pipe or hopper (first slot that takes it); true if everything went in. */
    private static boolean insert(IItemHandler handler, ItemStack stack) {
        return ItemHandlerHelper.insertItemStacked(handler, stack, false).isEmpty();
    }

    @GameTest(template = EMPTY)
    public static void recyclerSideSortsAndKeepsOutputFree(GameTestHelper helper) {
        RecyclerBlockEntity recycler = place(helper, Ic2Machine.RECYCLER);
        IItemHandler east = side(helper, Direction.EAST);
        helper.assertTrue(insert(east, new ItemStack(Items.COBBLESTONE, 10)), "cobblestone accepted");
        helper.assertTrue(insert(east, new ItemStack(Ic2Items.RE_BATTERY.get())), "battery accepted");
        helper.assertFalse(insert(east, new ItemStack(Items.REDSTONE)), "battery slot taken, not recycled instead");
        MachineInventory inventory = recycler.getInventory();
        helper.assertTrue(inventory.get(ElectricMachineBlockEntity.SLOT_INPUT).is(Items.COBBLESTONE), "input");
        helper.assertTrue(inventory.get(ElectricMachineBlockEntity.SLOT_BATTERY).is(Ic2Items.RE_BATTERY.get()),
                "battery slot");
        helper.assertTrue(inventory.isEmpty(ElectricMachineBlockEntity.SLOT_OUTPUT), "output stays empty");
        inventory.set(ElectricMachineBlockEntity.SLOT_OUTPUT, new ItemStack(Ic2Items.SCRAP.get(), 3));
        ItemStack taken = ItemStack.EMPTY;
        for (int i = 0; i < east.getSlots() && taken.isEmpty(); i++) {
            taken = east.extractItem(i, 64, false);
        }
        helper.assertTrue(taken.is(Ic2Items.SCRAP.get()) && taken.getCount() == 3, "only the output comes out");
        helper.assertTrue(inventory.get(ElectricMachineBlockEntity.SLOT_INPUT).getCount() == 10, "input kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void maceratorSideRejectsItemsWithoutRecipe(GameTestHelper helper) {
        ElectricMachineBlockEntity macerator = place(helper, Ic2Machine.MACERATOR);
        IItemHandler west = side(helper, Direction.WEST);
        helper.assertFalse(insert(west, new ItemStack(Items.DIRT)), "dirt rejected");
        helper.assertTrue(insert(west, new ItemStack(Items.IRON_ORE)), "iron ore accepted");
        helper.assertTrue(macerator.getInventory().get(ElectricMachineBlockEntity.SLOT_INPUT).is(Items.IRON_ORE),
                "ore in input");
        IItemHandler top = side(helper, Direction.UP);
        helper.assertTrue(top.getSlots() == 1, "top unchanged: input slot only");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void ironFurnaceSideSortsFuelAndOre(GameTestHelper helper) {
        IronFurnaceBlockEntity furnace = place(helper, Ic2Machine.IRON_FURNACE);
        IItemHandler north = side(helper, Direction.NORTH);
        helper.assertTrue(insert(north, new ItemStack(Items.COAL, 4)), "coal accepted");
        helper.assertTrue(insert(north, new ItemStack(Items.RAW_IRON, 4)), "raw iron accepted");
        helper.assertTrue(furnace.getInventory().get(0).is(Items.RAW_IRON), "raw iron to input");
        helper.assertTrue(furnace.getInventory().get(1).is(Items.COAL), "coal to fuel");
        helper.assertTrue(furnace.getInventory().isEmpty(2), "output stays empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void inductionFurnaceSideFillsBothInputs(GameTestHelper helper) {
        MachineBlockEntity furnace = place(helper, Ic2Machine.INDUCTION_FURNACE);
        IItemHandler south = side(helper, Direction.SOUTH);
        helper.assertTrue(insert(south, new ItemStack(Items.SAND, 64)), "sand accepted");
        helper.assertTrue(insert(south, new ItemStack(Items.SAND, 10)), "more sand accepted");
        helper.assertTrue(insert(south, new ItemStack(Items.REDSTONE)), "redstone accepted");
        MachineInventory inventory = furnace.getInventory();
        helper.assertTrue(inventory.get(InductionFurnaceBlockEntity.SLOT_INPUT_1).getCount() == 64
                && inventory.get(InductionFurnaceBlockEntity.SLOT_INPUT_2).getCount() == 10, "both inputs");
        helper.assertTrue(inventory.get(InductionFurnaceBlockEntity.SLOT_BATTERY).is(Items.REDSTONE), "battery");
        helper.assertTrue(inventory.isEmpty(InductionFurnaceBlockEntity.SLOT_OUTPUT_1)
                && inventory.isEmpty(InductionFurnaceBlockEntity.SLOT_OUTPUT_2), "outputs stay empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void massFabricatorTopTakesAmplifier(GameTestHelper helper) {
        MassFabricatorBlockEntity fabricator = place(helper, Ic2Machine.MASS_FABRICATOR);
        IItemHandler top = side(helper, Direction.UP);
        helper.assertFalse(insert(top, new ItemStack(Items.DIRT)), "dirt rejected");
        helper.assertTrue(insert(top, new ItemStack(Ic2Items.SCRAP.get(), 5)), "scrap accepted");
        helper.assertTrue(fabricator.getInventory().get(MassFabricatorBlockEntity.SLOT_AMPLIFIER)
                .is(Ic2Items.SCRAP.get()), "scrap to amplifier slot");
        helper.assertTrue(fabricator.getInventory().isEmpty(MassFabricatorBlockEntity.SLOT_OUTPUT), "output empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rotaryMaceratorSideSorts(GameTestHelper helper) {
        MachineBlockEntity machine = place(helper, Ic2Machine.ROTARY_MACERATOR);
        IItemHandler east = side(helper, Direction.EAST);
        helper.assertTrue(insert(east, new ItemStack(Items.IRON_ORE)), "ore accepted");
        helper.assertTrue(insert(east, new ItemStack(Ic2Items.RE_BATTERY.get())), "battery accepted");
        helper.assertFalse(insert(east, new ItemStack(Items.DIRT)), "dirt rejected");
        helper.assertTrue(machine.getInventory().get(AdvancedMachineType.SLOT_INPUT).is(Items.IRON_ORE), "input");
        helper.assertTrue(machine.getInventory().get(AdvancedMachineType.SLOT_ENERGY).is(Ic2Items.RE_BATTERY.get()),
                "energy slot");
        helper.assertTrue(machine.getInventory().isEmpty(2) && machine.getInventory().isEmpty(3), "outputs empty");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void vacuumFreezerSideTakesRecipeInput(GameTestHelper helper) {
        helper.setBlock(POS, GtBlocks.machine(GtMachine.VACUUM_FREEZER).get());
        VacuumFreezerBlockEntity freezer = helper.getBlockEntity(POS);
        IItemHandler west = side(helper, Direction.WEST);
        helper.assertFalse(insert(west, new ItemStack(Items.DIRT)), "dirt rejected");
        helper.assertTrue(insert(west, new ItemStack(Ic2Items.WATER_CELL.get())), "water cell accepted");
        helper.assertTrue(freezer.getInventory().get(VacuumFreezerBlockEntity.SLOT_INPUT)
                .is(Ic2Items.WATER_CELL.get()), "water cell to input");
        helper.assertTrue(freezer.getInventory().isEmpty(VacuumFreezerBlockEntity.SLOT_OUTPUT), "output empty");
        helper.succeed();
    }
}
