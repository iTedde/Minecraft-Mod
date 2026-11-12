package dev.craft.chargepads.item;

/**
 * Emitter modules (spec 47 §7, original item {@code u} damage 0–7). Field modules sit in the field slot, projector
 * modules in the projector slot; only modules up to the pad tier fit.
 */
public enum PadModule {
    DRAIN_CONVERSION(true, 2, 0),
    DAMAGE_CONVERSION(true, 2, 0),
    ARMOUR_PRIORITY(true, 2, 0),
    PROXIMITY_BOOSTER(false, 2, 0),
    WIDE_BAND_BOOSTER(false, 2, 0),
    FIELD_EXPANSION_ALPHA(false, 2, 1),
    FIELD_EXPANSION_DELTA(false, 3, 2),
    FIELD_EXPANSION_THETA(false, 3, 3);

    private final boolean field;
    private final int minTier;
    private final int expansion;

    PadModule(boolean field, int minTier, int expansion) {
        this.field = field;
        this.minTier = minTier;
        this.expansion = expansion;
    }

    /** Field slot (true) or projector slot (false). */
    public boolean isField() {
        return field;
    }

    public int getMinTier() {
        return minTier;
    }

    /** Horizontal field expansion in blocks (0 for all but the field expansion modules). */
    public int getExpansion() {
        return expansion;
    }
}
