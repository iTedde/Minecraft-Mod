package dev.craft.chargepads.item;

import java.util.Locale;

/**
 * Emitter modules (spec 47 §7, original item {@code u} damage 0–7). Field modules sit in the field slot, projector
 * modules in the projector slot; only modules up to the pad tier fit.
 */
public enum PadModule {
    DRAIN_CONVERSION(true, 2, 0, false),
    DAMAGE_CONVERSION(true, 2, 0, true),
    ARMOUR_PRIORITY(true, 2, 0, false),
    PROXIMITY_BOOSTER(false, 2, 0, false),
    WIDE_BAND_BOOSTER(false, 2, 0, true),
    FIELD_EXPANSION_ALPHA(false, 2, 1, false),
    FIELD_EXPANSION_DELTA(false, 3, 2, false),
    FIELD_EXPANSION_THETA(false, 3, 3, true);

    private final boolean field;
    private final int minTier;
    private final int expansion;
    private final boolean uncommon;

    PadModule(boolean field, int minTier, int expansion, boolean uncommon) {
        this.field = field;
        this.minTier = minTier;
        this.expansion = expansion;
        this.uncommon = uncommon;
    }

    /** Registry name, e.g. {@code drain_conversion_module}, {@code field_expansion_module_alpha}. */
    public String getName() {
        String name = name().toLowerCase(Locale.ROOT);
        return expansion > 0 ? name.replace("field_expansion", "field_expansion_module") : name + "_module";
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

    public boolean isUncommon() {
        return uncommon;
    }

    /** Drain and damage conversion: only available with {@code allowEmitterUpgrade} (original {@code u.o}). */
    public boolean isEmitter() {
        return this == DRAIN_CONVERSION || this == DAMAGE_CONVERSION;
    }
}
