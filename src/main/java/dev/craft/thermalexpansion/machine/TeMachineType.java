package dev.craft.thermalexpansion.machine;

/**
 * Thermal Expansion {@code Machines.Types} with {@code Machines.sideData} and {@code Machines.powerData} (spec 26 §4).
 * Side group {@code g} gives access to slots {@code start[g] .. start[g] + size[g]} (only if {@code show[g]}) and the
 * texture column {@code sideTex[g]}; group 0 is "none", the front is always group 0.
 */
public enum TeMachineType {
    POWERED_FURNACE("powered_furnace", 14, new int[]{0, 0, 1, 1}, new int[]{0, 1, 1, 1}, new int[]{0, 1, 4, 7},
            new boolean[]{false, true, false, true}, new byte[]{1, 1, 2, 2, 2, 2}, 0.5F, 2, 2),
    PULVERIZER("pulverizer", 0, new int[]{0, 0, 1, 3, 1, 1}, new int[]{0, 1, 2, 1, 3, 3}, new int[]{0, 1, 2, 3, 4, 7},
            new boolean[]{false, true, false, false, false, true}, new byte[]{3, 3, 2, 2, 2, 2}, 1.0F, 4, 4),
    SAWMILL("sawmill", 0, new int[]{0, 0, 1, 3, 1, 1}, new int[]{0, 1, 2, 1, 3, 3}, new int[]{0, 1, 2, 3, 4, 7},
            new boolean[]{false, true, false, false, false, true}, new byte[]{3, 3, 2, 2, 2, 2}, 0.5F, 2, 4),
    /** {@code TileSmelter.setFacing}: left side group 5, back side group 6. */
    INDUCTION_SMELTER("induction_smelter", 15, new int[]{0, 0, 2, 4, 2, 0, 1, 2}, new int[]{0, 2, 2, 1, 3, 1, 1, 3},
            new int[]{0, 1, 2, 3, 4, 5, 6, 7}, new boolean[]{false, true, false, false, false, true, true, true},
            new byte[]{3, 3, 2, 2, 2, 2}, 1.0F, 4, 5, 5, 6),
    MAGMA_CRUCIBLE("magma_crucible", 15, new int[]{0, 0, 0}, new int[]{0, 1, 0}, new int[]{0, 1, 4},
            new boolean[]{false, true, false}, new byte[]{1, 1, 2, 2, 2, 2}, 7.5F, 30, 1),
    LIQUID_TRANSPOSER("liquid_transposer", 0, new int[]{0, 0, 1, 0, 1}, new int[]{0, 1, 1, 0, 1},
            new int[]{0, 1, 2, 3, 7}, new boolean[]{false, true, false, false, true}, new byte[]{3, 3, 2, 2, 2, 2},
            1.0F, 4, 2);

    private final String name;
    private final int lightValue;
    private final int[] startGroup;
    private final int[] sizeGroup;
    private final int[] sideTex;
    private final boolean[] showGroup;
    private final byte[] defaultSides;
    private final float minPower;
    private final int maxPower;
    private final int maxEnergy;
    private final float minPowerLevel;
    private final float maxPowerLevel;
    private final float energyRamp;
    private final int slots;
    private final byte leftGroup;
    private final byte backGroup;

    TeMachineType(String name, int lightValue, int[] startGroup, int[] sizeGroup, int[] sideTex, boolean[] showGroup,
                  byte[] defaultSides, float minPower, int maxPower, int slots) {
        this(name, lightValue, startGroup, sizeGroup, sideTex, showGroup, defaultSides, minPower, maxPower, slots, 1, 1);
    }

    TeMachineType(String name, int lightValue, int[] startGroup, int[] sizeGroup, int[] sideTex, boolean[] showGroup,
                  byte[] defaultSides, float minPower, int maxPower, int slots, int leftGroup, int backGroup) {
        this.leftGroup = (byte) leftGroup;
        this.backGroup = (byte) backGroup;
        this.name = name;
        this.lightValue = lightValue;
        this.startGroup = startGroup;
        this.sizeGroup = sizeGroup;
        this.sideTex = sideTex;
        this.showGroup = showGroup;
        this.defaultSides = defaultSides;
        this.minPower = minPower;
        this.maxPower = maxPower;
        // PowerConfig: maxEnergy = 1200 · maxPower, levels at 20 % and 80 % (integer division as in the original).
        this.maxEnergy = maxPower * 1200;
        this.minPowerLevel = 2 * maxEnergy / 10;
        this.maxPowerLevel = 8 * maxEnergy / 10;
        this.energyRamp = maxPowerLevel / maxPower;
        this.slots = slots;
    }

    public String getName() {
        return name;
    }

    /** {@code Machines.LIGHT_VALUES}, while active. */
    public int getLightValue() {
        return lightValue;
    }

    public int getNumGroups() {
        return startGroup.length;
    }

    public int getStart(int group) {
        return startGroup[group];
    }

    /** {@code getSizeInventorySide}: 0 unless the group is shown. */
    public int getAccessSize(int group) {
        return showGroup[group] ? sizeGroup[group] : 0;
    }

    public int getSideTexture(int group) {
        return sideTex[group];
    }

    public byte[] getDefaultSides() {
        return defaultSides.clone();
    }

    public float getMinPower() {
        return minPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public float getMinPowerLevel() {
        return minPowerLevel;
    }

    public float getMaxPowerLevel() {
        return maxPowerLevel;
    }

    public float getEnergyRamp() {
        return energyRamp;
    }

    public int getSlots() {
        return slots;
    }

    /** {@code setFacing}: side group of the side left of the front. */
    public byte getLeftGroup() {
        return leftGroup;
    }

    /** {@code setFacing}: side group of the back. */
    public byte getBackGroup() {
        return backGroup;
    }
}
