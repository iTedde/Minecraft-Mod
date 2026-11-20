package ivorius.psychedelicraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.psychedelicraft.entity.LivingEntityDuck;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements LivingEntityDuck {
    private MixinLivingEntity() { super(null, null); }

    @Override
    @Invoker("jumpFromGround")
    public abstract void invokeJump();

    @ModifyVariable(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float modifyDamageOnModifyAppliedDamage(float initial, DamageSource source, float amount) {
        return DrugProperties.of(this).map(properties -> properties.onDamaged(source, initial)).orElse(initial);
    }
}
