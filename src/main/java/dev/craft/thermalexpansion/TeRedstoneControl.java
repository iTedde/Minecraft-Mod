package dev.craft.thermalexpansion;

/** CoFH {@code IRedstoneControl}: ignore redstone, or run only with the wanted signal. */
public interface TeRedstoneControl {
    boolean getRedstoneDisable();

    boolean getRedstoneState();

    void setRedstone(boolean disable, boolean state);
}
