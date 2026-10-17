package dev.craft.compactsolars;

import dev.craft.ic2.Ic2Machine;

/**
 * Compact Solars array tiers ({@code CompactSolarType}) as multiblocks: output cap, packet size, EU per sunny panel,
 * charge tier and the transformer the controller is made from. Each tier is a 3×3 grid of the tier below
 * ({@code docs/spec/11} §4). The HV array yields 2 EU per sunny panel up to 1024 EU/t in packets of 512 (user,
 * 2026-10-17).
 */
public enum SolarArrayType {
    LV("lv_solar_array", 8, 8, 1, 1, Ic2Machine.LV_TRANSFORMER, 'L'),
    MV("mv_solar_array", 64, 64, 1, 2, Ic2Machine.MV_TRANSFORMER, 'M'),
    HV("hv_solar_array", 1024, 512, 2, 3, Ic2Machine.HV_TRANSFORMER, 'H');

    private final String name;
    private final int output;
    private final int packetSize;
    private final int panelYield;
    private final int tier;
    private final Ic2Machine transformer;
    private final char symbol;

    SolarArrayType(String name, int output, int packetSize, int panelYield, int tier, Ic2Machine transformer,
                   char symbol) {
        this.name = name;
        this.output = output;
        this.packetSize = packetSize;
        this.panelYield = panelYield;
        this.tier = tier;
        this.transformer = transformer;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    /** Maximum EU per tick (8 / 64 / 1024). */
    public int getOutput() {
        return output;
    }

    /** Largest packet, i.e. the voltage (8 / 64 / 512). */
    public int getPacketSize() {
        return packetSize;
    }

    /** EU per tick and sunny panel (1 / 1 / 2). */
    public int getPanelYield() {
        return panelYield;
    }

    /** Charge tier of the charge slot. */
    public int getTier() {
        return tier;
    }

    public Ic2Machine getTransformer() {
        return transformer;
    }

    /** Symbol of this tier's controller in the layouts of {@link SolarArrayPatterns}. */
    public char getSymbol() {
        return symbol;
    }

    /** Edge length of the square: 3, 9, 27. */
    public int getSize() {
        int size = 3;
        for (int i = 0; i < ordinal(); i++) {
            size *= 3;
        }
        return size;
    }

    /** Distance from the controller to the edge. */
    public int getRadius() {
        return getSize() / 2;
    }
}
