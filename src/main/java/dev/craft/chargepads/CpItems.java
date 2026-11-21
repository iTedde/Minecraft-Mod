package dev.craft.chargepads;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.chargepads.item.EfficiencyUpgradeItem;
import dev.craft.chargepads.item.PadModule;
import dev.craft.chargepads.item.PadModuleItem;
import dev.craft.chargepads.item.UpgradeKitItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** ChargePads items (spec 47 §1); pads and kits stack to 4, upgrades and modules to 64. */
public final class CpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<BlockItem> STATIC_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.STATIC_CHARGE_PAD, new Item.Properties().stacksTo(4));
    public static final DeferredItem<BlockItem> CRYSTALIZOR_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.CRYSTALIZOR_CHARGE_PAD, new Item.Properties().stacksTo(4));
    public static final DeferredItem<BlockItem> LAPOTRONIC_CHARGE_PAD = ITEMS.registerSimpleBlockItem(
            CpBlocks.LAPOTRONIC_CHARGE_PAD, new Item.Properties().stacksTo(4).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<UpgradeKitItem> CRYSTALIZOR_UPGRADE_KIT = ITEMS.registerItem(
            "crystalizor_upgrade_kit", p -> new UpgradeKitItem(p, PadTier.STATIC), new Item.Properties().stacksTo(4));
    /** Intermediate product only: right-clicking a pad with it does nothing (§8). */
    public static final DeferredItem<Item> MFS_UNIT_UPGRADE = ITEMS.registerSimpleItem("mfs_unit_upgrade",
            new Item.Properties().stacksTo(4));
    public static final DeferredItem<UpgradeKitItem> LAPOTRONIC_UPGRADE_KIT = ITEMS.registerItem(
            "lapotronic_upgrade_kit", p -> new UpgradeKitItem(p, PadTier.CRYSTALIZOR),
            new Item.Properties().stacksTo(4).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<EfficiencyUpgradeItem> EFFICIENCY_UPGRADE = ITEMS.registerItem(
            "efficiency_upgrade", EfficiencyUpgradeItem::new, new Item.Properties());

    private static final Map<PadModule, DeferredItem<PadModuleItem>> MODULES = new EnumMap<>(PadModule.class);

    static {
        for (PadModule module : PadModule.values()) {
            Item.Properties properties = new Item.Properties();
            if (module.isUncommon()) {
                properties.rarity(Rarity.UNCOMMON);
            }
            MODULES.put(module, ITEMS.registerItem(module.getName(), p -> new PadModuleItem(p, module), properties));
        }
    }

    private CpItems() {
    }

    public static DeferredItem<PadModuleItem> module(PadModule module) {
        return MODULES.get(module);
    }
}
