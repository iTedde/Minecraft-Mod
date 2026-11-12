package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.item.CardItem;
import dev.craft.mffs.item.ForciciumCellItem;
import dev.craft.mffs.item.multitool.MultitoolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** MFFS basics, phase 48.1 (spec 48 §2–§6, §8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MffsGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final String LARGE = "empty_16x8x16";
    private static final BlockPos CAPACITOR = new BlockPos(2, 1, 2);
    private static final BlockPos MACHINE = new BlockPos(5, 1, 2);

    private MffsGameTests() {
    }

    private static <T extends MffsMachineBlockEntity> T place(GameTestHelper helper, BlockPos pos,
                                                              net.minecraft.world.level.block.Block block) {
        helper.setBlock(pos, block);
        return helper.getBlockEntity(pos);
    }

    /** Switch mode 2 (switch) and switched on. */
    private static void switchOn(MffsMachineBlockEntity machine) {
        while (machine.getSwitchMode() != 2) {
            machine.toggleSwitchMode();
        }
        if (!machine.getSwitchValue()) {
            machine.toggleSwitchValue();
        }
    }

    private static ItemStack powerLink(CapacitorBlockEntity capacitor) {
        ItemStack card = new ItemStack(MffsItems.CARD_POWER_LINK.get());
        CardItem.link(card, capacitor, capacitor.getPowerStorageId(), capacitor.getDeviceName());
        return card;
    }

    /**
     * 4 000 EU + one forcicium (250 cycles) → +12 000 FE per work step; the buffer goes to the linked capacitor
     * (whole buffer when the capacitor has more room).
     */
    @GameTest(template = EMPTY)
    public static void extractorFillsCapacitor(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        ExtractorBlockEntity extractor = place(helper, MACHINE, MffsBlocks.EXTRACTOR.get());
        helper.runAfterDelay(2, () -> {
            extractor.setStack(ExtractorBlockEntity.SLOT_POWER_LINK, powerLink(capacitor));
            extractor.setStack(ExtractorBlockEntity.SLOT_FORCICIUM, new ItemStack(MffsItems.FORCICIUM.get(), 2));
            switchOn(extractor);
            extractor.setWorkEnergy(4000);
        });
        helper.succeedWhen(() -> {
            helper.assertValueEqual(capacitor.getStorageAvailablePower(), 12000, "one pass");
            helper.assertValueEqual(extractor.getWorkCycle(), 249, "cycle used");
            helper.assertValueEqual(extractor.getStack(0).getCount(), 1, "one forcicium taken");
            helper.assertValueEqual(extractor.getWorkEnergy(), 0, "work energy used");
            helper.assertValueEqual(extractor.getPowerSourceId(), capacitor.getDeviceId(), "linked");
        });
    }

    /** EU input: demand only while active, over 2 048 EU the extractor explodes. */
    @GameTest(template = EMPTY)
    public static void extractorEnergyInput(GameTestHelper helper) {
        ExtractorBlockEntity extractor = place(helper, MACHINE, MffsBlocks.EXTRACTOR.get());
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(extractor.demandsEnergy(), 0, "inactive");
            switchOn(extractor);
            extractor.serverTick();
            helper.assertValueEqual(extractor.demandsEnergy(), 4000, "active");
            helper.assertValueEqual(extractor.injectEnergy(Direction.UP, 2048), 0, "stored");
            helper.assertValueEqual(extractor.injectEnergy(Direction.UP, 2048), 96, "rest back");
            extractor.injectEnergy(Direction.UP, 2049);
            helper.assertBlockNotPresent(MffsBlocks.EXTRACTOR.get(), MACHINE);
            helper.succeed();
        });
    }

    /** Cell in slot 4: work energy max 200 000, a full buffer adds one forcicium to the cell. */
    @GameTest(template = EMPTY)
    public static void extractorFillsCell(GameTestHelper helper) {
        ExtractorBlockEntity extractor = place(helper, MACHINE, MffsBlocks.EXTRACTOR.get());
        ItemStack cell = new ItemStack(MffsItems.FORCICIUM_CELL.get());
        helper.runAfterDelay(2, () -> {
            extractor.setStack(ExtractorBlockEntity.SLOT_CELL, cell);
            switchOn(extractor);
            extractor.setWorkEnergy(200000);
        });
        helper.succeedWhen(() -> {
            helper.assertValueEqual(extractor.getMaxWorkEnergy(), 200000, "cell mode");
            helper.assertValueEqual(ForciciumCellItem.getLevel(extractor.getStack(ExtractorBlockEntity.SLOT_CELL)), 1,
                    "one forcicium");
            helper.assertValueEqual(extractor.getWorkEnergy(), 0, "used");
        });
    }

    /** Capacitor charges a crystal in slot 2 at 100 000 FE per 10 ticks. */
    @GameTest(template = EMPTY)
    public static void capacitorChargesCrystal(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        helper.runAfterDelay(2, () -> {
            capacitor.setForcePower(1000000);
            capacitor.setStack(CapacitorBlockEntity.SLOT_LINK, new ItemStack(MffsItems.FORCE_ENERGY_CRYSTAL.get()));
        });
        helper.succeedWhen(() -> {
            ItemStack crystal = capacitor.getStack(CapacitorBlockEntity.SLOT_LINK);
            helper.assertValueEqual(MffsItems.FORCE_ENERGY_CRYSTAL.get().getAvailablePower(crystal), 100000,
                    "one step");
            helper.assertValueEqual(capacitor.getStorageAvailablePower(), 900000, "taken from storage");
            helper.assertValueEqual(capacitor.getPowerLinkMode(), CapacitorBlockEntity.MODE_CHARGE_ITEM, "mode 3");
        });
    }

    /** Range 8 without upgrades: a machine 9 blocks away has no link; one range upgrade (16) links it. */
    @GameTest(template = LARGE)
    public static void powerLinkRange(GameTestHelper helper) {
        BlockPos capacitorPos = new BlockPos(1, 1, 1);
        BlockPos extractorPos = new BlockPos(10, 1, 1);
        CapacitorBlockEntity capacitor = place(helper, capacitorPos, MffsBlocks.CAPACITOR.get());
        ExtractorBlockEntity extractor = place(helper, extractorPos, MffsBlocks.EXTRACTOR.get());
        helper.runAfterDelay(2, () -> extractor.setStack(ExtractorBlockEntity.SLOT_POWER_LINK, powerLink(capacitor)));
        helper.runAfterDelay(15, () -> {
            helper.assertValueEqual(extractor.getPowerSourceId(), 0, "out of range");
            capacitor.setStack(CapacitorBlockEntity.SLOT_RANGE,
                    new ItemStack(MffsItems.CAPACITOR_UPGRADE_RANGE.get()));
        });
        // The device count of a capacitor step still uses the range of the step before (checkslots runs after it).
        helper.runAfterDelay(16, () -> helper.succeedWhen(() -> {
            helper.assertValueEqual(capacitor.getTransmitRange(), 16, "range 16");
            helper.assertValueEqual(extractor.getPowerSourceId(), capacitor.getDeviceId(), "in range");
            helper.assertValueEqual(capacitor.getLinkedDevices(), 1, "one linked device");
        }));
    }

    /** Converter: 3 FE per delivered EU, here 32 EU packets into an active extractor next to it. */
    @GameTest(template = EMPTY)
    public static void converterEmitsEu(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        ConverterBlockEntity converter = place(helper, MACHINE, MffsBlocks.CONVERTER.get());
        ExtractorBlockEntity sink = place(helper, MACHINE.east(), MffsBlocks.EXTRACTOR.get());
        helper.runAfterDelay(2, () -> {
            capacitor.setForcePower(1000000);
            converter.setStack(ConverterBlockEntity.SLOT_POWER_LINK, powerLink(capacitor));
            converter.handleEvent(ConverterBlockEntity.EVENT_PACKET_SIZE, "+");
            helper.assertValueEqual(converter.getPacketSize(), 32, "packet 32");
            converter.handleEvent(ConverterBlockEntity.EVENT_IC_OUTPUT, "");
            converter.handleEvent(ConverterBlockEntity.EVENT_PACKET_SIZE, "+");
            helper.assertValueEqual(converter.getPacketSize(), 32, "locked while output is on");
            switchOn(converter);
            switchOn(sink);
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(sink.getWorkEnergy() > 0, "EU arrived");
            helper.assertValueEqual(1000000 - capacitor.getStorageAvailablePower(), sink.getWorkEnergy() * 3,
                    "3 FE per EU");
        });
    }

    /** Switch mode 1 follows redstone. */
    @GameTest(template = EMPTY)
    public static void redstoneSwitchMode(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        helper.runAfterDelay(2, () -> {
            helper.assertFalse(capacitor.isActive(), "off without signal");
            helper.setBlock(CAPACITOR.east(), Blocks.REDSTONE_BLOCK);
        });
        helper.succeedWhen(() -> helper.assertTrue(capacitor.isActive(), "on with signal"));
    }

    /**
     * Multitool: switch mode only works in switch mode 2 (1 000 FE); the wrench removes a machine on its front side,
     * a power link card in it drops as a blank card; sneak + use switches the mode with the same charge.
     */
    @GameTest(template = EMPTY)
    public static void multitoolSwitchAndWrench(GameTestHelper helper) {
        CapacitorBlockEntity capacitor = place(helper, CAPACITOR, MffsBlocks.CAPACITOR.get());
        CapacitorBlockEntity other = place(helper, MACHINE, MffsBlocks.CAPACITOR.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.runAfterDelay(2, () -> {
            ItemStack switchTool = MffsItems.MULTITOOL_SWITCH.get().charged();
            use(helper, player, switchTool, CAPACITOR, Direction.UP);
            helper.assertFalse(capacitor.getSwitchValue(), "redstone mode: no switch");
            capacitor.toggleSwitchMode();
            use(helper, player, switchTool, CAPACITOR, Direction.UP);
            helper.assertTrue(capacitor.getSwitchValue(), "switched");
            helper.assertValueEqual(MffsItems.MULTITOOL_SWITCH.get().getAvailablePower(switchTool), 999000, "cost");

            ItemStack wrench = MffsItems.MULTITOOL_WRENCH.get().charged();
            other.setStack(CapacitorBlockEntity.SLOT_LINK, powerLink(capacitor));
            use(helper, player, wrench, MACHINE, Direction.EAST);
            helper.assertValueEqual(other.getFacing(), Direction.EAST, "turned");
            use(helper, player, wrench, MACHINE, Direction.EAST);
            helper.assertBlockNotPresent(MffsBlocks.CAPACITOR.get(), MACHINE);
            helper.assertItemEntityPresent(MffsItems.CAPACITOR.get(), MACHINE, 2.0);
            helper.assertItemEntityPresent(MffsItems.CARD_BLANK.get(), MACHINE, 2.0);
            helper.assertValueEqual(MffsItems.MULTITOOL_WRENCH.get().getAvailablePower(wrench), 998000, "2 × 1 000");

            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            ItemStack switched = wrench.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                    .getObject();
            helper.assertTrue(switched.is(MffsItems.MULTITOOL_SWITCH.get()), "next mode");
            helper.assertValueEqual(((MultitoolItem) switched.getItem()).getAvailablePower(switched), 998000,
                    "charge kept");
            helper.succeed();
        });
    }

    private static void use(GameTestHelper helper, Player player, ItemStack stack, BlockPos relative,
                            Direction side) {
        BlockPos pos = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false));
        stack.getItem().onItemUseFirst(stack, context);
    }
}
