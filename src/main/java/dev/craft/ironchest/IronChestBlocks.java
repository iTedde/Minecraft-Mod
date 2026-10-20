package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One block per chest instead of the original metadata block (spec 22 §1). */
public final class IronChestBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<IronChestType, DeferredBlock<IronChestBlock>> CHESTS = new EnumMap<>(IronChestType.class);

    static {
        for (IronChestType type : IronChestType.values()) {
            // Material iron, hardness 3 (resistance 15 / 5 = 3); the obsidian chest returns 10 000.
            CHESTS.put(type, BLOCKS.register(type.getName() + "_chest", () -> new IronChestBlock(type,
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
                            .strength(3.0F, type.isExplosionResistant() ? 10000.0F : 3.0F).noOcclusion())));
        }
    }

    public static DeferredBlock<IronChestBlock> chest(IronChestType type) {
        return CHESTS.get(type);
    }

    private IronChestBlocks() {
    }
}
