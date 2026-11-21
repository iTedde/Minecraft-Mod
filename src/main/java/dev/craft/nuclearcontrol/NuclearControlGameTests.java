package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.nuclearcontrol.block.AlarmBlock;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import dev.craft.nuclearcontrol.block.AttachedBlock;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlock;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** IC2 Nuclear Control, phase 46.1 (spec 46 §3–4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class NuclearControlGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos REACTOR = new BlockPos(3, 1, 3);
    private static final BlockPos MONITOR = REACTOR.east();

    private NuclearControlGameTests() {
    }

    private static NuclearReactorBlockEntity reactor(GameTestHelper helper, BlockPos pos, int heat) {
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.NUCLEAR_REACTOR).get());
        NuclearReactorBlockEntity reactor = helper.getBlockEntity(pos);
        reactor.setHeat(heat);
        return reactor;
    }

    /** A flat block lying on stone. */
    private static void onFloor(GameTestHelper helper, BlockPos pos, BlockState state) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, state.setValue(AttachedBlock.FACING, Direction.UP));
    }

    /** Signal of the block at {@code pos} as seen by its neighbour in {@code side}. */
    private static int signalTowards(GameTestHelper helper, BlockPos pos, Direction side) {
        BlockPos absolute = helper.absolutePos(pos);
        return helper.getLevel().getBlockState(absolute).getSignal(helper.getLevel(), absolute, side.getOpposite());
    }

    /** Heat ≥ level: front texture "hot", signal on every side except towards the reactor; below the level off. */
    @GameTest(template = EMPTY)
    public static void thermalMonitorAtHeatLevel(GameTestHelper helper) {
        NuclearReactorBlockEntity reactor = reactor(helper, REACTOR, 600);
        onFloor(helper, MONITOR, NcBlocks.THERMAL_MONITOR.get().defaultBlockState());
        ThermalMonitorBlockEntity monitor = helper.getBlockEntity(MONITOR);
        helper.assertValueEqual(monitor.getHeatLevel(), ThermalMonitorBlockEntity.DEFAULT_HEAT_LEVEL, "default");
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(monitor.getOnFire(), 1, "hot"))
                .thenExecute(() -> {
                    helper.assertBlockProperty(MONITOR, ThermalMonitorBlock.STATUS, 1);
                    helper.assertValueEqual(signalTowards(helper, MONITOR, Direction.EAST), 15, "east");
                    helper.assertValueEqual(signalTowards(helper, MONITOR, Direction.UP), 15, "up");
                    helper.assertValueEqual(signalTowards(helper, MONITOR, Direction.WEST), 0, "towards reactor");
                    reactor.setHeat(499);
                })
                .thenWaitUntil(() -> helper.assertValueEqual(monitor.getOnFire(), 0, "cool"))
                .thenExecute(() -> {
                    helper.assertBlockProperty(MONITOR, ThermalMonitorBlock.STATUS, 0);
                    helper.assertValueEqual(signalTowards(helper, MONITOR, Direction.EAST), 0, "off");
                })
                .thenSucceed();
    }

    /** Without reactor the state is −1 (grey front); inverted the monitor gives a signal. */
    @GameTest(template = EMPTY)
    public static void thermalMonitorWithoutReactorInverted(GameTestHelper helper) {
        onFloor(helper, MONITOR, NcBlocks.THERMAL_MONITOR.get().defaultBlockState());
        ThermalMonitorBlockEntity monitor = helper.getBlockEntity(MONITOR);
        monitor.setInvertRedstone(true);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(monitor.getOnFire(), -1, "no reactor"))
                .thenExecute(() -> {
                    helper.assertBlockProperty(MONITOR, ThermalMonitorBlock.STATUS, 2);
                    helper.assertValueEqual(signalTowards(helper, MONITOR, Direction.EAST), 15, "inverted");
                })
                .thenSucceed();
    }

    /** Losing the supporting block drops the monitor as an item. */
    @GameTest(template = EMPTY)
    public static void thermalMonitorNeedsSupport(GameTestHelper helper) {
        onFloor(helper, MONITOR, NcBlocks.THERMAL_MONITOR.get().defaultBlockState());
        helper.setBlock(MONITOR.below(), Blocks.AIR);
        helper.assertBlockNotPresent(NcBlocks.THERMAL_MONITOR.get(), MONITOR);
        helper.assertItemEntityPresent(NcItems.THERMAL_MONITOR.get(), MONITOR, 2.0);
        helper.succeed();
    }

    /**
     * Remote monitor: 600 EU storage, 1 EU per tick, reads the reactor on its card (here 3 blocks away) and signals at
     * heat ≥ level; without energy the state is −2.
     */
    @GameTest(template = EMPTY)
    public static void remoteThermalMonitorReadsCard(GameTestHelper helper) {
        BlockPos monitorPos = new BlockPos(1, 1, 1);
        BlockPos reactorPos = new BlockPos(4, 1, 1);
        reactor(helper, reactorPos, 700);
        helper.setBlock(monitorPos, NcBlocks.REMOTE_THERMAL_MONITOR.get());
        RemoteThermalMonitorBlockEntity monitor = helper.getBlockEntity(monitorPos);
        helper.assertValueEqual(monitor.getMaxStorage(), 600, "storage");
        helper.assertValueEqual(monitor.getMaxSafeInput(), 32, "LV");
        ItemStack card = new ItemStack(NcItems.REACTOR_SENSOR_LOCATION_CARD.get());
        card.set(NcDataComponents.SENSOR_TARGET.get(), helper.absolutePos(reactorPos));
        monitor.getInventory().setStackInSlot(RemoteThermalMonitorBlockEntity.SLOT_CARD, card);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(monitor.getOnFire(), -2, "no energy"))
                .thenExecute(() -> helper.assertValueEqual(monitor.injectEnergy(Direction.UP, 32), 0, "accepted"))
                .thenWaitUntil(() -> helper.assertValueEqual(monitor.getOnFire(), 700, "hull heat"))
                .thenExecute(() -> {
                    helper.assertTrue(monitor.getEnergy() < 32, "1 EU per tick used");
                    helper.assertValueEqual(signalTowards(helper, monitorPos, Direction.UP), 15, "700 >= 500");
                    monitor.setHeatLevel(701);
                    monitor.injectEnergy(Direction.UP, 32);
                })
                .thenWaitUntil(() -> helper.assertValueEqual(signalTowards(helper, monitorPos, Direction.UP), 0,
                        "700 < 701"))
                .thenSucceed();
    }

    /** Upgrades: storage +10 000 each, transformer ×4 packet size; a packet above the limit blows it up. */
    @GameTest(template = EMPTY)
    public static void remoteThermalMonitorUpgradesAndOvervoltage(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, NcBlocks.REMOTE_THERMAL_MONITOR.get());
        RemoteThermalMonitorBlockEntity monitor = helper.getBlockEntity(pos);
        monitor.getInventory().setStackInSlot(2, new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get(), 2));
        monitor.getInventory().setStackInSlot(3, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));
        helper.assertValueEqual(monitor.getMaxStorage(), 20600, "storage");
        helper.assertValueEqual(monitor.getMaxSafeInput(), 128, "MV");
        helper.assertValueEqual(monitor.getTier(), 2, "tier");
        helper.assertValueEqual(monitor.injectEnergy(Direction.UP, 128), 0, "128 accepted");
        monitor.injectEnergy(Direction.UP, 129);
        helper.assertBlockNotPresent(NcBlocks.REMOTE_THERMAL_MONITOR.get(), pos);
        helper.succeed();
    }

    /** Thermometer: hull heat in the chat, 10 damage; digital: needs 100 EU, uses 50. */
    @GameTest(template = EMPTY)
    public static void thermometers(GameTestHelper helper) {
        reactor(helper, REACTOR, 1234);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack thermometer = new ItemStack(NcItems.THERMOMETER.get());
        useOn(helper, player, thermometer, REACTOR);
        helper.assertValueEqual(thermometer.getDamageValue(), 10, "damage");
        ItemStack digital = new ItemStack(NcItems.DIGITAL_THERMOMETER.get());
        ElectricItems.charge(digital, 99, Integer.MAX_VALUE, true, false);
        useOn(helper, player, digital, REACTOR);
        helper.assertValueEqual(ElectricItems.getCharge(digital), 99, "too little energy");
        ElectricItems.charge(digital, 1, Integer.MAX_VALUE, true, false);
        useOn(helper, player, digital, REACTOR);
        helper.assertValueEqual(ElectricItems.getCharge(digital), 50, "50 EU used");
        helper.succeed();
    }

    /** The remote sensor kit becomes a card for the reactor's position. */
    @GameTest(template = EMPTY)
    public static void remoteSensorKit(GameTestHelper helper) {
        reactor(helper, REACTOR, 0);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NcItems.REMOTE_SENSOR_KIT.get()));
        useOn(helper, player, player.getMainHandItem(), REACTOR);
        ItemStack card = player.getMainHandItem();
        helper.assertTrue(card.is(NcItems.REACTOR_SENSOR_LOCATION_CARD.get()), "card");
        helper.assertValueEqual(card.get(NcDataComponents.SENSOR_TARGET.get()), helper.absolutePos(REACTOR), "target");
        helper.succeed();
    }

    private static void useOn(GameTestHelper helper, Player player, ItemStack stack, BlockPos pos) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(pos);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        stack.onItemUseFirst(context);
    }

    /** Industrial alarm on redstone: the lamp runs 7, 7, 15, 0 (original bug); off again without power. */
    @GameTest(template = EMPTY)
    public static void industrialAlarmLight(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        onFloor(helper, pos, NcBlocks.INDUSTRIAL_ALARM.get().defaultBlockState());
        AlarmBlockEntity alarm = helper.getBlockEntity(pos);
        helper.assertValueEqual(alarm.getRange(), 64, "default range");
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(alarm.isPowered(), "powered"))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos, AlarmBlock.LAMP, 2))
                .thenExecute(() -> helper.setBlock(pos.east(), Blocks.AIR))
                .thenWaitUntil(() -> helper.assertBlockProperty(pos, AlarmBlock.LAMP, 0))
                .thenExecute(() -> helper.assertFalse(alarm.isPowered(), "off"))
                .thenSucceed();
    }
}
