package dev.craft.gravisuite.client;

import dev.craft.Craft;
import dev.craft.gravisuite.GsItems;
import dev.craft.gravisuite.item.GraviToolItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GraviSuiteClient {
    /** Item property of the GraviTool: mode 1–4 divided by 4 (pack texture index 16–19). */
    public static final ResourceLocation TOOL_MODE_PROPERTY =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "tool_mode");

    private GraviSuiteClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(GsItems.GRAVITOOL.get(), TOOL_MODE_PROPERTY,
                (stack, level, entity, seed) -> GraviToolItem.getMode(stack) / 4.0F));
    }
}
