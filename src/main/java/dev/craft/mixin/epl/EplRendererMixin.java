package dev.craft.mixin.epl;

import com.enhancedplayerlist.Config;
import com.enhancedplayerlist.client.ClientStatsManager;
import com.enhancedplayerlist.client.PlayerListRenderer;
import com.enhancedplayerlist.data.PlayerStatsData;
import dev.craft.compat.epl.EplMinedBlocks;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spec 39 §3: value of the "blocksMined" column. EPL's own switch has no case for it and puts an empty component; this
 * replaces it with the synced sum (gray for offline players like the other columns).
 */
@Mixin(value = PlayerListRenderer.class, remap = false)
public abstract class EplRendererMixin {
    @Inject(method = "getPlayerStatsMap", at = @At("RETURN"))
    private static void craft$minedBlocks(UUID playerId, CallbackInfoReturnable<Map<String, Component>> cir) {
        Map<String, Component> map = cir.getReturnValue();
        if (playerId == null || map.isEmpty() || !Config.visibleStats.contains(EplMinedBlocks.STAT)) {
            return;
        }
        PlayerStatsData stats = ClientStatsManager.getPlayerStats().get(playerId);
        if (stats == null) {
            return;
        }
        MutableComponent value = Component.literal(EplMinedBlocks.format(stats.getBlocksMined(), Config.compactMode));
        if (!stats.isOnline() && Config.grayOutOffline) {
            value.withStyle(ChatFormatting.GRAY);
        }
        map.put(EplMinedBlocks.STAT, value);
    }
}
