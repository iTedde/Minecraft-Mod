package dev.craft.ic2.client;

import dev.craft.Craft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class Ic2Client {
    private Ic2Client() {
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CableModel.LOADER_ID, CableModel.LOADER);
    }
}
