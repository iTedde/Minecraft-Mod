package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.item.CounterSensorCardItem;
import dev.craft.nuclearcontrol.item.DigitalThermometerItem;
import dev.craft.nuclearcontrol.item.EnergyArrayCardItem;
import dev.craft.nuclearcontrol.item.EnergySensorCardItem;
import dev.craft.nuclearcontrol.item.GtSensorCardItem;
import dev.craft.nuclearcontrol.item.GtSensorKitItem;
import dev.craft.nuclearcontrol.item.LiquidSensorCardItem;
import dev.craft.nuclearcontrol.item.ReactorSensorCardItem;
import dev.craft.nuclearcontrol.item.SensorKitItem;
import dev.craft.nuclearcontrol.item.TextCardItem;
import dev.craft.nuclearcontrol.item.ThermometerItem;
import dev.craft.nuclearcontrol.item.TimeCardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Nuclear Control items (spec 46 §1); the original's metadata items are separate items here. */
public final class NcItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> THERMAL_MONITOR = ITEMS.registerSimpleBlockItem(NcBlocks.THERMAL_MONITOR);
    public static final DeferredItem<BlockItem> INDUSTRIAL_ALARM =
            ITEMS.registerSimpleBlockItem(NcBlocks.INDUSTRIAL_ALARM);
    public static final DeferredItem<BlockItem> HOWLER_ALARM = ITEMS.registerSimpleBlockItem(NcBlocks.HOWLER_ALARM);
    public static final DeferredItem<BlockItem> REMOTE_THERMAL_MONITOR =
            ITEMS.registerSimpleBlockItem(NcBlocks.REMOTE_THERMAL_MONITOR);
    public static final DeferredItem<BlockItem> INFORMATION_PANEL =
            ITEMS.registerSimpleBlockItem(NcBlocks.INFORMATION_PANEL);
    public static final DeferredItem<BlockItem> INFORMATION_PANEL_EXTENDER =
            ITEMS.registerSimpleBlockItem(NcBlocks.INFORMATION_PANEL_EXTENDER);
    public static final DeferredItem<BlockItem> ENERGY_COUNTER = ITEMS.registerSimpleBlockItem(NcBlocks.ENERGY_COUNTER);
    public static final DeferredItem<BlockItem> AVERAGE_COUNTER =
            ITEMS.registerSimpleBlockItem(NcBlocks.AVERAGE_COUNTER);
    public static final DeferredItem<BlockItem> RANGE_TRIGGER = ITEMS.registerSimpleBlockItem(NcBlocks.RANGE_TRIGGER);

    public static final DeferredItem<ThermometerItem> THERMOMETER = ITEMS.registerItem("thermometer",
            ThermometerItem::new);
    public static final DeferredItem<DigitalThermometerItem> DIGITAL_THERMOMETER =
            ITEMS.registerItem("digital_thermometer", DigitalThermometerItem::new);

    public static final DeferredItem<ReactorSensorCardItem> REACTOR_SENSOR_LOCATION_CARD =
            ITEMS.registerItem("reactor_sensor_location_card", ReactorSensorCardItem::new);
    public static final DeferredItem<EnergySensorCardItem> ENERGY_SENSOR_LOCATION_CARD =
            ITEMS.registerItem("energy_sensor_location_card", EnergySensorCardItem::new);
    public static final DeferredItem<EnergyArrayCardItem> ENERGY_ARRAY_LOCATION_CARD =
            ITEMS.registerItem("energy_array_location_card", EnergyArrayCardItem::new);
    public static final DeferredItem<LiquidSensorCardItem> LIQUID_SENSOR_LOCATION_CARD =
            ITEMS.registerItem("liquid_sensor_location_card", LiquidSensorCardItem::new);
    public static final DeferredItem<CounterSensorCardItem> COUNTER_SENSOR_LOCATION_CARD =
            ITEMS.registerItem("counter_sensor_location_card", CounterSensorCardItem::new);
    public static final DeferredItem<TextCardItem> TEXT_CARD = ITEMS.registerItem("text_card", TextCardItem::new);
    public static final DeferredItem<TimeCardItem> TIME_CARD = ITEMS.registerItem("time_card", TimeCardItem::new);

    public static final DeferredItem<SensorKitItem> REMOTE_SENSOR_KIT = ITEMS.registerItem("remote_sensor_kit",
            properties -> new SensorKitItem(properties, REACTOR_SENSOR_LOCATION_CARD, SensorKitItem::reactor));
    public static final DeferredItem<SensorKitItem> ENERGY_SENSOR_KIT = ITEMS.registerItem("energy_sensor_kit",
            properties -> new SensorKitItem(properties, ENERGY_SENSOR_LOCATION_CARD, SensorKitItem::energy));
    public static final DeferredItem<SensorKitItem> LIQUID_SENSOR_KIT = ITEMS.registerItem("liquid_sensor_kit",
            properties -> new SensorKitItem(properties, LIQUID_SENSOR_LOCATION_CARD, SensorKitItem::liquid));
    public static final DeferredItem<SensorKitItem> COUNTER_SENSOR_KIT = ITEMS.registerItem("counter_sensor_kit",
            properties -> new SensorKitItem(properties, COUNTER_SENSOR_LOCATION_CARD, SensorKitItem::counter));

    /** {@code ItemUpgrade} damage 0: stacks to 64. */
    public static final DeferredItem<Item> RANGE_UPGRADE = ITEMS.registerSimpleItem("range_upgrade");
    /** {@code ItemUpgrade} damage 1: enables the colour choice of the information panel. */
    public static final DeferredItem<Item> COLOR_UPGRADE = ITEMS.registerSimpleItem("color_upgrade");

    /** GregTech's addon to Nuclear Control (spec 46 §8): kit in the GregTech tab, card in no tab. */
    public static final DeferredItem<GtSensorCardItem> GT_SENSOR_CARD = ITEMS.registerItem("gt_sensor_card",
            GtSensorCardItem::new);
    public static final DeferredItem<GtSensorKitItem> GT_SENSOR_KIT = ITEMS.registerItem("gt_sensor_kit",
            properties -> new GtSensorKitItem(properties, GT_SENSOR_CARD));

    private NcItems() {
    }
}
