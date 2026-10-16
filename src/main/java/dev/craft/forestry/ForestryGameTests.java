package dev.craft.forestry;

import java.util.List;

import dev.craft.Craft;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.circuit.CircuitBoard;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.CircuitLayout;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.machine.ErrorState;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import dev.craft.forestry.menu.SolderingIronMenu;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the Thermionic Fabricator, the Carpenter and the soldering iron. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ForestryGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos MACHINE = new BlockPos(2, 1, 2);

    private ForestryGameTests() {
    }

    /** Copper tube pattern " X /#X#/XXX" in the fabricator grid. */
    private static void copperTubePattern(FabricatorBlockEntity fabricator) {
        String[] pattern = {" X ", "#X#", "XXX"};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                char c = pattern[row].charAt(column);
                ItemStack stack = c == 'X' ? new ItemStack(Items.COPPER_INGOT)
                        : c == '#' ? new ItemStack(Items.REDSTONE) : ItemStack.EMPTY;
                fabricator.getInventory().setStackInSlot(FabricatorBlockEntity.SLOT_CRAFTING_1 + column + row * 3,
                        stack);
            }
        }
    }

    @GameTest(template = EMPTY, timeoutTicks = 800)
    public static void fabricatorHeatsMeltsGlassAndCraftsTubes(GameTestHelper helper) {
        helper.setBlock(MACHINE, FrBlocks.THERMIONIC_FABRICATOR.get());
        FabricatorBlockEntity fabricator = helper.getBlockEntity(MACHINE);
        fabricator.getInventory().setStackInSlot(FabricatorBlockEntity.SLOT_METAL, new ItemStack(Items.GLASS, 2));
        copperTubePattern(fabricator);
        fabricator.getInventory().setStackInSlot(FabricatorBlockEntity.SLOT_INVENTORY_1,
                new ItemStack(Items.COPPER_INGOT, 5));
        fabricator.getInventory().setStackInSlot(FabricatorBlockEntity.SLOT_INVENTORY_1 + 1,
                new ItemStack(Items.REDSTONE, 2));
        helper.onEachTick(() -> fabricator.getPowerProvider().setEnergyStored(250.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(fabricator.getHeat() >= 1000, "heat reached the melting point of glass");
            helper.assertTrue(fabricator.getMoltenTank().getFluidAmount() >= 500, "glass molten");
            ItemStack result = fabricator.getResult();
            helper.assertTrue(result.is(FrItems.tube(ElectronTube.COPPER).get()) && result.getCount() == 4,
                    "four copper tubes offered");
            int before = fabricator.getMoltenTank().getFluidAmount();
            ItemStack taken = fabricator.takeResult(false);
            helper.assertValueEqual(taken.getCount(), 4, "tubes taken from the stock");
            helper.assertValueEqual(fabricator.getMoltenTank().getFluidAmount(), before - 500, "500 mB used");
            helper.assertTrue(fabricator.getInventory().getStackInSlot(FabricatorBlockEntity.SLOT_INVENTORY_1)
                    .isEmpty(), "five copper ingots used");
            helper.assertTrue(fabricator.getInventory().getStackInSlot(FabricatorBlockEntity.SLOT_INVENTORY_1 + 1)
                    .isEmpty(), "two redstone used");
            helper.assertValueEqual(fabricator.getInventory().getStackInSlot(FabricatorBlockEntity.SLOT_CRAFTING_1 + 1)
                    .getCount(), 1, "grid untouched");
        });
    }

    @GameTest(template = EMPTY)
    public static void fabricatorGlassSolidifiesWhenCold(GameTestHelper helper) {
        helper.setBlock(MACHINE, FrBlocks.THERMIONIC_FABRICATOR.get());
        FabricatorBlockEntity fabricator = helper.getBlockEntity(MACHINE);
        fabricator.getMoltenTank().fill(new FluidStack(FrFluids.LIQUID_GLASS.get(), 1000),
                IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(fabricator.getMoltenTank().getFluidAmount(), 950, "5 mB lost per tick");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void fabricatorWithoutStockUsesTheGrid(GameTestHelper helper) {
        helper.setBlock(MACHINE, FrBlocks.THERMIONIC_FABRICATOR.get());
        FabricatorBlockEntity fabricator = helper.getBlockEntity(MACHINE);
        copperTubePattern(fabricator);
        fabricator.getMoltenTank().fill(new FluidStack(FrFluids.LIQUID_GLASS.get(), 1000),
                IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(fabricator.takeResult(false).isEmpty(), "automation needs the stock");
        ItemStack taken = fabricator.takeResult(true);
        helper.assertTrue(taken.is(FrItems.tube(ElectronTube.COPPER).get()), "crafted from the grid");
        helper.assertTrue(fabricator.getInventory().getStackInSlot(FabricatorBlockEntity.SLOT_CRAFTING_1 + 1)
                .isEmpty(), "grid consumed");
        helper.assertValueEqual(fabricator.getMoltenTank().getFluidAmount(), 500, "500 mB used");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void carpenterFillsWaterAndMakesBasicBoard(GameTestHelper helper) {
        helper.setBlock(MACHINE, FrBlocks.CARPENTER.get());
        CarpenterBlockEntity carpenter = helper.getBlockEntity(MACHINE);
        basicBoardPattern(carpenter);
        carpenter.getInventory().setStackInSlot(CarpenterBlockEntity.SLOT_INVENTORY_1, new ItemStack(Items.REDSTONE, 6));
        carpenter.getInventory().setStackInSlot(CarpenterBlockEntity.SLOT_INVENTORY_1 + 1,
                new ItemStack(Ic2Items.TIN_INGOT.get()));
        carpenter.getInventory().setStackInSlot(CarpenterBlockEntity.SLOT_CAN_INPUT, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue(carpenter.getFluidHandler().drain(1, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "nothing can be drained");
        helper.onEachTick(() -> carpenter.getPowerProvider().setEnergyStored(400.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(carpenter.getInventory().getStackInSlot(CarpenterBlockEntity.SLOT_CAN_INPUT)
                    .is(Items.BUCKET), "bucket emptied");
            ItemStack product = carpenter.getInventory().getStackInSlot(CarpenterBlockEntity.SLOT_PRODUCT);
            helper.assertTrue(product.is(FrItems.circuitBoard(CircuitBoardType.BASIC).get()), "basic board made");
            helper.assertValueEqual(carpenter.getResourceTank().getFluidAmount(), 0, "1000 mB water used");
            helper.assertTrue(carpenter.getInventory().getStackInSlot(CarpenterBlockEntity.SLOT_INVENTORY_1).isEmpty()
                    && carpenter.getInventory().getStackInSlot(CarpenterBlockEntity.SLOT_INVENTORY_1 + 1).isEmpty(),
                    "stock used");
            helper.assertValueEqual(carpenter.getInventory().getStackInSlot(CarpenterBlockEntity.SLOT_CRAFTING_1)
                    .getCount(), 1, "pattern kept");
        });
    }

    /** Checked every 40 ticks: no recipe, then missing resources once pattern and liquid name a recipe. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void carpenterReportsRecipeAndResourceErrors(GameTestHelper helper) {
        helper.setBlock(MACHINE, FrBlocks.CARPENTER.get());
        CarpenterBlockEntity carpenter = helper.getBlockEntity(MACHINE);
        helper.runAfterDelay(45, () -> {
            helper.assertValueEqual(carpenter.getErrorState(), ErrorState.NO_RECIPE, "empty pattern");
            basicBoardPattern(carpenter);
            carpenter.getFluidHandler().fill(new FluidStack(Fluids.WATER, 100), IFluidHandler.FluidAction.EXECUTE);
            carpenter.updateRecipe();
        });
        helper.runAfterDelay(90, () -> {
            helper.assertValueEqual(carpenter.getErrorState(), ErrorState.NO_RESOURCE, "no stock");
            helper.succeed();
        });
    }

    /** Basic circuit board pattern "R R/R#R/R R" in the carpenter grid. */
    private static void basicBoardPattern(CarpenterBlockEntity carpenter) {
        String[] pattern = {"R R", "R#R", "R R"};
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                char c = pattern[row].charAt(column);
                ItemStack stack = c == 'R' ? new ItemStack(Items.REDSTONE)
                        : c == '#' ? new ItemStack(Ic2Items.TIN_INGOT.get()) : ItemStack.EMPTY;
                carpenter.getInventory().setStackInSlot(CarpenterBlockEntity.SLOT_CRAFTING_1 + column + row * 3,
                        stack);
            }
        }
    }

    @GameTest(template = EMPTY)
    public static void solderingIronSoldersOnlyFullBoards(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        SolderingIronMenu menu = new SolderingIronMenu(0, player.getInventory());
        Container inventory = menu.getSolderingInventory();
        inventory.setItem(SolderingIronMenu.SLOT_INGREDIENT_1, new ItemStack(FrItems.tube(ElectronTube.TIN).get(), 3));
        inventory.setItem(SolderingIronMenu.SLOT_BLANK,
                new ItemStack(FrItems.circuitBoard(CircuitBoardType.BASIC).get()));
        ItemStack finished = inventory.getItem(SolderingIronMenu.SLOT_FINISHED);
        CircuitBoard board = CircuitBoardItem.getCircuitBoard(finished);
        helper.assertTrue(board != null && board.layout() == CircuitLayout.ENGINE_TIN
                && board.circuits().equals(List.of(Circuit.ELECTRIC_BOOST_1)), "boost I soldered");
        helper.assertValueEqual(inventory.getItem(SolderingIronMenu.SLOT_INGREDIENT_1).getCount(), 2, "one tube used");
        helper.assertTrue(inventory.getItem(SolderingIronMenu.SLOT_BLANK).isEmpty(), "blank board used");

        // Two copper tubes on an enhanced board: the choke limit (1) leaves one socket empty → nothing happens.
        inventory.setItem(SolderingIronMenu.SLOT_FINISHED, ItemStack.EMPTY);
        inventory.setItem(SolderingIronMenu.SLOT_INGREDIENT_1, new ItemStack(FrItems.tube(ElectronTube.COPPER).get()));
        inventory.setItem(SolderingIronMenu.SLOT_INGREDIENT_1 + 1,
                new ItemStack(FrItems.tube(ElectronTube.COPPER).get()));
        inventory.setItem(SolderingIronMenu.SLOT_BLANK,
                new ItemStack(FrItems.circuitBoard(CircuitBoardType.ENHANCED).get()));
        helper.assertTrue(inventory.getItem(SolderingIronMenu.SLOT_FINISHED).isEmpty(), "limit prevents soldering");

        menu.removed(player);
        helper.assertTrue(inventory.isEmpty(), "everything dropped on close");
        helper.succeed();
    }
}
