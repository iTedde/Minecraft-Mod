package dev.craft.ic2.crop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 {@code CropCocoa} (id 12): grows to 4 with nutrients of 3 or more (400, last step 900), back to 3. */
public class CocoaCrop extends CropCard {
    @Override
    public String name() {
        return "cocoa";
    }

    @Override
    public String discoveredBy() {
        return "Notch";
    }

    @Override
    public int tier() {
        return 3;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0 -> 1;
            case 1 -> 3;
            case 3 -> 4;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Brown", "Food", "Stem"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() == 4 ? 26 : crop.getSize() + 15;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() <= 3 && crop.getNutrients() >= 3;
    }

    @Override
    public int weightInfluences(CropBlockEntity crop, float humidity, float nutrients, float air) {
        return (int) (humidity * 0.8 + nutrients * 1.3 + air * 0.9);
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 4;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(Items.COCOA_BEANS);
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 3 ? 900 : 400;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }
}
