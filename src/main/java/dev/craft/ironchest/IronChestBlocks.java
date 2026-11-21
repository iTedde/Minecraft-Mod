package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * One block per chest instead of the original metadata block (spec 22 §1), plus the copper chest stages
 * (spec 35 §1).
 */
public final class IronChestBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    private static final Map<IronChestType, DeferredBlock<? extends IronChestBlock>> CHESTS =
            new EnumMap<>(IronChestType.class);
    private static final Map<WeatherState, DeferredBlock<WeatheringCopperChestBlock>> COPPER_CHESTS =
            new EnumMap<>(WeatherState.class);
    private static final Map<WeatherState, DeferredBlock<CopperChestBlock>> WAXED_COPPER_CHESTS =
            new EnumMap<>(WeatherState.class);

    static {
        for (IronChestType type : IronChestType.values()) {
            if (type != IronChestType.COPPER) {
                CHESTS.put(type, BLOCKS.register(type.getName() + "_chest", () -> new IronChestBlock(type,
                        properties(type))));
                continue;
            }
            // The unaffected stage keeps the id craft:copper_chest, so existing chests and the item stay.
            for (WeatherState stage : WeatherState.values()) {
                String prefix = stage == WeatherState.UNAFFECTED ? "" : stage.getSerializedName() + "_";
                COPPER_CHESTS.put(stage, BLOCKS.register(prefix + "copper_chest",
                        () -> new WeatheringCopperChestBlock(stage, properties(type))));
                WAXED_COPPER_CHESTS.put(stage, BLOCKS.register("waxed_" + prefix + "copper_chest",
                        () -> new CopperChestBlock(stage, properties(type))));
            }
            CHESTS.put(type, COPPER_CHESTS.get(WeatherState.UNAFFECTED));
        }
    }

    /** Material iron, hardness 3 (resistance 15 / 5 = 3); the obsidian chest returns 10 000. */
    private static BlockBehaviour.Properties properties(IronChestType type) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
                .strength(3.0F, type.isExplosionResistant() ? 10000.0F : 3.0F).noOcclusion();
    }

    @SuppressWarnings("unchecked")
    public static DeferredBlock<IronChestBlock> chest(IronChestType type) {
        return (DeferredBlock<IronChestBlock>) CHESTS.get(type);
    }

    public static DeferredBlock<? extends CopperChestBlock> copperChest(WeatherState stage, boolean waxed) {
        return waxed ? WAXED_COPPER_CHESTS.get(stage) : COPPER_CHESTS.get(stage);
    }

    private IronChestBlocks() {
    }
}
