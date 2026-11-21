package dev.craft.chargepads;

/**
 * The three charge pads (spec 47 §2, original enum {@code d}). The tier is also the charge tier (items above it are
 * not charged) and the tier of the battery slot.
 */
public enum PadTier {
    STATIC(1, 40_000, 32, 3.5F, 3, 3, -10, 5, 1, 0),
    CRYSTALIZOR(2, 600_000, 128, 2.5F, 6, 6, 0, 50, 2, 2),
    LAPOTRONIC(3, 10_000_000, 512, 1.5F, 9, 10, 0, 500, 3, 4);

    /** Upgrades stack to 16 per slot in the pad ({@code getInventoryStackLimit}). */
    public static final int SLOT_LIMIT = 16;
    public static final int BATTERY_SLOT = 0;
    /** Module slots of crystalizor and lapotronic pads: projector first, then field (original slots 1 and 2). */
    public static final int PROJECTOR_SLOT = 1;
    public static final int FIELD_SLOT = 2;

    private final int tier;
    private final int baseCapacity;
    private final int baseRate;
    private final float fallLimit;
    private final int fallDamage;
    private final int zapMinRadius;
    private final int zapOffset;
    private final int zapDivisor;
    private final int upgradeSlots;
    private final int breakerZap;

    PadTier(int tier, int baseCapacity, int baseRate, float fallLimit, int fallDamage, int zapMinRadius,
            int zapOffset, int zapDivisor, int upgradeSlots, int breakerZap) {
        this.tier = tier;
        this.baseCapacity = baseCapacity;
        this.baseRate = baseRate;
        this.fallLimit = fallLimit;
        this.fallDamage = fallDamage;
        this.zapMinRadius = zapMinRadius;
        this.zapOffset = zapOffset;
        this.zapDivisor = zapDivisor;
        this.upgradeSlots = upgradeSlots;
        this.breakerZap = breakerZap;
    }

    public int getTier() {
        return tier;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    /** Base charge rate = base maximum input (32/128/512 EU/t). */
    public int getBaseRate() {
        return baseRate;
    }

    /** Capacity added per energy storage upgrade: tier × 10 000 EU. */
    public int getStoragePerUpgrade() {
        return tier * 10_000;
    }

    /** The pad an upgrade kit turns this one into (§8); the lapotronic pad has none. */
    public PadTier next() {
        return this == STATIC ? CRYSTALIZOR : LAPOTRONIC;
    }

    public boolean hasModuleSlots() {
        return tier > 1;
    }

    public int getUpgradeSlots() {
        return upgradeSlots;
    }

    /** Index of the first upgrade slot (after battery and, from the crystalizor on, the two module slots). */
    public int getFirstUpgradeSlot() {
        return hasModuleSlots() ? 3 : 1;
    }

    public int getSlotCount() {
        return getFirstUpgradeSlot() + upgradeSlots;
    }

    /** Falls from more than this distance zap the entity (§6). */
    public boolean isZappingFall(float fallDistance) {
        return fallDistance > fallLimit;
    }

    public int getFallDamage() {
        return fallDamage;
    }

    /**
     * Discharge damage when the pad is broken without a wrench (§6): {@code ⌈max(0, ⌈storage / 1000⌉ − r) / s⌉}.
     */
    public int getDischargeDamage(int storage) {
        int kilo = (int) Math.ceil(storage / 1000.0);
        return (int) Math.ceil(Math.max(0, kilo - zapOffset) / (double) zapDivisor);
    }

    public int getZapRadius(int damage) {
        return Math.max(damage, zapMinRadius);
    }

    /** Extra damage for the player breaking the pad: 0 / 2 / 4. */
    public int getBreakerZap() {
        return breakerZap;
    }
}
