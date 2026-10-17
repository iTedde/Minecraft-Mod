package dev.craft.ic2.crop;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code CropHops} (id 19): grows to 7 at light 9 or more (600 per step), harvested back to 3. */
public class HopsCrop extends CropCard {
    @Override
    public String name() {
        return "hops";
    }

    @Override
    public int tier() {
        return 5;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0, 1 -> 2;
            case 3, 4 -> 1;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Green", "Ingredient", "Wheat"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() >= 5 ? crop.getSize() + 39 : crop.getSize() + 1;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return 600;
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
        return new ItemStack(Ic2Items.HOPS.get());
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }
}
