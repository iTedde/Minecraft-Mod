package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Blocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<CableType, DeferredBlock<CableBlock>> CABLES = new EnumMap<>(CableType.class);

    static {
        for (CableType type : CableType.values()) {
            CABLES.put(type, BLOCKS.register(type.getName(), () -> new CableBlock(type,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(CableBlock.HARDNESS, CableBlock.EXPLOSION_RESISTANCE)
                            .sound(SoundType.WOOL)
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) ->
                                    state.getValue(CableBlock.FOAM) != CableBlock.Foam.NONE))));
        }
    }

    private Ic2Blocks() {
    }

    public static DeferredBlock<CableBlock> cable(CableType type) {
        return CABLES.get(type);
    }
}
