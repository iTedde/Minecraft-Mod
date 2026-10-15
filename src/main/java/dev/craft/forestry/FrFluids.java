package dev.craft.forestry;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Forestry {@code liquidGlass}: molten glass of the Thermionic Fabricator, no block and no container. */
public final class FrFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Craft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Craft.MODID);

    public static final DeferredHolder<FluidType, FluidType> LIQUID_GLASS_TYPE = FLUID_TYPES.register("liquid_glass",
            () -> new FluidType(FluidType.Properties.create()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> LIQUID_GLASS = FLUIDS.register("liquid_glass",
            () -> new BaseFlowingFluid.Source(FrFluids.liquidGlassProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_LIQUID_GLASS = FLUIDS.register(
            "flowing_liquid_glass", () -> new BaseFlowingFluid.Flowing(FrFluids.liquidGlassProperties()));

    private static BaseFlowingFluid.Properties liquidGlassProperties;

    private FrFluids() {
    }

    private static BaseFlowingFluid.Properties liquidGlassProperties() {
        if (liquidGlassProperties == null) {
            liquidGlassProperties = new BaseFlowingFluid.Properties(LIQUID_GLASS_TYPE, LIQUID_GLASS,
                    FLOWING_LIQUID_GLASS);
        }
        return liquidGlassProperties;
    }
}
