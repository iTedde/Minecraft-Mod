package dev.craft.mixin;

import dev.craft.core.MovementCap;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.mps.MpsMovement;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MPS sets {@code jumpMovementFactor} for one tick (spec 29 §6); creative flight keeps its own speed, flight with the
 * gravitation chestplate is capped at sprinting speed without the sprint doubling (spec 37 §3.5).
 */
@Mixin(Player.class)
public abstract class PlayerFlyingSpeedMixin {
    @Shadow
    @Final
    private Abilities abilities;

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void craft$mpsAirSpeed(CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (!abilities.flying) {
            Float airSpeed = MpsMovement.getAirSpeed(player);
            if (airSpeed != null) {
                cir.setReturnValue(airSpeed);
            }
        } else if (GraviChestplateItem.limitsFlyingSpeed(player)) {
            cir.setReturnValue(MovementCap.FLY_ACCELERATION);
        }
    }
}
