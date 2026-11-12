package dev.craft.gregtech.machine;

/**
 * {@code IGregTechDeviceInformation} of the machines that give information (spec 46 §8): electrolyzer, centrifuge,
 * blast furnace, grinder, implosion compressor and vacuum freezer. All six report the same three lines.
 */
public interface GtDeviceInformation {
    int getProgressTime();

    int getMaxProgressTime();

    default String getMainInfo() {
        return "Progress:";
    }

    /** {@code mProgresstime / 20 + "secs"}, without a space as in the original. */
    default String getSecondaryInfo() {
        return getProgressTime() / 20 + "secs";
    }

    default String getTertiaryInfo() {
        return "/" + getMaxProgressTime() / 20 + "secs";
    }
}
