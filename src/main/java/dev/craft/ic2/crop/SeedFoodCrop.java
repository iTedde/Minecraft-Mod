package dev.craft.ic2.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * IC2 {@code CropSeedFood} (id 20, carrots) and {@code CropPotato} (id 21): grow to 3 at light 9 or more, harvested
 * back to 1. Potatoes are poisonous with 1/50.
 */
public class SeedFoodCrop extends CropCard {
    private final String name;
    private final int sprite;
    private final String color;
    private final String attribute;
    private final Item gain;
    private final boolean potato;

    public SeedFoodCrop(String name, int sprite, String color, String attribute, Item gain, boolean potato) {
        this.name = name;
        this.sprite = sprite;
        this.color = color;
        this.attribute = attribute;
        this.gain = gain;
        this.potato = potato;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 1 -> 4;
            case 4 -> 2;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{color, "Food", attribute};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() < 3 ? 47 + crop.getSize() : sprite;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 3 && crop.getLightLevel() >= 9;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 3;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        if (potato && crop.getLevel().random.nextInt(50) == 0) {
            return new ItemStack(Items.POISONOUS_POTATO);
        }
        return new ItemStack(gain);
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 1;
    }
}
