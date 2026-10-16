package dev.craft.buildcraft;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.buildcraft.builders.MarkerBlockEntity;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.factory.MiningWellBlockEntity;
import dev.craft.buildcraft.factory.PumpBlockEntity;
import dev.craft.buildcraft.factory.QuarryBlockEntity;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.factory.TankBlockEntity;
import dev.craft.buildcraft.transport.FluidPipeBlockEntity;
import dev.craft.buildcraft.transport.PowerPipeBlockEntity;
import dev.craft.buildcraft.transport.StructurePipeBlockEntity;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BcBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    private static final Map<EngineType, Supplier<BlockEntityType<EngineBlockEntity>>> ENGINES =
            new EnumMap<>(EngineType.class);

    static {
        for (EngineType type : EngineType.values()) {
            ENGINES.put(type, BLOCK_ENTITY_TYPES.register(type.getName(), () -> {
                EngineBlock block = BcBlocks.engine(type).get();
                return BlockEntityType.Builder.<EngineBlockEntity>of(type::create, block).build(null);
            }));
        }
    }

    public static final Supplier<BlockEntityType<MarkerBlockEntity>> MARKER = BLOCK_ENTITY_TYPES.register("landmark",
            () -> BlockEntityType.Builder.of(MarkerBlockEntity::new, BcBlocks.MARKER.get()).build(null));
    public static final Supplier<BlockEntityType<QuarryBlockEntity>> QUARRY = BLOCK_ENTITY_TYPES.register("quarry",
            () -> BlockEntityType.Builder.of(QuarryBlockEntity::new, BcBlocks.QUARRY.get()).build(null));
    public static final Supplier<BlockEntityType<TankBlockEntity>> TANK = BLOCK_ENTITY_TYPES.register("tank",
            () -> BlockEntityType.Builder.of(TankBlockEntity::new, BcBlocks.TANK.get()).build(null));
    public static final Supplier<BlockEntityType<PumpBlockEntity>> PUMP = BLOCK_ENTITY_TYPES.register("pump",
            () -> BlockEntityType.Builder.of(PumpBlockEntity::new, BcBlocks.PUMP.get()).build(null));
    public static final Supplier<BlockEntityType<RefineryBlockEntity>> REFINERY = BLOCK_ENTITY_TYPES.register(
            "refinery", () -> BlockEntityType.Builder.of(RefineryBlockEntity::new, BcBlocks.REFINERY.get()).build(null));
    public static final Supplier<BlockEntityType<MiningWellBlockEntity>> MINING_WELL = BLOCK_ENTITY_TYPES.register(
            "mining_well",
            () -> BlockEntityType.Builder.of(MiningWellBlockEntity::new, BcBlocks.MINING_WELL.get()).build(null));

    /** All item pipes share one type; the pipe type comes from the block. */
    public static final Supplier<BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE = BLOCK_ENTITY_TYPES.register(
            "item_pipe", () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, Arrays.stream(
                    PipeType.values()).filter(type -> type.getTransport() == PipeType.Transport.ITEMS)
                    .map(type -> BcBlocks.pipe(type).get()).toArray(Block[]::new))
                    .build(null));
    public static final Supplier<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = BLOCK_ENTITY_TYPES.register(
            "fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, Arrays.stream(
                    PipeType.values()).filter(type -> type.getTransport() == PipeType.Transport.FLUIDS)
                    .map(type -> BcBlocks.pipe(type).get()).toArray(Block[]::new))
                    .build(null));
    public static final Supplier<BlockEntityType<PowerPipeBlockEntity>> POWER_PIPE = BLOCK_ENTITY_TYPES.register(
            "power_pipe", () -> BlockEntityType.Builder.of(PowerPipeBlockEntity::new, pipes(PipeType.Transport.POWER))
                    .build(null));
    public static final Supplier<BlockEntityType<StructurePipeBlockEntity>> STRUCTURE_PIPE =
            BLOCK_ENTITY_TYPES.register("structure_pipe", () -> BlockEntityType.Builder.of(
                    StructurePipeBlockEntity::new, pipes(PipeType.Transport.STRUCTURE)).build(null));

    private BcBlockEntityTypes() {
    }

    private static Block[] pipes(PipeType.Transport transport) {
        return Arrays.stream(PipeType.values()).filter(type -> type.getTransport() == transport)
                .map(type -> BcBlocks.pipe(type).get()).toArray(Block[]::new);
    }

    public static BlockEntityType<EngineBlockEntity> engine(EngineType type) {
        return ENGINES.get(type).get();
    }
}
