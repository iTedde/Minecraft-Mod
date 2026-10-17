package dev.craft.ic2.crop;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.reactor.GradualItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the Crop-Matron (phase 12.7c). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CropMatronGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** One full pass over the 9×3×9 area. */
    private static final int PASS = 243;
    private static final BlockPos MATRON = new BlockPos(4, 2, 4);

    private CropMatronGameTests() {
    }

    private static CropMatronBlockEntity placeMatron(GameTestHelper helper) {
        helper.setBlock(MATRON, Ic2Blocks.machine(Ic2Machine.CROP_MATRON).get());
        return helper.getBlockEntity(MATRON);
    }

    private static CropBlockEntity placeCrop(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        helper.setBlock(pos, Ic2Blocks.CROP.get());
        return (CropBlockEntity) helper.getBlockEntity(pos);
    }

    private static void scanPass(CropMatronBlockEntity matron) {
        for (int i = 0; i < PASS; i++) {
            matron.scan();
        }
    }

    @GameTest(template = EMPTY)
    public static void tendsCropInRange(GameTestHelper helper) {
        CropMatronBlockEntity matron = placeMatron(helper);
        CropBlockEntity crop = placeCrop(helper, MATRON.offset(2, 0, -3));
        matron.getInventory().set(CropMatronBlockEntity.SLOT_FERTILIZER, new ItemStack(Ic2Items.FERTILIZER.get(), 2));
        matron.getInventory().set(CropMatronBlockEntity.SLOT_HYDRATION, new ItemStack(Ic2Items.HYDRATING_CELL.get()));
        matron.getInventory().set(CropMatronBlockEntity.SLOT_WEED_EX, new ItemStack(Ic2Items.WEED_EX.get()));
        matron.setEnergyForTest(CropMatronBlockEntity.MAX_ENERGY);
        int nutrients = crop.getNutrientStorage();
        int water = crop.getWaterStorage();
        int ex = crop.getExStorage();
        scanPass(matron);
        helper.assertValueEqual(crop.getNutrientStorage(), nutrients + 90, "fertilizer +90");
        helper.assertValueEqual(crop.getWaterStorage(), 180, "hydration up to 180");
        helper.assertValueEqual(crop.getExStorage(), ex + 50, "Weed-EX +50");
        helper.assertValueEqual(matron.getInventory().get(CropMatronBlockEntity.SLOT_FERTILIZER).getCount(), 1,
                "one fertilizer used");
        helper.assertValueEqual(matron.getInventory().get(CropMatronBlockEntity.SLOT_HYDRATION).getDamageValue(),
                180 - water, "cell wears by the water added");
        helper.assertValueEqual(matron.getInventory().get(CropMatronBlockEntity.SLOT_WEED_EX).getDamageValue(), 1,
                "Weed-EX wears by one");
        helper.assertValueEqual(matron.getEnergy(), CropMatronBlockEntity.MAX_ENERGY - PASS - 30,
                "1 EU per position, 10 EU per application");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void slotsMoveUpAndStacksRunOut(GameTestHelper helper) {
        CropMatronBlockEntity matron = placeMatron(helper);
        placeCrop(helper, MATRON.offset(-1, 0, 1));
        matron.getInventory().set(2, new ItemStack(Ic2Items.FERTILIZER.get()));
        ItemStack cell = new ItemStack(Ic2Items.HYDRATING_CELL.get());
        cell.setDamageValue(GradualItem.MAX_DAMAGE - 5);
        matron.getInventory().set(CropMatronBlockEntity.SLOT_HYDRATION, cell);
        ItemStack weedEx = new ItemStack(Ic2Items.WEED_EX.get());
        weedEx.setDamageValue(weedEx.getMaxDamage() - 1);
        matron.getInventory().set(8, weedEx);
        scanPass(matron);
        // IC2 moves 1→0 before 2→1, so a stack climbs one slot per crop visit.
        helper.assertTrue(matron.getInventory().get(2).isEmpty()
                && matron.getInventory().get(1).is(Ic2Items.FERTILIZER.get()), "fertilizer moved up one slot");
        scanPass(matron);
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_FERTILIZER).isEmpty(),
                "last fertilizer used");
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_HYDRATION).isEmpty(),
                "used-up cell removed");
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_WEED_EX).isEmpty()
                && matron.getInventory().get(7).isEmpty() && matron.getInventory().get(8).isEmpty(),
                "worn-out Weed-EX removed");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void needsEnergyAndIgnoresOutOfRange(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.CROP_MATRON).get());
        CropMatronBlockEntity matron = helper.getBlockEntity(pos);
        CropBlockEntity far = placeCrop(helper, pos.east(5));
        matron.getInventory().set(CropMatronBlockEntity.SLOT_FERTILIZER, new ItemStack(Ic2Items.FERTILIZER.get(), 4));
        matron.setEnergyForTest(CropMatronBlockEntity.MIN_ENERGY - 1);
        helper.runAfterDelay(5, () -> {
            int nutrients = far.getNutrientStorage();
            helper.assertValueEqual(matron.getEnergy(), CropMatronBlockEntity.MIN_ENERGY - 1, "idle below 31 EU");
            scanPass(matron);
            helper.assertValueEqual(far.getNutrientStorage(), nutrients, "x + 5 is out of range");
            helper.assertValueEqual(matron.getInventory().get(CropMatronBlockEntity.SLOT_FERTILIZER).getCount(), 4,
                    "nothing used");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void sidedSlots(GameTestHelper helper) {
        CropMatronBlockEntity matron = placeMatron(helper);
        IItemHandler down = matron.getItemHandler(Direction.DOWN);
        IItemHandler up = matron.getItemHandler(Direction.UP);
        IItemHandler side = matron.getItemHandler(Direction.NORTH);
        helper.assertTrue(down.getSlots() == 3 && up.getSlots() == 3 && side.getSlots() == 3, "three slots per side");
        down.insertItem(0, new ItemStack(Ic2Items.WEED_EX.get()), false);
        up.insertItem(0, new ItemStack(Ic2Items.HYDRATING_CELL.get()), false);
        side.insertItem(0, new ItemStack(Ic2Items.FERTILIZER.get()), false);
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_WEED_EX).is(Ic2Items.WEED_EX.get()),
                "bottom → slots 6–8");
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_HYDRATION)
                .is(Ic2Items.HYDRATING_CELL.get()), "top → slots 3–5");
        helper.assertTrue(matron.getInventory().get(CropMatronBlockEntity.SLOT_FERTILIZER)
                .is(Ic2Items.FERTILIZER.get()), "sides → slots 0–2");
        helper.succeed();
    }
}
