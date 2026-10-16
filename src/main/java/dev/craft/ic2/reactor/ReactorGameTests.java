package dev.craft.ic2.reactor;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.reactor.HeatStorageItem;
import dev.craft.ic2.recipe.GradualRepairRecipe;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the nuclear reactor and its components (spec 06, phase 7a). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ReactorGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private ReactorGameTests() {
    }

    /** Reactor with a redstone block below (uranium only works with a signal). */
    private static NuclearReactorBlockEntity reactor(GameTestHelper helper, boolean powered) {
        if (powered) {
            helper.setBlock(POS.below(), Blocks.REDSTONE_BLOCK);
        }
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        return helper.getBlockEntity(POS);
    }

    private static ItemStack stack(net.minecraft.world.level.ItemLike item) {
        return new ItemStack(item);
    }

    /** One cell alone: 1 pulse = output 1 (5 EU/t), hull heat sumUp(1)·4 = 4, one damage step. */
    @GameTest(template = EMPTY)
    public static void singleCell(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 1, "output");
        helper.assertValueEqual(reactor.getHeat(), 4, "hull heat");
        helper.assertValueEqual(reactor.getItemAt(0, 0).getDamageValue(), 1, "damage");
        helper.succeed();
    }

    /** Without a redstone signal uranium does nothing at all. */
    @GameTest(template = EMPTY)
    public static void noRedstoneNoOutput(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, false);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 0, "output");
        helper.assertValueEqual(reactor.getHeat(), 0, "hull heat");
        helper.assertValueEqual(reactor.getItemAt(0, 0).getDamageValue(), 0, "damage");
        helper.succeed();
    }

    /** Two neighbouring cells: 2 pulses each = output 4, heat 2 · sumUp(2) · 4 = 24. */
    @GameTest(template = EMPTY)
    public static void twoCells(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 4, "output");
        helper.assertValueEqual(reactor.getHeat(), 24, "hull heat");
        helper.succeed();
    }

    /** Quad cell: 4 iterations of 3 self pulses = output 12, heat 4 · sumUp(3) · 4 = 96. */
    @GameTest(template = EMPTY)
    public static void quadCell(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(1, 1, stack(Ic2Items.QUAD_URANIUM_CELL.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 12, "output");
        helper.assertValueEqual(reactor.getHeat(), 96, "hull heat");
        helper.succeed();
    }

    /** A reflector reflects the pulse: output +1 from the reflector, heat from 2 pulses (12), reflector worn. */
    @GameTest(template = EMPTY)
    public static void reflector(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, stack(Ic2Items.NEUTRON_REFLECTOR.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 2, "output");
        helper.assertValueEqual(reactor.getHeat(), 12, "hull heat");
        helper.assertValueEqual(Ic2Items.NEUTRON_REFLECTOR.get().getDamageOfStack(reactor.getItemAt(1, 0)), 1,
                "reflector wear");
        helper.succeed();
    }

    /** The cell's heat goes into the neighbouring coolant cell instead of the hull. */
    @GameTest(template = EMPTY)
    public static void coolantCellTakesHeat(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, stack(Ic2Items.COOLANT_CELL_10K.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getHeat(), 0, "hull heat");
        helper.assertValueEqual(HeatStorageItem.getHeatOfStack(reactor.getItemAt(1, 0)), 4, "coolant heat");
        helper.succeed();
    }

    /** An overheated coolant cell is destroyed and the negative rest lowers the heat the hull gets. */
    @GameTest(template = EMPTY)
    public static void coolantCellBurnsUp(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        ItemStack coolant = stack(Ic2Items.COOLANT_CELL_10K.get());
        Ic2Items.COOLANT_CELL_10K.get().setHeatForStack(coolant, 9998);
        reactor.setItemAt(1, 0, coolant);
        reactor.runCycle();
        // 4 heat: 9998 + 4 > 10000 → destroyed, rest 10000 − 10002 + 1 = −1 → remaining heat −1, nothing for the hull.
        helper.assertTrue(reactor.getItemAt(1, 0).isEmpty(), "coolant destroyed");
        helper.assertValueEqual(reactor.getHeat(), 0, "hull heat");
        helper.succeed();
    }

    /** Reactor heat vent: drains 5 from the hull and vents 5 itself. */
    @GameTest(template = EMPTY)
    public static void reactorHeatVent(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, false);
        reactor.setHeat(100);
        reactor.setItemAt(2, 2, stack(Ic2Items.REACTOR_HEAT_VENT.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getHeat(), 95, "hull heat");
        helper.assertValueEqual(HeatStorageItem.getHeatOfStack(reactor.getItemAt(2, 2)), 0, "vent heat");
        helper.succeed();
    }

    /** Reactor heat exchanger balances towards the hull ratio, at most 72 per cycle. */
    @GameTest(template = EMPTY)
    public static void reactorHeatExchanger(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, false);
        reactor.setHeat(5000);
        reactor.setItemAt(0, 0, stack(Ic2Items.REACTOR_HEAT_EXCHANGER.get()));
        reactor.runCycle();
        // med = (0/5000 + 5000/10000) / 2 = 0.25 → hull wants 2500, change capped at −72.
        helper.assertValueEqual(reactor.getHeat(), 4928, "hull heat");
        helper.assertValueEqual(HeatStorageItem.getHeatOfStack(reactor.getItemAt(0, 0)), 72, "exchanger heat");
        helper.succeed();
    }

    /** Plating raises the maximum heat and lowers the heat effect modifier. */
    @GameTest(template = EMPTY)
    public static void plating(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, false);
        reactor.setItemAt(0, 0, stack(Ic2Items.REACTOR_PLATING.get()));
        reactor.setItemAt(1, 0, stack(Ic2Items.HEAT_CAPACITY_REACTOR_PLATING.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getMaxHeat(), 13000, "max heat");
        helper.assertTrue(Math.abs(reactor.getHeatEffectModifier() - 0.95F * 0.99F) < 1.0E-6F, "hem");
        helper.succeed();
    }

    /** Isotope cell next to a uranium cell: one level per pulse (+ heat / 3000). */
    @GameTest(template = EMPTY)
    public static void isotopeCellReEnriches(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setHeat(6000);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        ItemStack isotope = stack(Ic2Items.ISOTOPE_CELL.get());
        isotope.setDamageValue(9999);
        reactor.setItemAt(1, 0, isotope);
        reactor.runCycle();
        helper.assertValueEqual(reactor.getItemAt(1, 0).getDamageValue(), 9999 - 1 - 2, "isotope level");
        // the isotope cell counts as pulse but adds no output
        helper.assertValueEqual(reactor.getOutput(), 1, "output");
        helper.succeed();
    }

    /** Chambers widen the matrix; items beyond the width and non-components are ejected. */
    @GameTest(template = EMPTY)
    public static void chambersAndEjection(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, false);
        helper.assertValueEqual(reactor.getReactorSize(), 3, "size without chambers");
        helper.setBlock(POS.east(), Ic2Blocks.REACTOR_CHAMBER.get());
        helper.setBlock(POS.above(), Ic2Blocks.REACTOR_CHAMBER.get());
        helper.assertValueEqual(reactor.getReactorSize(), 5, "size with two chambers");
        reactor.setItemAt(5, 0, stack(Ic2Items.HEAT_VENT.get()));
        reactor.setItemAt(0, 1, stack(Items.DIRT));
        reactor.setItemAt(0, 2, stack(Ic2Items.NEAR_DEPLETED_URANIUM_CELL.get()));
        reactor.runCycle();
        helper.assertTrue(reactor.getItemAt(5, 0).isEmpty(), "outside the width ejected");
        helper.assertTrue(reactor.getItemAt(0, 1).isEmpty(), "dirt ejected");
        helper.assertFalse(reactor.getItemAt(0, 2).isEmpty(), "near-depleted cell stays");
        helper.assertItemEntityPresent(Items.DIRT);
        helper.assertItemEntityPresent(Ic2Items.HEAT_VENT.get());
        helper.succeed();
    }

    /** A chamber needs exactly one reactor next to it. */
    @GameTest(template = EMPTY)
    public static void chamberNeedsOneReactor(GameTestHelper helper) {
        reactor(helper, false);
        helper.setBlock(POS.east(2), Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        BlockPos between = helper.absolutePos(POS.east());
        helper.assertValueEqual(ReactorChamberBlock.countReactors(helper.getLevel(), between), 2, "two reactors");
        helper.assertValueEqual(ReactorChamberBlock.countReactors(helper.getLevel(),
                helper.absolutePos(POS.west())), 1, "one reactor");
        helper.succeed();
    }

    /** The reactor's 5 EU/t reach an adjacent BatBox. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void feedsEnergyNet(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, true);
        reactor.setItemAt(0, 0, stack(Ic2Items.URANIUM_CELL.get()));
        helper.setBlock(POS.west(), Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.succeedWhen(() -> {
            ElectricStorageBlockEntity batbox = helper.getBlockEntity(POS.west());
            helper.assertTrue(batbox.getEnergy() >= 50, "batbox charged");
        });
    }

    /** RSH condensator: one redstone repairs 10 000 heat. */
    @GameTest(template = EMPTY)
    public static void condensatorRepair(GameTestHelper helper) {
        ItemStack condensator = stack(Ic2Items.RSH_CONDENSATOR.get());
        Ic2Items.RSH_CONDENSATOR.get().setDamageForStack(condensator, 15000);
        GradualRepairRecipe recipe = new GradualRepairRecipe(Ic2Items.RSH_CONDENSATOR.get(),
                Ingredient.of(Items.REDSTONE), 10000);
        CraftingInput input = CraftingInput.of(2, 1, List.of(condensator, stack(Items.REDSTONE)));
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertValueEqual(Ic2Items.RSH_CONDENSATOR.get().getDamageOfStack(result), 5000, "repaired");
        helper.succeed();
    }
}
