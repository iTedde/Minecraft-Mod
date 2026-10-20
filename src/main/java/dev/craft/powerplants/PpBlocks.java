package dev.craft.powerplants;

import dev.craft.Craft;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.powerplants.block.PlantControllerBlock;
import dev.craft.powerplants.block.ConductingPartBlockEntity;
import dev.craft.powerplants.block.PlantPartBlock;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.boiler.HighPressureBoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import dev.craft.powerplants.turbine.LargeSteamTurbineBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import dev.craft.powerplants.waterturbine.LargeWaterTurbineBlockEntity;
import dev.craft.powerplants.waterturbine.WaterTurbineBlockEntity;
import dev.craft.powerplants.windturbine.LargeWindTurbineBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    public static final DeferredBlock<PlantPartBlock> COKE_OVEN_BRICKS = BLOCKS.register("coke_oven_bricks",
            () -> new PlantPartBlock(bricks()));
    public static final DeferredBlock<PlantControllerBlock> COKE_OVEN = BLOCKS.register("coke_oven",
            () -> new PlantControllerBlock(bricks().lightLevel(PpBlocks::activeLight), CokeOvenBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> BOILER_CASING = BLOCKS.register("boiler_casing",
            () -> new PlantPartBlock(bricks()));
    public static final DeferredBlock<PlantControllerBlock> BOILER_FIREBOX = BLOCKS.register("boiler_firebox",
            () -> new PlantControllerBlock(bricks().lightLevel(PpBlocks::activeLight), BoilerBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> TURBINE_CASING = BLOCKS.register("turbine_casing",
            () -> new PlantPartBlock(metal(), ConductingPartBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> TURBINE_ROTOR = BLOCKS.register("turbine_rotor",
            () -> new PlantPartBlock(metal()));
    public static final DeferredBlock<PlantControllerBlock> STEAM_TURBINE = BLOCKS.register("steam_turbine",
            () -> new PlantControllerBlock(metal(), SteamTurbineBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> WATER_TURBINE_CASING = BLOCKS.register("water_turbine_casing",
            () -> new PlantPartBlock(metal(), ConductingPartBlockEntity::new));
    public static final DeferredBlock<PlantControllerBlock> WATER_TURBINE = BLOCKS.register("water_turbine",
            () -> new PlantControllerBlock(metal(), WaterTurbineBlockEntity::new));
    // large stages (spec 23)
    public static final DeferredBlock<PlantPartBlock> HIGH_PRESSURE_BOILER_CASING = BLOCKS.register(
            "high_pressure_boiler_casing", () -> new PlantPartBlock(metal()));
    public static final DeferredBlock<PlantControllerBlock> HIGH_PRESSURE_BOILER_FIREBOX = BLOCKS.register(
            "high_pressure_boiler_firebox", () -> new PlantControllerBlock(metal().lightLevel(PpBlocks::activeLight),
                    HighPressureBoilerBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> LARGE_TURBINE_CASING = BLOCKS.register("large_turbine_casing",
            () -> new PlantPartBlock(metal(), ConductingPartBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> LARGE_TURBINE_ROTOR = BLOCKS.register("large_turbine_rotor",
            () -> new PlantPartBlock(metal()));
    public static final DeferredBlock<PlantControllerBlock> LARGE_STEAM_TURBINE = BLOCKS.register(
            "large_steam_turbine", () -> new PlantControllerBlock(metal(), LargeSteamTurbineBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> LARGE_WATER_TURBINE_CASING = BLOCKS.register(
            "large_water_turbine_casing", () -> new PlantPartBlock(metal(), ConductingPartBlockEntity::new));
    public static final DeferredBlock<PlantControllerBlock> LARGE_WATER_TURBINE = BLOCKS.register(
            "large_water_turbine", () -> new PlantControllerBlock(metal(), LargeWaterTurbineBlockEntity::new));
    public static final DeferredBlock<PlantPartBlock> WIND_TURBINE_MAST = BLOCKS.register("wind_turbine_mast",
            () -> new PlantPartBlock(metal(), ConductingPartBlockEntity::new));
    public static final DeferredBlock<LargeWindTurbineBlock> LARGE_WIND_TURBINE = BLOCKS.register(
            "large_wind_turbine", () -> new LargeWindTurbineBlock(metal()));
    public static final DeferredBlock<Block> COKE_BLOCK = BLOCKS.registerSimpleBlock("coke_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops());

    private PpBlocks() {
    }

    private static int activeLight(BlockState state) {
        return state.getValue(MachineBlock.ACTIVE) ? 13 : 0;
    }

    /** Coke oven and boiler blocks: hardness 2.0, resistance 10, pickaxe (spec 14 §2, §3). */
    private static BlockBehaviour.Properties bricks() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    /** Turbine blocks and all large stages: hardness 5.0, resistance 10, pickaxe, metal (spec 14 §4, §5, spec 23). */
    private static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                .sound(SoundType.METAL).requiresCorrectToolForDrops();
    }
}
