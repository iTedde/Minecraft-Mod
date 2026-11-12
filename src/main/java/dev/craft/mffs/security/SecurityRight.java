package dev.craft.mffs.security;

/** {@code SecurityRight} (spec 48 §7.1). */
public enum SecurityRight {
    /** ForceField Bypass. */
    FFB,
    /** Edit MFFS Block. */
    EB,
    /** Config Security Rights. */
    CSR,
    /** Stay in Area. */
    SR,
    /** Open Secure Storage. */
    OSS,
    /** Change Protected Block (EE3 only in the original, no effect in Craft). */
    RPB,
    /** Allow have all Items. */
    AAI,
    /** Use Control System. */
    UCS
}
