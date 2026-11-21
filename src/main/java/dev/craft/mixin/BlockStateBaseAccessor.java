package dev.craft.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets compat code raise the cached light level of foreign block states (Ad Astra lamps, user 2026-11-06). */
@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {
    @Mutable
    @Accessor("lightEmission")
    void craft$setLightEmission(int lightEmission);
}
