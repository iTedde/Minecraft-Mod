package dev.craft.ic2.crop;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code CropWeed} (id 0): grows to size 3, cannot be harvested, a left click removes it, never trampled. */
public class WeedCrop extends CropCard {
    @Override
    public String name() {
        return "weed";
    }

    @Override
    public int tier() {
        return 0;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 2 -> 1;
            case 4 -> 5;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Weed", "Bad"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() + 8;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 3;
    }

    @Override
    public boolean leftclick(CropBlockEntity crop, Player player) {
        crop.reset();
        return true;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return false;
    }

    @Nullable
    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return null;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return 300;
    }

    @Override
    public boolean onEntityCollision(CropBlockEntity crop, Entity entity) {
        return false;
    }
}
