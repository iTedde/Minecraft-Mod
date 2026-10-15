package dev.craft.ic2.item;

import java.util.List;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * IC2 {@code ItemTinCan}: food (2, 0.95) that is always edible, heals one heart and returns the empty tin can. The
 * variant ({@link Ic2DataComponents#CAN_META}) adds the effects of the food it was canned from (spec 05 §6).
 */
public class FilledTinCanItem extends Item {
    public static final FoodProperties FOOD = new FoodProperties.Builder().nutrition(2).saturationModifier(0.95F)
            .alwaysEdible().build();
    public static final int USE_DURATION = 20;
    public static final String BAD_KEY = "item.craft.filled_tin_can.bad";

    public FilledTinCanItem(Properties properties) {
        super(properties.food(FOOD));
    }

    public static ItemStack create(int meta, int count) {
        ItemStack stack = new ItemStack(Ic2Items.FILLED_TIN_CAN.get(), count);
        stack.set(Ic2DataComponents.CAN_META.get(), meta);
        return stack;
    }

    public static int getMeta(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.CAN_META.get(), 0);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int meta = getMeta(stack);
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && !level.isClientSide()) {
            applyEffects(player, meta);
            onEaten(player);
        }
        return result;
    }

    /** {@code onFoodEaten}: effects of the variant; durations from the source food's heal amount as in IC2. */
    public static void applyEffects(Player player, int meta) {
        switch (meta) {
            case 1 -> {
                if (player.getRandom().nextFloat() < 0.8F) {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600 / 2, 0));
                }
            }
            case 2 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 100 / 1, 0));
            case 3 -> {
                if (player.getRandom().nextFloat() < 0.3F) {
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600 / 1, 0));
                }
            }
            case 4 -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100 / 2, 0));
            case 5 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600 / 2, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000 / 2, 0));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000 / 2, 0));
            }
            default -> {
            }
        }
    }

    /** {@code ItemTinCan.onEaten}: heals 2 and hands back the empty can (dropped if the inventory is full). */
    public static void onEaten(Player player) {
        player.heal(2.0F);
        ItemStack can = new ItemStack(Ic2Items.TIN_CAN.get());
        if (!player.getInventory().add(can)) {
            player.drop(can, false);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int meta = getMeta(stack);
        if (meta >= 1 && meta <= 3) {
            tooltip.add(Component.translatable(BAD_KEY));
        }
    }
}
