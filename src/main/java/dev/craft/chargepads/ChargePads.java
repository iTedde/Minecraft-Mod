package dev.craft.chargepads;

import dev.craft.chargepads.particle.CpParticles;
import dev.craft.core.CraftCreativeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Entry point of Charge Pads 2.4.1 (spec 47). Everything sits in the IC2 tab (§11, decided 2026-11-12). */
public final class ChargePads {
    private ChargePads() {
    }

    public static void init(IEventBus modBus) {
        CpBlocks.BLOCKS.register(modBus);
        CpItems.ITEMS.register(modBus);
        CpBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        CpMenus.MENUS.register(modBus);
        CpParticles.PARTICLE_TYPES.register(modBus);
        modBus.addListener(ChargePads::registerCapabilities);
        modBus.addListener(ChargePads::addToCreativeTab);
    }

    /** {@code IInventory} without sides: every slot from every side, filtered like the GUI slots. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CpBlockEntityTypes.CHARGE_PAD.get(),
                (pad, side) -> pad.getInventory());
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() != CraftCreativeTabs.IC2.get()) {
            return;
        }
        for (DeferredHolder<Item, ? extends Item> holder : CpItems.ITEMS.getEntries()) {
            event.accept(new ItemStack(holder.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
