package dev.craft.ic2.brewing;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Mug of beer or rum (IC2 {@code ItemBooze}), carrying the packed barrel value ({@link Booze}). Beer feeds and stacks
 * mining fatigue, then (with rising fatigue) strength, slowness, resistance, nausea and instant damage; rum needs 100 %
 * progress for fire resistance and resistance, otherwise it acts like spoiled beer ("Black Stuff").
 */
public class BoozeMugItem extends DrinkItem {
    /** Number of solid, hops and time ratio names (IC2 {@code solidRatio}, {@code hopsRatio}, {@code timeRatio}). */
    public static final int SOLID_NAMES = 7;
    public static final int HOPS_NAMES = 7;
    public static final int TIME_NAMES = 5;
    private static final int[] BASE_DURATION = {300, 600, 900, 1200, 1600, 2000, 2400};
    private static final float[] BASE_INTENSITY = {0.4F, 0.75F, 1.0F, 1.5F, 2.0F};
    private static final float RUM_STACKABILITY = 2.0F;
    private static final int RUM_DURATION = 600;

    public BoozeMugItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        int value = Booze.getValue(stack);
        String key = getDescriptionId();
        return switch (Booze.getType(value)) {
            case Booze.TYPE_BEER -> Booze.getTimeRatio(value) == Booze.BLACK_STUFF
                    ? Component.translatable(key + ".black_stuff")
                    : Component.translatable(key + ".beer",
                            Component.translatable(key + ".solid." + Booze.getSolidRatio(value)),
                            Component.translatable(key + ".hops." + Booze.getHopsRatio(value)),
                            Component.translatable(key + ".time." + Booze.getTimeRatio(value)));
            case Booze.TYPE_RUM -> Component.translatable(key + ".rum");
            default -> Component.translatable(key + ".zero");
        };
    }

    /** Icon offset in IC2's {@code item_0.png} from sprite 192: beer by time ratio, rum 8, none 0. */
    public static int getIconOffset(ItemStack stack) {
        int value = Booze.getValue(stack);
        return switch (Booze.getType(value)) {
            case Booze.TYPE_BEER -> Booze.getTimeRatio(value);
            case Booze.TYPE_RUM -> 8;
            default -> 0;
        };
    }

    @Override
    protected void drink(ItemStack stack, Level level, Player player) {
        int value = Booze.getValue(stack);
        int type = Booze.getType(value);
        if (type == Booze.TYPE_BEER) {
            if (Booze.getTimeRatio(value) == Booze.BLACK_STUFF) {
                drinkBlackStuff(level, player);
                return;
            }
            int solidRatio = Booze.getSolidRatio(value);
            int alc = Booze.getHopsRatio(value);
            int duration = BASE_DURATION[solidRatio];
            float intensity = BASE_INTENSITY[Booze.getTimeRatio(value)];
            player.getFoodData().eat(6 - alc, solidRatio * 0.15F);
            int max = (int) (intensity * (alc * 0.5F));
            MobEffectInstance slow = player.getEffect(MobEffects.DIG_SLOWDOWN);
            int fatigue = slow != null ? slow.getAmplifier() : -1;
            amplifyEffect(player, MobEffects.DIG_SLOWDOWN, max, intensity, duration);
            if (fatigue > -1) {
                amplifyEffect(player, MobEffects.DAMAGE_BOOST, max, intensity, duration);
                if (fatigue > 0) {
                    amplifyEffect(player, MobEffects.MOVEMENT_SLOWDOWN, max / 2, intensity, duration);
                    if (fatigue > 1) {
                        amplifyEffect(player, MobEffects.DAMAGE_RESISTANCE, max - 1, intensity, duration);
                        if (fatigue > 2) {
                            amplifyEffect(player, MobEffects.CONFUSION, 0, intensity, duration);
                            if (fatigue > 3) {
                                player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, level.random.nextInt(3)));
                            }
                        }
                    }
                }
            }
        } else if (type == Booze.TYPE_RUM) {
            if (Booze.getRumProgress(value) < 100) {
                drinkBlackStuff(level, player);
                return;
            }
            amplifyEffect(player, MobEffects.FIRE_RESISTANCE, 0, RUM_STACKABILITY, RUM_DURATION);
            MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
            int resistanceLevel = resistance != null ? resistance.getAmplifier() : -1;
            amplifyEffect(player, MobEffects.DAMAGE_RESISTANCE, 2, RUM_STACKABILITY, RUM_DURATION);
            if (resistanceLevel >= 0) {
                amplifyEffect(player, MobEffects.BLINDNESS, 0, RUM_STACKABILITY, RUM_DURATION);
            }
            if (resistanceLevel >= 1) {
                amplifyEffect(player, MobEffects.CONFUSION, 0, RUM_STACKABILITY, RUM_DURATION);
            }
        }
    }

    /** IC2 {@code amplifyEffect}: extends the duration up to {@code duration · (1 + 2 · intensity)} and steps up. */
    private static void amplifyEffect(Player player, Holder<MobEffect> effect, int max, float intensity,
            int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null) {
            player.addEffect(new MobEffectInstance(effect, duration, 0));
            return;
        }
        int newDuration = current.getDuration();
        int maxNewDuration = Math.max((int) (duration * (1.0F + intensity * 2.0F) - newDuration) / 2, 0);
        newDuration += Math.min(duration, maxNewDuration);
        int amplifier = current.getAmplifier();
        if (amplifier < max) {
            amplifier++;
        }
        player.addEffect(new MobEffectInstance(effect, newDuration, amplifier));
    }

    /** IC2 {@code drinkBlackStuff}: one of nothing, nausea, blindness, poison (long or strong) or instant damage. */
    private static void drinkBlackStuff(Level level, Player player) {
        switch (level.random.nextInt(6)) {
            case 1 -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 1200, 0));
            case 2 -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 2400, 0));
            case 3 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 2400, 0));
            case 4 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 2));
            case 5 -> player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, level.random.nextInt(4)));
            default -> {
            }
        }
    }
}
