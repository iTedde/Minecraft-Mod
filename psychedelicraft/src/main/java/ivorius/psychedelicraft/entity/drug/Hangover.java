package ivorius.psychedelicraft.entity.drug;

import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.util.NbtSerialisable;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;

/**
 * Hangover after drinking Psychedelicraft alcohol (Craft addition, spec 52 §6).
 * <p>
 * The highest alcohol value since the player was last sober ({@code peak}) is tracked. Once the alcohol value
 * drops below {@link #SOBER} (sleeping resets it to zero), a hangover of level I–III starts that lasts
 * {@code peak} × one Minecraft day (at most one day). Drinking again pauses it; once sober again it continues with
 * the higher peak.
 */
public class Hangover implements NbtSerialisable {
    public static final float SOBER = 0.05F;
    public static final float LIGHT = 0.2F;
    public static final float MEDIUM = 0.5F;
    public static final float HEAVY = 0.8F;
    public static final int MAX_DURATION = 24000;

    public static final int WATER_RELIEF = 2 * 60 * 20;
    public static final int COFFEE_RELIEF = 5 * 60 * 20;

    private static final int NAUSEA_DURATION = 6 * 20;
    private static final int VOMIT_NAUSEA_DURATION = 8 * 20;
    private static final int VOMIT_HUNGER = 4;
    private static final int SIDE_EFFECT_DURATION = 60;

    private final DrugProperties properties;

    private float peak;
    private int ticks;
    private int level;
    private boolean active;
    private int nauseaCooldown;
    private int vomitCooldown;

    public Hangover(DrugProperties properties) {
        this.properties = properties;
    }

    public static int levelFor(float peak) {
        return peak >= HEAVY ? 3 : peak >= MEDIUM ? 2 : peak >= LIGHT ? 1 : 0;
    }

    public static int durationFor(float peak) {
        return Math.min(MAX_DURATION, Mth.ceil(peak * MAX_DURATION));
    }

    public float getPeak() {
        return peak;
    }

    public int getTicks() {
        return ticks;
    }

    public int getLevel() {
        return level;
    }

    /**
     * @return true while the hangover runs (not paused by fresh alcohol)
     */
    public boolean isActive() {
        return active && ticks > 0;
    }

    public void onTick() {
        Player entity = properties.asEntity();
        if (entity.level().isClientSide || entity.isCreative() || entity.isSpectator()) {
            return;
        }

        float alcohol = properties.getDrugValue(DrugType.ALCOHOL);
        if (alcohol >= SOBER) {
            if (alcohol > peak) {
                peak = alcohol;
                properties.markDirty();
            }
            if (active) {
                active = false;
                entity.removeEffect(PSEffects.HANGOVER);
                properties.markDirty();
            }
            return;
        }

        if (!active) {
            if (levelFor(peak) == 0) {
                if (peak != 0 || ticks != 0) {
                    clear();
                }
                return;
            }
            start();
        }

        if (--ticks <= 0) {
            clear();
            return;
        }
        applyEffects(entity, entity.level().random);
    }

    private void start() {
        RandomSource random = properties.asEntity().level().random;
        level = levelFor(peak);
        ticks = Math.max(ticks, durationFor(peak));
        active = true;
        if (nauseaCooldown <= 0) {
            nauseaCooldown = nextNauseaDelay(random);
        }
        if (vomitCooldown <= 0) {
            vomitCooldown = nextVomitDelay(random);
        }
        syncDisplay(properties.asEntity());
        properties.markDirty();
    }

    private void clear() {
        boolean wasRunning = ticks > 0 || active;
        peak = 0;
        ticks = 0;
        level = 0;
        active = false;
        nauseaCooldown = 0;
        vomitCooldown = 0;
        if (wasRunning) {
            properties.asEntity().removeEffect(PSEffects.HANGOVER);
        }
        properties.markDirty();
    }

    private void applyEffects(Player entity, RandomSource random) {
        if (entity.tickCount % 20 == 0) {
            syncDisplay(entity);
            addSideEffect(entity, MobEffects.DIG_SLOWDOWN, SIDE_EFFECT_DURATION);
            if (level >= 2) {
                addSideEffect(entity, MobEffects.MOVEMENT_SLOWDOWN, SIDE_EFFECT_DURATION);
                addSideEffect(entity, MobEffects.HUNGER, SIDE_EFFECT_DURATION);
            }
            if (level >= 3) {
                addSideEffect(entity, MobEffects.WEAKNESS, SIDE_EFFECT_DURATION);
            }
        }

        if (--nauseaCooldown <= 0) {
            nauseaCooldown = nextNauseaDelay(random);
            addSideEffect(entity, MobEffects.CONFUSION, NAUSEA_DURATION);
        }

        if (level >= 3 && --vomitCooldown <= 0) {
            vomitCooldown = nextVomitDelay(random);
            vomit(entity);
        }
    }

    public void vomit(Player entity) {
        entity.getFoodData().setFoodLevel(Math.max(0, entity.getFoodData().getFoodLevel() - VOMIT_HUNGER));
        addSideEffect(entity, MobEffects.CONFUSION, VOMIT_NAUSEA_DURATION);
        Stomach stomach = properties.getStomach();
        if (!stomach.vomitIntoBag()) {
            stomach.playBarfNoise();
            for (int i = 0; i < 3; i++) {
                entity.drop(PSItems.VOMIT.getDefaultInstance(), true, true).setNeverPickUp();
            }
        }
    }

    private static void addSideEffect(Player entity, Holder<MobEffect> effect, int duration) {
        MobEffectInstance current = entity.getEffect(effect);
        if (current == null || current.getAmplifier() <= 0 && current.getDuration() < duration) {
            entity.addEffect(new MobEffectInstance(effect, duration, 0, true, false, true));
        }
    }

    private void syncDisplay(Player entity) {
        MobEffectInstance current = entity.getEffect(PSEffects.HANGOVER);
        if (current == null || current.getAmplifier() != level - 1 || Math.abs(current.getDuration() - ticks) > 40) {
            entity.removeEffect(PSEffects.HANGOVER);
            entity.addEffect(new MobEffectInstance(PSEffects.HANGOVER, ticks, level - 1, false, false, true));
        }
    }

    /**
     * Relieves the hangover: water shortens it, coffee shortens it more and lowers the level by one (not below I).
     *
     * @return true if there was a hangover to relieve
     */
    public boolean relieve(boolean coffee) {
        if (ticks <= 0) {
            return false;
        }
        ticks = Math.max(0, ticks - (coffee ? COFFEE_RELIEF : WATER_RELIEF));
        if (coffee && level > 1) {
            level--;
        }
        if (ticks == 0) {
            clear();
        } else if (active) {
            syncDisplay(properties.asEntity());
            properties.markDirty();
        }
        return true;
    }

    private static int nextNauseaDelay(RandomSource random) {
        return random.nextIntBetweenInclusive(60 * 20, 120 * 20);
    }

    private static int nextVomitDelay(RandomSource random) {
        return random.nextIntBetweenInclusive(2 * 60 * 20, 5 * 60 * 20);
    }

    public void copyFrom(Hangover old, boolean alive) {
        if (alive) {
            peak = old.peak;
            ticks = old.ticks;
            level = old.level;
            active = old.active;
            nauseaCooldown = old.nauseaCooldown;
            vomitCooldown = old.vomitCooldown;
        } else {
            peak = 0;
            ticks = 0;
            level = 0;
            active = false;
            nauseaCooldown = 0;
            vomitCooldown = 0;
        }
    }

    @Override
    public void toNbt(CompoundTag compound, Provider lookup) {
        compound.putFloat("peak", peak);
        compound.putInt("ticks", ticks);
        compound.putInt("level", level);
        compound.putBoolean("active", active);
        compound.putInt("nauseaCooldown", nauseaCooldown);
        compound.putInt("vomitCooldown", vomitCooldown);
    }

    @Override
    public void fromNbt(CompoundTag compound, Provider lookup) {
        peak = compound.getFloat("peak");
        ticks = compound.getInt("ticks");
        level = compound.getInt("level");
        active = compound.getBoolean("active");
        nauseaCooldown = compound.getInt("nauseaCooldown");
        vomitCooldown = compound.getInt("vomitCooldown");
    }
}
