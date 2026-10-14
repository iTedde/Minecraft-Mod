package dev.craft.ic2.cable;

import java.util.Locale;

/** The 13 IC2 cable types (IC2 meta order), values from {@code docs/spec/01-eu-system.md} §3. */
public enum CableType {
    INSULATED_COPPER(0.2, 32, 33, 6, true),
    COPPER(0.3, 8, 33, 4, false),
    GOLD(0.5, 8, 129, 3, false),
    INSULATED_GOLD(0.45, 32, 129, 5, true),
    DOUBLE_INSULATED_GOLD(0.4, 128, 129, 6, true),
    HV(1.0, 0, 2049, 6, false),
    INSULATED_HV(0.95, 128, 2049, 8, true),
    DOUBLE_INSULATED_HV(0.9, 512, 2049, 10, true),
    QUADRUPLE_INSULATED_HV(0.8, 9001, 2049, 12, true),
    GLASS_FIBRE(0.025, 9001, 513, 4, true),
    TIN(0.025, 3, 6, 5, false),
    DETECTOR(0.5, 9001, 2049, 8, false),
    SPLITTER(0.5, 9001, 2049, 8, false);

    public static final int INSULATION_BREAKDOWN_ENERGY = 9001;

    private final double conductionLoss;
    private final int insulationEnergyAbsorption;
    private final int conductorBreakdownEnergy;
    private final int thicknessPixels;
    private final boolean colorable;

    CableType(double conductionLoss, int insulationEnergyAbsorption, int conductorBreakdownEnergy,
              int thicknessPixels, boolean colorable) {
        this.conductionLoss = conductionLoss;
        this.insulationEnergyAbsorption = insulationEnergyAbsorption;
        this.conductorBreakdownEnergy = conductorBreakdownEnergy;
        this.thicknessPixels = thicknessPixels;
        this.colorable = colorable;
    }

    /** Registry path of block and item, e.g. {@code insulated_copper_cable}. */
    public String getName() {
        return name().toLowerCase(Locale.ROOT) + "_cable";
    }

    public double getConductionLoss() {
        return conductionLoss;
    }

    public int getInsulationEnergyAbsorption() {
        return insulationEnergyAbsorption;
    }

    public int getConductorBreakdownEnergy() {
        return conductorBreakdownEnergy;
    }

    /** Cable thickness in block units. */
    public float getThickness() {
        return thicknessPixels / 16.0F;
    }

    public boolean isColorable() {
        return colorable;
    }

    /** Type after adding one insulation layer (insulation cutter, right click), or {@code this}. */
    public CableType insulated() {
        return switch (this) {
            case COPPER -> INSULATED_COPPER;
            case GOLD -> INSULATED_GOLD;
            case INSULATED_GOLD -> DOUBLE_INSULATED_GOLD;
            case HV -> INSULATED_HV;
            case INSULATED_HV -> DOUBLE_INSULATED_HV;
            case DOUBLE_INSULATED_HV -> QUADRUPLE_INSULATED_HV;
            default -> this;
        };
    }

    /** Type after removing one insulation layer (insulation cutter, left click), or {@code this}. */
    public CableType stripped() {
        return switch (this) {
            case INSULATED_COPPER -> COPPER;
            case INSULATED_GOLD -> GOLD;
            case DOUBLE_INSULATED_GOLD -> INSULATED_GOLD;
            case INSULATED_HV -> HV;
            case DOUBLE_INSULATED_HV -> INSULATED_HV;
            case QUADRUPLE_INSULATED_HV -> DOUBLE_INSULATED_HV;
            default -> this;
        };
    }
}
