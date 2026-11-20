/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.item.component.ItemDrugs;
import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.AABB;

public class SmokeableItem extends Item {
    private final int useStages;

    public SmokeableItem(int useStages, Properties settings) {
        super(settings);
        this.useStages = useStages;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 25;
    }

    public int getStages() {
        return useStages;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        DrugProperties.of(entity).ifPresent(drugProperties -> {
            ItemDrugs.get(stack).applyTo(stack, drugProperties);
        });

        if (!(entity instanceof Player player && player.getAbilities().instabuild)) {
            if (!stack.isDamageableItem()) {
                stack.shrink(1);
            } else {
                stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
            }
        }

        return super.finishUsingItem(stack, world, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PlacedDrinksBlock.tryPlace(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!DrugProperties.of(player).isBreathingSmoke()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    public void onIncinerated(ItemStack stack, ServerLevel world, BlockPos pos, AbstractFurnaceBlockEntity furnace) {
        ItemDrugs drugs = ItemDrugs.get(stack);
        world.getEntitiesOfClass(Player.class, new AABB(pos).inflate(3), EntitySelector.NO_SPECTATORS).forEach(player -> {
            drugs.applyTo(stack, DrugProperties.of(player));
        });

        var effect = new DrugDustParticleEffect(PSParticles.EXHALED_SMOKE, drugs.smokeColor().orElse(ItemDrugs.DEFAULT_SMOKE_COLOR), 1);
        for (int i = 0; i < 30; i++) {
            world.sendParticles(effect,
                    world.random.triangle(pos.getX() + 0.5, 0.3),
                    pos.getY() + 1,
                    world.random.triangle(pos.getZ() + 0.5, 0.3),
                    1,
                    world.random.triangle(0, 0.3),
                    world.random.triangle(0.8, 0.3),
                    world.random.triangle(0, 0.3), 0.001F);
        }
    }
}
