package dev.craft.compactsolars;

/**
 * Compact Solars array tiers ({@code CompactSolarType}). Since 2026-10-24 (user) the arrays are leftovers without a
 * function and without a recipe; each one is taken apart into 8 of the tier below ({@code docs/spec/11} §4).
 */
public enum SolarArrayType {
    LV("lv_solar_array", 1),
    MV("mv_solar_array", 2),
    HV("hv_solar_array", 3);

    private final String name;
    private final int tier;

    SolarArrayType(String name, int tier) {
        this.name = name;
        this.tier = tier;
    }

    public String getName() {
        return name;
    }

    /** Charge tier of the charge slot. */
    public int getTier() {
        return tier;
    }
}
