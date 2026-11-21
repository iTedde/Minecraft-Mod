package ivorius.psychedelicraft;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface PSDamageTypes {
    List<ResourceKey<DamageType>> REGISTRY = new ArrayList<>();

    ResourceKey<DamageType> ALCOHOL_POSIONING = register("alcohol_poisoning");
    ResourceKey<DamageType> RESPIRATORY_FAILURE = register("respiratory_failure");
    ResourceKey<DamageType> STROKE = register("stroke");
    ResourceKey<DamageType> HEART_FAILURE = register("heart_failure");
    ResourceKey<DamageType> HEART_ATTACK = register("heart_attack");
    ResourceKey<DamageType> KIDNEY_FAILURE = register("kidney_failure");
    ResourceKey<DamageType> IN_SLEEP = register("in_sleep");
    ResourceKey<DamageType> OVER_EATING = register("over_eating");
    ResourceKey<DamageType> MOLOTOV = register("molotov");
    ResourceKey<DamageType> SELF_MOLOTOV = register("self_molotov");
    ResourceKey<DamageType> OVERDOSE = register("overdose");
    ResourceKey<DamageType> TEETH_GRINDING = register("teeth_grinding");
    ResourceKey<DamageType> GLASS_SHARD = register("glass_shard");
    ResourceKey<DamageType> CANCER = register("cancer");

    static ResourceKey<DamageType> molotov(Entity target, @Nullable Entity attacker) {
        return target == attacker ? SELF_MOLOTOV : MOLOTOV;
    }

    static DamageSource create(Level world, ResourceKey<DamageType> type) {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type));
    }

    static DamageSource create(Level world, Entity source, @Nullable Entity attacker, ResourceKey<DamageType> type) {
        return new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), source, attacker);
    }

    static boolean damage(Level world, Entity target, DamageSource source, float amount) {
        if (target instanceof LivingEntity l) {
            Difficulty difficulty = world.getDifficulty();
            float health = l.getHealth();
            amount = switch (difficulty) {
                case PEACEFUL -> Math.min(health - 0.5F, amount);
                case EASY -> amount * 0.5F;
                case HARD -> amount * 0.75F;
                case NORMAL -> amount;
            };
            if (world.getLevelData().isHardcore() && amount >= health) {
                if (world.getRandom().nextInt(200) == 0) {
                    amount = Math.min(health - 0.5F, amount);
                } else {
                    amount = (float)(health * world.getRandom().triangle(0.8F, 0.3F));
                }
            }
        }
        return target.hurt(source, amount);
    }

    private static ResourceKey<DamageType> register(String name) {
        var key = ResourceKey.create(Registries.DAMAGE_TYPE, Psychedelicraft.id(name));
        REGISTRY.add(key);
        return key;
    }

    static void bootstrap() {}
}
