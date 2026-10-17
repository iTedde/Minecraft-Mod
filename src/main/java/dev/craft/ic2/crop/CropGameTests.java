package dev.craft.ic2.crop;

import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the crop framework (phase 12.7a). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CropGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private CropGameTests() {
    }

    /** Farmland (moisture 7) with a crop stick on top; returns the crop tile. */
    private static CropBlockEntity placeCrop(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        helper.setBlock(pos, Ic2Blocks.CROP.get());
        return (CropBlockEntity) helper.getBlockEntity(pos);
    }

    private static Player playerHolding(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static int countItems(GameTestHelper helper, BlockPos pos, Item item) {
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(2.0), entity -> entity.getItem().is(item));
        return items.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    @GameTest(template = EMPTY)
    public static void stickUpgradeBaseSeedAndFarmlandLoss(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        helper.setBlock(pos.above(2), Blocks.GLOWSTONE);
        helper.runAfterDelay(2, () -> stickUpgradeBaseSeedAndFarmlandLoss(helper, pos, crop));
    }

    private static void stickUpgradeBaseSeedAndFarmlandLoss(GameTestHelper helper, BlockPos pos, CropBlockEntity crop) {
        Player player = playerHolding(helper, new ItemStack(Ic2Items.CROP.get(), 2));
        helper.assertTrue(crop.rightclick(player) && crop.isUpgraded(), "crop stick makes a crossing stick");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "one stick used");
        helper.assertTrue(crop.leftclick(player) && !crop.isUpgraded(), "left click removes the crossing stick");
        helper.assertTrue(countItems(helper, pos, Ic2Items.CROP.get()) == 1, "crossing stick dropped");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS, 3));
        helper.assertTrue(crop.rightclick(player), "wheat seeds plant wheat");
        helper.assertTrue(crop.crop() == Crops.WHEAT && crop.getSize() == 1 && crop.getGrowth() == 1
                && crop.getGain() == 1 && crop.getResistance() == 1 && crop.getScanLevel() == 1, "base seed values");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "one seed used");
        helper.assertTrue(crop.rightclick(player) && crop.getNutrientStorage() == 25, "seeds on a planted crop feed it");

        helper.setBlock(pos.below(), Blocks.DIRT);
        helper.assertBlockNotPresent(Ic2Blocks.CROP.get(), pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void harvestAndPickWheat(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        Player player = playerHolding(helper, ItemStack.EMPTY);
        crop.forcePlant(Crops.WHEAT.getId(), 6, 0, 0, 0, 4);
        helper.assertFalse(crop.rightclick(player), "size 6 cannot be harvested");
        crop.setSize(7);
        helper.assertTrue(crop.rightclick(player), "harvest at size 7");
        helper.assertTrue(crop.getSize() == 2, "wheat goes back to size 2");
        helper.assertTrue(countItems(helper, pos, Items.WHEAT) >= 1, "wheat dropped");

        crop.setSize(1);
        helper.assertTrue(crop.leftclick(player) && crop.getCropId() == -1, "picking empties the stick");
        helper.assertTrue(countItems(helper, pos, Ic2Items.CROP_SEED_BAG.get()) == 0
                && countItems(helper, pos, Items.WHEAT_SEEDS) == 0, "size 1 never drops seeds");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void careItemsFillStorages(GameTestHelper helper) {
        CropBlockEntity crop = placeCrop(helper, new BlockPos(2, 2, 2));
        Player player = playerHolding(helper, new ItemStack(Items.WATER_BUCKET));
        crop.forcePlant(Crops.WHEAT.getId(), 1, 1, 1, 1, 1);
        helper.assertTrue(crop.rightclick(player) && crop.getWaterStorage() == 10, "bucket sets water to 10");
        helper.assertTrue(crop.rightclick(player) && player.getMainHandItem().is(Items.WATER_BUCKET),
                "full bucket still used but kept");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.HYDRATING_CELL.get()));
        helper.assertTrue(crop.rightclick(player) && crop.getWaterStorage() == 200, "hydration cell fills to 200");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 190, "cell wears by the water given");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.FERTILIZER.get(), 2));
        helper.assertTrue(crop.rightclick(player) && crop.getNutrientStorage() == 100, "fertilizer +100");
        helper.assertFalse(crop.rightclick(player), "no more fertilizer at 100");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "only one fertilizer used");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.WEED_EX.get()));
        helper.assertTrue(crop.rightclick(player) && crop.getExStorage() == 50, "Weed-EX +50");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "Weed-EX used once");
        crop.rightclick(player);
        helper.assertFalse(crop.applyWeedEx(true), "by hand only below 100");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void crossingBreedsFromTwoNeighbours(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 2, 3);
        CropBlockEntity stick = placeCrop(helper, center);
        CropBlockEntity west = placeCrop(helper, center.west());
        CropBlockEntity east = placeCrop(helper, center.east());
        helper.setBlock(center.above(2), Blocks.GLOWSTONE);
        west.forcePlant(Crops.WHEAT.getId(), 3, 10, 20, 4, 1);
        east.forcePlant(Crops.WHEAT.getId(), 3, 12, 22, 6, 1);
        helper.runAfterDelay(2, () -> {
            stick.rightclick(playerHolding(helper, new ItemStack(Ic2Items.CROP.get())));
            boolean crossed = false;
            for (int i = 0; i < 2000 && !crossed; i++) {
                crossed = stick.attemptCrossing();
            }
            helper.assertTrue(crossed, "crossing happened");
            // since 12.7b any species that can grow on the stick may come out, wheat (2 × 500) most likely
            helper.assertTrue(stick.getCropId() > 0 && stick.getSize() == 1 && !stick.isUpgraded(),
                    "a new plant of size 1");
            helper.assertTrue(Math.abs(stick.getGrowth() - 11) <= 2 && Math.abs(stick.getGain() - 21) <= 2
                    && Math.abs(stick.getResistance() - 5) <= 2, "stats are the average ± parents");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void crossingWeightsFollowIc2(GameTestHelper helper) {
        helper.assertTrue(CropBlockEntity.calculateRatioFor(Crops.WHEAT, Crops.WHEAT) == 500, "same species 500");
        // stat differences 0/4/1/0/3 → 3 + 2 + 1 + 3 + 2, no common attribute, no tier penalty
        helper.assertTrue(CropBlockEntity.calculateRatioFor(Crops.WHEAT, Crops.WEED) == 11, "wheat from weed");
        helper.assertTrue(CropBlockEntity.calculateRatioFor(Crops.WEED, Crops.WHEAT) == 11, "weed from wheat");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void weedSpreadsOnlyWestOrNorth(GameTestHelper helper) {
        BlockPos center = new BlockPos(3, 2, 3);
        CropBlockEntity weed = placeCrop(helper, center);
        CropBlockEntity west = placeCrop(helper, center.west());
        CropBlockEntity east = placeCrop(helper, center.east());
        CropBlockEntity south = placeCrop(helper, center.south());
        weed.forcePlant(Crops.WHEAT.getId(), 2, 30, 0, 0, 0);
        helper.assertTrue(weed.crop().isWeed(weed), "growth ≥ 24 from size 2 counts as weed");
        for (int i = 0; i < 400 && west.getCropId() != Crops.WEED.getId(); i++) {
            weed.generateWeed();
        }
        helper.assertTrue(west.getCropId() == Crops.WEED.getId() && west.getGrowth() >= 30, "west neighbour weeded");
        helper.assertTrue(east.getCropId() == -1 && south.getCropId() == -1, "east and south never (IC2 fall-through)");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void seedBagPlantsAndCropnalyzerScans(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        helper.setBlock(pos.above(2), Blocks.GLOWSTONE);
        helper.runAfterDelay(2, () -> seedBagPlantsAndCropnalyzerScans(helper, pos, crop));
    }

    private static void seedBagPlantsAndCropnalyzerScans(GameTestHelper helper, BlockPos pos, CropBlockEntity crop) {
        ItemStack bag = CropSeedItem.create(Crops.WHEAT.getId(), 5, 6, 7, 0);
        helper.assertTrue(bag.getHoverName().getString().equals("Unknown Seeds"), "unscanned bag name");

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, new ItemStack(Ic2Items.CROPNALYZER.get()));
        CropnalyzerMenu menu = new CropnalyzerMenu(0, player.getInventory(), 0);
        ItemStack battery = ElectricItems.charged(new ItemStack(Ic2Items.RE_BATTERY.get()));
        int charge = ElectricItems.getCharge(battery);
        menu.getSlot(CropnalyzerMenu.SLOT_INPUT).set(bag);
        menu.getSlot(CropnalyzerMenu.SLOT_BATTERY).set(battery);
        menu.scan();
        ItemStack scanned = menu.getScannedStack();
        helper.assertTrue(CropSeedItem.getSeed(scanned).scan() == 1, "scanned to level 1");
        helper.assertTrue(ElectricItems.getCharge(menu.getSlot(CropnalyzerMenu.SLOT_BATTERY).getItem())
                == charge - 10, "level 1 costs 10 EU");
        helper.assertTrue(scanned.getHoverName().getString().equals("Wheat Seeds"), "named after the crop");

        player.setItemInHand(InteractionHand.MAIN_HAND, scanned.copy());
        BlockPos abs = helper.absolutePos(pos);
        InteractionResult result = scanned.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        helper.assertTrue(result.consumesAction() && player.getMainHandItem().isEmpty(), "bag planted and used up");
        helper.assertTrue(crop.crop() == Crops.WHEAT && crop.getGrowth() == 5 && crop.getGain() == 6
                && crop.getResistance() == 7 && crop.getScanLevel() == 1 && crop.getSize() == 1, "bag values");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void environmentValues(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 3, 2);
        helper.setBlock(pos.below(3), Blocks.STONE);
        helper.setBlock(pos.below(2), Blocks.DIRT);
        CropBlockEntity crop = placeCrop(helper, pos);
        crop.setStorage(40, 5, 0);
        int base = crop.updateHumidity() - 2 - 2 - 1;
        helper.setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 6));
        helper.assertTrue(crop.updateHumidity() == base + 2 + 1, "moisture 7 and water ≥ 5 add 2 each");
        int nutrientBase = crop.updateNutrients() - 1 - 2;
        helper.setBlock(pos.below(2), Blocks.STONE);
        helper.assertTrue(crop.updateNutrients() == nutrientBase + 2, "dirt 2 below adds 1, storage 40 adds 2");
        helper.succeed();
    }
}
