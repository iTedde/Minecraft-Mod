package dev.craft.mixin.epl;

import com.enhancedplayerlist.Config;
import com.enhancedplayerlist.client.event.ClientEventHandler;
import dev.craft.compat.epl.EplMinedBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Spec 39 §3: header of the "blocksMined" column ("Mined" instead of the generated "BlocksMined"). */
@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class EplHeaderMixin {
    @Inject(method = "formatStatHeader", at = @At("HEAD"), cancellable = true)
    private static void craft$minedHeader(String stat, CallbackInfoReturnable<String> cir) {
        if (EplMinedBlocks.STAT.equals(stat)) {
            cir.setReturnValue(EplMinedBlocks.header(Config.compactMode));
        }
    }
}
