package dev.craft.gregtech.item;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.reactor.HeatStorageItem;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** GregTech reactor components in the IC2 reactor (phase 7b). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtReactorGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private GtReactorGameTests() {
    }

    private static NuclearReactorBlockEntity reactor(GameTestHelper helper) {
        helper.setBlock(POS.below(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        return helper.getBlockEntity(POS);
    }

    /** Thorium (pulse rate −5): output and heat only every fifth cycle; durability counts every cycle. */
    @GameTest(template = EMPTY)
    public static void thoriumCellEveryFifthCycle(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper);
        reactor.setItemAt(0, 0, new ItemStack(GtItems.THORIUM_CELL.get()));
        for (int cycle = 1; cycle <= 4; cycle++) {
            reactor.runCycle();
            helper.assertValueEqual(reactor.getOutput(), 0, "output in cycle " + cycle);
        }
        helper.assertValueEqual(reactor.getHeat(), 0, "no heat before the fifth cycle");
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 1, "output in cycle 5");
        helper.assertValueEqual(reactor.getHeat(), 4, "heat in cycle 5");
        helper.assertValueEqual(GtItems.THORIUM_CELL.get().getDurabilityOfStack(reactor.getItemAt(0, 0)), 5,
                "durability");
        helper.succeed();
    }

    /** Plutonium (pulse rate 2): two outputs per pulse and the heat step twice: 4 + 4. */
    @GameTest(template = EMPTY)
    public static void plutoniumCell(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper);
        reactor.setItemAt(0, 0, new ItemStack(GtItems.PLUTONIUM_CELL.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 2, "output");
        helper.assertValueEqual(reactor.getHeat(), 8, "hull heat");
        helper.succeed();
    }

    /** The iridium reflector sends the pulse back to the uranium cell and never wears out. */
    @GameTest(template = EMPTY)
    public static void iridiumReflector(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper);
        reactor.setItemAt(0, 0, new ItemStack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, new ItemStack(GtItems.IRIDIUM_NEUTRON_REFLECTOR.get()));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 2, "output");
        helper.assertValueEqual(reactor.getHeat(), 12, "hull heat");
        helper.assertValueEqual(reactor.getItemAt(1, 0).getDamageValue(), 0, "no wear");
        helper.succeed();
    }

    /** A stack of two iridium reflectors does not reflect. */
    @GameTest(template = EMPTY)
    public static void stackedReflectorDoesNothing(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper);
        reactor.setItemAt(0, 0, new ItemStack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, new ItemStack(GtItems.IRIDIUM_NEUTRON_REFLECTOR.get(), 2));
        reactor.runCycle();
        helper.assertValueEqual(reactor.getOutput(), 1, "output");
        helper.assertValueEqual(reactor.getHeat(), 4, "hull heat");
        helper.succeed();
    }

    /** Helium coolant cell stores heat like an IC2 coolant cell (bar in 100 steps). */
    @GameTest(template = EMPTY)
    public static void heliumCoolantCell(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper);
        reactor.setItemAt(0, 0, new ItemStack(Ic2Items.URANIUM_CELL.get()));
        reactor.setItemAt(1, 0, new ItemStack(GtItems.HELIUM_COOLANT_CELL_60K.get()));
        reactor.runCycle();
        ItemStack coolant = reactor.getItemAt(1, 0);
        helper.assertValueEqual(HeatStorageItem.getHeatOfStack(coolant), 4, "coolant heat");
        helper.assertValueEqual(coolant.getMaxDamage(), 100, "bar steps");
        helper.assertValueEqual(reactor.getHeat(), 0, "hull heat");
        helper.succeed();
    }
}
