package dev.craft.ic2.terraformer;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.TerraformerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Blueprint algorithms on fixed columns and the terraformer itself (phase 12.5). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class TerraformerGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final BlockPos COLUMN = new BlockPos(3, 4, 3);
    /**
     * Terraformer heights whose search start (+10 / +20) lies inside the structure: the test area has a barrier
     * ceiling the scans would find first.
     */
    private static final int NEAR = -4;
    private static final int FAR = -14;

    private TerraformerGameTests() {
    }

    private static boolean run(GameTestHelper helper, TerraformerBlueprint blueprint, int terraformerY) {
        BlockPos column = helper.absolutePos(COLUMN);
        return blueprint.terraform(helper.getLevel(), column.getX(), column.getZ(),
                helper.absolutePos(new BlockPos(0, terraformerY, 0)).getY());
    }

    /** Dirt becomes grass, then a plant grows on it. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void cultivation(GameTestHelper helper) {
        helper.setBlock(COLUMN, Blocks.DIRT);
        CultivationBlueprint blueprint = new CultivationBlueprint();
        helper.assertTrue(run(helper, blueprint, NEAR), "first step");
        helper.assertBlockPresent(Blocks.GRASS_BLOCK, COLUMN);
        helper.assertTrue(run(helper, blueprint, NEAR), "second step");
        helper.assertFalse(helper.getBlockState(COLUMN.above()).isAir(), "plant on grass");
        helper.succeed();
    }

    /** Grass and the dirt below both turn into sand in one step. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void desertification(GameTestHelper helper) {
        helper.setBlock(COLUMN, Blocks.GRASS_BLOCK);
        helper.setBlock(COLUMN.below(), Blocks.DIRT);
        helper.assertTrue(run(helper, new DesertificationBlueprint(), NEAR), "step");
        helper.assertBlockPresent(Blocks.SAND, COLUMN);
        helper.assertBlockPresent(Blocks.SAND, COLUMN.below());
        helper.succeed();
    }

    /** Water freezes; ice over water thickens. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void chilling(GameTestHelper helper) {
        helper.setBlock(COLUMN, Blocks.WATER);
        helper.setBlock(COLUMN.below(), Blocks.WATER);
        ChillingBlueprint blueprint = new ChillingBlueprint();
        helper.assertTrue(run(helper, blueprint, NEAR), "freeze");
        helper.assertBlockPresent(Blocks.ICE, COLUMN);
        helper.assertTrue(run(helper, blueprint, NEAR), "thicken");
        helper.assertBlockPresent(Blocks.ICE, COLUMN.below());
        helper.succeed();
    }

    /** Wheat ripens at once. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void irrigation(GameTestHelper helper) {
        helper.setBlock(COLUMN.below(), Blocks.FARMLAND);
        helper.setBlock(COLUMN, Blocks.WHEAT);
        run(helper, new IrrigationBlueprint(), NEAR);
        helper.assertValueEqual(helper.getBlockState(COLUMN).getValue(CropBlock.AGE), CropBlock.MAX_AGE, "age");
        helper.succeed();
    }

    /** Above the terraformer's height natural blocks are removed, obsidian is not. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void flatification(GameTestHelper helper) {
        helper.setBlock(COLUMN, Blocks.STONE);
        FlatificationBlueprint blueprint = new FlatificationBlueprint();
        helper.assertTrue(run(helper, blueprint, FAR), "remove above");
        helper.assertBlockPresent(Blocks.AIR, COLUMN);
        helper.setBlock(COLUMN, Blocks.OBSIDIAN);
        helper.assertFalse(run(helper, blueprint, FAR), "obsidian stays");
        helper.succeed();
    }

    /** A mushroom is planted on mycelium. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void mushroom(GameTestHelper helper) {
        helper.setBlock(COLUMN, Blocks.MYCELIUM);
        helper.assertTrue(run(helper, new MushroomBlueprint(), FAR), "step");
        helper.assertTrue(helper.getBlockState(COLUMN.above()).is(Blocks.BROWN_MUSHROOM)
                || helper.getBlockState(COLUMN.above()).is(Blocks.RED_MUSHROOM), "mushroom");
        helper.succeed();
    }

    /** The terraformer spends energy per attempt (a tenth on failure) and ejects the blueprint as an item. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void terraformerUsesEnergyAndEjects(GameTestHelper helper) {
        BlockPos pos = new BlockPos(7, 1, 7);
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.TERRAFORMER).get());
        TerraformerBlockEntity terraformer = helper.getBlockEntity(pos);
        terraformer.insertBlueprint(new ItemStack(Ic2Items.FLATIFICATION_BLUEPRINT.get()));
        terraformer.setEnergyForTest(100000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(terraformer.getEnergy() < 100000, "energy used");
            helper.assertTrue(terraformer.isActive(), "active");
            helper.assertTrue(terraformer.ejectBlueprint(), "ejected");
            helper.assertTrue(terraformer.getBlueprintStack().isEmpty(), "slot empty");
            helper.assertEntityPresent(EntityType.ITEM, pos, 2.0);
            helper.succeed();
        });
    }
}
