package ivorius.psychedelicraft.item;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.fluid.ConsumableFluid;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SyringeItem extends DrinkableItem {
    public SyringeItem(Properties settings) {
        super(settings, FluidVolumes.SYRINGE, DrinkableItem.DEFAULT_MAX_USE_TIME, ConsumableFluid.ConsumptionType.INJECT);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float baseAttackDamage, DamageSource damageSource) {
        if (!(target instanceof LivingEntity l)) {
            return 0;
        }
        float hurtTime = l.hurtDuration == 0 ? 0 : l.hurtTime / (float)l.hurtDuration;
        return (float)target.getRandom().triangle(3, 1.5) * (1 + hurtTime);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        InteractionHand hand = stack == attacker.getMainHandItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var effect = new FluidParticleEffect(PSParticles.FLUID_SPLASH, PSFluids.TOMATO.getDefaultStack());
        //for (int i = 0; i < 10; i++) {
            ((ServerLevel)target.level()).sendParticles(effect, target.getRandomX(1), target.getEyeY(), target.getRandomZ(1), 10, 0, 0, 0, 0.3F);
        //}
        if (!(target instanceof Player) || !canUse(stack, target.level(), target)) {
            return false;
        }
        attacker.setItemInHand(hand, use(stack, target.level(), target, attacker));

        return true;
    }

    @Override
    @Nullable
    protected SoundEvent getUseSound() {
        return PSSounds.ITEM_SYRINGE_INJECT;
    }
}
