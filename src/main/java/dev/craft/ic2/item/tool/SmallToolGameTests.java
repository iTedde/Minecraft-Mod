package dev.craft.ic2.item.tool;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.block.RubberMatBlock;
import dev.craft.ic2.cable.CableBlockEntity;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ToolboxItem;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the small IC2 tools and blocks (phase 12.1). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class SmallToolGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private SmallToolGameTests() {
    }

    private static UseOnContext click(GameTestHelper helper, Player player, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    @GameTest(template = EMPTY)
    public static void insulationCutterStripsAndInsulates(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack cutter = Ic2Items.INSULATION_CUTTER.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, cutter);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        InsulationCutterItem.cutInsulationFrom(cutter, helper.getLevel(), helper.absolutePos(pos));
        helper.assertBlockPresent(Ic2Blocks.cable(CableType.COPPER).get(), pos);
        helper.assertValueEqual(cutter.getDamageValue(), 3, "damage after stripping");
        helper.assertItemEntityPresent(Ic2Items.RUBBER.get(), pos, 1.0);
        helper.assertValueEqual(cutter.useOn(click(helper, player, pos)), InteractionResult.PASS, "no rubber");
        helper.assertBlockPresent(Ic2Blocks.cable(CableType.COPPER).get(), pos);
        player.getInventory().add(new ItemStack(Ic2Items.RUBBER.get(), 2));
        cutter.useOn(click(helper, player, pos));
        helper.assertBlockPresent(Ic2Blocks.cable(CableType.INSULATED_COPPER).get(), pos);
        helper.assertValueEqual(cutter.getDamageValue(), 4, "damage after insulating");
        helper.assertValueEqual(player.getInventory().countItem(Ic2Items.RUBBER.get()), 1, "rubber left");
        InsulationCutterItem.damageCutter(cutter, InsulationCutterItem.DURABILITY - 4);
        helper.assertFalse(cutter.isEmpty(), "cutter survives at exactly 512 damage");
        InsulationCutterItem.damageCutter(cutter, 1);
        helper.assertTrue(cutter.isEmpty(), "cutter breaks above 512 damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void painterColorsAndEmpties(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack red = Ic2Items.painter(1).toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, red);
        BlockPos cablePos = new BlockPos(1, 1, 1);
        BlockPos woolPos = new BlockPos(3, 1, 1);
        helper.setBlock(cablePos, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.setBlock(woolPos, Blocks.WHITE_WOOL);
        red.useOn(click(helper, player, cablePos));
        CableBlockEntity cable = (CableBlockEntity) helper.getBlockEntity(cablePos);
        helper.assertValueEqual(cable.getColor(), 1, "cable color");
        helper.assertValueEqual(red.getDamageValue(), 1, "damage after one stroke");
        helper.assertValueEqual(red.useOn(click(helper, player, cablePos)), InteractionResult.PASS, "same color");

        red.setDamageValue(PainterItem.DURABILITY - 1);
        red.set(Ic2DataComponents.AUTO_REFILL.get(), true);
        player.getInventory().setItem(5, new ItemStack(Items.RED_DYE, 2));
        red.useOn(click(helper, player, woolPos));
        helper.assertBlockPresent(Blocks.RED_WOOL, woolPos);
        helper.assertValueEqual(red.getDamageValue(), 0, "refilled");
        helper.assertValueEqual(player.getInventory().countItem(Items.RED_DYE), 1, "dye used");

        ItemStack black = Ic2Items.painter(0).toStack();
        black.setDamageValue(PainterItem.DURABILITY - 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, black);
        black.useOn(click(helper, player, cablePos));
        helper.assertValueEqual(cable.getColor(), 0, "black painter removes the color (original bug)");
        helper.assertTrue(player.getMainHandItem().is(Ic2Items.PAINTER.get()), "empty painter after 32 strokes");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void painterTogglesAutoRefill(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack painter = Ic2Items.painter(4).toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, painter);
        painter.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertFalse(painter.getOrDefault(Ic2DataComponents.AUTO_REFILL.get(), false), "needs the mode key");
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.MODE_SWITCH);
        painter.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        Ic2Keyboard.processKeyUpdate(player, 0);
        helper.assertTrue(painter.getOrDefault(Ic2DataComponents.AUTO_REFILL.get(), false), "auto refill on");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void toolboxPacksAndUnpacks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack toolbox = Ic2Items.TOOLBOX.toStack();
        var items = player.getInventory().items;
        items.set(0, toolbox);
        items.set(1, Ic2Items.TREETAP.toStack());
        items.set(2, new ItemStack(Items.DIRT, 5));
        items.set(3, new ItemStack(Ic2Items.cable(CableType.INSULATED_COPPER).get(), 10));
        items.set(4, Ic2Items.FILLED_FUEL_CAN.toStack());
        player.getInventory().selected = 0;
        toolbox.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(ToolboxItem.isFull(toolbox), "packed");
        helper.assertValueEqual(ToolboxItem.getContents(toolbox).size(), 2, "treetap and cables");
        helper.assertTrue(items.get(1).isEmpty() && items.get(3).isEmpty(), "slots emptied");
        helper.assertValueEqual(items.get(2).getCount(), 5, "dirt stays");
        helper.assertFalse(items.get(4).isEmpty(), "fuel can stays");
        helper.assertTrue(toolbox.getHoverName().getString().equals(
                Ic2Items.TREETAP.toStack().getHoverName().getString()), "name of the first content");
        toolbox.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertFalse(ToolboxItem.isFull(toolbox), "unpacked");
        helper.assertTrue(items.get(1).is(Ic2Items.TREETAP.get()), "treetap back in slot 1");
        helper.assertValueEqual(items.get(3).getCount(), 10, "cables back in slot 3");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void euMeterStoresMeasurement(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack meter = Ic2Items.EU_METER.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, meter);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        helper.assertValueEqual(meter.onItemUseFirst(click(helper, player, new BlockPos(1, 0, 1))),
                InteractionResult.PASS, "no energy tile");
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(meter.onItemUseFirst(click(helper, player, pos)), InteractionResult.SUCCESS,
                    "measure cable");
            var measurement = meter.get(Ic2DataComponents.EU_MEASUREMENT.get());
            helper.assertTrue(measurement != null && measurement.pos().equals(helper.absolutePos(pos)),
                    "position stored");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void rubberMatBouncesAndTears(GameTestHelper helper) {
        BlockPos left = new BlockPos(1, 2, 1);
        BlockPos right = new BlockPos(4, 2, 1);
        helper.setBlock(left, Blocks.STONE);
        helper.setBlock(right, Blocks.STONE);
        BlockPos mat = new BlockPos(2, 2, 1);
        helper.setBlock(mat, Ic2Blocks.RUBBER_MAT.get());
        helper.setBlock(new BlockPos(3, 2, 1), Ic2Blocks.RUBBER_MAT.get());
        BlockPos loose = new BlockPos(2, 2, 4);
        helper.setBlock(new BlockPos(1, 2, 4), Blocks.STONE);
        helper.setBlock(loose, Ic2Blocks.RUBBER_MAT.get());

        RubberMatBlock block = Ic2Blocks.RUBBER_MAT.get();
        helper.assertTrue(block.canSupportWeight(helper.getLevel(), helper.absolutePos(mat)), "held on both sides");
        helper.assertBlockProperty(mat, RubberMatBlock.SUPPORTED, true);

        ItemEntity item = new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.STONE));
        item.setDeltaMovement(0.1, -0.5, 0.0);
        BlockState state = helper.getBlockState(mat);
        state.entityInside(helper.getLevel(), helper.absolutePos(mat), item);
        helper.assertTrue(Math.abs(item.getDeltaMovement().y - 0.4) < 1.0E-6, "bounced with factor -0.8");
        helper.assertTrue(Math.abs(item.getDeltaMovement().x - 0.11) < 1.0E-6, "x speed x1.1");

        var pig = helper.spawn(EntityType.PIG, loose);
        helper.getBlockState(loose).entityInside(helper.getLevel(), helper.absolutePos(loose), pig);
        helper.assertBlockNotPresent(Ic2Blocks.RUBBER_MAT.get(), loose);
        helper.succeed();
    }
}
