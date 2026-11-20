package ivorius.psychedelicraft.mixin.datafix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;

@Mixin(ItemStackComponentizationFix.class)
abstract class MixinItemStackComponentizationFix {
    @Inject(method = "fixItemStack", at = @At("TAIL"))
    private static void unicopia_fixStack(ItemStackComponentizationFix.ItemStackData data, Dynamic<?> dynamic, CallbackInfo info) {
        ivorius.psychedelicraft.datafix.Schemas.fixComponents(data, dynamic);
    }
}