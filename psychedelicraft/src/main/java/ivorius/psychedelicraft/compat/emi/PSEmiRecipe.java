package ivorius.psychedelicraft.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.widget.WidgetHolder;

/**
 * Common base of all Psychedelicraft EMI recipes (upstream: {@code compat/tia/PSRecipe}).
 */
abstract class PSEmiRecipe implements EmiRecipe {
    @Override
    public abstract PSEmiCategory getCategory();

    @Override
    public int getDisplayWidth() {
        return getCategory().width;
    }

    @Override
    public int getDisplayHeight() {
        return getCategory().height;
    }

    static void addArrow(WidgetHolder widgets, int x, int y) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y);
    }

    static void addAnimatedFlame(WidgetHolder widgets, int x, int y, int millis) {
        widgets.addTexture(EmiTexture.EMPTY_FLAME, x, y);
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, x, y, millis, false, true, true);
    }

    /** Arrow fill time in real milliseconds for a process of {@code ticks} game ticks, kept watchable. */
    static int arrowMillis(long ticks) {
        return (int) Math.max(1000, Math.min(20_000, ticks * 50));
    }
}
