package dev.craft.nuclearcontrol;

import java.util.List;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtMachine;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.nuclearcontrol.block.AverageCounterBlockEntity;
import dev.craft.nuclearcontrol.block.CounterBlock;
import dev.craft.nuclearcontrol.block.CounterBlockEntity;
import dev.craft.nuclearcontrol.block.EnergyCounterBlockEntity;
import dev.craft.nuclearcontrol.block.RangeTriggerBlock;
import dev.craft.nuclearcontrol.block.RangeTriggerBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.item.CounterSensorCardItem;
import dev.craft.nuclearcontrol.item.GtSensorCardItem;
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

/** IC2 Nuclear Control, phase 46.3: counters, range trigger, counter and GregTech cards (spec 46 §5.3, §7, §8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CounterGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Counter input west, BatBox (front north) east of it. */
    private static final BlockPos COUNTER = new BlockPos(2, 1, 2);
    private static final BlockPos BATBOX = new BlockPos(3, 1, 2);

    private CounterGameTests() {
    }

    private static <T extends CounterBlockEntity> T counterWithBatBox(GameTestHelper helper, boolean average) {
        helper.setBlock(BATBOX, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.setBlock(COUNTER, (average ? NcBlocks.AVERAGE_COUNTER : NcBlocks.ENERGY_COUNTER).get()
                .defaultBlockState().setValue(CounterBlock.FACING, Direction.WEST));
        return helper.getBlockEntity(COUNTER);
    }

    private static ItemStack useOn(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        stack.onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        return player.getMainHandItem();
    }

    /**
     * Energy counter: accepts only at the front, passes whole 32-EU packets to the BatBox and counts them; reset
     * clears the reading. A transformer upgrade raises the packet size to 128.
     */
    @GameTest(template = EMPTY)
    public static void energyCounterCountsPackets(GameTestHelper helper) {
        EnergyCounterBlockEntity counter = counterWithBatBox(helper, false);
        helper.assertValueEqual(counter.demandsEnergy(), 64, "buffer 2 × 32");
        helper.assertFalse(counter.acceptsEnergyFrom(null, Direction.EAST), "output side takes nothing");
        counter.injectEnergy(Direction.WEST, 32);
        counter.injectEnergy(Direction.WEST, 32);
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(BATBOX);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(counter.getCounter(), 64L, "counted"))
                .thenExecute(() -> {
                    helper.assertValueEqual(batbox.getEnergy(), 64, "BatBox got both packets");
                    counter.reset();
                    helper.assertValueEqual(counter.getCounter(), 0L, "reset");
                    counter.getInventory().setStackInSlot(0, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));
                    helper.assertValueEqual(counter.getPacketSize(), 128, "one transformer upgrade");
                    helper.assertValueEqual(counter.getMaxSafeInput(), 128, "safe input");
                })
                .thenSucceed();
    }

    /** A packet above the packet size removes the counter (explosion 0.8). */
    @GameTest(template = EMPTY)
    public static void counterExplodesOnLargePacket(GameTestHelper helper) {
        EnergyCounterBlockEntity counter = counterWithBatBox(helper, false);
        counter.injectEnergy(Direction.WEST, 33);
        helper.assertBlockNotPresent(NcBlocks.ENERGY_COUNTER.get(), COUNTER);
        helper.succeed();
    }

    /** Average counter: 32 EU every tick averages to 32 EU/t; periods other than 1/3/5/10 are ignored. */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void averageCounterAverages(GameTestHelper helper) {
        AverageCounterBlockEntity counter = counterWithBatBox(helper, true);
        counter.setPeriod(3);
        counter.setPeriod(4);
        helper.assertValueEqual(counter.getPeriod(), 3, "period");
        counter.setPeriod(1);
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(BATBOX);
        helper.onEachTick(() -> {
            batbox.setEnergy(0);
            if (counter.demandsEnergy() >= 32) {
                counter.injectEnergy(Direction.WEST, 32);
            }
        });
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(counter.getAverage(), 32, "average EU/t"))
                .thenSucceed();
    }

    /**
     * Counter kit → counter card; the card reads the energy counter as "Energy: …EU" and the average counter as
     * output. The card can be taken back apart (2 circuits, recipe in datagen).
     */
    @GameTest(template = EMPTY)
    public static void counterKitAndCard(GameTestHelper helper) {
        EnergyCounterBlockEntity counter = counterWithBatBox(helper, false);
        counter.injectEnergy(Direction.WEST, 32);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack card = useOn(helper, player, new ItemStack(NcItems.COUNTER_SENSOR_KIT.get()), COUNTER);
        helper.assertTrue(card.is(NcItems.COUNTER_SENSOR_LOCATION_CARD.get()), "card");
        helper.assertValueEqual(useOn(helper, player, new ItemStack(NcItems.COUNTER_SENSOR_KIT.get()),
                new BlockPos(5, 1, 5)).getItem(), NcItems.COUNTER_SENSOR_KIT.get(), "air: kit stays");
        CounterSensorCardItem item = NcItems.COUNTER_SENSOR_LOCATION_CARD.get();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(counter.getCounter(), 32L, "counted"))
                .thenExecute(() -> {
                    CardData data = CardData.of(card);
                    helper.assertValueEqual(item.update(helper.getLevel(), helper.absolutePos(COUNTER), data, 8),
                            CardState.OK, "state");
                    List<PanelLine> lines = item.lines(CounterSensorCardItem.DISPLAY_ENERGY, data, false,
                            helper.getLevel());
                    helper.assertValueEqual(lines.getFirst().left(), "32", "reading");
                    helper.setBlock(COUNTER, Blocks.AIR);
                    helper.assertValueEqual(item.update(helper.getLevel(), helper.absolutePos(COUNTER), data, 8),
                            CardState.NO_TARGET, "no counter");
                })
                .thenSucceed();
    }

    /**
     * Range trigger: energy card on a BatBox; ≥ upper limit → active (redstone, red display), below the lower limit
     * → passive, in between the state stays; inverted the signal flips at once; without card → unknown.
     */
    @GameTest(template = EMPTY)
    public static void rangeTriggerHysteresis(GameTestHelper helper) {
        BlockPos batboxPos = new BlockPos(1, 1, 1);
        helper.setBlock(batboxPos, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        ElectricStorageBlockEntity batbox = helper.getBlockEntity(batboxPos);
        BlockPos triggerPos = new BlockPos(3, 1, 1);
        helper.setBlock(triggerPos, NcBlocks.RANGE_TRIGGER.get());
        RangeTriggerBlockEntity trigger = helper.getBlockEntity(triggerPos);
        trigger.setLimit(false, 1000L);
        trigger.setLimit(true, 500L);
        ItemStack card = new ItemStack(NcItems.ENERGY_SENSOR_LOCATION_CARD.get());
        card.set(NcDataComponents.SENSOR_TARGET.get(), helper.absolutePos(batboxPos));
        trigger.getInventory().setStackInSlot(RangeTriggerBlockEntity.SLOT_CARD, card);
        helper.assertValueEqual(trigger.getOnFire(), RangeTriggerBlockEntity.STATE_PASSIVE, "empty BatBox");

        batbox.setEnergy(1000);
        trigger.update();
        helper.assertValueEqual(trigger.getOnFire(), RangeTriggerBlockEntity.STATE_ACTIVE, "≥ max");
        helper.assertValueEqual(helper.getBlockState(triggerPos).getValue(RangeTriggerBlock.STATUS), 2, "red");
        helper.assertTrue(trigger.isEmittingSignal(), "signal");
        batbox.setEnergy(700);
        trigger.update();
        helper.assertValueEqual(trigger.getOnFire(), RangeTriggerBlockEntity.STATE_ACTIVE, "between: stays");
        batbox.setEnergy(499);
        trigger.update();
        helper.assertValueEqual(trigger.getOnFire(), RangeTriggerBlockEntity.STATE_PASSIVE, "< min");
        helper.assertFalse(trigger.isEmittingSignal(), "no signal");
        trigger.setInvertRedstone(true);
        helper.assertTrue(trigger.isEmittingSignal(), "inverted");
        helper.assertValueEqual(helper.getLevel().getBestNeighborSignal(helper.absolutePos(triggerPos.east())),
                15, "neighbour sees it");
        trigger.getInventory().setStackInSlot(RangeTriggerBlockEntity.SLOT_CARD, ItemStack.EMPTY);
        helper.assertValueEqual(trigger.getOnFire(), RangeTriggerBlockEntity.STATE_UNKNOWN, "no card");
        helper.assertValueEqual(helper.getBlockState(triggerPos).getValue(RangeTriggerBlock.STATUS), 0, "grey");
        helper.assertFalse(RangeTriggerBlockEntity.isItemValid(RangeTriggerBlockEntity.SLOT_CARD,
                new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get())), "only energy cards");
        helper.succeed();
    }

    /** GregTech kit → GregTech card (no chat message); the card shows "Progress:", "0secs", "/0secs". */
    @GameTest(template = EMPTY)
    public static void gregTechSensorCard(GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(2, 1, 2);
        helper.setBlock(machinePos, GtBlocks.machine(GtMachine.VACUUM_FREEZER).get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack card = useOn(helper, player, new ItemStack(NcItems.GT_SENSOR_KIT.get()), machinePos);
        helper.assertTrue(card.is(NcItems.GT_SENSOR_CARD.get()), "card");
        CardData data = CardData.of(card);
        GtSensorCardItem item = NcItems.GT_SENSOR_CARD.get();
        helper.assertValueEqual(item.update(helper.getLevel(), helper.absolutePos(machinePos), data, 8),
                CardState.OK, "state");
        List<PanelLine> lines = item.lines(GtSensorCardItem.DISPLAY_MAIN | GtSensorCardItem.DISPLAY_SECOND
                | GtSensorCardItem.DISPLAY_TERTIARY, data, true, helper.getLevel());
        helper.assertValueEqual(lines.size(), 3, "three lines");
        helper.assertValueEqual(lines.get(0).left(), "Progress:", "main");
        helper.assertValueEqual(lines.get(1).left(), "0secs", "secondary");
        helper.assertValueEqual(lines.get(2).left(), "/0secs", "tertiary");
        helper.assertTrue(useOn(helper, player, new ItemStack(NcItems.GT_SENSOR_KIT.get()), new BlockPos(5, 1, 5))
                .is(NcItems.GT_SENSOR_KIT.get()), "air: kit stays");
        helper.succeed();
    }
}
