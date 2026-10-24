package dev.craft.lightshield.client.compat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import dev.craft.lightshield.ShieldLights;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * LambDynamicLights entry point (spec 30 §3a), named in {@code neoforge.mods.toml} and only loaded when LDL is
 * installed. Keeps one {@link LightConeBehavior} per player in the client level who holds a lit shield.
 */
public final class LightShieldDynamicLights implements DynamicLightsInitializer {
    private static final Map<UUID, LightConeBehavior> CONES = new HashMap<>();
    private static DynamicLightBehaviorManager manager;

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        manager = context.dynamicLightBehaviorManager();
        NeoForge.EVENT_BUS.addListener(LightShieldDynamicLights::clientTick);
    }

    /** Abstract in the API, but superseded by the context variant above. */
    @Override
    @SuppressWarnings("removal")
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
    }

    private static void clientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        Iterator<LightConeBehavior> it = CONES.values().iterator();
        while (it.hasNext()) {
            LightConeBehavior cone = it.next();
            Player player = cone.player();
            if (player.isRemoved() || player.level() != level || ShieldLights.litShield(player).isEmpty()) {
                cone.remove();
                it.remove();
            } else {
                cone.tick();
            }
        }
        if (level == null) {
            return;
        }
        for (Player player : level.players()) {
            if (!CONES.containsKey(player.getUUID()) && !ShieldLights.litShield(player).isEmpty()) {
                LightConeBehavior cone = new LightConeBehavior(player);
                CONES.put(player.getUUID(), cone);
                manager.add(cone);
            }
        }
    }
}
