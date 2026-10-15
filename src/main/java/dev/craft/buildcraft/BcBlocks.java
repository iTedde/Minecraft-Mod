package dev.craft.buildcraft;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.engine.EngineType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BcBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<EngineType, DeferredBlock<EngineBlock>> ENGINES = new EnumMap<>(EngineType.class);

    static {
        // BuildCraft BlockEngine: Material.iron (needs a pickaxe for drops), hardness 0.5, default step sound.
        for (EngineType type : EngineType.values()) {
            ENGINES.put(type, BLOCKS.register(type.getName(), () -> new EngineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(0.5F).requiresCorrectToolForDrops().sound(SoundType.STONE)
                    .noOcclusion(), type)));
        }
    }

    private BcBlocks() {
    }

    public static DeferredBlock<EngineBlock> engine(EngineType type) {
        return ENGINES.get(type);
    }
}
