package dev.craft.core.client;

import java.util.function.Consumer;

import net.minecraft.client.renderer.Rect2i;

/**
 * A screen that draws outside its GUI rectangle (ledgers); a recipe viewer integration keeps these areas clear.
 */
public interface ExtraAreas {
    /** Screen coordinates of the current extra areas. */
    void addExtraAreas(Consumer<Rect2i> consumer);
}
