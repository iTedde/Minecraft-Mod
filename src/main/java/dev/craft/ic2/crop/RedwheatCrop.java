package dev.craft.ic2.crop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * IC2 {@code CropRedWheat} (id 15): grows to 7 at light 5–10; ripe it glows (7) and emits redstone. Harvest: redstone
 * when powered, otherwise redstone or wheat 50:50; back to 2.
 */
public class RedwheatCrop extends CropCard {
    @Override
    public String name() {
        return "redwheat";
    }

    @Override
    public String discoveredBy() {
        return "raa1337";
    }

    @Override
    public int tier() {
        return 6;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0 -> 3;
            case 3 -> 2;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Red", "Redstone", "Wheat"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() == 7 ? 27 : crop.getSize() + 1;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 7 && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 7;
    }

    @Override
    public float dropGainChance() {
        return 0.5F;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return !crop.getLevel().hasNeighborSignal(crop.getBlockPos()) && !crop.getLevel().random.nextBoolean()
                ? new ItemStack(Items.WHEAT) : new ItemStack(Items.REDSTONE);
    }

    @Override
    public boolean emitRedstone(CropBlockEntity crop) {
        return crop.getSize() == 7;
    }

    @Override
    public int getEmittedLight(CropBlockEntity crop) {
        return crop.getSize() == 7 ? 7 : 0;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return 600;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 2;
    }
}
