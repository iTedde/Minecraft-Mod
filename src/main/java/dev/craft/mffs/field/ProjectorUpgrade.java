package dev.craft.mffs.field;

/**
 * The nine projector upgrades ({@code options/*}, spec 48 §9.3). The items and their effects follow in phase 48.3;
 * the modules already know which of them they support.
 */
public enum ProjectorUpgrade {
    TOUCH_DAMAGE,
    SPONGE,
    FIELD_MANIPULATOR,
    BLOCK_BREAKER,
    FIELD_JAMMER,
    CAMOUFLAGE,
    FIELD_FUSION,
    NPC_DEFENSE,
    DEFENSE_STATION
}
