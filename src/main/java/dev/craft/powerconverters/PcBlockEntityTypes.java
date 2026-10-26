package dev.craft.powerconverters;

import dev.craft.Craft;
import dev.craft.powerconverters.power.BcConsumerBlockEntity;
import dev.craft.powerconverters.power.BcProducerBlockEntity;
import dev.craft.powerconverters.power.ChargerBlockEntity;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity;
import dev.craft.powerconverters.power.FeProducerBlockEntity;
import dev.craft.powerconverters.power.Ic2ConsumerBlockEntity;
import dev.craft.powerconverters.power.Ic2ProducerBlockEntity;
import java.util.Arrays;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PcBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<EnergyBridgeBlockEntity>> ENERGY_BRIDGE = BLOCK_ENTITY_TYPES.register(
            "energy_bridge", () -> BlockEntityType.Builder.of(EnergyBridgeBlockEntity::new,
                    PcBlocks.ENERGY_BRIDGE.get()).build(null));
    public static final Supplier<BlockEntityType<ChargerBlockEntity>> UNIVERSAL_CHARGER = BLOCK_ENTITY_TYPES.register(
            "universal_charger", () -> BlockEntityType.Builder.of(ChargerBlockEntity::new,
                    PcBlocks.UNIVERSAL_CHARGER.get()).build(null));
    public static final Supplier<BlockEntityType<BcConsumerBlockEntity>> BC_CONSUMER = BLOCK_ENTITY_TYPES.register(
            "bc_consumer", () -> BlockEntityType.Builder.of(BcConsumerBlockEntity::new,
                    PcBlocks.converter(ConverterType.BC_CONSUMER).get()).build(null));
    public static final Supplier<BlockEntityType<BcProducerBlockEntity>> BC_PRODUCER = BLOCK_ENTITY_TYPES.register(
            "bc_producer", () -> BlockEntityType.Builder.of(BcProducerBlockEntity::new,
                    PcBlocks.converter(ConverterType.BC_PRODUCER).get()).build(null));
    public static final Supplier<BlockEntityType<Ic2ConsumerBlockEntity>> IC2_CONSUMER = BLOCK_ENTITY_TYPES.register(
            "ic2_consumer", () -> BlockEntityType.Builder.of(Ic2ConsumerBlockEntity::new, ic2Blocks(true))
                    .build(null));
    public static final Supplier<BlockEntityType<Ic2ProducerBlockEntity>> IC2_PRODUCER = BLOCK_ENTITY_TYPES.register(
            "ic2_producer", () -> BlockEntityType.Builder.of(Ic2ProducerBlockEntity::new, ic2Blocks(false))
                    .build(null));
    public static final Supplier<BlockEntityType<FeProducerBlockEntity>> FE_PRODUCER = BLOCK_ENTITY_TYPES.register(
            "fe_producer", () -> BlockEntityType.Builder.of(FeProducerBlockEntity::new,
                    PcBlocks.converter(ConverterType.FE_PRODUCER).get()).build(null));

    private PcBlockEntityTypes() {
    }

    private static Block[] ic2Blocks(boolean consumer) {
        return Arrays.stream(ConverterType.values())
                .filter(type -> type.getSystem() == PowerSystem.INDUSTRIALCRAFT && type.isConsumer() == consumer)
                .map(type -> PcBlocks.converter(type).get()).toArray(Block[]::new);
    }
}
