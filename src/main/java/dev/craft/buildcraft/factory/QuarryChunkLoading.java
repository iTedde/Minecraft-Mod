package dev.craft.buildcraft.factory;

import dev.craft.Craft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/** Forced chunks of the quarries (BuildCraft used a Forge chunk loading ticket per quarry). */
public final class QuarryChunkLoading {
    public static final TicketController CONTROLLER =
            new TicketController(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "quarry"));

    private QuarryChunkLoading() {
    }

    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }
}
