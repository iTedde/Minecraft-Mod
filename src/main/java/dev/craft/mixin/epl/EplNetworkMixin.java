package dev.craft.mixin.epl;

import com.enhancedplayerlist.network.NetworkHandler;
import com.enhancedplayerlist.network.PlayerStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spec 39 §3: EPL broadcasts its stats payload to every player; a connection without the EPL channel (GameTest mock
 * players, clients without the mod) makes NeoForge throw and breaks the whole broadcast. Send only to players whose
 * connection knows the payload.
 */
@Mixin(value = NetworkHandler.class, remap = false)
public abstract class EplNetworkMixin {
    @Inject(method = "sendToAllPlayers", at = @At("HEAD"), cancellable = true)
    private static void craft$sendToCapablePlayers(PlayerStatsPacket packet, CallbackInfo ci) {
        ci.cancel();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection != null && player.connection.hasChannel(packet)) {
                player.connection.send(packet);
            }
        }
    }
}
