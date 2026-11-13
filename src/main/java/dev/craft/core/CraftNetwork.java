package dev.craft.core;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

/** Network helpers shared by the packages. */
public final class CraftNetwork {
    private CraftNetwork() {
    }

    /**
     * Like {@code PacketDistributor.sendToPlayersTrackingChunk}, but skips connections that never negotiated the
     * payload. Real clients always have Craft's channels; GameTest mock players have none, and the plain call throws
     * as soon as one of them stands near a ticking pipe.
     */
    public static void sendToTracking(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
            if (player.connection != null && player.connection.hasChannel(payload)) {
                player.connection.send(payload);
            }
        }
    }
}
