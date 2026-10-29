package dev.craft.compactsolars;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * In-world tests for solar power since 2026-10-24 (user): only solar panels, ×1.2 for panels in a complete 9×9 square,
 * arrays are leftovers. Panels are built on the floor (y 1); storage blocks replace the floor below a panel so they
 * do not shade it.
 */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CompactSolarsGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final String BATCH = "compact_solars";

    private CompactSolarsGameTests() {
    }

    private static Block panel() {
        return Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get();
    }

    /** Fills the {@code size}×{@code size} square with corner {@code corner} (relative) with panels. */
    private static void square(GameTestHelper helper, BlockPos corner, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(corner.offset(x, 0, z), panel());
            }
        }
    }

    private static boolean inField(GameTestHelper helper, BlockPos pos) {
        return SolarPanelBlockEntity.isInField(helper.getLevel(), helper.absolutePos(pos));
    }

    /** Clear noon, so parts with open sky see the sun. */
    private static void sunshine(GameTestHelper helper) {
        helper.getLevel().setDayTime(6000);
        helper.getLevel().setWeatherParameters(24000, 0, false, false);
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void fullSquareOfPanelsIsAField(GameTestHelper helper) {
        BlockPos corner = new BlockPos(1, 1, 1);
        square(helper, corner, 9);
        // a tenth column and a loose panel next to the square
        helper.setBlock(corner.offset(9, 0, 0), panel());
        helper.setBlock(new BlockPos(14, 1, 14), panel());
        helper.assertTrue(inField(helper, corner), "corner in field");
        helper.assertTrue(inField(helper, corner.offset(4, 0, 4)), "centre in field");
        helper.assertTrue(inField(helper, corner.offset(8, 0, 8)), "opposite corner in field");
        helper.assertFalse(inField(helper, corner.offset(9, 0, 0)), "tenth column without a full square");
        helper.assertFalse(inField(helper, new BlockPos(14, 1, 14)), "loose panel");
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void squareWithAGapIsNoField(GameTestHelper helper) {
        BlockPos corner = new BlockPos(1, 1, 1);
        square(helper, corner, 9);
        helper.destroyBlock(corner.offset(4, 0, 4));
        helper.assertFalse(inField(helper, corner), "corner");
        helper.assertFalse(inField(helper, corner.offset(8, 0, 8)), "opposite corner");
        // a 10×10 square stays a field around one missing corner
        BlockPos big = new BlockPos(1, 2, 1);
        square(helper, big, 10);
        helper.destroyBlock(big);
        helper.assertTrue(inField(helper, big.offset(9, 0, 9)), "10×10 without a corner");
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 120)
    public static void panelInFieldDeliversOneAndAFifth(GameTestHelper helper) {
        sunshine(helper);
        BlockPos corner = new BlockPos(1, 1, 1);
        square(helper, corner, 9);
        BlockPos fieldPanel = corner.offset(4, 0, 4);
        BlockPos loosePanel = new BlockPos(13, 1, 13);
        helper.setBlock(loosePanel, panel());
        helper.setBlock(fieldPanel.below(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.setBlock(loosePanel.below(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(10, () -> {
            SolarPanelBlockEntity panel = helper.getBlockEntity(fieldPanel);
            helper.assertTrue(panel.isInField(), "panel in field");
            helper.assertTrue(panel.isSunVisible(), "sun");
            ElectricStorageBlockEntity fieldBox = helper.getBlockEntity(fieldPanel.below());
            ElectricStorageBlockEntity looseBox = helper.getBlockEntity(loosePanel.below());
            int fieldBefore = fieldBox.getEnergy();
            int looseBefore = looseBox.getEnergy();
            helper.runAfterDelay(50, () -> {
                // spec 38: all 81 panels conduct into the one BatBox, each 60 EU over 50 ticks
                helper.assertValueEqual(fieldBox.getEnergy() - fieldBefore, 81 * 60, "field over 50 ticks");
                helper.assertValueEqual(looseBox.getEnergy() - looseBefore, 50, "loose panel over 50 ticks");
                helper.succeed();
            });
        });
    }

    /** Spec 38: panels pass the EU of their neighbours on, one cable at the end collects a whole row. */
    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 120)
    public static void panelsConductToOneCable(GameTestHelper helper) {
        sunshine(helper);
        for (int x = 1; x <= 5; x++) {
            helper.setBlock(new BlockPos(x, 1, 3), panel());
        }
        helper.setBlock(new BlockPos(6, 1, 3), Ic2Blocks.cable(CableType.TIN).get());
        BlockPos box = new BlockPos(7, 1, 3);
        helper.setBlock(box, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(10, () -> {
            ElectricStorageBlockEntity batbox = helper.getBlockEntity(box);
            int before = batbox.getEnergy();
            helper.runAfterDelay(50, () -> {
                helper.assertValueEqual(batbox.getEnergy() - before, 250, "five panels over 50 ticks");
                helper.succeed();
            });
        });
    }

    /** Spec 38: panels only accept energy from panels, so another source cannot route through a field. */
    @GameTest(template = EMPTY, batch = BATCH)
    public static void otherSourcesDoNotRouteThroughPanels(GameTestHelper helper) {
        BlockPos generator = new BlockPos(1, 1, 3);
        helper.setBlock(generator, Ic2Blocks.machine(Ic2Machine.GENERATOR).get());
        helper.setBlock(new BlockPos(2, 1, 3), panel());
        helper.setBlock(new BlockPos(3, 1, 3), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(2, () -> {
            EnergyTile source = helper.getBlockEntity(generator);
            EnergyTile batbox = helper.getBlockEntity(new BlockPos(3, 1, 3));
            List<EnergyTile> targets = EnergyNets.get(helper.getLevel()).discoverTargets(source, false, 32);
            helper.assertFalse(targets.contains(batbox), "generator reaches the BatBox through a panel");
            EnergyTile solar = helper.getBlockEntity(new BlockPos(2, 1, 3));
            helper.assertTrue(EnergyNets.get(helper.getLevel()).discoverTargets(solar, false, 2).contains(batbox),
                    "panel still feeds the BatBox");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void leftoverArrayDeliversNothing(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(3, 1, 3);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(centre.offset(x, 0, z), x == 0 && z == 0
                        ? CsBlocks.array(SolarArrayType.LV).get() : panel());
            }
        }
        helper.setBlock(centre.below(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(40, () -> {
            ElectricStorageBlockEntity batbox = helper.getBlockEntity(centre.below());
            helper.assertValueEqual(batbox.getEnergy(), 0, "BatBox under the array");
            SolarPanelBlockEntity ringPanel = helper.getBlockEntity(centre.east());
            helper.assertTrue(ringPanel.getStorage() > 0, "panels next to an array produce themselves");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void arraysComeApartIntoTheTierBelow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack panels = craft(level, new ItemStack(CsItems.array(SolarArrayType.LV).get()));
        helper.assertTrue(panels.is(Ic2Items.machine(Ic2Machine.SOLAR_PANEL).get()) && panels.getCount() == 8,
                "LV → 8 panels");
        ItemStack lv = craft(level, new ItemStack(CsItems.array(SolarArrayType.MV).get()));
        helper.assertTrue(lv.is(CsItems.array(SolarArrayType.LV).get()) && lv.getCount() == 8, "MV → 8 LV");
        ItemStack mv = craft(level, new ItemStack(CsItems.array(SolarArrayType.HV).get()));
        helper.assertTrue(mv.is(CsItems.array(SolarArrayType.MV).get()) && mv.getCount() == 8, "HV → 8 MV");
        ItemStack[] ring = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            ring[i] = new ItemStack(Ic2Items.machine(Ic2Machine.SOLAR_PANEL).get());
        }
        ring[4] = new ItemStack(Ic2Items.machine(Ic2Machine.LV_TRANSFORMER).get());
        helper.assertTrue(craft(level, 3, 3, List.of(ring)).isEmpty(), "no recipe for arrays");
        helper.succeed();
    }

    private static ItemStack craft(ServerLevel level, ItemStack single) {
        return craft(level, 1, 1, List.of(single));
    }

    private static ItemStack craft(ServerLevel level, int width, int height, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, height, items);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
