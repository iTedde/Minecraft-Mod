package dev.craft.ironchest.client;

import dev.craft.Craft;
import dev.craft.ironchest.IronChestBlockEntityTypes;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestMenus;
import dev.craft.ironchest.IronChestType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class IronChestClient {
    private IronChestClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(IronChestMenus.IRON_CHEST.get(), IronChestScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(IronChestBlockEntityTypes.IRON_CHEST.get(), IronChestRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        Item[] chests = new Item[IronChestType.values().length];
        for (IronChestType type : IronChestType.values()) {
            chests[type.ordinal()] = IronChestItems.chest(type).get();
        }
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new IronChestRenderer.ItemRenderer();
                }
                return renderer;
            }
        }, chests);
    }
}
