package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

/** IC2 {@code TileEntityIronFurnace.getFuelValueFor}, also used by the generator. */
public final class FuelValues {
    /** Burn time of lava containers in IC2 machines (vanilla furnaces use the lava bucket's 20000). */
    public static final int LAVA_CONTAINER = 2000;

    private FuelValues() {
    }

    public static int getFuelValueFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (containsLava(stack)) {
            return LAVA_CONTAINER;
        }
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    /** Liquid-registry lookup: lava bucket, lava cell or any other filled lava container. */
    public static boolean containsLava(ItemStack stack) {
        if (stack.is(Ic2Items.LAVA_CELL.get())) {
            return true;
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        return fluid.getFluid().isSame(Fluids.LAVA);
    }

    /** Liquid-registry lookup for water containers (bucket, water cell, others). */
    public static boolean containsWater(ItemStack stack) {
        if (stack.is(Ic2Items.WATER_CELL.get())) {
            return true;
        }
        FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
        return fluid.getFluid().isSame(Fluids.WATER);
    }
}
