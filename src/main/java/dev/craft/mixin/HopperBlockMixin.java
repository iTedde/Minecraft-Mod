package dev.craft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Hoppers ignore redstone (user 2026-10-26): the block never sees a signal, so it stays (or turns back) enabled. */
@Mixin(HopperBlock.class)
public abstract class HopperBlockMixin {
    @Redirect(method = "checkPoweredState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean craft$ignoreRedstone(Level level, BlockPos pos) {
        return false;
    }
}
