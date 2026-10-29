package dev.craft.ic2.brewing;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Coffee mug (IC2 {@code ItemMugCoffee} metadata 0 cold, 1 dark, 2 with milk and sugar): raises saturation (spec 37
 * §3.7, instead of speed) and haste;
 * a level of 3 or more (2 less for milk coffee) causes nausea, 4 or more also instant damage.
 */
public class CoffeeMugItem extends DrinkItem {
    public static final int COLD = 0;
    public static final int DARK = 1;
    public static final int MILK = 2;

    private final int meta;

    public CoffeeMugItem(Properties properties, int meta) {
        super(properties);
        this.meta = meta;
    }

    @Override
    protected void drink(ItemStack stack, Level level, Player player) {
        int highest = Math.max(amplifyEffect(player, MobEffects.SATURATION),
                amplifyEffect(player, MobEffects.DIG_SPEED));
        if (meta == MILK) {
            highest -= 2;
        }
        if (highest >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (highest - 2) * 200, 0));
            if (highest >= 4) {
                player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, highest - 3));
            }
        }
    }

    /** IC2 {@code amplifyEffect}: returns the new amplifier (1 for a fresh effect). */
    private int amplifyEffect(Player player, Holder<MobEffect> effect) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null) {
            player.addEffect(new MobEffectInstance(effect, 300, 0));
            return 1;
        }
        int max = meta == DARK ? 5 : meta == MILK ? 6 : 1;
        int amplifier = current.getAmplifier();
        if (amplifier < max) {
            amplifier++;
        }
        int duration = current.getDuration() + (meta == COLD ? 600 : 1200);
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));
        return amplifier;
    }
}
