package dev.craft.core.crafting;

import net.minecraft.world.item.crafting.CraftingInput;
import org.jetbrains.annotations.Nullable;

/**
 * Where a trimmed {@link CraftingInput} sat in its crafting grid. Vanilla drops this when trimming; IC2 recipes with
 * a fixed 3×3 layout ({@code AdvRecipe} with blank rows) need it. Added to {@code CraftingInput} by a mixin and set
 * for every input built from a crafting container.
 */
public interface GridPosition {
    @Nullable
    Position craft$getGridPosition();

    void craft$setGridPosition(Position position);

    /** Grid position of {@code input}, or {@code null} when it was not built from a crafting grid. */
    @Nullable
    static Position of(CraftingInput input) {
        return ((GridPosition) (Object) input).craft$getGridPosition();
    }

    /**
     * @param gridWidth  width of the crafting grid (3 for a crafting table, 2 for the inventory)
     * @param gridHeight height of the crafting grid
     * @param left       column of the trimmed input's first column
     * @param top        row of the trimmed input's first row
     */
    record Position(int gridWidth, int gridHeight, int left, int top) {
    }
}
