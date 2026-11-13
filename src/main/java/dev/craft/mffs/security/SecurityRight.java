package dev.craft.mffs.security;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * {@code SecurityRight} (spec 48 §7.1): key, English name and icon index in {@code AdvSecStationButtons.png}
 * (the ordinal). The security station shows the buttons sorted by key.
 */
public enum SecurityRight {
    /** ForceField Bypass. */
    FFB("ForceField Bypass"),
    /** Edit MFFS Block. */
    EB("Edit MFFS Block"),
    /** Config Security Rights. */
    CSR("Config Security Rights"),
    /** Stay in Area. */
    SR("Stay in Area"),
    /** Open Secure Storage. */
    OSS("Open Secure Storage"),
    /** Change Protected Block (EE3 only in the original, no effect in Craft). */
    RPB("Change Protected Block"),
    /** Allow have all Items. */
    AAI("Allow have all Items"),
    /** Use Control System. */
    UCS("Use Control System");

    /** Button order of the security station: the keys sorted alphabetically. */
    public static final List<SecurityRight> SORTED = Arrays.stream(values())
            .sorted(Comparator.comparing(SecurityRight::name)).toList();

    private final String displayName;

    SecurityRight(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String translationKey() {
        return "gui.craft.mffs.right." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public int mask() {
        return 1 << ordinal();
    }

    public static SecurityRight byKey(String key) {
        for (SecurityRight right : values()) {
            if (right.name().equals(key)) {
                return right;
            }
        }
        return null;
    }
}
