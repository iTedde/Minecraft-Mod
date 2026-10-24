package dev.craft.gravisuite;

import dev.craft.core.CraftCreativeTabs;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.gravisuite.item.UltimateSolarHelmetItem;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Entry point of the GraviSuite content (spec 28). */
public final class GraviSuite {
    private GraviSuite() {
    }

    public static void init(IEventBus modBus) {
        GsDataComponents.COMPONENTS.register(modBus);
        GsSounds.SOUNDS.register(modBus);
        GsItems.MATERIALS.register(modBus);
        GsItems.ITEMS.register(modBus);
        GraviChestplateItem.ATTACHMENT_TYPES.register(modBus);
        UltimateSolarHelmetItem.ATTACHMENT_TYPES.register(modBus);
        modBus.addListener(GraviSuite::addToCreativeTab);
        modBus.addListener(GsKeyPayload::register);
    }

    /** Like the original everything sits in the IC2 tab, electric items charged and empty. */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != CraftCreativeTabs.IC2.get()) {
            return;
        }
        for (DeferredHolder<Item, ? extends Item> holder : GsItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof ElectricItem) {
                event.accept(ElectricItems.charged(new ItemStack(item)),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
