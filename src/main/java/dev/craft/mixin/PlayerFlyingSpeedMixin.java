package dev.craft.mixin;

import dev.craft.mps.MpsMovement;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** MPS sets {@code jumpMovementFactor} for one tick (spec 29 §6); creative flight keeps its own speed. */
@Mixin(Player.class)
public abstract class PlayerFlyingSpeedMixin {
    @Shadow
    @Final
    private Abilities abilities;

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void craft$mpsAirSpeed(CallbackInfoReturnable<Float> cir) {
        if (!abilities.flying) {
            Float airSpeed = MpsMovement.getAirSpeed((Player) (Object) this);
            if (airSpeed != null) {
                cir.setReturnValue(airSpeed);
            }
        }
    }
}
