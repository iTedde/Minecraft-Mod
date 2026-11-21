package dev.craft.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hoppers ignore redstone (user 2026-10-26): hoppers that were switched off before (enabled=false saved in old worlds)
 * keep moving items even before a neighbour update turns them back on.
 */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @ModifyExpressionValue(method = "tryMoveItems", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private static Comparable<?> craft$alwaysEnabled(Comparable<?> enabled) {
        return Boolean.TRUE;
    }
}
