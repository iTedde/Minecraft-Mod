package dev.craft.compactsolars;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CsBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<SolarArrayType, DeferredBlock<SolarArrayBlock>> ARRAYS =
            new EnumMap<>(SolarArrayType.class);

    static {
        for (SolarArrayType type : SolarArrayType.values()) {
            ARRAYS.put(type, BLOCKS.register(type.getName(), () -> new SolarArrayBlock(properties(), type)));
        }
    }

    private CsBlocks() {
    }

    public static DeferredBlock<SolarArrayBlock> array(SolarArrayType type) {
        return ARRAYS.get(type);
    }

    /** {@code BlockCompactSolar}: Material.iron, hardness 3.0, metal sound. */
    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}
