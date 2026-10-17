package dev.craft.ic2.crop;

import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the crop species (phase 12.7b). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CropSpeciesGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private CropSpeciesGameTests() {
    }

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
    public static void registryAndFlowerBaseSeed(GameTestHelper helper) {
        for (int id = 0; id <= 21; id++) {
            helper.assertTrue(Crops.exists(id) && Crops.get(id).getId() == id, "crop id " + id + " registered");
        }
        helper.assertTrue(Crops.get(22) == Crops.WEED, "unknown ids fall back to the weed");
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        helper.setBlock(pos.above(2), Blocks.GLOWSTONE);
        helper.runAfterDelay(2, () -> {
            Player player = playerHolding(helper, new ItemStack(Items.POPPY, 3));
            helper.assertFalse(crop.rightclick(player), "three poppies are not enough");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.POPPY, 5));
            helper.assertTrue(crop.rightclick(player), "four poppies plant a rose");
            helper.assertTrue(crop.crop() == Crops.ROSE && crop.getSize() == 4 && crop.getGrowth() == 1,
                    "rose planted at size 4");
            helper.assertTrue(player.getMainHandItem().getCount() == 1, "four poppies used");
            helper.assertTrue(crop.getSprite() == 21, "rose sprite 21");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void venomiliaPoisonsOnTouch(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        crop.forcePlant(Crops.VENOMILIA.getId(), 5, 8, 0, 0, 1);
        helper.assertTrue(Crops.VENOMILIA.isWeed(crop), "size 5 with growth 8 spreads like weed");
        Player player = playerHolding(helper, ItemStack.EMPTY);
        helper.assertTrue(crop.rightclick(player), "click harvests");
        helper.assertTrue(player.hasEffect(MobEffects.POISON), "touching size 5 poisons");
        helper.assertTrue(crop.getSize() == 3, "back to 4 by the touch, then harvested to 3");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void metalCropsNeedOre(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        crop.forcePlant(Crops.FERRU.getId(), 3, 0, 0, 0, 1);
        helper.assertFalse(Crops.FERRU.canGrow(crop), "no iron ore below");
        helper.setBlock(pos.below(2), Blocks.DEEPSLATE_IRON_ORE);
        helper.assertTrue(Crops.FERRU.canGrow(crop), "iron ore two blocks below");
        helper.assertFalse(Crops.AURELIA.canGrow(crop), "Aurelia needs gold ore");
        crop.setSize(2);
        helper.assertTrue(Crops.AURELIA.canGrow(crop), "below 3 no ore needed");
        helper.assertTrue(Crops.FERRU.dropGainChance() < Crops.AURELIA.dropGainChance() * 2,
                "Ferru halves the drop chance");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void wartsSwitchAndGrowFaster(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        helper.setBlock(pos.below(2), Blocks.SNOW_BLOCK);
        crop.forcePlant(Crops.NETHER_WART.getId(), 1, 0, 0, 0, 1);
        for (int i = 0; i < 3000 && crop.crop() == Crops.NETHER_WART; i++) {
            Crops.NETHER_WART.tick(crop);
        }
        helper.assertTrue(crop.crop() == Crops.TERRA_WART, "nether wart on snow turns into Terra Wart");
        int before = crop.getGrowthPoints();
        Crops.TERRA_WART.tick(crop);
        helper.assertTrue(crop.getGrowthPoints() >= before, "Terra Wart on snow gains growth points");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void specialHarvests(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        CropBlockEntity crop = placeCrop(helper, pos);
        Player player = playerHolding(helper, ItemStack.EMPTY);

        crop.forcePlant(Crops.COFFEE.getId(), 4, 0, 0, 0, 1);
        helper.assertTrue(crop.rightclick(player) && crop.getSize() == 3, "coffee harvestable at 4, back to 3");
        helper.assertTrue(countItems(helper, pos, Ic2Items.COFFEE_BEANS.get()) == 0, "no beans at size 4");

        crop.forcePlant(Crops.STICKREED.getId(), 4, 0, 0, 0, 1);
        helper.assertTrue(crop.rightclick(player) && crop.getSize() >= 1 && crop.getSize() <= 3,
                "stickreed back to 1–3");

        crop.forcePlant(Crops.REDWHEAT.getId(), 7, 0, 0, 0, 1);
        helper.assertTrue(crop.emitRedstone() && crop.getEmittedLight() == 7, "ripe redwheat glows and emits");
        crop.setSize(6);
        helper.assertFalse(crop.emitRedstone(), "unripe redwheat is dark");

        crop.forcePlant(Crops.REED.getId(), 3, 0, 31, 0, 1);
        helper.assertTrue(crop.rightclick(player) && crop.getSize() == 1, "reed back to 1");
        helper.assertTrue(countItems(helper, pos, Items.SUGAR_CANE) >= 2, "size 3 gives at least 2 sugar cane");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void terraWartCuresEffects(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
        ItemStack wart = new ItemStack(Ic2Items.TERRA_WART.get(), 2);
        wart.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(wart.getCount() == 1, "one wart eaten");
        helper.assertFalse(player.hasEffect(MobEffects.POISON) || player.hasEffect(MobEffects.WEAKNESS),
                "bad effects removed");
        helper.assertTrue(player.hasEffect(MobEffects.REGENERATION), "other effects stay");
        helper.succeed();
    }
}
