package dev.craft.forestry.engine;

import java.util.List;

import dev.craft.Craft;
import dev.craft.forestry.FrBlocks;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.circuit.CircuitBoard;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.CircuitLayout;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the Electrical Engine. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ElectricalEngineGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos ENGINE = new BlockPos(2, 1, 2);
    private static final BlockPos REDSTONE = ENGINE.north();

    private ElectricalEngineGameTests() {
    }

    private static ElectricalEngineBlockEntity placeEngine(GameTestHelper helper, boolean redstone) {
        if (redstone) {
            helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        }
        helper.setBlock(ENGINE, FrBlocks.ELECTRICAL_ENGINE.get());
        return helper.getBlockEntity(ENGINE);
    }

    @GameTest(template = EMPTY)
    public static void convertsSixEuIntoTwoMjPerTickWithRedstone(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, true);
        helper.assertValueEqual(engine.injectEnergy(Direction.UP, 60), 0, "60 EU accepted");
        helper.succeedWhen(() -> {
            helper.assertValueEqual(engine.getEuStored(), 0, "all EU burnt");
            helper.assertValueEqual(engine.getStoredEnergy(), 20, "10 cycles of 2 MJ");
        });
    }

    @GameTest(template = EMPTY)
    public static void doesNothingWithoutRedstone(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, false);
        engine.injectEnergy(Direction.UP, 60);
        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(engine.getEuStored(), 60, "no EU used");
            helper.assertValueEqual(engine.getStoredEnergy(), 0, "no MJ made");
            helper.assertValueEqual(engine.getHeat(), 0, "no heat");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void acceptsAnyVoltageUpToStoragePlus511(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, false);
        helper.assertValueEqual(engine.demandsEnergy(), 12, "demands its storage");
        helper.assertValueEqual(engine.injectEnergy(Direction.UP, 2048), 2048 - 523, "takes 12 + 511 EU");
        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(FrBlocks.ELECTRICAL_ENGINE.get(), ENGINE);
            helper.assertValueEqual(engine.demandsEnergy(), 12 - 523, "over-full");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void circuitBoardChangesTheEnergyConfig(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, false);
        ItemStack board = CircuitBoardItem.create(FrItems.circuitBoard(CircuitBoardType.INTRICATE).get(),
                new CircuitBoard(CircuitLayout.ENGINE_TIN, List.of(Circuit.ELECTRIC_BOOST_2, Circuit.ELECTRIC_BOOST_2)));
        engine.setSocket(board);
        helper.assertValueEqual(engine.getEuForCycle(), 36, "6 + 2 · 15 EU");
        helper.assertValueEqual(engine.getMjPerCycle(), 10, "2 + 2 · 4 MJ");
        helper.assertValueEqual(engine.getEuStorage(), 72, "12 + 2 · 30 EU");
        CompoundTag saved = engine.saveCustomOnly(helper.getLevel().registryAccess());
        engine.setSocket(ItemStack.EMPTY);
        helper.assertValueEqual(engine.getEuForCycle(), 6, "board removed");
        engine.loadCustomOnly(saved, helper.getLevel().registryAccess());
        helper.assertValueEqual(engine.getMjPerCycle(), 10, "circuits re-applied on load");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void turnsToAndPowersAnMjReceiver(GameTestHelper helper) {
        helper.setBlock(ENGINE.east(), FrBlocks.CARPENTER.get());
        CarpenterBlockEntity carpenter = helper.getBlockEntity(ENGINE.east());
        placeEngine(helper, true);
        helper.onEachTick(() -> {
            ElectricalEngineBlockEntity engine = helper.getBlockEntity(ENGINE);
            engine.injectEnergy(Direction.UP, 100);
        });
        helper.succeedWhen(() -> {
            ElectricalEngineBlockEntity engine = helper.getBlockEntity(ENGINE);
            helper.assertValueEqual(engine.getOrientation(), Direction.EAST, "output turned to the carpenter");
            helper.assertTrue(carpenter.getPowerProvider().getEnergyStored() > 0.0F,
                    "MJ delivered by a piston stroke");
        });
    }

    @GameTest(template = EMPTY)
    public static void meltingForcesACooldownInsteadOfExploding(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, true);
        CompoundTag tag = engine.saveCustomOnly(helper.getLevel().registryAccess());
        tag.putInt("EngineHeat", ElectricalEngineBlockEntity.MAX_HEAT);
        tag.putInt("EngineStoredEnergy", 100);
        engine.loadCustomOnly(tag, helper.getLevel().registryAccess());
        engine.injectEnergy(Direction.UP, 60);
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.isForceCooldown(), "forced cooldown");
            helper.assertTrue(engine.getStoredEnergy() < 100, "2 MJ destroyed per tick");
            helper.assertValueEqual(engine.getEuStored(), 60, "no EU burnt while cooling");
            helper.assertBlockPresent(FrBlocks.ELECTRICAL_ENGINE.get(), ENGINE);
        });
    }

    @GameTest(template = EMPTY)
    public static void drawsUpToThreeCyclesPerTickFromABattery(GameTestHelper helper) {
        ElectricalEngineBlockEntity engine = placeEngine(helper, true);
        ItemStack battery = ElectricItems.charged(new ItemStack(Ic2Items.RE_BATTERY.get()));
        int full = ElectricItems.getCharge(battery);
        engine.getInventory().setStackInSlot(0, battery);
        helper.succeedWhen(() -> {
            int used = full - ElectricItems.getCharge(engine.getInventory().getStackInSlot(0));
            helper.assertTrue(used >= 12, "battery discharged");
            helper.assertTrue(engine.getStoredEnergy() > 0, "MJ made from battery EU");
        });
    }
}
