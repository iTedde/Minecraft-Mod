package dev.craft.ic2.crop;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 {@code CropReed} (id 10): grows to 3, harvested from 2 for size − 1 sugar cane, never trampled. */
public class ReedCrop extends CropCard {
    @Override
    public String name() {
        return "reed";
    }

    @Override
    public String discoveredBy() {
        return "Notch";
    }

    @Override
    public int tier() {
        return 2;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 2 -> 1;
            case 4 -> 2;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Reed"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() + 27;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 3;
    }

    @Override
    public int weightInfluences(CropBlockEntity crop, float humidity, float nutrients, float air) {
        return (int) (humidity * 1.2 + nutrients + air * 0.8);
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() > 1;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(Items.SUGAR_CANE, crop.getSize() - 1);
    }

    @Override
    public boolean onEntityCollision(CropBlockEntity crop, Entity entity) {
        return false;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return 200;
    }
}
