package dev.craft.ironchest;

import java.util.Locale;
import javax.annotation.Nullable;

/**
 * Iron Chests {@code ChestChangerType} (spec 22 §3). A {@code null} source is the vanilla chest ({@code WOOD}). The
 * order is the original one and gives the icon index in {@code item_textures.png}.
 */
public enum ChestUpgradeType {
    IRON_GOLD(IronChestType.IRON, IronChestType.GOLD),
    GOLD_DIAMOND(IronChestType.GOLD, IronChestType.DIAMOND),
    COPPER_SILVER(IronChestType.COPPER, IronChestType.SILVER),
    SILVER_GOLD(IronChestType.SILVER, IronChestType.GOLD),
    COPPER_IRON(IronChestType.COPPER, IronChestType.IRON),
    DIAMOND_CRYSTAL(IronChestType.DIAMOND, IronChestType.CRYSTAL),
    WOOD_IRON(null, IronChestType.IRON),
    WOOD_COPPER(null, IronChestType.COPPER),
    DIAMOND_OBSIDIAN(IronChestType.DIAMOND, IronChestType.OBSIDIAN);

    @Nullable
    private final IronChestType source;
    private final IronChestType target;

    ChestUpgradeType(@Nullable IronChestType source, IronChestType target) {
        this.source = source;
        this.target = target;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT) + "_upgrade";
    }

    /** {@code canUpgrade}: {@code null} asks for the vanilla chest. */
    public boolean canUpgrade(@Nullable IronChestType from) {
        return from == source;
    }

    @Nullable
    public IronChestType getSource() {
        return source;
    }

    public IronChestType getTarget() {
        return target;
    }
}
