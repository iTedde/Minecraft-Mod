package dev.craft.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.craft.ic2.item.armor.QuantumArmorItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Spec 37 §3.1: a sprint jump costs no hunger with charged quantum leggings. */
@Mixin(Player.class)
public abstract class SprintJumpExhaustionMixin {
    @WrapWithCondition(method = "jumpFromGround",
            at = @At(value = "INVOKE", target = "causeFoodExhaustion(F)V"), require = 1)
    private boolean craft$quantumSprintJump(Player player, float exhaustion) {
        return !QuantumArmorItem.sprintsWithoutHunger(player);
    }
}
