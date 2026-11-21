/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Created by Sollace on Jan 1 2023
 */
public class DrinkableItem extends Item {
    public static final int FLUID_PER_INJECTION = FluidVolumes.SYRINGE;
    public static final int DEFAULT_MAX_USE_TIME = (int)(1.6F * 20);

    private final int consumptionVolume;
    private final int consumptionTime;
    private final ConsumableFluid.ConsumptionType consumptionType;

    public DrinkableItem(Properties settings, int consumptionVolume, int consumptionTime, ConsumableFluid.ConsumptionType consumptionType) {
        super(settings.stacksTo(1));
        this.consumptionVolume = consumptionVolume;
        this.consumptionTime = consumptionTime;
        this.consumptionType = consumptionType;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return ItemFluids.set(stack.copy(), ItemFluids.EMPTY);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return ItemFluids.of(stack).isEmpty() ? UseAnim.NONE : consumptionType.getUseAction();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        return use(stack, world, entity, entity);
    }

    protected boolean canUse(ItemStack stack, Level world, LivingEntity entity) {
        return ConsumableFluid.canConsume(stack, entity, consumptionVolume, consumptionType);
    }

    protected ItemStack use(ItemStack stack, Level world, LivingEntity entity, LivingEntity user) {
        @Nullable SoundEvent sound = getUseSound();
        if (sound != null) {
            entity.playSound(sound, 2, (float)entity.getRandom().triangle(0.5, 0.2));
        }
        return ConsumableFluid.consume(stack, entity, consumptionVolume, EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(user), consumptionType);
    }

    @Nullable
    protected SoundEvent getUseSound() {
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (canUse(stack, world, player)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PlacedDrinksBlock.tryPlace(context);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return consumptionTime;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemFluids fluid = ItemFluids.of(stack);

        if (!fluid.isEmpty()) {
            return Component.translatable(getDescriptionId() + ".filled", fluid.fluid().getName(fluid));
        }

        return super.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !ItemFluids.of(stack).isEmpty() && FluidCapacity.getPercentage(stack) < 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int)(MAX_BAR_WIDTH * FluidCapacity.getPercentage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }
}
