package dev.craft.buildcraft;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import net.minecraft.core.registries.Registries;
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

    private BcBlockEntityTypes() {
    }

    public static BlockEntityType<EngineBlockEntity> engine(EngineType type) {
        return ENGINES.get(type).get();
    }
}
