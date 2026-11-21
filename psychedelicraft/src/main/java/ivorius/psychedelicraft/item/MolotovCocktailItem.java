/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.entity.MolotovCocktailEntity;
import ivorius.psychedelicraft.fluid.Combustable;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class MolotovCocktailItem extends DrinkableItem {
    public MolotovCocktailItem(Properties settings) {
        super(settings, FluidVolumes.BOTTLE, DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.DRINK);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 7200;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        float strength = user.getUseItemRemainingTicks() / (float)getUseDuration(stack, user);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!world.isClientSide) {
            MolotovCocktailEntity projectile = new MolotovCocktailEntity(world, user);
            projectile.setItem(stack);
            projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0, 0.5F * strength, 1F);
            world.addFreshEntity(projectile);
        }

        if (user instanceof Player player) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (ItemFluids.of(stack).isEmpty()) {
            return Component.translatable(getDescriptionId(stack) + ".empty");
        }
        if (ItemFluids.of(stack).isIn(FluidTags.LAVA)) {
            return Component.translatable(getDescriptionId(stack) + ".lava");
        }

        return Component.translatable(getDescriptionId(stack) + ".quality." + getQuality(stack));
    }

    private int getQuality(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);
        if (fluids.fluid() instanceof Combustable exploding) {
            float explStr = exploding.getExplosionStrength(fluids) * 0.8f;
            float fireStr = exploding.getFireStrength(fluids) * 0.6f;

            return Mth.clamp(Mth.floor((fireStr + explStr) + 0.5f), 0, 7);
        }

        return 0;
    }
}
