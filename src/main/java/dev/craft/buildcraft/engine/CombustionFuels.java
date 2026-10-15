package dev.craft.buildcraft.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Fuels and coolants of the combustion engine (BuildCraft {@code IronEngineFuel}, {@code IronEngineCoolant}) and
 * the filled containers it accepts (Forge {@code LiquidContainerRegistry}). Oil and fuel are added with the oil
 * fluids.
 */
public final class CombustionFuels {
    /** MJ per tick and burn time in ticks per 1000 mB. */
    public record Fuel(float powerPerCycle, int totalBurningTime) {
    }

    private static final Map<Fluid, Fuel> FUELS = new LinkedHashMap<>();
    private static final Map<Fluid, Float> COOLANTS = new LinkedHashMap<>();

    static {
        registerFuel(Fluids.LAVA, 1.0F, 20000);
        registerCoolant(Fluids.WATER, 1.0F);
    }

    private CombustionFuels() {
    }

    public static void registerFuel(Fluid fluid, float powerPerCycle, int totalBurningTime) {
        FUELS.put(fluid, new Fuel(powerPerCycle, totalBurningTime));
    }

    public static void registerCoolant(Fluid fluid, float coolingPerUnit) {
        COOLANTS.put(fluid, coolingPerUnit);
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

    /**
     * Fluid in a filled container: buckets and other fluid containers, plus the IC2 water and lava cells (registered
     * as 1000 mB containers by IC2).
     */
    public static FluidStack getFluidForFilledItem(ItemStack stack) {
        if (stack.is(Ic2Items.WATER_CELL.get())) {
            return new FluidStack(Fluids.WATER, 1000);
        }
        if (stack.is(Ic2Items.LAVA_CELL.get())) {
            return new FluidStack(Fluids.LAVA, 1000);
        }
        return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    }
}
