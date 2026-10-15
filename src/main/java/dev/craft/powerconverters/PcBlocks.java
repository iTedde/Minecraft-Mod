package dev.craft.powerconverters;

import dev.craft.Craft;
import dev.craft.powerconverters.block.ChargerBlock;
import dev.craft.powerconverters.block.EnergyBridgeBlock;
import dev.craft.powerconverters.block.PowerConverterBlock;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PcBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    public static final DeferredBlock<EnergyBridgeBlock> ENERGY_BRIDGE = BLOCKS.register("energy_bridge",
            () -> new EnergyBridgeBlock(properties()));
    public static final DeferredBlock<ChargerBlock> UNIVERSAL_CHARGER = BLOCKS.register("universal_charger",
            () -> new ChargerBlock(properties()));
    private static final Map<ConverterType, DeferredBlock<PowerConverterBlock>> CONVERTERS =
            new EnumMap<>(ConverterType.class);

    static {
        for (ConverterType type : ConverterType.values()) {
            CONVERTERS.put(type, BLOCKS.register(type.getName(), () -> new PowerConverterBlock(type, properties())));
        }
    }

    private PcBlocks() {
    }

    public static DeferredBlock<PowerConverterBlock> converter(ConverterType type) {
        return CONVERTERS.get(type);
    }

    /** {@code BlockPowerConverter}: Material.clay (no tool needed), hardness 1.0, default (stone) sound. */
    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).strength(1.0F).sound(SoundType.STONE);
    }
}
