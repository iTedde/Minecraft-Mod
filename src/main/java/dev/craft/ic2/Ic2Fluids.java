package dev.craft.ic2;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * IC2 {@code itemCoolant} as liquid "coolant": only in tanks and in the coolant cells (10k/30k/60k = 1000/3000/6000
 * mB), no block and no bucket (spec 06 §6).
 */
public final class Ic2Fluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Craft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Craft.MODID);

    public static final DeferredHolder<FluidType, FluidType> COOLANT_TYPE = FLUID_TYPES.register("coolant",
            () -> new FluidType(FluidType.Properties.create()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> COOLANT = FLUIDS.register("coolant",
            () -> new BaseFlowingFluid.Source(Ic2Fluids.coolantProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_COOLANT = FLUIDS.register(
            "flowing_coolant", () -> new BaseFlowingFluid.Flowing(Ic2Fluids.coolantProperties()));

    private static BaseFlowingFluid.Properties coolantProperties;

    private Ic2Fluids() {
    }

    private static BaseFlowingFluid.Properties coolantProperties() {
        if (coolantProperties == null) {
            coolantProperties = new BaseFlowingFluid.Properties(COOLANT_TYPE, COOLANT, FLOWING_COOLANT);
        }
        return coolantProperties;
    }
}
