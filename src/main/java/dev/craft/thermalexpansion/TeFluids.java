package dev.craft.thermalexpansion;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Molten Redstone and Molten Ender ({@code itemLiquid} meta 0 and 64): no world block and no container, as in TE 2.2.2.2
 * – they only exist in machine tanks (magma crucible, liquid transposer) and liquiducts.
 */
public final class TeFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Craft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Craft.MODID);

    public static final DeferredHolder<FluidType, FluidType> MOLTEN_REDSTONE_TYPE = FLUID_TYPES.register(
            "molten_redstone", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_ENDER_TYPE = FLUID_TYPES.register(
            "molten_ender", () -> new FluidType(FluidType.Properties.create()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_REDSTONE = FLUIDS.register(
            "molten_redstone", () -> new BaseFlowingFluid.Source(TeFluids.redstoneProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_REDSTONE = FLUIDS.register(
            "flowing_molten_redstone", () -> new BaseFlowingFluid.Flowing(TeFluids.redstoneProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MOLTEN_ENDER = FLUIDS.register(
            "molten_ender", () -> new BaseFlowingFluid.Source(TeFluids.enderProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MOLTEN_ENDER = FLUIDS.register(
            "flowing_molten_ender", () -> new BaseFlowingFluid.Flowing(TeFluids.enderProperties()));

    private static BaseFlowingFluid.Properties redstoneProperties;
    private static BaseFlowingFluid.Properties enderProperties;

    private TeFluids() {
    }

    private static BaseFlowingFluid.Properties redstoneProperties() {
        if (redstoneProperties == null) {
            redstoneProperties = new BaseFlowingFluid.Properties(MOLTEN_REDSTONE_TYPE, MOLTEN_REDSTONE,
                    FLOWING_MOLTEN_REDSTONE);
        }
        return redstoneProperties;
    }

    private static BaseFlowingFluid.Properties enderProperties() {
        if (enderProperties == null) {
            enderProperties = new BaseFlowingFluid.Properties(MOLTEN_ENDER_TYPE, MOLTEN_ENDER, FLOWING_MOLTEN_ENDER);
        }
        return enderProperties;
    }
}
