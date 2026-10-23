package dev.craft.enderstorage;

import dev.craft.Craft;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<EnderStorageItem> ENDER_CHEST = ITEMS.registerItem("ender_storage_chest",
            properties -> new EnderStorageItem(EsBlocks.ENDER_CHEST.get(), properties));
    public static final DeferredItem<EnderPouchItem> ENDER_POUCH = ITEMS.registerItem("ender_pouch",
            EnderPouchItem::new);
    public static final DeferredItem<EnderStorageItem> ENDER_TANK = ITEMS.registerItem("ender_storage_tank",
            properties -> new EnderStorageItem(EsBlocks.ENDER_TANK.get(), properties));

    private EsItems() {
    }
}
