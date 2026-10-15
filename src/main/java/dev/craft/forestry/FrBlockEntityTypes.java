package dev.craft.forestry;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.forestry.engine.ElectricalEngineBlockEntity;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FrBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<FabricatorBlockEntity>> FABRICATOR = BLOCK_ENTITY_TYPES.register(
            "thermionic_fabricator",
            () -> BlockEntityType.Builder.of(FabricatorBlockEntity::new, FrBlocks.THERMIONIC_FABRICATOR.get())
                    .build(null));
    public static final Supplier<BlockEntityType<CarpenterBlockEntity>> CARPENTER = BLOCK_ENTITY_TYPES.register(
            "carpenter", () -> BlockEntityType.Builder.of(CarpenterBlockEntity::new, FrBlocks.CARPENTER.get())
                    .build(null));
    public static final Supplier<BlockEntityType<ElectricalEngineBlockEntity>> ELECTRICAL_ENGINE =
            BLOCK_ENTITY_TYPES.register("electrical_engine", () -> BlockEntityType.Builder.of(
                    ElectricalEngineBlockEntity::new, FrBlocks.ELECTRICAL_ENGINE.get()).build(null));

    private FrBlockEntityTypes() {
    }
}
