package dev.craft.enderstorage;

import dev.craft.Craft;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Own blocks instead of the original metadata block (spec 27 §2). */
public final class EsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** Material rock, hardness 20, resistance 100 (1.4.7, ×3/5 = 60). */
    public static final DeferredBlock<EnderChestBlock> ENDER_CHEST = BLOCKS.register("ender_storage_chest",
            () -> new EnderChestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(20.0F, 60.0F)
                    .sound(SoundType.STONE).noOcclusion()));

    private EsBlocks() {
    }
}
