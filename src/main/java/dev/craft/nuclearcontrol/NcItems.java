package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.item.DigitalThermometerItem;
import dev.craft.nuclearcontrol.item.ReactorSensorKitItem;
import dev.craft.nuclearcontrol.item.SensorLocationCardItem;
import dev.craft.nuclearcontrol.item.ThermometerItem;
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

    public static final DeferredItem<ThermometerItem> THERMOMETER = ITEMS.registerItem("thermometer",
            ThermometerItem::new);
    public static final DeferredItem<DigitalThermometerItem> DIGITAL_THERMOMETER =
            ITEMS.registerItem("digital_thermometer", DigitalThermometerItem::new);
    public static final DeferredItem<ReactorSensorKitItem> REMOTE_SENSOR_KIT = ITEMS.registerItem("remote_sensor_kit",
            ReactorSensorKitItem::new);
    public static final DeferredItem<SensorLocationCardItem> REACTOR_SENSOR_LOCATION_CARD =
            ITEMS.registerItem("reactor_sensor_location_card", SensorLocationCardItem::new);
    /** {@code ItemUpgrade} damage 0: stacks to 64. */
    public static final DeferredItem<Item> RANGE_UPGRADE = ITEMS.registerSimpleItem("range_upgrade");

    private NcItems() {
    }
}
