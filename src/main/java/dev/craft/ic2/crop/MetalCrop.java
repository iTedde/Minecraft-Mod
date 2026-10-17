package dev.craft.ic2.crop;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * IC2 {@code CropFerru} (id 13) and {@code CropAurelia} (id 14): the last step to 4 needs the ore 1–3 blocks below;
 * harvested back to 2. Ferru halves the drop chance.
 */
public class MetalCrop extends CropCard {
    private final String name;
    private final int tier;
    private final int color;
    private final String[] attributes;
    private final int ripeSprite;
    private final Block[] ores;
    private final Supplier<? extends Item> gain;
    private final float gainFactor;
    private final int lastStep;
    private final int step;

    public MetalCrop(String name, int tier, int color, String[] attributes, int ripeSprite, Block[] ores,
            Supplier<? extends Item> gain, float gainFactor, int step, int lastStep) {
        this.name = name;
        this.tier = tier;
        this.color = color;
        this.attributes = attributes;
        this.ripeSprite = ripeSprite;
        this.ores = ores;
        this.gain = gain;
        this.gainFactor = gainFactor;
        this.step = step;
        this.lastStep = lastStep;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int tier() {
        return tier;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 0 -> 2;
            case 3 -> color;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return attributes;
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        return crop.getSize() == 4 ? ripeSprite : 31 + crop.getSize();
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 3 || crop.getSize() == 3 && crop.isBlockBelow(ores);
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 4;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(gain.get());
    }

    @Override
    public float dropGainChance() {
        return super.dropGainChance() / gainFactor;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() == 3 ? lastStep : step;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 2;
    }
}
