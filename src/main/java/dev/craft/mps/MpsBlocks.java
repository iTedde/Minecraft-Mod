package dev.craft.mps;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.mps.block.TinkerTableBlock;
import dev.craft.mps.block.TinkerTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Tinker Table (spec 29 §4). */
public final class MpsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    /** Material iron, hardness 1.5, resistance 1 000 (1.4.7, ×3/5 → 600), metal sound, light 0.4 (= 6). */
    public static final DeferredBlock<TinkerTableBlock> TINKER_TABLE = BLOCKS.register("tinker_table",
            () -> new TinkerTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(1.5F, 600.0F).sound(SoundType.METAL).lightLevel(state -> 6)
                    .requiresCorrectToolForDrops().noOcclusion()));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<TinkerTableBlockEntity>> TINKER_TABLE_ENTITY =
            BLOCK_ENTITY_TYPES.register("tinker_table", () -> BlockEntityType.Builder.of(TinkerTableBlockEntity::new,
                    TINKER_TABLE.get()).build(null));

    private MpsBlocks() {
    }
}
