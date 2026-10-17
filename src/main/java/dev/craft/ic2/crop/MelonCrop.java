package dev.craft.ic2.crop;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 {@code CropMelon} (id 3): grows to 4 (250, last step 700); a melon block (1/3) or 2–5 slices, back to 3. */
public class MelonCrop extends CropCard {
    @Override
    public String name() {
        return "melon";
    }

    @Override
    public String discoveredBy() {
        return "Chao";
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 1 -> 4;
            case 3 -> 2;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Green", "Food", "Stem"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() == 4 ? 20 : crop.getSize() + 15;
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
        RandomSource random = crop.getLevel().random;
        return random.nextInt(3) == 0 ? new ItemStack(Items.MELON)
                : new ItemStack(Items.MELON_SLICE, random.nextInt(4) + 2);
    }

    @Override
    public ItemStack getSeeds(CropBlockEntity crop) {
        return crop.getGain() <= 1 && crop.getGrowth() <= 1 && crop.getResistance() <= 1
                ? new ItemStack(Items.MELON_SEEDS, crop.getLevel().random.nextInt(2) + 1) : super.getSeeds(crop);
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 3 ? 700 : 250;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }
}
