package dev.craft.ic2.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code CropColorFlower} (ids 4–8): grows to 4 at light 12 or more (400, last step 600), harvested back to 3
 * for one dye. Dandelion and Rose are Notch's, the others Alblaka's.
 */
public class ColorFlowerCrop extends CropCard {
    private final String name;
    private final String discoverer;
    private final String[] attributes;
    private final int sprite;
    private final Item dye;

    public ColorFlowerCrop(String name, String discoverer, String[] attributes, int sprite, Item dye) {
        this.name = name;
        this.discoverer = discoverer;
        this.attributes = attributes;
        this.sprite = sprite;
        this.dye = dye;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String discoveredBy() {
        return discoverer;
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0, 1, 4 -> 1;
            case 3 -> 5;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return attributes;
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return switch (crop.getSize()) {
            case 1 -> 12;
            case 2 -> 13;
            case 3 -> 14;
            case 4 -> sprite;
            default -> 0;
        };
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() <= 3 && crop.getLightLevel() >= 12;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 4;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(dye);
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 3 ? 600 : 400;
    }
}
