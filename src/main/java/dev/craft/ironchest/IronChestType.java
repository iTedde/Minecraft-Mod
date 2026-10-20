package dev.craft.ironchest;

import java.util.Locale;

/**
 * Iron Chests {@code IronChestType} with the GUI sizes of {@code GUIChest.GUI} (spec 22 §1). The order is the original
 * metadata order.
 */
public enum IronChestType {
    IRON(54, 9, 184, 202, "ironcontainer"),
    GOLD(81, 9, 184, 256, "goldcontainer"),
    DIAMOND(108, 12, 238, 256, "diamondcontainer"),
    COPPER(45, 9, 184, 184, "coppercontainer"),
    SILVER(72, 9, 184, 238, "silvercontainer"),
    CRYSTAL(108, 12, 238, 256, "diamondcontainer"),
    OBSIDIAN(108, 12, 238, 256, "diamondcontainer");

    /** Number of top stacks the crystal chest shows ({@code TileEntityIronChest.topStacks}). */
    public static final int TOP_STACKS = 8;

    private final int size;
    private final int rowLength;
    private final int guiWidth;
    private final int guiHeight;
    private final String guiTexture;

    IronChestType(int size, int rowLength, int guiWidth, int guiHeight, String guiTexture) {
        this.size = size;
        this.rowLength = rowLength;
        this.guiWidth = guiWidth;
        this.guiHeight = guiHeight;
        this.guiTexture = guiTexture;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int getSize() {
        return size;
    }

    public int getRowLength() {
        return rowLength;
    }

    public int getRowCount() {
        return size / rowLength;
    }

    public int getGuiWidth() {
        return guiWidth;
    }

    public int getGuiHeight() {
        return guiHeight;
    }

    public String getGuiTexture() {
        return guiTexture;
    }

    /** Only the crystal chest is see-through and shows its most common items. */
    public boolean isTransparent() {
        return this == CRYSTAL;
    }

    /** Only the obsidian chest withstands explosions (resistance 10 000). */
    public boolean isExplosionResistant() {
        return this == OBSIDIAN;
    }
}
