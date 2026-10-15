package dev.craft.buildcraft.engine;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The three engines of BuildCraft 3.4.3 (former block metadata 0–2). */
public enum EngineType {
    REDSTONE("redstone_engine", "wood", RedstoneEngineBlockEntity::new),
    STIRLING("stirling_engine", "stone", StirlingEngineBlockEntity::new),
    COMBUSTION("combustion_engine", "iron", CombustionEngineBlockEntity::new);

    private final String name;
    private final String material;
    private final BiFunction<BlockPos, BlockState, ? extends EngineBlockEntity> factory;

    EngineType(String name, String material, BiFunction<BlockPos, BlockState, ? extends EngineBlockEntity> factory) {
        this.name = name;
        this.material = material;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    /** Suffix of the base texture ({@code base_wood}, {@code base_stone}, {@code base_iron}). */
    public String getMaterial() {
        return material;
    }

    public EngineBlockEntity create(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    public boolean hasMenu() {
        return this != REDSTONE;
    }
}
