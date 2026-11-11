package dev.craft.compat.oml;

import dev.craft.Craft;
import dev.craft.core.energy.EnergyNet;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 43: EU into Open Modular Lighting floodlights. Passes trivially without the mod. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class OmlEuInputGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos SOURCE = new BlockPos(0, 1, 1);
    private static final BlockPos CABLE = new BlockPos(1, 1, 1);
    private static final BlockPos LIGHT = new BlockPos(2, 1, 1);

    private OmlEuInputGameTests() {
    }

    private static Block floodlight() {
        return BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath(OmlEuInput.MOD_ID, "electric_floodlight"));
    }

    private static TestSource setUp(GameTestHelper helper, int voltage) {
        helper.setBlock(CABLE, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(LIGHT, floodlight());
        return new TestSource(helper.absolutePos(SOURCE), voltage);
    }

    @GameTest(template = EMPTY)
    public static void floodlightTakesEuAsFe(GameTestHelper helper) {
        if (!ModList.get().isLoaded(OmlEuInput.MOD_ID)) {
            helper.succeed();
            return;
        }
        EnergyNet net = EnergyNets.get(helper.getLevel());
        TestSource source = setUp(helper, 32);
        net.add(source);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(CABLE, CableBlock.CONNECTIONS.get(Direction.EAST), true);
            helper.assertTrue(net.getTile(helper.absolutePos(LIGHT)) instanceof OmlEuInput.LightSink,
                    "light must join the EU net");
            IEnergyStorage storage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                    helper.absolutePos(LIGHT), Direction.WEST);
            helper.assertTrue(storage != null, "floodlight must have an FE storage");
            int before = storage.getEnergyStored();
            helper.assertValueEqual(net.emitEnergyFrom(source, 32), 0, "undelivered EU");
            helper.assertValueEqual(storage.getEnergyStored() - before, 32, "FE received for 32 EU");
            net.remove(source);
            helper.setBlock(LIGHT, Blocks.AIR);
        });
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(net.getTile(helper.absolutePos(LIGHT)) == null, "broken light must leave the net");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void floodlightExplodesAboveLv(GameTestHelper helper) {
        if (!ModList.get().isLoaded(OmlEuInput.MOD_ID)) {
            helper.succeed();
            return;
        }
        EnergyNet net = EnergyNets.get(helper.getLevel());
        TestSource source = setUp(helper, 128);
        net.add(source);
        helper.runAfterDelay(2, () -> {
            net.emitEnergyFrom(source, 128);
            net.remove(source);
            helper.assertBlockNotPresent(floodlight(), LIGHT);
            helper.succeed();
        });
    }

    private record TestSource(BlockPos getBlockPos, int getMaxEnergyOutput) implements EnergySource {
        @Override
        public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
            return true;
        }
    }
}
