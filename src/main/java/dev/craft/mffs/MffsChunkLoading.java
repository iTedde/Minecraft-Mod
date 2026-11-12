package dev.craft.mffs;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

/** Pack {@code Chunkloader=true} (spec 48 §5, §11): every MFFS machine keeps its own chunk loaded. */
public final class MffsChunkLoading {
    public static final TicketController CONTROLLER =
            new TicketController(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mffs"));

    private MffsChunkLoading() {
    }

    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    public static void force(ServerLevel level, BlockPos pos, boolean add) {
        CONTROLLER.forceChunk(level, pos, pos.getX() >> 4, pos.getZ() >> 4, add, true);
    }
}
