package dev.craft.powerplants;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.powerplants.block.PlantPartBlockEntity;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    /** All casing parts of all power plants share one type. */
    public static final Supplier<BlockEntityType<PlantPartBlockEntity>> PLANT_PART = BLOCK_ENTITY_TYPES.register(
            "plant_part", () -> BlockEntityType.Builder.of(PlantPartBlockEntity::new,
                    PpBlocks.COKE_OVEN_BRICKS.get(), PpBlocks.BOILER_CASING.get(), PpBlocks.TURBINE_CASING.get(),
                    PpBlocks.TURBINE_ROTOR.get()).build(null));
    public static final Supplier<BlockEntityType<CokeOvenBlockEntity>> COKE_OVEN = BLOCK_ENTITY_TYPES.register(
            "coke_oven", () -> BlockEntityType.Builder.of(CokeOvenBlockEntity::new, PpBlocks.COKE_OVEN.get())
                    .build(null));
    public static final Supplier<BlockEntityType<BoilerBlockEntity>> BOILER = BLOCK_ENTITY_TYPES.register(
            "boiler", () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, PpBlocks.BOILER_FIREBOX.get())
                    .build(null));
    public static final Supplier<BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE = BLOCK_ENTITY_TYPES
            .register("steam_turbine", () -> BlockEntityType.Builder.of(SteamTurbineBlockEntity::new,
                    PpBlocks.STEAM_TURBINE.get()).build(null));

    private PpBlockEntityTypes() {
    }
}
