package dev.craft.powerplants;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Steam of the coal power plant (spec 14 §1): only in tanks, no block and no bucket, gaseous. */
public final class PpFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Craft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Craft.MODID);

    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE = FLUID_TYPES.register("steam",
            () -> new FluidType(FluidType.Properties.create().density(-1000).temperature(373)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> STEAM = FLUIDS.register("steam",
            () -> new BaseFlowingFluid.Source(PpFluids.steamProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_STEAM = FLUIDS.register(
            "flowing_steam", () -> new BaseFlowingFluid.Flowing(PpFluids.steamProperties()));

    private static BaseFlowingFluid.Properties steamProperties;

    private PpFluids() {
    }

    private static BaseFlowingFluid.Properties steamProperties() {
        if (steamProperties == null) {
            steamProperties = new BaseFlowingFluid.Properties(STEAM_TYPE, STEAM, FLOWING_STEAM);
        }
        return steamProperties;
    }
}
