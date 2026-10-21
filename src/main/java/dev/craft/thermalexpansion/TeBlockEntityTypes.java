package dev.craft.thermalexpansion;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.block.PortableTankBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyConduitBlockEntity;
import dev.craft.thermalexpansion.transport.LiquiductBlockEntity;
import dev.craft.thermalexpansion.energy.EnergyCellBlockEntity;
import dev.craft.thermalexpansion.energy.MagmaticEngineBlockEntity;
import dev.craft.thermalexpansion.energy.SteamEngineBlockEntity;
import dev.craft.thermalexpansion.machine.AqueousAccumulatorBlockEntity;
import dev.craft.thermalexpansion.machine.GlacialPrecipitatorBlockEntity;
import dev.craft.thermalexpansion.machine.IgneousExtruderBlockEntity;
import dev.craft.thermalexpansion.machine.CrucibleBlockEntity;
import dev.craft.thermalexpansion.machine.PoweredFurnaceBlockEntity;
import dev.craft.thermalexpansion.machine.SmelterBlockEntity;
import dev.craft.thermalexpansion.machine.TransposerBlockEntity;
import dev.craft.thermalexpansion.machine.SecondaryOutputMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entity types of Thermal Expansion (spec 26). */
public final class TeBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    public static final Supplier<BlockEntityType<PoweredFurnaceBlockEntity>> POWERED_FURNACE =
            BLOCK_ENTITY_TYPES.register("powered_furnace", () -> BlockEntityType.Builder.of(
                    PoweredFurnaceBlockEntity::new, TeBlocks.POWERED_FURNACE.get()).build(null));
    public static final Supplier<BlockEntityType<SecondaryOutputMachineBlockEntity>> PULVERIZER =
            BLOCK_ENTITY_TYPES.register("pulverizer", () -> BlockEntityType.Builder.of(
                    SecondaryOutputMachineBlockEntity::pulverizer, TeBlocks.PULVERIZER.get()).build(null));
    public static final Supplier<BlockEntityType<SecondaryOutputMachineBlockEntity>> SAWMILL =
            BLOCK_ENTITY_TYPES.register("sawmill", () -> BlockEntityType.Builder.of(
                    SecondaryOutputMachineBlockEntity::sawmill, TeBlocks.SAWMILL.get()).build(null));
    public static final Supplier<BlockEntityType<SmelterBlockEntity>> INDUCTION_SMELTER =
            BLOCK_ENTITY_TYPES.register("induction_smelter", () -> BlockEntityType.Builder.of(
                    SmelterBlockEntity::new, TeBlocks.INDUCTION_SMELTER.get()).build(null));
    public static final Supplier<BlockEntityType<CrucibleBlockEntity>> MAGMA_CRUCIBLE =
            BLOCK_ENTITY_TYPES.register("magma_crucible", () -> BlockEntityType.Builder.of(
                    CrucibleBlockEntity::new, TeBlocks.MAGMA_CRUCIBLE.get()).build(null));
    public static final Supplier<BlockEntityType<TransposerBlockEntity>> LIQUID_TRANSPOSER =
            BLOCK_ENTITY_TYPES.register("liquid_transposer", () -> BlockEntityType.Builder.of(
                    TransposerBlockEntity::new, TeBlocks.LIQUID_TRANSPOSER.get()).build(null));
    public static final Supplier<BlockEntityType<GlacialPrecipitatorBlockEntity>> GLACIAL_PRECIPITATOR =
            BLOCK_ENTITY_TYPES.register("glacial_precipitator", () -> BlockEntityType.Builder.of(
                    GlacialPrecipitatorBlockEntity::new, TeBlocks.GLACIAL_PRECIPITATOR.get()).build(null));
    public static final Supplier<BlockEntityType<IgneousExtruderBlockEntity>> IGNEOUS_EXTRUDER =
            BLOCK_ENTITY_TYPES.register("igneous_extruder", () -> BlockEntityType.Builder.of(
                    IgneousExtruderBlockEntity::new, TeBlocks.IGNEOUS_EXTRUDER.get()).build(null));
    public static final Supplier<BlockEntityType<AqueousAccumulatorBlockEntity>> AQUEOUS_ACCUMULATOR =
            BLOCK_ENTITY_TYPES.register("aqueous_accumulator", () -> BlockEntityType.Builder.of(
                    AqueousAccumulatorBlockEntity::new, TeBlocks.AQUEOUS_ACCUMULATOR.get()).build(null));
    public static final Supplier<BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE =
            BLOCK_ENTITY_TYPES.register("steam_engine", () -> BlockEntityType.Builder.of(
                    SteamEngineBlockEntity::new, TeBlocks.STEAM_ENGINE.get()).build(null));
    public static final Supplier<BlockEntityType<MagmaticEngineBlockEntity>> MAGMATIC_ENGINE =
            BLOCK_ENTITY_TYPES.register("magmatic_engine", () -> BlockEntityType.Builder.of(
                    MagmaticEngineBlockEntity::new, TeBlocks.MAGMATIC_ENGINE.get()).build(null));
    public static final Supplier<BlockEntityType<EnergyCellBlockEntity>> ENERGY_CELL =
            BLOCK_ENTITY_TYPES.register("redstone_energy_cell", () -> BlockEntityType.Builder.of(
                    EnergyCellBlockEntity::new, TeBlocks.ENERGY_CELL.get()).build(null));
    public static final Supplier<BlockEntityType<EnergyConduitBlockEntity>> ENERGY_CONDUIT =
            BLOCK_ENTITY_TYPES.register("redstone_energy_conduit", () -> BlockEntityType.Builder.of(
                    EnergyConduitBlockEntity::new, TeBlocks.ENERGY_CONDUIT.get()).build(null));
    public static final Supplier<BlockEntityType<LiquiductBlockEntity>> LIQUIDUCT =
            BLOCK_ENTITY_TYPES.register("liquiduct", () -> BlockEntityType.Builder.of(
                    LiquiductBlockEntity::new, TeBlocks.LIQUIDUCT.get()).build(null));
    public static final Supplier<BlockEntityType<PortableTankBlockEntity>> PORTABLE_TANK =
            BLOCK_ENTITY_TYPES.register("portable_tank", () -> BlockEntityType.Builder.of(
                    PortableTankBlockEntity::new, TeBlocks.PORTABLE_TANK.get()).build(null));

    private TeBlockEntityTypes() {
    }
}
