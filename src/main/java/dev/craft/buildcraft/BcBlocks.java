package dev.craft.buildcraft;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.buildcraft.builders.MarkerBlock;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.factory.FrameBlock;
import dev.craft.buildcraft.factory.MiningPipeBlock;
import dev.craft.buildcraft.factory.MiningWellBlock;
import dev.craft.buildcraft.factory.PumpBlock;
import dev.craft.buildcraft.factory.RefineryBlock;
import dev.craft.buildcraft.factory.TankBlock;
import dev.craft.buildcraft.factory.QuarryBlock;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.transport.GenericPipeBlock;
import dev.craft.buildcraft.transport.PipeType;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BcBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<EngineType, DeferredBlock<EngineBlock>> ENGINES = new EnumMap<>(EngineType.class);
    private static final Map<PipeType, DeferredBlock<GenericPipeBlock>> PIPES = new EnumMap<>(PipeType.class);

    static {
        // BuildCraft BlockEngine: Material.iron (needs a pickaxe for drops), hardness 0.5, default step sound.
        for (EngineType type : EngineType.values()) {
            ENGINES.put(type, BLOCKS.register(type.getName(), () -> new EngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(0.5F).requiresCorrectToolForDrops().sound(SoundType.STONE)
                    .noOcclusion(), type)));
        }
        // BuildCraft BlockGenericPipe: Material.glass, hardness pipes.durability = 0.25, stone step sound.
        for (PipeType type : PipeType.values()) {
            PIPES.put(type, BLOCKS.register(type.getName(), () -> GenericPipeBlock.create(BlockBehaviour.Properties
                    .of().strength(0.25F).sound(SoundType.STONE).noOcclusion().isRedstoneConductor((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false), type)));
        }
    }

    /** BuildCraft {@code BlockOilStill}/{@code BlockOilFlowing}: Material.water, hardness 100. */
    public static final DeferredBlock<LiquidBlock> OIL = BLOCKS.register("oil", () -> new LiquidBlock(BcFluids.OIL.get(),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).replaceable().noCollission().strength(100.0F)
                    .pushReaction(PushReaction.DESTROY).noLootTable().liquid().sound(SoundType.EMPTY)));

    /** BuildCraft {@code BlockMarker}: Material.circuits, hardness 0, light 0.5. */
    public static final DeferredBlock<MarkerBlock> MARKER = BLOCKS.register("landmark", () -> new MarkerBlock(
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(state -> 7).sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY)));
    /** BuildCraft {@code BlockQuarry}: Material.iron, hardness 1.5, resistance like stone. */
    public static final DeferredBlock<QuarryBlock> QUARRY = BLOCKS.register("quarry", () -> new QuarryBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));
    /** BuildCraft {@code BlockFrame}: Material.glass, hardness 0.5, no drops. */
    public static final DeferredBlock<FrameBlock> FRAME = BLOCKS.register("frame", () -> new FrameBlock(
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.STONE).noOcclusion().noLootTable()
                    .isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));
    /** BuildCraft {@code BlockTank}: Material.glass, hardness 0.5. */
    public static final DeferredBlock<TankBlock> TANK = BLOCKS.register("tank", () -> new TankBlock(
            BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.GLASS).noOcclusion()
                    .isRedstoneConductor((s, l, p) -> false).isViewBlocking((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)));
    /** BuildCraft {@code BlockPump}: Material.iron, hardness 5. */
    public static final DeferredBlock<PumpBlock> PUMP = BLOCKS.register("pump", () -> new PumpBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));
    /** BuildCraft {@code BlockRefinery}: Material.iron, hardness 0.5, drawn by its renderer. */
    public static final DeferredBlock<RefineryBlock> REFINERY = BLOCKS.register("refinery", () -> new RefineryBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).noOcclusion()));
    /** BuildCraft {@code BlockMiningWell}: Material.ground, hardness 1.5, resistance 10 (= 6 in 1.21 units). */
    public static final DeferredBlock<MiningWellBlock> MINING_WELL = BLOCKS.register("mining_well",
            () -> new MiningWellBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)));
    /** BuildCraft {@code BlockPlainPipe}: Material.glass, hardness 0, no drops, no item. */
    public static final DeferredBlock<MiningPipeBlock> MINING_PIPE = BLOCKS.register("mining_pipe",
            () -> new MiningPipeBlock(BlockBehaviour.Properties.of().instabreak().sound(SoundType.STONE)
                    .noOcclusion().noLootTable().isViewBlocking((s, l, p) -> false)
                    .isSuffocating((s, l, p) -> false)));

    private BcBlocks() {
    }

    public static DeferredBlock<EngineBlock> engine(EngineType type) {
        return ENGINES.get(type);
    }

    public static DeferredBlock<GenericPipeBlock> pipe(PipeType type) {
        return PIPES.get(type);
    }
}
