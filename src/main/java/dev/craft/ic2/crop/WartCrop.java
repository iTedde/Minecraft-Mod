package dev.craft.ic2.crop;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * IC2 {@code CropNetherWart} (id 16) and {@code CropTerraWart} (id 17): grow to 3; on their own block below (soul
 * sand / snow block) they gain half a growth rate extra each cycle, on the other one they turn into the other wart
 * with 1/300. The switch does not re-render, as in IC2.
 */
public class WartCrop extends CropCard {
    private final String name;
    private final String discoverer;
    private final int[] stats;
    private final String[] attributes;
    private final boolean terra;
    private final Supplier<Block> home;
    private final Supplier<Block> foreign;
    private final Supplier<CropCard> other;
    private final Supplier<? extends Item> gain;
    private final float gainChance;

    public WartCrop(String name, String discoverer, int[] stats, String[] attributes, boolean terra,
            Supplier<Block> home, Supplier<Block> foreign, Supplier<CropCard> other, Supplier<? extends Item> gain,
            float gainChance) {
        this.name = name;
        this.discoverer = discoverer;
        this.stats = stats;
        this.attributes = attributes;
        this.terra = terra;
        this.home = home;
        this.foreign = foreign;
        this.other = other;
        this.gain = gain;
        this.gainChance = gainChance;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String discoveredBy() {
        return discoverer;
    }

    @Override
    public int tier() {
        return 5;
    }

    @Override
    public int stat(int n) {
        return n >= 0 && n < stats.length ? stats[n] : 0;
    }

    @Override
    public String[] attributes() {
        return attributes;
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        if (terra) {
            return crop.getSize() == 1 ? 37 : crop.getSize() + 38;
        }
        return crop.getSize() + 36;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() < 3;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() == 3;
    }

    @Override
    public float dropGainChance() {
        return gainChance;
    }

    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        return new ItemStack(gain.get());
    }

    @Override
    public void tick(CropBlockEntity crop) {
        if (crop.isBlockBelow(home.get())) {
            if (canGrow(crop)) {
                crop.setGrowthPoints((int) (crop.getGrowthPoints() + crop.calcGrowthRate() * 0.5));
            }
        } else if (crop.isBlockBelow(foreign.get()) && crop.getLevel().random.nextInt(300) == 0) {
            crop.setCropId(other.get().getId());
        }
    }
}
