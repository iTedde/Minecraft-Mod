package dev.craft.compat.euinput;

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

/**
 * Phases 43/45: EU into foreign FE machines (OML floodlight 1:1, Ad Astra compressor and AE2 controller 1:4).
 * Each test passes trivially without its mod.
 */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ForeignEuInputsGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos SOURCE = new BlockPos(0, 1, 1);
    private static final BlockPos CABLE = new BlockPos(1, 1, 1);
    private static final BlockPos MACHINE = new BlockPos(2, 1, 1);

    private ForeignEuInputsGameTests() {
    }

    private static Block block(ForeignEuInputs.Rule rule, String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(rule.modId(), path));
    }

    /**
     * Cable + machine; after two ticks checks the net entry and that 32 EU arrive as {@code 32 * fePerEu} FE. With
     * {@code exact == false} (AE2: the controller hands energy on to its grid at once) only a rise of the local buffer
     * is checked; that every EU was taken is checked in both cases.
     */
    private static void takesEu(GameTestHelper helper, ForeignEuInputs.Rule rule, String path, boolean exact) {
        if (!ModList.get().isLoaded(rule.modId())) {
            helper.succeed();
            return;
        }
        EnergyNet net = EnergyNets.get(helper.getLevel());
        helper.setBlock(CABLE, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(MACHINE, block(rule, path));
        TestSource source = new TestSource(helper.absolutePos(SOURCE), 32);
        net.add(source);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(CABLE, CableBlock.CONNECTIONS.get(Direction.EAST), true);
            helper.assertTrue(net.getTile(helper.absolutePos(MACHINE)) instanceof ForeignEuInputs.ForeignSink,
                    path + " must join the EU net");
            IEnergyStorage storage = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                    helper.absolutePos(MACHINE), Direction.WEST);
            helper.assertTrue(storage != null, path + " must have an FE storage");
            int before = storage.getEnergyStored();
            helper.assertValueEqual(net.emitEnergyFrom(source, 32), 0, "undelivered EU");
            int gained = storage.getEnergyStored() - before;
            if (exact) {
                helper.assertValueEqual(gained, 32 * rule.fePerEu(), "FE for 32 EU");
            } else {
                helper.assertTrue(gained > 0, path + " must store FE, gained " + gained);
            }
            net.remove(source);
            helper.setBlock(MACHINE, Blocks.AIR);
        });
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(net.getTile(helper.absolutePos(MACHINE)) == null, "removed block must leave the net");
            helper.succeed();
        });
    }

    private static void explodesAboveLv(GameTestHelper helper, ForeignEuInputs.Rule rule, String path) {
        if (!ModList.get().isLoaded(rule.modId())) {
            helper.succeed();
            return;
        }
        EnergyNet net = EnergyNets.get(helper.getLevel());
        helper.setBlock(CABLE, Ic2Blocks.cable(CableType.GLASS_FIBRE).get());
        helper.setBlock(MACHINE, block(rule, path));
        TestSource source = new TestSource(helper.absolutePos(SOURCE), 128);
        net.add(source);
        helper.runAfterDelay(2, () -> {
            net.emitEnergyFrom(source, 128);
            net.remove(source);
            helper.assertBlockNotPresent(block(rule, path), MACHINE);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void floodlightTakesEuAsFe(GameTestHelper helper) {
        takesEu(helper, ForeignEuInputs.OML, "electric_floodlight", true);
    }

    @GameTest(template = EMPTY)
    public static void floodlightExplodesAboveLv(GameTestHelper helper) {
        explodesAboveLv(helper, ForeignEuInputs.OML, "electric_floodlight");
    }

    @GameTest(template = EMPTY)
    public static void adAstraCompressorTakesEu(GameTestHelper helper) {
        takesEu(helper, ForeignEuInputs.AD_ASTRA, "compressor", true);
    }

    @GameTest(template = EMPTY)
    public static void adAstraCompressorExplodesAboveLv(GameTestHelper helper) {
        explodesAboveLv(helper, ForeignEuInputs.AD_ASTRA, "compressor");
    }

    @GameTest(template = EMPTY)
    public static void ae2ControllerTakesEu(GameTestHelper helper) {
        takesEu(helper, ForeignEuInputs.AE2, "controller", false);
    }

    @GameTest(template = EMPTY)
    public static void ae2InscriberTakesEu(GameTestHelper helper) {
        takesEu(helper, ForeignEuInputs.AE2, "inscriber", true);
    }

    private record TestSource(BlockPos getBlockPos, int getMaxEnergyOutput) implements EnergySource {
        @Override
        public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
            return true;
        }
    }
}
