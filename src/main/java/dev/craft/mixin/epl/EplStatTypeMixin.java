package dev.craft.mixin.epl;

import com.enhancedplayerlist.Config;
import dev.craft.compat.epl.EplMinedBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Spec 39 §3: the config validator accepts "blocksMined" in {@code visibleStats}. */
@Mixin(value = Config.StatType.class, remap = false)
public abstract class EplStatTypeMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private static void craft$acceptMinedBlocks(String stat, CallbackInfoReturnable<Boolean> cir) {
        if (EplMinedBlocks.STAT.equals(stat)) {
            cir.setReturnValue(true);
        }
    }
}
