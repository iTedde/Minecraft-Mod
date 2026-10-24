package dev.craft.lightshield;

import dev.craft.Craft;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LsItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<ElectricLightShieldItem> ELECTRIC_LIGHT_SHIELD =
            ITEMS.registerItem("electric_light_shield", ElectricLightShieldItem::new);

    private LsItems() {
    }
}
