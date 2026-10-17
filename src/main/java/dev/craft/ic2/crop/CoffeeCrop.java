package dev.craft.ic2.crop;

import javax.annotation.Nullable;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code CropCoffee} (id 18): grows to 5 at light 9 or more (1400; 700 at 3, 2100 at 4). Harvestable from 4, but
 * only size 5 gives coffee beans; back to 3.
 */
public class CoffeeCrop extends CropCard {
    @Override
    public String name() {
        return "coffee";
    }

    @Override
    public String discoveredBy() {
        return "Snoochy";
    }

    @Override
    public int tier() {
        return 7;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0, 2 -> 1;
            case 1 -> 4;
            case 3 -> 2;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Leaves", "Ingrident", "Beans"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        if (crop.getSize() == 5) {
            return 43;
        }
        return crop.getSize() == 4 ? 42 : 31 + crop.getSize();
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 5 && crop.getLightLevel() >= 9;
    }

    @Override
    public int weightInfluences(CropBlockEntity crop, float humidity, float nutrients, float air) {
        return (int) (0.4 * humidity + 1.4 * nutrients + 1.2 * air);
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        if (crop.getSize() == 3) {
            return (int) (super.growthDuration(crop) * 0.5);
        }
        return crop.getSize() == 4 ? (int) (super.growthDuration(crop) * 1.5) : super.growthDuration(crop);
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() >= 4;
    }

    @Nullable
    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return crop.getSize() == 4 ? null : new ItemStack(Ic2Items.COFFEE_BEANS.get());
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }
}
