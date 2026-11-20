package ivorius.psychedelicraft.entity.drug;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.BagContentsComponent;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Stomach implements NbtSerialisable {

    private final DrugProperties properties;

    private int vomitCount;
    private int vomitCooldown;
    private int vomitingTicks;
    private int hungerCooldown;

    private int previousFoodLevel;

    private final Player entity;

    public Stomach(DrugProperties properties) {
        this.properties = properties;
        this.entity = properties.asEntity();
    }

    public boolean reset() {
        boolean changed = getStomach().getLockedState() != null
                || getGlut().getOvereating() > 0
                || vomitingTicks > 0;
        getStomach().unlockHunger();
        getGlut().setOvereating(0);
        vomitCount = 0;
        vomitCooldown = 0;
        hungerCooldown = 0;
        vomitingTicks = 0;
        properties.markDirty();
        return changed;
    }

    public void copyFrom(Stomach old, boolean alive) {
        if (alive) {
            vomitCount = old.vomitCount;
            vomitCooldown = old.vomitCooldown;
            vomitingTicks = old.vomitingTicks;
            hungerCooldown = old.hungerCooldown;
            previousFoodLevel = old.previousFoodLevel;
            getStomach().setLockedState(old.getStomach().getLockedState());
            getGlut().setOvereating(old.getGlut().getOvereating());
        } else {
            reset();
        }
    }

    public LockableHungerManager getStomach() {
        return (LockableHungerManager)entity.getFoodData();
    }

    public GluttonyManager getGlut() {
        return (GluttonyManager)entity.getFoodData();
    }

    public int getCooldown() {
        return getStomach().getLockedState() == null ? 0 : hungerCooldown;
    }

    public int setVisibleState(int ticks) {
        boolean shaking = vomitingTicks > 0 || getGlut().getOvereating() > 8
                || getStomach().getLockedState() != null && (
                       getStomach().getActualFoodLevel() <= 3
                    || getStomach().getActualFoodLevel() != previousFoodLevel
                    || hungerCooldown < 50
        );
        if (shaking) {
            getStomach().setShanksShaking(true);
            return 0;
        }
        return ticks;
    }

    public void onTick() {
        previousFoodLevel = getStomach().getActualFoodLevel();
        final float hungerSuppression = Mth.clamp(properties.getModifier(Drug.HUNGER_SUPPRESSION), -1, 1);
        final boolean shouldLockHunger = Math.abs(hungerSuppression) > Mth.EPSILON;

        if (shouldLockHunger != (getStomach().getLockedState() != null)) {
            if (shouldLockHunger) {
                hungerCooldown = 120;
                getStomach().lockHunger(hungerSuppression > 0, hungerSuppression);
            } else if (--hungerCooldown <= 0) {
                getStomach().unlockHunger();
            }

            properties.markDirty();
        }

        if (shouldLockHunger && getStomach().getLockedState() != null) {
            getStomach().getLockedState().setRate(hungerSuppression);
        }

        if (getGlut().getOvereating() >= 10) {
            vomit();
        }

        if (vomitingTicks > 0) {
            vomitingTicks--;
            if (entity.tickCount % (int)(1 + entity.level().random.nextFloat() * 3) == 0) {
                int count = (int)(entity.level().random.nextFloat() * (vomitingTicks / 2));
                for (int i = 0; i < count; i++) {
                    vomitingTicks--;
                    if (!entity.level().isClientSide) {
                        entity.drop(PSItems.VOMIT.getDefaultInstance(), true, true).setNeverPickUp();
                        playBarfNoise();
                    }
                }
                properties.markDirty();
            }
        }
        if (vomitCooldown > 0) {
            properties.markDirty();
            if (vomitCooldown-- <= 0) {
                vomitCount = 0;
            }
        }
    }

    public void vomit() {
        ItemStack heldItem = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (heldItem.is(PSItems.PAPER_BAG) && BagContentsComponent.get(heldItem).isEmpty()) {
            playBarfNoise();

            if (!entity.isCreative()) {
                heldItem.shrink(1);
            }
            if (heldItem.isEmpty()) {
                entity.setItemInHand(InteractionHand.OFF_HAND, PSItems.BAG_O_VOMIT.getDefaultInstance());
            } else {
                entity.getInventory().placeItemBackInInventory(PSItems.BAG_O_VOMIT.getDefaultInstance());
            }
        } else {
            vomitingTicks = entity.level().random.nextIntBetweenInclusive(10, 100);
            if (++vomitCount > 16) {
                entity.hurt(properties.damageOf(PSDamageTypes.OVER_EATING), Integer.MAX_VALUE);
            }
        }
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1));
        getGlut().setOvereating(0);
        properties.markDirty();
    }

    private void playBarfNoise() {
        entity.level().playSound(null, entity, SoundEvents.VILLAGER_DEATH, entity.getSoundSource(), 1, (float)entity.level().random.triangle(0.5, 0.25));
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        if (compound.contains("hunger", Tag.TAG_COMPOUND)) {
            getStomach().setLockedState(LockableHungerManager.State.fromNbt(compound.getCompound("hunger")));
        } else {
            getStomach().unlockHunger();
        }
        vomitCount = compound.getInt("vomitCount");
        vomitCooldown = compound.getInt("vomitCooldown");
        vomitingTicks = compound.getInt("vomitingTicks");
        hungerCooldown = compound.getInt("hungerCooldown");
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        LockableHungerManager.State lockedHungerState = getStomach().getLockedState();
        if (lockedHungerState != null) {
            compound.put("hunger", lockedHungerState.toNbt());
        }
        compound.putInt("vomitCount", vomitCount);
        compound.putInt("vomitCooldown", vomitCooldown);
        compound.putInt("vomitingTicks", vomitingTicks);
        compound.putInt("hungerCooldown", hungerCooldown);
    }
}
