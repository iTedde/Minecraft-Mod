package dev.craft.ic2.cable;

import dev.craft.Craft;
import dev.craft.core.energy.EnergyNet;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for cables and the energy net ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CableGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private CableGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void adjacentCablesConnect(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        BlockPos b = new BlockPos(2, 1, 1);
        helper.setBlock(a, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(b, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(a, CableBlock.CONNECTIONS.get(Direction.EAST), true);
            helper.assertBlockProperty(b, CableBlock.CONNECTIONS.get(Direction.WEST), true);
            helper.assertBlockProperty(a, CableBlock.CONNECTIONS.get(Direction.WEST), false);
        });
    }

    @GameTest(template = EMPTY)
    public static void differentColorsDisconnect(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        BlockPos b = new BlockPos(2, 1, 1);
        helper.setBlock(a, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(b, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.runAfterDelay(1, () -> {
            cable(helper, a).changeColor(1);
            cable(helper, b).changeColor(2);
        });
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(a, CableBlock.CONNECTIONS.get(Direction.EAST), false);
            helper.assertBlockProperty(b, CableBlock.CONNECTIONS.get(Direction.WEST), false);
            cable(helper, b).changeColor(1);
        });
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(a, CableBlock.CONNECTIONS.get(Direction.EAST), true);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void uncoloredCableCannotBePaintedIfNotColorable(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        helper.setBlock(a, Ic2Blocks.cable(CableType.COPPER).get());
        helper.runAfterDelay(1, () -> {
            helper.assertFalse(cable(helper, a).changeColor(3), "uninsulated copper must not be paintable");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void energyFlowsThroughCables(GameTestHelper helper) {
        EnergyNet net = EnergyNets.get(helper.getLevel());
        TestSource source = new TestSource(helper.absolutePos(new BlockPos(0, 1, 1)), 32);
        TestSink sink = new TestSink(helper.absolutePos(new BlockPos(4, 1, 1)));
        for (int x = 1; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 1, 1), Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        }
        net.add(source);
        net.add(sink);
        helper.runAfterDelay(1, () -> {
            helper.assertValueEqual(net.emitEnergyFrom(source, 32), 0, "undelivered EU");
            helper.assertValueEqual(sink.received, 32, "received EU");
            net.remove(source);
            net.remove(sink);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void overloadedTinCableMelts(GameTestHelper helper) {
        EnergyNet net = EnergyNets.get(helper.getLevel());
        TestSource source = new TestSource(helper.absolutePos(new BlockPos(0, 1, 1)), 32);
        TestSink sink = new TestSink(helper.absolutePos(new BlockPos(2, 1, 1)));
        BlockPos tin = new BlockPos(1, 1, 1);
        helper.setBlock(tin, Ic2Blocks.cable(CableType.TIN).get());
        net.add(source);
        net.add(sink);
        helper.runAfterDelay(1, () -> {
            net.emitEnergyFrom(source, 10);
            helper.assertValueEqual(sink.received, 10, "received EU");
            helper.assertBlockPresent(Blocks.AIR, tin);
            net.remove(source);
            net.remove(sink);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void insulationChangeKeepsColor(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        helper.setBlock(a, Ic2Blocks.cable(CableType.INSULATED_GOLD).get());
        helper.runAfterDelay(1, () -> {
            cable(helper, a).changeColor(5);
            CableBlock.changeType(helper.getLevel(), helper.absolutePos(a), CableType.INSULATED_GOLD.insulated());
        });
        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(Ic2Blocks.cable(CableType.DOUBLE_INSULATED_GOLD).get(), a);
            helper.assertValueEqual(cable(helper, a).getColor(), 5, "color");
            helper.assertTrue(EnergyNets.isAdded(helper.getLevel(), cable(helper, a)), "re-added to net");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void wetFoamHardensWithSand(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 1, 1);
        helper.setBlock(a, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.runAfterDelay(1, () -> {
            CableBlock.setFoam(helper.getLevel(), helper.absolutePos(a), CableBlock.Foam.WET);
            helper.assertValueEqual(cable(helper, a).getFoamColor(), CableBlockEntity.WET_FOAM_COLOR, "foam color");
            helper.useBlock(a, helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
        });
        helper.runAfterDelay(2, () -> {
            BlockState state = helper.getBlockState(a);
            helper.assertValueEqual(state.getValue(CableBlock.FOAM), CableBlock.Foam.WET, "foam without sand");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void detectorCableEmitsRedstoneWhileConducting(GameTestHelper helper) {
        EnergyNet net = EnergyNets.get(helper.getLevel());
        TestSource source = new TestSource(helper.absolutePos(new BlockPos(0, 1, 1)), 32);
        TestSink sink = new TestSink(helper.absolutePos(new BlockPos(2, 1, 1)));
        BlockPos detector = new BlockPos(1, 1, 1);
        helper.setBlock(detector, Ic2Blocks.cable(CableType.DETECTOR).get());
        net.add(source);
        net.add(sink);
        helper.onEachTick(() -> net.emitEnergyFrom(source, 10));
        helper.succeedWhen(() -> {
            helper.assertTrue(cable(helper, detector).isActive(), "detector active");
            helper.assertValueEqual(helper.getLevel().getSignal(helper.absolutePos(detector), Direction.EAST),
                    15, "redstone signal");
            net.remove(source);
            net.remove(sink);
        });
    }

    private static CableBlockEntity cable(GameTestHelper helper, BlockPos pos) {
        return (CableBlockEntity) helper.getBlockEntity(pos);
    }

    private record TestSource(BlockPos getBlockPos, int getMaxEnergyOutput) implements EnergySource {
        @Override
        public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
            return true;
        }
    }

    private static final class TestSink implements EnergySink {
        private final BlockPos pos;
        int received;

        TestSink(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
            return true;
        }

        @Override
        public int demandsEnergy() {
            return Integer.MAX_VALUE - received;
        }

        @Override
        public int injectEnergy(Direction direction, int amount) {
            received += amount;
            return 0;
        }

        @Override
        public int getMaxSafeInput() {
            return Integer.MAX_VALUE;
        }
    }
}
