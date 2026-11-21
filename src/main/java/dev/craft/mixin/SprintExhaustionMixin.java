package dev.craft.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.craft.ic2.item.armor.QuantumArmorItem;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Spec 37 §3.1: walking and swimming exhaustion while sprinting is skipped with charged quantum leggings. */
@Mixin(ServerPlayer.class)
public abstract class SprintExhaustionMixin {
    @WrapWithCondition(method = "checkMovementStatistics",
            at = @At(value = "INVOKE", target = "causeFoodExhaustion(F)V"), require = 1)
    private boolean craft$quantumSprint(ServerPlayer player, float exhaustion) {
        return !QuantumArmorItem.sprintsWithoutHunger(player);
    }
}
