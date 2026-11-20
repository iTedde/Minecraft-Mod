package ivorius.psychedelicraft.test;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.CannabisPlantBlock;
import ivorius.psychedelicraft.block.DistilleryBlock;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.fluid.AlcoholicFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Game mechanics of the port (Phase 52.2): growing, drying, mashing, fermenting, maturing, distilling, consuming.
 * Long processing times are skipped by fast-forwarding the progress counters; the processing step itself runs
 * through the normal block entity tick.
 */
@GameTestHolder(Psychedelicraft.DEFAULT_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class MechanicsGameTests {
    private static final String EMPTY = "empty_16x8x16";

    private MechanicsGameTests() {
    }

    private static ItemFluids fermentedWine(int fermentation, int amount) {
        return AlcoholicFluid.FERMENTATION.set(PSFluids.RED_GRAPES.getDefaultStack(amount), fermentation);
    }

    /** Runs one processing step of a fluid processing block entity (fermenting, maturing, distilling). */
    private static void runProcessingStep(ServerLevel level, ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity be) {
        be.tick(level);
        be.setTimeProcessed(be.getTimeNeeded());
        be.tick(level);
    }

    @GameTest(template = EMPTY)
    public static void cannabisGrowsToThreeBlocks(GameTestHelper helper) {
        BlockPos soil = new BlockPos(1, 1, 1);
        helper.setBlock(soil, Blocks.FARMLAND);
        BlockPos plant = soil.above();
        helper.setBlock(plant, PSBlocks.CANNABIS);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(plant);
        for (int i = 0; i < 80; i++) {
            BlockState state = level.getBlockState(abs);
            PSBlocks.CANNABIS.applyGrowth(level, abs, state, false);
        }
        helper.assertBlockPresent(PSBlocks.CANNABIS, plant.above());
        helper.assertBlockPresent(PSBlocks.CANNABIS, plant.above(2));
        helper.assertBlockNotPresent(PSBlocks.CANNABIS, plant.above(3));
        BlockState bottom = level.getBlockState(abs);
        helper.assertTrue(bottom.getValue(PSBlocks.CANNABIS.getAgeProperty()) == PSBlocks.CANNABIS.getMaxAge(bottom),
                "bottom part not fully grown");
        var drops = Block.getDrops(bottom, level, abs, null);
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(PSItems.CANNABIS_SEEDS) || s.is(PSItems.CANNABIS_LEAF)),
                "mature cannabis drops " + drops);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void dryingTableDriesCannabisBuds(GameTestHelper helper) {
        BlockPos table = new BlockPos(1, 1, 1);
        helper.setBlock(table, PSBlocks.DRYING_TABLE);
        // lit furnace two blocks away, facing the table: guaranteed heat independent of daylight
        helper.setBlock(table.east(2), Blocks.FURNACE.defaultBlockState()
                .setValue(AbstractFurnaceBlock.FACING, Direction.WEST)
                .setValue(AbstractFurnaceBlock.LIT, true));
        DryingTableBlockEntity be = helper.getBlockEntity(table);
        for (int slot = 1; slot <= 9; slot++) {
            be.setItem(slot, new ItemStack(PSItems.CANNABIS_BUDS));
        }
        int cookingTime = be.propertyDelegate.get(2);
        helper.assertTrue(cookingTime > 0, "no drying recipe selected");
        helper.assertTrue(be.getHeatRatio() >= 0.1F, "no heat from furnace: " + be.getHeatRatio());

        CompoundTag tag = be.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putFloat("dryingProgress", cookingTime - 0.05F);
        be.loadWithComponents(tag, helper.getLevel().registryAccess());

        helper.succeedWhen(() -> {
            ItemStack out = be.getItem(DryingTableBlockEntity.OUTPUT_SLOT_INDEX);
            helper.assertTrue(out.is(PSItems.DRIED_CANNABIS_BUDS) && out.getCount() == 3, "output " + out);
            for (int slot = 1; slot <= 9; slot++) {
                helper.assertTrue(be.getItem(slot).isEmpty(), "input slot " + slot + " not consumed");
            }
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void mashTubMashesGrapesIntoMust(GameTestHelper helper) {
        BlockPos tub = new BlockPos(3, 1, 3);
        helper.setBlock(tub, PSBlocks.MASH_TUB);
        MashTubBlockEntity be = helper.getBlockEntity(tub);
        be.getPrimaryTank().setContents(SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.BUCKET * 3));

        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(tub));
        ItemEntity grapes = new ItemEntity(helper.getLevel(), center.x, center.y, center.z, new ItemStack(PSItems.WINE_GRAPES, 8));
        grapes.setNoGravity(true);
        grapes.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(grapes);

        helper.succeedWhen(() -> {
            helper.assertTrue(grapes.isRemoved(), "grapes were not taken in");
            ItemFluids contents = be.getPrimaryTank().getContents();
            helper.assertTrue(contents.isOf(PSFluids.RED_GRAPES), "tub contains " + contents.fluid().getId());
            helper.assertTrue(contents.amount() == FluidVolumes.BUCKET * 3, "amount " + contents.amount());
            helper.assertTrue(be.getSuppliedIngredients().isEmpty(), "ingredients not used up");
        });
    }

    @GameTest(template = EMPTY)
    public static void mashTubFermentsTwiceThenAcetifies(GameTestHelper helper) {
        BlockPos tub = new BlockPos(3, 1, 3);
        helper.setBlock(tub, PSBlocks.MASH_TUB);
        MashTubBlockEntity be = helper.getBlockEntity(tub);
        be.getPrimaryTank().setContents(PSFluids.RED_GRAPES.getDefaultStack(FluidVolumes.BUCKET * 3));
        ServerLevel level = helper.getLevel();

        runProcessingStep(level, be);
        helper.assertTrue(AlcoholicFluid.FERMENTATION.get(be.getPrimaryTank().getContents()) == 1, "first fermentation missing");
        runProcessingStep(level, be);
        helper.assertTrue(AlcoholicFluid.FERMENTATION.get(be.getPrimaryTank().getContents()) == 2, "second fermentation missing");
        runProcessingStep(level, be);
        helper.assertTrue(AlcoholicFluid.VINEGAR.get(be.getPrimaryTank().getContents()), "over-fermented wine did not turn to vinegar");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void barrelMaturesFermentedWineOnly(GameTestHelper helper) {
        BlockPos barrel = new BlockPos(1, 1, 1);
        helper.setBlock(barrel, PSBlocks.OAK_BARREL);
        BarrelBlockEntity be = helper.getBlockEntity(barrel);
        ServerLevel level = helper.getLevel();

        be.getPrimaryTank().setContents(PSFluids.RED_GRAPES.getDefaultStack(FluidVolumes.BUCKET));
        be.tick(level);
        helper.assertFalse(be.isActive(), "unfermented must should not mature");

        be.getPrimaryTank().setContents(fermentedWine(2, FluidVolumes.BUCKET));
        runProcessingStep(level, be);
        runProcessingStep(level, be);
        helper.assertTrue(AlcoholicFluid.MATURATION.get(be.getPrimaryTank().getContents()) == 2,
                "maturation " + AlcoholicFluid.MATURATION.get(be.getPrimaryTank().getContents()));
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void distilleryDistilsIntoFlask(GameTestHelper helper) {
        BlockPos distillery = new BlockPos(1, 2, 1);
        helper.setBlock(distillery.below(), Blocks.CAMPFIRE);
        helper.setBlock(distillery, PSBlocks.DISTILLERY.defaultBlockState().setValue(DistilleryBlock.FACING, Direction.EAST));
        helper.setBlock(distillery.east(), PSBlocks.FLASK);
        DistilleryBlockEntity be = helper.getBlockEntity(distillery);
        FlaskBlockEntity flask = helper.getBlockEntity(distillery.east());
        ServerLevel level = helper.getLevel();

        be.getPrimaryTank().setContents(fermentedWine(2, FluidVolumes.BUCKET * 4));
        double before = alcoholOf(be.getPrimaryTank().getContents());
        runProcessingStep(level, be);
        runProcessingStep(level, be);
        ItemFluids after = be.getPrimaryTank().getContents();
        helper.assertTrue(AlcoholicFluid.DISTILLATION.get(after) == 2, "distillation " + AlcoholicFluid.DISTILLATION.get(after));
        helper.assertTrue(alcoholOf(after) > before, "distilling did not raise the alcohol content");
        helper.assertTrue(flask.getPrimaryTank().getContents().isOf(PSFluids.SLURRY), "flask did not receive slurry: "
                + flask.getPrimaryTank().getContents().fluid().getId());

        be.getPrimaryTank().setContents(PSFluids.RED_GRAPES.getDefaultStack(FluidVolumes.BUCKET));
        be.tick(level);
        helper.assertFalse(be.isActive(), "unfermented must should not distil");
        helper.succeed();
    }

    private static double alcoholOf(ItemFluids fluids) {
        java.util.concurrent.atomic.AtomicReference<Double> total = new java.util.concurrent.atomic.AtomicReference<>(0D);
        ((AlcoholicFluid)fluids.fluid()).getDrugInfluencesPerLiter(fluids, influence -> {
            if (influence.isOf(DrugType.ALCOHOL)) {
                total.set(total.get() + influence.target());
            }
        });
        return total.get();
    }

    private static Player survivalPlayer(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "psychedelicraft-test"));
        player.setPos(helper.absoluteVec(new Vec3(1, 1, 1)));
        player.setGameMode(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(10); // wine has food value and can only be drunk when hungry
        return player;
    }

    private static void tickDrugs(Player player, int ticks) {
        DrugProperties properties = DrugProperties.of(player);
        for (int i = 0; i < ticks; i++) {
            player.tickCount = i;
            properties.onTick();
        }
    }

    @GameTest(template = EMPTY)
    public static void drinkingWineRaisesAlcohol(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        ItemStack mug = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), fermentedWine(2, FluidVolumes.MUG));
        ItemStack rest = mug.getItem().finishUsingItem(mug, helper.getLevel(), player);
        helper.assertTrue(ItemFluids.of(rest).amount() < FluidVolumes.MUG, "nothing was drunk: before " + ItemFluids.of(mug) + " after " + ItemFluids.of(rest) + " food " + player.getFoodData().getFoodLevel() + " canConsume " + ((ivorius.psychedelicraft.fluid.ConsumableFluid)PSFluids.RED_GRAPES).canConsume(mug, player, ivorius.psychedelicraft.fluid.ConsumableFluid.ConsumptionType.DRINK));
        tickDrugs(player, 400);
        float alcohol = DrugProperties.of(player).getDrugValue(DrugType.ALCOHOL);
        helper.assertTrue(alcohol > 0.05F, "alcohol after drinking " + alcohol);

        ItemStack must = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), PSFluids.RED_GRAPES.getDefaultStack(FluidVolumes.MUG));
        Player sober = survivalPlayer(helper);
        must.getItem().finishUsingItem(must, helper.getLevel(), sober);
        tickDrugs(sober, 400);
        helper.assertTrue(DrugProperties.of(sober).getDrugValue(DrugType.ALCOHOL) == 0, "unfermented must contains alcohol");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void smokingJointRaisesCannabisAndWearsOff(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        ItemStack joint = new ItemStack(PSItems.JOINT);
        joint.getItem().finishUsingItem(joint, helper.getLevel(), player);
        helper.assertTrue(joint.getDamageValue() == 1, "joint not used: damage " + joint.getDamageValue());
        tickDrugs(player, 400);
        float peak = DrugProperties.of(player).getDrugValue(DrugType.CANNABIS);
        helper.assertTrue(peak > 0.01F, "cannabis after smoking " + peak);
        tickDrugs(player, 24000);
        float later = DrugProperties.of(player).getDrugValue(DrugType.CANNABIS);
        helper.assertTrue(later < peak, "cannabis did not wear off: " + peak + " -> " + later);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void vomitingFillsPaperBagOrDropsVomit(GameTestHelper helper) {
        Player bagged = survivalPlayer(helper);
        bagged.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(PSItems.PAPER_BAG));
        DrugProperties.of(bagged).getStomach().vomit();
        helper.assertTrue(bagged.getOffhandItem().is(PSItems.BAG_O_VOMIT), "bag not filled: " + bagged.getOffhandItem());
        helper.assertTrue(bagged.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION), "no nausea after vomiting");

        Player player = survivalPlayer(helper);
        DrugProperties.of(player).getStomach().vomit();
        tickDrugs(player, 200);
        var vomit = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4),
                item -> item.getItem().is(PSItems.VOMIT));
        helper.assertFalse(vomit.isEmpty(), "no vomit dropped");
        helper.succeed();
    }
}
