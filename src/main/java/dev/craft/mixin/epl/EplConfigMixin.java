package dev.craft.mixin.epl;

import com.enhancedplayerlist.Config;
import dev.craft.compat.epl.EplMinedBlocks;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Spec 39 §3: "blocksMined" is part of EPL's default columns (read lazily by the config's default supplier). */
@Mixin(value = Config.class, remap = false)
public abstract class EplConfigMixin {
    @Shadow
    @Final
    @Mutable
    private static List<String> DEFAULT_STATS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void craft$addMinedBlocks(CallbackInfo ci) {
        DEFAULT_STATS = EplMinedBlocks.withMinedBlocks(DEFAULT_STATS);
    }
}
