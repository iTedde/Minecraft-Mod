package dev.craft.ic2.crop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * IC2 {@code CropWheat} (id 1): grows to 7 at light 9 or more, harvested at 7 back to 2; seeds with all stats at 1 or
 * below drop as vanilla wheat seeds.
 */
public class WheatCrop extends CropCard {
    @Override
    public String name() {
        return "wheat";
    }

    @Override
    public String discoveredBy() {
        return "Notch";
    }

    @Override
    public int tier() {
        return 1;
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
        return new String[]{"Yellow", "Food", "Wheat"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() + 1;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 7 && crop.getLightLevel() >= 9;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 7;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(Items.WHEAT);
    }

    @Override
    public ItemStack getSeeds(CropBlockEntity crop) {
        return crop.getGain() <= 1 && crop.getGrowth() <= 1 && crop.getResistance() <= 1
                ? new ItemStack(Items.WHEAT_SEEDS) : super.getSeeds(crop);
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 2;
    }
}
