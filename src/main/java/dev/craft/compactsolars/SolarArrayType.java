package dev.craft.compactsolars;

import dev.craft.ic2.Ic2Machine;

/**
 * Compact Solars array tiers ({@code CompactSolarType}) as 3×3 multiblocks (user, 2026-10-20; {@code docs/spec/11} §4):
 * the array in the centre and every part of the ring of at most its tier count with their base value (the panels
 * inside: 1 / 8 / 64 / 512), the sum is multiplied by the centre's bonus.
 */
public enum SolarArrayType {
    LV("lv_solar_array", 8, 120, 1, Ic2Machine.LV_TRANSFORMER, 'L'),
    MV("mv_solar_array", 64, 140, 2, Ic2Machine.MV_TRANSFORMER, 'M'),
    HV("hv_solar_array", 512, 150, 3, Ic2Machine.HV_TRANSFORMER, 'H');

    private final String name;
    private final int baseValue;
    private final int bonusPercent;
    private final int tier;
    private final Ic2Machine transformer;
    private final char symbol;

    SolarArrayType(String name, int baseValue, int bonusPercent, int tier, Ic2Machine transformer, char symbol) {
        this.name = name;
        this.baseValue = baseValue;
        this.bonusPercent = bonusPercent;
        this.tier = tier;
        this.transformer = transformer;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    /** EU per tick this array adds to a structure when it sees the sun (8 / 64 / 512, its panels). */
    public int getBaseValue() {
        return baseValue;
    }

    /** Largest packet, i.e. the voltage (8 / 64 / 512, the original output). */
    public int getPacketSize() {
        return baseValue;
    }

    /** Bonus of an array of this tier in the centre, in percent (120 / 140 / 150). */
    public int getBonusPercent() {
        return bonusPercent;
    }

    /** EU per tick of a structure with this centre whose sunny parts add up to {@code sunnyValue}, rounded down. */
    public int output(int sunnyValue) {
        return sunnyValue * bonusPercent / 100;
    }

    /** Charge tier of the charge slot. */
    public int getTier() {
        return tier;
    }

    public Ic2Machine getTransformer() {
        return transformer;
    }

    /** Symbol of this tier's array in the centre of {@link SolarArrayPatterns#layout}. */
    public char getSymbol() {
        return symbol;
    }
}
