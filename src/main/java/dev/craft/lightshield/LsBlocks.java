package dev.craft.lightshield;

import dev.craft.Craft;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** Invisible light of the shield's flashlight cone and glow (spec 51 §3), level from the block state. */
    public static final DeferredBlock<ShieldLightBlock> SHIELD_LIGHT = BLOCKS.register("shield_light",
            () -> new ShieldLightBlock(BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion()
                    .noLootTable().instabreak().pushReaction(PushReaction.DESTROY)
                    .lightLevel(state -> state.getValue(ShieldLightBlock.LEVEL))));

    private LsBlocks() {
    }
}
