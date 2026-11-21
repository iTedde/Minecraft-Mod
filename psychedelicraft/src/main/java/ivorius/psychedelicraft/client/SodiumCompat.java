package ivorius.psychedelicraft.client;

import java.lang.reflect.Method;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.fml.loading.LoadingModList;

/**
 * Tells Sodium that an animated sprite is in use. Reflective, so Sodium stays an optional runtime dependency
 * (upstream compiled against the Sodium API).
 */
public final class SodiumCompat {
    private static final Method MARK_ACTIVE = find();
    private static final Object SPRITE_UTIL = instance();

    private SodiumCompat() {
    }

    private static Method find() {
        if (LoadingModList.get() == null || LoadingModList.get().getModFileById("sodium") == null) {
            return null;
        }
        try {
            Class<?> type = Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
            return type.getMethod("markSpriteActive", TextureAtlasSprite.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static Object instance() {
        if (MARK_ACTIVE == null) {
            return null;
        }
        try {
            return MARK_ACTIVE.getDeclaringClass().getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static void markSpriteActive(TextureAtlasSprite sprite) {
        if (MARK_ACTIVE == null || SPRITE_UTIL == null) {
            return;
        }
        try {
            MARK_ACTIVE.invoke(SPRITE_UTIL, sprite);
        } catch (ReflectiveOperationException e) {
            // ignore, purely an optimisation hint for Sodium
        }
    }
}
