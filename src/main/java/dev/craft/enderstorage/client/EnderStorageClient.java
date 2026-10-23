package dev.craft.enderstorage.client;

import dev.craft.Craft;
import dev.craft.enderstorage.EsBlockEntityTypes;
import dev.craft.enderstorage.EsDataComponents;
import dev.craft.enderstorage.EsItems;
import dev.craft.enderstorage.EsMenus;
import dev.craft.enderstorage.Frequency;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class EnderStorageClient {
    private EnderStorageClient() {
    }

    /** Pouch base: global or personal (diamond latch). */
    public static final ResourceLocation PERSONAL_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "personal");
    /** {@code ItemEnderPouch.getColorFromDamage}: band colour per wool colour, from {@code enderpouch.png}. */
    private static final int[] BAND_COLOURS = {0xE3E3E3, 0xE9792C, 0xC65CCF, 0x7293D7, 0xCDBE1E, 0x3DC631,
            0xDD8EA4, 0x4B4B4B, 0x909999, 0x246B89, 0x772EB7, 0x242F8D, 0x52311B, 0x3A5019, 0x9C2B27, 0x161212};

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(EsItems.ENDER_POUCH.get(), PERSONAL_PROPERTY,
                (stack, level, entity, seed) -> frequency(stack).isPersonal() ? 1.0F : 0.0F));
    }

    /** Layers 1–3 are the white band masks, tinted with the colour of button 1–3. */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex >= 1 && tintIndex <= 3
                ? 0xFF000000 | BAND_COLOURS[frequency(stack).colour(tintIndex - 1)] : -1, EsItems.ENDER_POUCH.get());
    }

    private static Frequency frequency(ItemStack stack) {
        return stack.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(EsMenus.ENDER_STORAGE.get(), EnderStorageScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EsBlockEntityTypes.ENDER_CHEST.get(), EnderChestRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EnderChestRenderer.ItemRenderer();
                }
                return renderer;
            }
        }, EsItems.ENDER_CHEST.get());
    }
}
