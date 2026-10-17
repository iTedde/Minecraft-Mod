package dev.craft.buildcraft.engine;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.ic2.Ic2Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fuels and coolants of the combustion engine (BuildCraft {@code IronEngineFuel}, {@code IronEngineCoolant}) and
 * the filled containers it accepts (Forge {@code LiquidContainerRegistry}).
 */
public final class CombustionFuels {
    /** MJ per tick and burn time in ticks per 1000 mB. */
    public record Fuel(float powerPerCycle, int totalBurningTime) {
    }

    private static final Map<Fluid, Fuel> FUELS = new LinkedHashMap<>();
    private static final Map<Fluid, Float> COOLANTS = new LinkedHashMap<>();

    static {
        registerFuel(Fluids.LAVA, 1.0F, 20000);
        registerFuel(BcFluids.OIL.get(), 3.0F, 20000);
        registerFuel(BcFluids.FUEL.get(), 6.0F, 100000);
        registerCoolant(Fluids.WATER, 1.0F);
        // IC2 bcIntegration32x: IC2 coolant cools five times as well as water.
        registerCoolant(Ic2Fluids.COOLANT.get(), 5.0F);
    }

    private CombustionFuels() {
    }

    public static void registerFuel(Fluid fluid, float powerPerCycle, int totalBurningTime) {
        FUELS.put(fluid, new Fuel(powerPerCycle, totalBurningTime));
    }

    public static void registerCoolant(Fluid fluid, float coolingPerUnit) {
        COOLANTS.put(fluid, coolingPerUnit);
    }

    public static Map<Fluid, Fuel> fuels() {
        return Collections.unmodifiableMap(FUELS);
    }

    public static Map<Fluid, Float> coolants() {
        return Collections.unmodifiableMap(COOLANTS);
    }

    @Nullable
    public static Fuel getFuel(FluidStack stack) {
        return stack.isEmpty() ? null : FUELS.get(stack.getFluid());
    }

    /** Heat removed per mB, or {@code null} if the fluid is no coolant. */
    @Nullable
    public static Float getCoolant(FluidStack stack) {
        return stack.isEmpty() ? null : COOLANTS.get(stack.getFluid());
    }

    /** Fluid in a filled container (Forge {@code LiquidContainerRegistry}). */
    public static FluidStack getFluidForFilledItem(ItemStack stack) {
        return LiquidContainers.getLiquidForFilledItem(stack);
    }
}
