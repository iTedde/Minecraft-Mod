package dev.craft.lightshield.client.compat;

import dev.craft.lightshield.ShieldLights;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.EntityType;

/**
 * LambDynamicLights entry point (spec 30 §3a), named in {@code neoforge.mods.toml} and only loaded when LDL is
 * installed. A player holding a lit shield shines like one holding a torch: light 14 around them. LDL merges this
 * with the player's other light sources (held torch etc.) by taking the maximum.
 */
public final class LightShieldDynamicLights implements DynamicLightsInitializer {
    /** Same as a torch. */
    public static final int LIGHT_LEVEL = 14;

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER, DynamicLightHandler.makeHandler(
                player -> ShieldLights.litShield(player).isEmpty() ? 0 : LIGHT_LEVEL, player -> false));
    }

    /** Abstract in the API, but superseded by the context variant above. */
    @Override
    @SuppressWarnings("removal")
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
    }
}
