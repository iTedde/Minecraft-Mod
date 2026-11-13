package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.ExtractorBlockEntity;
import dev.craft.mffs.block.MffsMachineBlock;
import dev.craft.mffs.block.ForceFieldBlock;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlock;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** MFFS blocks (spec 48 §1.1); the stations follow in phase 48.4. */
public final class MffsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    /** {@code BlockMonazitOre}: stone, hardness 3, resistance 5, drops itself. */
    public static final DeferredBlock<Block> MONAZIT_ORE = BLOCKS.registerSimpleBlock("monazit_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 5.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE));
    public static final DeferredBlock<Block> DEEPSLATE_MONAZIT_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_monazit_ore", BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.5F, 5.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE));

    public static final DeferredBlock<MffsMachineBlock> EXTRACTOR = machine("mffs_extractor",
            ExtractorBlockEntity::new);
    public static final DeferredBlock<MffsMachineBlock> CAPACITOR = machine("mffs_capacitor",
            CapacitorBlockEntity::new);
    public static final DeferredBlock<MffsMachineBlock> CONVERTER = machine("mffs_converter",
            ConverterBlockEntity::new);

    public static final DeferredBlock<ProjectorBlock> PROJECTOR = BLOCKS.register("mffs_projector",
            () -> new ProjectorBlock(machineProperties()));

    /**
     * {@code BlockForceField}: unbreakable, resistance 999, not opaque, no light, no drops, no redstone conduction,
     * cannot be pushed.
     */
    public static final DeferredBlock<ForceFieldBlock> FORCE_FIELD = BLOCKS.register("force_field",
            () -> new ForceFieldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(-1.0F, 999.0F)
                    .sound(SoundType.GLASS).noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK)
                    .isRedstoneConductor((state, level, pos) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false).isValidSpawn((state, level, pos, type) -> false)));

    private MffsBlocks() {
    }

    /** {@code BlockMFFSBase}: material iron, unbreakable (−1), resistance 100, stone sound. */
    private static DeferredBlock<MffsMachineBlock> machine(
            String name, BiFunction<BlockPos, BlockState, ? extends MffsMachineBlockEntity> factory) {
        return BLOCKS.register(name, () -> new MffsMachineBlock(machineProperties(), factory));
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0F, 100.0F)
                .sound(SoundType.STONE);
    }

    /** Machines with the plain front/back/side model (the projector has one per module type). */
    public static List<DeferredBlock<MffsMachineBlock>> machines() {
        return List.of(EXTRACTOR, CAPACITOR, CONVERTER);
    }
}
