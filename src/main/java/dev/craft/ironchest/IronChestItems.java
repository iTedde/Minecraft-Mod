package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Chest block items and the nine upgrade items (spec 22 §1, §3). */
public final class IronChestItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final Map<IronChestType, DeferredItem<BlockItem>> CHESTS = new EnumMap<>(IronChestType.class);
    private static final Map<ChestUpgradeType, DeferredItem<ChestUpgradeItem>> UPGRADES =
            new EnumMap<>(ChestUpgradeType.class);

    static {
        for (IronChestType type : IronChestType.values()) {
            CHESTS.put(type, ITEMS.registerSimpleBlockItem(IronChestBlocks.chest(type)));
        }
        for (ChestUpgradeType type : ChestUpgradeType.values()) {
            UPGRADES.put(type, ITEMS.registerItem(type.getName(),
                    properties -> new ChestUpgradeItem(type, properties.stacksTo(1))));
        }
    }

    public static DeferredItem<BlockItem> chest(IronChestType type) {
        return CHESTS.get(type);
    }

    public static DeferredItem<ChestUpgradeItem> upgrade(ChestUpgradeType type) {
        return UPGRADES.get(type);
    }

    private IronChestItems() {
    }
}
