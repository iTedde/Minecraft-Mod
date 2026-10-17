package dev.craft.ic2.crop;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * IC2 {@code CropStickReed} (id 11): sugar cane like the reed up to 3, sticky resin at 4 (then back to 1–3), never
 * trampled.
 */
public class StickreedCrop extends CropCard {
    @Override
    public String name() {
        return "stickreed";
    }

    @Override
    public String discoveredBy() {
        return "raa1337";
    }

    @Override
    public int tier() {
        return 4;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0 -> 2;
            case 2, 4 -> 1;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Reed", "Resin"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() + 27;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 4;
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
        return crop.getSize() <= 3 ? new ItemStack(Items.SUGAR_CANE, crop.getSize() - 1)
                : new ItemStack(Ic2Items.RESIN.get());
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return crop.getSize() == 4 ? 3 - crop.getLevel().random.nextInt(3) : 1;
    }

    @Override
    public boolean onEntityCollision(CropBlockEntity crop, Entity entity) {
        return false;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 4 ? 400 : 100;
    }
}
