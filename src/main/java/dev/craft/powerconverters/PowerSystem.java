package dev.craft.powerconverters;

import org.jetbrains.annotations.Nullable;

/**
 * Power Converters {@code PowerSystem}: internal energy units (PCU) per unit of input/output, as configured in the
 * pack's {@code PowerConverters.cfg}. The id is the registration order and is synced to the bridge GUI.
 */
public enum PowerSystem {
    BUILDCRAFT("BC", 4375, 4375, null, null, "MJ/t"),
    INDUSTRIALCRAFT("IC2", 1800, 1800, new String[]{"LV", "MV", "HV", "EV"}, new int[]{32, 128, 512, 2048}, "EU/t");

    private final String abbreviation;
    private final int energyPerInput;
    private final int energyPerOutput;
    private final String @Nullable [] voltageNames;
    private final int @Nullable [] voltageValues;
    private final String unit;

    PowerSystem(String abbreviation, int energyPerInput, int energyPerOutput, String @Nullable [] voltageNames,
                int @Nullable [] voltageValues, String unit) {
        this.abbreviation = abbreviation;
        this.energyPerInput = energyPerInput;
        this.energyPerOutput = energyPerOutput;
        this.voltageNames = voltageNames;
        this.voltageValues = voltageValues;
        this.unit = unit;
    }

    public static @Nullable PowerSystem byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }

    public int getId() {
        return ordinal();
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getEnergyPerInput() {
        return energyPerInput;
    }

    public int getEnergyPerOutput() {
        return energyPerOutput;
    }

    public String @Nullable [] getVoltageNames() {
        return voltageNames;
    }

    public int getVoltageValue(int index) {
        return voltageValues == null ? 0 : voltageValues[index];
    }

    public String getUnit() {
        return unit;
    }
}
