package dev.craft.powerconverters;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.powerconverters.block.PowerConverterBlock;
import dev.craft.powerconverters.power.BcConsumerBlockEntity;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity;
import dev.craft.powerconverters.power.Ic2ConsumerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the Power Converters. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PowerConvertersGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos BRIDGE = new BlockPos(1, 1, 2);
    private static final BlockPos COMPONENT = BRIDGE.east();
    private static final BlockPos SECOND_COMPONENT = COMPONENT.east();
    private static final BlockPos SECOND_BRIDGE = SECOND_COMPONENT.east();

    private PowerConvertersGameTests() {
    }

    private static EnergyBridgeBlockEntity bridge(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, PcBlocks.ENERGY_BRIDGE.get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY)
    public static void ic2ConsumerStoresEuInBridgeWithoutOvervoltage(GameTestHelper helper) {
        EnergyBridgeBlockEntity bridge = bridge(helper, BRIDGE);
        helper.setBlock(COMPONENT, PcBlocks.converter(ConverterType.IC2_LV_CONSUMER).get());
        Ic2ConsumerBlockEntity consumer = helper.getBlockEntity(COMPONENT);
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(consumer.demandsEnergy(), 16_000_000 / 1800, "free buffer in EU");
            helper.assertValueEqual(consumer.injectEnergy(Direction.EAST, 2048), 0, "2048 EU accepted");
            helper.assertValueEqual(bridge.getEnergyStored(), 2048 * 1800, "1 EU = 1800 PCU");
            helper.assertValueEqual(consumer.getInputRate(), 2048, "rate of this tick");
            helper.runAfterDelay(2, () -> {
                helper.assertBlockPresent(PcBlocks.converter(ConverterType.IC2_LV_CONSUMER).get(), COMPONENT);
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY)
    public static void ic2ProducerSendsOnlyFullPackets(GameTestHelper helper) {
        EnergyBridgeBlockEntity source = bridge(helper, BRIDGE);
        source.setEnergyStored(100_000);
        helper.setBlock(COMPONENT, PcBlocks.converter(ConverterType.IC2_LV_PRODUCER).get());
        helper.setBlock(SECOND_COMPONENT, PcBlocks.converter(ConverterType.IC2_LV_CONSUMER).get());
        EnergyBridgeBlockEntity target = bridge(helper, SECOND_BRIDGE);
        // 55 EU: one 32 EU packet, 23 EU back (the 1000 PCU below one EU are lost), then 23 < 32 sends nothing.
        helper.succeedWhen(() -> {
            helper.assertValueEqual(source.getEnergyStored(), 23 * 1800, "source after one packet");
            helper.assertValueEqual(target.getEnergyStored(), 32 * 1800, "target got one packet");
        });
    }

    @GameTest(template = EMPTY)
    public static void bcConsumerMovesWholeMjAndKeepsFraction(GameTestHelper helper) {
        EnergyBridgeBlockEntity bridge = bridge(helper, BRIDGE);
        helper.setBlock(COMPONENT, PcBlocks.converter(ConverterType.BC_CONSUMER).get());
        BcConsumerBlockEntity consumer = helper.getBlockEntity(COMPONENT);
        consumer.getPowerProvider().setEnergyStored(10.5F);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(bridge.getEnergyStored(), 10 * 4375, "10 MJ = 43750 PCU");
            helper.assertValueEqual(consumer.getPowerProvider().getEnergyStored(), 0.5F, "fraction stays");
        });
    }

    @GameTest(template = EMPTY)
    public static void bcProducerFeedsAdjacentReceptor(GameTestHelper helper) {
        EnergyBridgeBlockEntity source = bridge(helper, BRIDGE);
        source.setEnergyStored(150 * 4375);
        helper.setBlock(COMPONENT, PcBlocks.converter(ConverterType.BC_PRODUCER).get());
        helper.setBlock(SECOND_COMPONENT, PcBlocks.converter(ConverterType.BC_CONSUMER).get());
        EnergyBridgeBlockEntity target = bridge(helper, SECOND_BRIDGE);
        // At most 100 MJ (maxEnergyReceived of the consumer) per tick.
        helper.succeedWhen(() -> {
            helper.assertValueEqual(source.getEnergyStored(), 0, "source empty");
            helper.assertValueEqual(target.getEnergyStored(), 150 * 4375, "all MJ arrived");
        });
    }

    @GameTest(template = EMPTY)
    public static void chargerChargesItemsInAdjacentChest(GameTestHelper helper) {
        BlockPos chest = SECOND_COMPONENT;
        helper.setBlock(chest, Blocks.CHEST);
        ItemStack battery = new ItemStack(Ic2Items.RE_BATTERY.get());
        ((ChestBlockEntity) helper.getBlockEntity(chest)).setItem(0, battery);
        helper.setBlock(COMPONENT, PcBlocks.UNIVERSAL_CHARGER.get());
        EnergyBridgeBlockEntity bridge = bridge(helper, BRIDGE);
        bridge.setEnergyStored(1_000_000);
        helper.succeedWhen(() -> {
            ItemStack stack = ((ChestBlockEntity) helper.getBlockEntity(chest)).getItem(0);
            helper.assertTrue(ElectricItems.getCharge(stack) > 0, "battery charged");
            helper.assertTrue(bridge.getEnergyStored() < 1_000_000, "bridge used");
        });
    }

    @GameTest(template = EMPTY)
    public static void sideTurnsConnectedNextToCable(GameTestHelper helper) {
        helper.setBlock(COMPONENT, PcBlocks.converter(ConverterType.IC2_MV_CONSUMER).get());
        helper.setBlock(SECOND_COMPONENT, Ic2Blocks.cable(CableType.INSULATED_COPPER).get());
        EnergyBridgeBlockEntity bridge = bridge(helper, BRIDGE);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(COMPONENT, PowerConverterBlock.property(Direction.EAST), true);
            helper.assertBlockProperty(COMPONENT, PowerConverterBlock.property(Direction.WEST), false);
            EnergyBridgeBlockEntity.SideData data = bridge.getDataForSide(Direction.EAST);
            helper.assertTrue(data.consumer() && data.connected(), "bridge sees a connected consumer");
            helper.assertValueEqual(data.voltageIndex(), 1, "MV");
        });
    }
}
