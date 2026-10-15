package dev.craft.gregtech.machine;

import java.util.List;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.block.IndustrialTntBlock;
import dev.craft.ic2.block.IndustrialTntEntity;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the implosion compressor and industrial TNT (phase 6b.5b). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ImplosionCompressorGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** The casing cube lies directly below the controller. */
    private static final BlockPos CONTROLLER = new BlockPos(5, 4, 5);

    private ImplosionCompressorGameTests() {
    }

    /** Builds the 3 × 3 × 3 cube: standard casings on the corners, reinforced casings elsewhere, air inside. */
    private static ImplosionCompressorBlockEntity build(GameTestHelper helper) {
        for (int dy = -3; dy <= -1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == -2 && dz == 0) {
                        continue;
                    }
                    boolean corner = dx != 0 && dy != -2 && dz != 0;
                    helper.setBlock(CONTROLLER.offset(dx, dy, dz), GtBlocks.casing(corner
                            ? GtCasingBlock.Type.STANDARD : GtCasingBlock.Type.REINFORCED).get());
                }
            }
        }
        helper.setBlock(CONTROLLER, GtBlocks.machine(GtMachine.IMPLOSION_COMPRESSOR).get());
        ImplosionCompressorBlockEntity compressor = helper.getBlockEntity(CONTROLLER);
        compressor.skipStartupForTest();
        return compressor;
    }

    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void formsOnCasingCube(GameTestHelper helper) {
        ImplosionCompressorBlockEntity compressor = build(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(compressor.isMachine(), "structure formed");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, true);
        });
    }

    /** Corners must be standard casings: a reinforced corner leaves the machine incomplete. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void cornerNeedsStandardCasing(GameTestHelper helper) {
        ImplosionCompressorBlockEntity compressor = build(helper);
        helper.setBlock(CONTROLLER.offset(1, -1, 1), GtBlocks.casing(GtCasingBlock.Type.REINFORCED).get());
        helper.runAfterDelay(30, () -> {
            helper.assertFalse(compressor.isMachine(), "incomplete");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, false);
            helper.succeed();
        });
    }

    /** Alloy ingot + 8 ITNT: 21 ticks at 32 EU/t, iridium plate and 4 dark ash. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void compressesIridiumPlate(GameTestHelper helper) {
        ImplosionCompressorBlockEntity compressor = build(helper);
        compressor.setStoredEnergyForTest(10000);
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_1,
                new ItemStack(GtItems.IRIDIUM_ALLOY_INGOT.get()));
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_2,
                new ItemStack(Ic2Items.INDUSTRIAL_TNT.get(), 10));
        helper.succeedWhen(() -> {
            helper.assertTrue(compressor.getInventory().get(ImplosionCompressorBlockEntity.SLOT_OUTPUT_1)
                    .is(Ic2Items.IRIDIUM_PLATE.get()), "iridium plate");
            helper.assertValueEqual(compressor.getInventory().get(ImplosionCompressorBlockEntity.SLOT_OUTPUT_2)
                    .getCount(), 4, "dark ash");
            helper.assertValueEqual(compressor.getInventory().get(ImplosionCompressorBlockEntity.SLOT_INPUT_2)
                    .getCount(), 2, "eight ITNT used");
            helper.assertValueEqual(compressor.getStoredEU(), 10000 - 21 * 32, "energy used");
        });
    }

    /** The lookup is not shapeless: ITNT in the first slot does not start the recipe. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void recipeNeedsSlotOrder(GameTestHelper helper) {
        ImplosionCompressorBlockEntity compressor = build(helper);
        compressor.setStoredEnergyForTest(10000);
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_1,
                new ItemStack(Ic2Items.INDUSTRIAL_TNT.get(), 32));
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_2,
                new ItemStack(GtItems.DIAMOND_DUST.get(), 4));
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(compressor.isMachine(), "formed");
            helper.assertValueEqual(compressor.getProgressTime(), 0, "not started");
            helper.assertValueEqual(compressor.getStoredEU(), 10000, "no energy used");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void diamondDustBecomesIndustrialDiamonds(GameTestHelper helper) {
        ImplosionCompressorBlockEntity compressor = build(helper);
        compressor.setStoredEnergyForTest(10000);
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_1,
                new ItemStack(GtItems.DIAMOND_DUST.get(), 4));
        compressor.getInventory().set(ImplosionCompressorBlockEntity.SLOT_INPUT_2,
                new ItemStack(Ic2Items.INDUSTRIAL_TNT.get(), 32));
        helper.succeedWhen(() -> {
            ItemStack out = compressor.getInventory().get(ImplosionCompressorBlockEntity.SLOT_OUTPUT_1);
            helper.assertTrue(out.is(Ic2Items.INDUSTRIAL_DIAMOND.get()) && out.getCount() == 3, "3 diamonds");
            helper.assertValueEqual(compressor.getInventory().get(ImplosionCompressorBlockEntity.SLOT_OUTPUT_2)
                    .getCount(), 16, "dark ash");
        });
    }

    @GameTest(template = EMPTY)
    public static void gemDustsAreAmplifiers(GameTestHelper helper) {
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.DIAMOND_DUST.get())),
                125000, "diamond dust");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.EMERALD_DUST.get())),
                50000, "emerald dust");
        helper.succeed();
    }

    // --- industrial TNT ---

    /** Removes primed ITNT so it cannot blow up neighbouring test structures. */
    private static List<IndustrialTntEntity> takePrimed(GameTestHelper helper) {
        List<IndustrialTntEntity> primed = helper.getEntities(Ic2EntityTypes.INDUSTRIAL_TNT.get());
        primed.forEach(IndustrialTntEntity::discard);
        return primed;
    }

    @GameTest(template = EMPTY)
    public static void itntPrimesWhenPlacedPowered(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(pos, Ic2Blocks.INDUSTRIAL_TNT.get());
        helper.assertBlockPresent(Blocks.AIR, pos);
        List<IndustrialTntEntity> primed = takePrimed(helper);
        helper.assertValueEqual(primed.size(), 1, "primed entities");
        helper.assertValueEqual(primed.getFirst().getFuse(), 60, "fuse");
        helper.succeed();
    }

    /** ITNT cannot be picked up: breaking it by hand primes it and drops nothing. */
    @GameTest(template = EMPTY)
    public static void itntPrimesWhenBroken(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        helper.setBlock(pos, Ic2Blocks.INDUSTRIAL_TNT.get());
        BlockPos abs = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(abs);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertFalse(state.onDestroyedByPlayer(helper.getLevel(), abs, player, true,
                Fluids.EMPTY.defaultFluidState()), "no drops");
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.assertValueEqual(takePrimed(helper).size(), 1, "primed entities");
        helper.succeed();
    }

    /** Destroyed by an explosion: fuse rand(15) + 7. */
    @GameTest(template = EMPTY)
    public static void itntChainFuseIsShort(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        IndustrialTntBlock block = Ic2Blocks.INDUSTRIAL_TNT.get();
        block.wasExploded(helper.getLevel(), helper.absolutePos(pos), null);
        List<IndustrialTntEntity> primed = takePrimed(helper);
        helper.assertValueEqual(primed.size(), 1, "primed entities");
        int fuse = primed.getFirst().getFuse();
        helper.assertTrue(fuse >= 7 && fuse <= 21, "fuse " + fuse);
        helper.succeed();
    }
}
