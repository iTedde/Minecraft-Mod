package dev.craft.mffs;

/** Values of the pack's {@code ModularForceFieldSystem.cfg} that Craft uses (spec 48, header). */
public final class MffsConfig {
    /** FE per extractor work cycle ({@code ExtractorPassForceEnergyGenerate}). */
    public static final int EXTRACTOR_PASS_FORCE_ENERGY = 12000;
    /** Work cycles per forcicium ({@code ForceciumWorkCylce}). */
    public static final int FORCICIUM_WORK_CYCLE = 250;
    /** Work cycles per forcicium from a cell ({@code ForceciumCellWorkCylce}). */
    public static final int FORCICIUM_CELL_WORK_CYCLE = 230;
    /** FE per EU of the converter ({@code 12000 / 4000}). */
    public static final int FE_PER_EU = EXTRACTOR_PASS_FORCE_ENERGY / 4000;

    private MffsConfig() {
    }
}
