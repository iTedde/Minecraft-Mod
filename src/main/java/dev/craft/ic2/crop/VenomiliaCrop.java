package dev.craft.ic2.crop;

import javax.annotation.Nullable;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * IC2 {@code CropVenomilia} (id 9): purple dye at 4, grin powder at 5. At size 5 any living entity touching it is
 * poisoned for 5–14 s and the plant drops back to 4; sneaking players are spared 49 times in 50. Clicking without
 * sneaking counts as touching.
 */
public class VenomiliaCrop extends CropCard {
    @Override
    public String name() {
        return "venomilia";
    }

    @Override
    public String discoveredBy() {
        return "raGan";
    }

    @Override
    public int tier() {
        return 3;
    }

    @Override
    public int stat(int n) {
        return switch (n) {
            case 1 -> 1;
            case 0, 2, 3, 4 -> 3;
            default -> 0;
        };
    }

    @Override
    public String[] attributes() {
        return new String[]{"Purple", "Flower", "Tulip", "Poison"};
    }

    @Override
    public int getSpriteIndex(CropBlockEntity crop) {
        if (crop.getSize() <= 3) {
            return crop.getSize() + 11;
        }
        return crop.getSize() == 4 ? 23 : 25;
    }

    @Override
    public boolean canGrow(CropBlockEntity crop) {
        return crop.getSize() <= 4 && crop.getLightLevel() >= 12 || crop.getSize() == 5;
    }

    @Override
    public boolean canBeHarvested(CropBlockEntity crop) {
        return crop.getSize() >= 4;
    }

    @Nullable
    @Override
    public ItemStack getGain(CropBlockEntity crop) {
        if (crop.getSize() == 5) {
            return new ItemStack(Ic2Items.GRIN_POWDER.get());
        }
        return crop.getSize() >= 4 ? new ItemStack(Items.PURPLE_DYE) : null;
    }

    @Override
    public int getSizeAfterHarvest(CropBlockEntity crop) {
        return 3;
    }

    @Override
    public int growthDuration(CropBlockEntity crop) {
        return crop.getSize() >= 3 ? 600 : 400;
    }

    @Override
    public boolean rightclick(CropBlockEntity crop, Player player) {
        if (!player.isShiftKeyDown()) {
            onEntityCollision(crop, player);
        }
        return crop.harvest(true);
    }

    @Override
    public boolean leftclick(CropBlockEntity crop, Player player) {
        if (!player.isShiftKeyDown()) {
            onEntityCollision(crop, player);
        }
        return crop.pick(true);
    }

    @Override
    public boolean onEntityCollision(CropBlockEntity crop, Entity entity) {
        if (crop.getSize() == 5 && entity instanceof LivingEntity living) {
            if (entity instanceof Player player && player.isShiftKeyDown()
                    && crop.getLevel().random.nextInt(50) != 0) {
                return super.onEntityCollision(crop, entity);
            }
            living.addEffect(new MobEffectInstance(MobEffects.POISON,
                    (crop.getLevel().random.nextInt(10) + 5) * 20, 0));
            crop.setSize(4);
            crop.updateState();
        }
        return super.onEntityCollision(crop, entity);
    }

    @Override
    public boolean isWeed(CropBlockEntity crop) {
        return crop.getSize() == 5 && crop.getGrowth() >= 8;
    }
}
