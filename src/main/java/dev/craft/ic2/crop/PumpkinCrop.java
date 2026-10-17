package dev.craft.ic2.crop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 {@code CropPumpkin} (id 2): grows to 4 (the last step 600 instead of 200), harvested back to 3. */
public class PumpkinCrop extends CropCard {
    @Override
    public String name() {
        return "pumpkin";
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
            case 1, 4 -> 1;
            case 3 -> 3;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Orange", "Decoration", "Stem"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() + 15;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() <= 3;
    }

    @Override
    public int weightInfluences(CropBlockEntity crop, float humidity, float nutrients, float air) {
        return (int) (humidity * 1.1 + nutrients * 0.9 + air);
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 4;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(Items.PUMPKIN);
    }

    @Override
    public ItemStack getSeeds(CropBlockEntity crop) {
        return crop.getGain() <= 1 && crop.getGrowth() <= 1 && crop.getResistance() <= 1
                ? new ItemStack(Items.PUMPKIN_SEEDS, crop.getLevel().random.nextInt(3) + 1) : super.getSeeds(crop);
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 3 ? 600 : 200;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }
}
