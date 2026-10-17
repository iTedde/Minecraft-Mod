package dev.craft.ic2.crop;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * One crop species (IC2 API {@code CropCard}). The defaults are IC2's; species override what differs. The tile is
 * passed in like IC2's {@code TECrop}.
 */
public abstract class CropCard {
    private int id = -1;

    /** Registry name, also used for the translation key {@code crop.craft.<name>}. */
    public abstract String name();

    public Component displayName() {
        return Component.translatable("crop.craft." + name());
    }

    public String discoveredBy() {
        return "Alblaka";
    }

    /** Cropnalyzer description line 0 (attributes 1–2) or 1 (attributes 3–4), exactly as IC2 joins them. */
    public String desc(int line) {
        String[] att = attributes();
        if (att.length == 0) {
            return "";
        }
        if (line == 0) {
            String s = att[0];
            if (att.length >= 2) {
                s = s + ", " + att[1];
                if (att.length >= 3) {
                    s = s + ",";
                }
            }
            return s;
        }
        if (att.length < 3) {
            return "";
        }
        String s = att[2];
        if (att.length >= 4) {
            s = s + ", " + att[3];
        }
        return s;
    }

    public abstract int tier();

    /** Breeding stats 0–4 (chemistry, food, defensive, color, weed), used only by the crossing weights. */
    public abstract int stat(int n);

    public abstract String[] attributes();

    /** Index on the IC2 sprite sheet {@code crops_0.png}. */
    public abstract int getSpriteIndex(CropBlockEntity crop);

    public int growthDuration(CropBlockEntity crop) {
        return tier() * 200;
    }

    public abstract boolean canGrow(CropBlockEntity crop);

    public int weightInfluences(CropBlockEntity crop, float humidity, float nutrients, float air) {
        return (int) (humidity + nutrients + air);
    }

    public boolean canCross(CropBlockEntity crop) {
        return crop.getSize() >= 3;
    }

    public boolean rightclick(CropBlockEntity crop, Player player) {
        return crop.harvest(true);
    }

    public abstract boolean canBeHarvested(CropBlockEntity crop);

    public float dropGainChance() {
        float base = 1.0F;
        for (int i = 0; i < tier(); i++) {
            base = (float) (base * 0.95);
        }
        return base;
    }

    @Nullable
    public abstract ItemStack getGain(CropBlockEntity crop);

    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 1;
    }

    public boolean leftclick(CropBlockEntity crop, Player player) {
        return crop.pick(true);
    }

    public float dropSeedChance(CropBlockEntity crop) {
        if (crop.getSize() == 1) {
            return 0.0F;
        }
        float base = 0.5F;
        if (crop.getSize() == 2) {
            base /= 2.0F;
        }
        for (int i = 0; i < tier(); i++) {
            base = (float) (base * 0.8);
        }
        return base;
    }

    public ItemStack getSeeds(CropBlockEntity crop) {
        return CropSeedItem.create(crop.getCropId(), crop.getGrowth(), crop.getGain(), crop.getResistance(),
                crop.getScanLevel());
    }

    public void onNeighbourChange(CropBlockEntity crop) {
    }

    public boolean emitRedstone(CropBlockEntity crop) {
        return false;
    }

    public void onBlockDestroyed(CropBlockEntity crop) {
    }

    public int getEmittedLight(CropBlockEntity crop) {
        return 0;
    }

    /** Returns true if the entity may trample the crop: sprinting living entities. */
    public boolean onEntityCollision(CropBlockEntity crop, Entity entity) {
        return entity instanceof LivingEntity living && living.isSprinting();
    }

    public void tick(CropBlockEntity crop) {
    }

    public boolean isWeed(CropBlockEntity crop) {
        return crop.getSize() >= 2 && (crop.getCropId() == 0 || crop.getGrowth() >= 24);
    }

    public final int getId() {
        return id;
    }

    final void setId(int id) {
        this.id = id;
    }
}
