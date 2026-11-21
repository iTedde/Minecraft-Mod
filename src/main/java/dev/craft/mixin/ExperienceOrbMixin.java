package dev.craft.mixin;

import dev.craft.lightshield.ElectricLightShieldItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mending on the electric light shield (spec 30 §5): vanilla only repairs damaged items, the shield has no damage, so
 * held shields take their XP first and the rest goes through vanilla repair.
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Shadow
    private int repairPlayerItems(ServerPlayer player, int value) {
        throw new AssertionError();
    }

    @Inject(method = "repairPlayerItems", at = @At("HEAD"), cancellable = true)
    private void craft$chargeLightShield(ServerPlayer player, int value, CallbackInfoReturnable<Integer> cir) {
        int rest = ElectricLightShieldItem.repairWithXp(player, value);
        if (rest != value) {
            cir.setReturnValue(rest > 0 ? repairPlayerItems(player, rest) : 0);
        }
    }
}
