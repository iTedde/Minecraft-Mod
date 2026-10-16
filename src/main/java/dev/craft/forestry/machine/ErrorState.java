package dev.craft.forestry.machine;

/**
 * Forestry {@code EnumErrorCode}, only the states our machines set. The icon index points into
 * {@code forestry_errors.png} (16 × 16 icons, 16 per row) like the original.
 */
public enum ErrorState {
    OK("ok", 1),
    NO_FUEL("no_fuel", 5),
    NO_RESOURCE("no_resource", 8),
    NO_SPACE("no_space", 17),
    NO_RECIPE("no_recipe", 19),
    FORCED_COOLDOWN("forced_cooldown", 21);

    private static final ErrorState[] VALUES = values();

    private final String name;
    private final int iconIndex;

    ErrorState(String name, int iconIndex) {
        this.name = name;
        this.iconIndex = iconIndex;
    }

    public static ErrorState byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : OK;
    }

    /** Short text, also the ledger tooltip. */
    public String getDescriptionKey() {
        return "gui.craft.forestry.error." + name;
    }

    public String getHelpKey() {
        return "gui.craft.forestry.error." + name + ".help";
    }

    public int getIconIndex() {
        return iconIndex;
    }
}
