package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.item.BlankCardItem;
import dev.craft.mffs.item.ForceEnergyCrystalItem;
import dev.craft.mffs.item.ForciciumCellItem;
import dev.craft.mffs.item.InfinitePowerCardItem;
import dev.craft.mffs.item.PowerLinkCardItem;
import dev.craft.mffs.item.ProjectorModuleItem;
import dev.craft.mffs.item.ProjectorUpgradeOptionItem;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import java.util.EnumMap;
import java.util.Map;
import dev.craft.mffs.item.multitool.FieldTeleporterMultitoolItem;
import dev.craft.mffs.item.multitool.SwitchMultitoolItem;
import dev.craft.mffs.item.multitool.WrenchMultitoolItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** MFFS items (spec 48 §1.2) with the original stack sizes. */
public final class MffsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> MONAZIT_ORE = ITEMS.registerSimpleBlockItem(MffsBlocks.MONAZIT_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_MONAZIT_ORE =
            ITEMS.registerSimpleBlockItem(MffsBlocks.DEEPSLATE_MONAZIT_ORE);
    public static final DeferredItem<BlockItem> EXTRACTOR = ITEMS.registerSimpleBlockItem(MffsBlocks.EXTRACTOR);
    public static final DeferredItem<BlockItem> CAPACITOR = ITEMS.registerSimpleBlockItem(MffsBlocks.CAPACITOR);
    public static final DeferredItem<BlockItem> CONVERTER = ITEMS.registerSimpleBlockItem(MffsBlocks.CONVERTER);
    public static final DeferredItem<BlockItem> PROJECTOR = ITEMS.registerSimpleBlockItem(MffsBlocks.PROJECTOR);

    public static final DeferredItem<Item> FORCICIUM = ITEMS.registerSimpleItem("forcicium");
    public static final DeferredItem<ForciciumCellItem> FORCICIUM_CELL = ITEMS.registerItem("forcicium_cell",
            ForciciumCellItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<ForceEnergyCrystalItem> FORCE_ENERGY_CRYSTAL = ITEMS.registerItem(
            "force_energy_crystal", ForceEnergyCrystalItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> PROJECTOR_FOCUS_MATRIX = ITEMS.registerSimpleItem("projector_focus_matrix");
    public static final DeferredItem<Item> FIELD_MODULATOR_DISTANCE =
            ITEMS.registerSimpleItem("field_modulator_distance");
    public static final DeferredItem<Item> FIELD_MODULATOR_STRENGTH =
            ITEMS.registerSimpleItem("field_modulator_strength");
    public static final DeferredItem<Item> EXTRACTOR_BOOSTER = ITEMS.registerSimpleItem("mffs_extractor_booster",
            new Item.Properties().stacksTo(19));
    public static final DeferredItem<Item> CAPACITOR_UPGRADE_CAPACITY = ITEMS.registerSimpleItem(
            "mffs_capacitor_upgrade_capacity", new Item.Properties().stacksTo(9));
    public static final DeferredItem<Item> CAPACITOR_UPGRADE_RANGE = ITEMS.registerSimpleItem(
            "mffs_capacitor_upgrade_range", new Item.Properties().stacksTo(9));
    public static final DeferredItem<BlankCardItem> CARD_BLANK = ITEMS.registerItem("mffs_card_blank",
            BlankCardItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<PowerLinkCardItem> CARD_POWER_LINK = ITEMS.registerItem("mffs_card_power_link",
            PowerLinkCardItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<InfinitePowerCardItem> CARD_INFINITE_POWER = ITEMS.registerItem(
            "mffs_card_infinite_power", InfinitePowerCardItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<WrenchMultitoolItem> MULTITOOL_WRENCH = ITEMS.registerItem(
            "mffs_multitool_wrench", WrenchMultitoolItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<SwitchMultitoolItem> MULTITOOL_SWITCH = ITEMS.registerItem(
            "mffs_multitool_switch", SwitchMultitoolItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<FieldTeleporterMultitoolItem> MULTITOOL_FIELD_TELEPORTER = ITEMS.registerItem(
            "mffs_multitool_field_teleporter", FieldTeleporterMultitoolItem::new, new Item.Properties().stacksTo(1));

    /** Projector modules (stack 8), registry {@code projector_module_<shape>}. */
    public static final Map<ProjectorModule, DeferredItem<ProjectorModuleItem>> MODULES = new EnumMap<>(
            ProjectorModule.class);

    /** Projector upgrades (stack 8), registry {@code projector_upgrade_<name>}. */
    public static final Map<ProjectorUpgrade, DeferredItem<ProjectorUpgradeOptionItem>> UPGRADES = new EnumMap<>(
            ProjectorUpgrade.class);

    static {
        for (ProjectorModule module : ProjectorModule.values()) {
            MODULES.put(module, ITEMS.registerItem("projector_module_" + module.name().toLowerCase(java.util.Locale.ROOT),
                    properties -> new ProjectorModuleItem(module, properties), new Item.Properties().stacksTo(8)));
        }
        for (ProjectorUpgrade upgrade : ProjectorUpgrade.values()) {
            UPGRADES.put(upgrade, ITEMS.registerItem("projector_upgrade_"
                            + upgrade.name().toLowerCase(java.util.Locale.ROOT),
                    properties -> new ProjectorUpgradeOptionItem(upgrade, properties),
                    new Item.Properties().stacksTo(8)));
        }
    }

    private MffsItems() {
    }

    public static DeferredItem<ProjectorModuleItem> module(ProjectorModule module) {
        return MODULES.get(module);
    }

    public static DeferredItem<ProjectorUpgradeOptionItem> upgrade(ProjectorUpgrade upgrade) {
        return UPGRADES.get(upgrade);
    }
}
