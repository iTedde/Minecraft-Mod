package dev.craft.nuclearcontrol;

import dev.craft.core.CraftCreativeTabs;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Entry point of IC2 Nuclear Control 1.4.5 (spec 46). Everything sits in the IC2 tab. */
public final class NuclearControl {
    private NuclearControl() {
    }

    public static void init(IEventBus modBus) {
        NcDataComponents.COMPONENTS.register(modBus);
        NcBlocks.BLOCKS.register(modBus);
        NcItems.ITEMS.register(modBus);
        NcBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        NcMenus.MENUS.register(modBus);
        NcSounds.SOUNDS.register(modBus);
        NcRecipes.SERIALIZERS.register(modBus);
        modBus.addListener(NuclearControl::registerCapabilities);
        modBus.addListener(NuclearControl::addToCreativeTab);
        modBus.addListener(NcPayloads::register);
    }

    /**
     * The remote thermal monitor and the information panel are {@code IInventory}s with {@code isItemValid}: every slot
     * from every side.
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NcBlockEntityTypes.REMOTE_THERMAL_MONITOR.get(),
                (monitor, side) -> monitor.getInventory());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, NcBlockEntityTypes.INFORMATION_PANEL.get(),
                (panel, side) -> panel.getInventory());
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != CraftCreativeTabs.IC2.get()) {
            return;
        }
        for (DeferredHolder<Item, ? extends Item> holder : NcItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof ElectricItem) {
                event.accept(ElectricItems.charged(new ItemStack(item)),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            event.accept(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
