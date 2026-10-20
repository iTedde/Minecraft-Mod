package dev.craft.powerplants;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.powerplants.block.PlantPartBlock;
import dev.craft.powerplants.block.PlantPartBlockEntity;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.boiler.HighPressureBoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import dev.craft.powerplants.turbine.LargeSteamTurbineBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import dev.craft.powerplants.waterturbine.LargeWaterTurbineBlockEntity;
import dev.craft.powerplants.waterturbine.WaterTurbineBlockEntity;
import dev.craft.powerplants.windturbine.LargeWindTurbineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    /** All casing parts of all power plants share one type; the block picks the part class. */
    public static final Supplier<BlockEntityType<PlantPartBlockEntity>> PLANT_PART = BLOCK_ENTITY_TYPES.register(
            "plant_part", () -> BlockEntityType.Builder.of(PlantPartBlock::createPart,
                    PpBlocks.COKE_OVEN_BRICKS.get(), PpBlocks.BOILER_CASING.get(), PpBlocks.TURBINE_CASING.get(),
                    PpBlocks.TURBINE_ROTOR.get(), PpBlocks.WATER_TURBINE_CASING.get(),
                    PpBlocks.HIGH_PRESSURE_BOILER_CASING.get(), PpBlocks.LARGE_TURBINE_CASING.get(),
                    PpBlocks.LARGE_TURBINE_ROTOR.get(), PpBlocks.LARGE_WATER_TURBINE_CASING.get(),
                    PpBlocks.WIND_TURBINE_MAST.get()).build(null));
    public static final Supplier<BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN = BLOCK_ENTITY_TYPES.register(
            "coke_oven", () -> BlockEntityType.Builder.of(CokeOvenBlockEntity::new, PpBlocks.COKE_OVEN.get())
                    .build(null));
    public static final Supplier<BlockEntityType<BoilerBlockEntity>> BOILER = BLOCK_ENTITY_TYPES.register(
            "boiler", () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, PpBlocks.BOILER_FIREBOX.get())
                    .build(null));
    public static final Supplier<BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE = BLOCK_ENTITY_TYPES
            .register("steam_turbine", () -> BlockEntityType.Builder.of(SteamTurbineBlockEntity::new,
                    PpBlocks.STEAM_TURBINE.get()).build(null));
    public static final Supplier<BlockEntityType<WaterTurbineBlockEntity>> WATER_TURBINE = BLOCK_ENTITY_TYPES
            .register("water_turbine", () -> BlockEntityType.Builder.of(WaterTurbineBlockEntity::new,
                    PpBlocks.WATER_TURBINE.get()).build(null));
    public static final Supplier<BlockEntityType<HighPressureBoilerBlockEntity>> HIGH_PRESSURE_BOILER =
            BLOCK_ENTITY_TYPES.register("high_pressure_boiler", () -> BlockEntityType.Builder.of(
                    HighPressureBoilerBlockEntity::new, PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX.get()).build(null));
    public static final Supplier<BlockEntityType<LargeSteamTurbineBlockEntity>> LARGE_STEAM_TURBINE =
            BLOCK_ENTITY_TYPES.register("large_steam_turbine", () -> BlockEntityType.Builder.of(
                    LargeSteamTurbineBlockEntity::new, PpBlocks.LARGE_STEAM_TURBINE.get()).build(null));
    public static final Supplier<BlockEntityType<LargeWaterTurbineBlockEntity>> LARGE_WATER_TURBINE =
            BLOCK_ENTITY_TYPES.register("large_water_turbine", () -> BlockEntityType.Builder.of(
                    LargeWaterTurbineBlockEntity::new, PpBlocks.LARGE_WATER_TURBINE.get()).build(null));
    public static final Supplier<BlockEntityType<LargeWindTurbineBlockEntity>> LARGE_WIND_TURBINE =
            BLOCK_ENTITY_TYPES.register("large_wind_turbine", () -> BlockEntityType.Builder.of(
                    LargeWindTurbineBlockEntity::new, PpBlocks.LARGE_WIND_TURBINE.get()).build(null));

    private PpBlockEntityTypes() {
    }
}
