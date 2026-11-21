package dev.craft.chargepads;

/**
 * {@code ChargePads.cfg} of the pack (spec 47 head): every switch at its default. Constants instead of a config file
 * (1:1 with the pack, no other value is used).
 */
public final class ChargePadsConfig {
    public static final boolean ALLOW_EMITTER_UPGRADE = true;
    public static final boolean DISCHARGE_ZAP = true;
    public static final boolean FALLING_ZAP = true;
    public static final boolean UPGRADE_ANY_SIDE = false;
    public static final boolean UPGRADE_ZAP = true;
    public static final double UPGRADE_ZAP_MODIFIER = 1.0;

    private ChargePadsConfig() {
    }
}
