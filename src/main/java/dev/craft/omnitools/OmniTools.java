package dev.craft.omnitools;

import dev.craft.Craft;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** OmniTools 3.0.4 (spec 50 §1): OmniWrench, Mystical Plinth, Forge Lexicon. */
public final class OmniTools {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    public static final DeferredItem<OmniWrenchItem> OMNIWRENCH =
            ITEMS.registerItem("omniwrench", OmniWrenchItem::new, new Item.Properties());

    private OmniTools() {
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
