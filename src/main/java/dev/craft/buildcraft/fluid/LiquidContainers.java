package dev.craft.buildcraft.fluid;

import java.util.List;

import dev.craft.buildcraft.BcItems;
import dev.craft.ic2.Ic2Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * Forge 1.4.7 {@code LiquidContainerRegistry} as used by the BuildCraft machines: the filled containers registered in
 * the pack (vanilla buckets, BuildCraft oil/fuel buckets, IC2 water/lava cells), each holding 1000 mB.
 */
public final class LiquidContainers {
    private record Entry(Fluid fluid, Item filled, Item empty) {
    }

    private LiquidContainers() {
    }

    private static List<Entry> entries() {
        return List.of(new Entry(Fluids.WATER, Items.WATER_BUCKET, Items.BUCKET),
                new Entry(Fluids.LAVA, Items.LAVA_BUCKET, Items.BUCKET),
                new Entry(BcFluids.OIL.get(), BcItems.OIL_BUCKET.get(), Items.BUCKET),
                new Entry(BcFluids.FUEL.get(), BcItems.FUEL_BUCKET.get(), Items.BUCKET),
                new Entry(Fluids.WATER, Ic2Items.WATER_CELL.get(), Ic2Items.EMPTY_CELL.get()),
                new Entry(Fluids.LAVA, Ic2Items.LAVA_CELL.get(), Ic2Items.EMPTY_CELL.get()));
    }

    /** {@code getLiquidForFilledItem}: the liquid in a filled container, or empty. */
    public static FluidStack getLiquidForFilledItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (Entry entry : entries()) {
            if (stack.is(entry.filled())) {
                return new FluidStack(entry.fluid(), FluidType.BUCKET_VOLUME);
            }
        }
        return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    }

    /** The empty container left over from a filled one (bucket, empty cell), or empty if unknown. */
    public static ItemStack emptyContainer(ItemStack filled) {
        for (Entry entry : entries()) {
            if (filled.is(entry.filled())) {
                return new ItemStack(entry.empty());
            }
        }
        return filled.hasCraftingRemainingItem() ? filled.getCraftingRemainingItem() : ItemStack.EMPTY;
    }

    /** {@code fillLiquidContainer}: the filled container if {@code available} holds enough, otherwise empty. */
    public static ItemStack fillLiquidContainer(FluidStack available, ItemStack container) {
        if (available.isEmpty() || container.isEmpty()) {
            return ItemStack.EMPTY;
        }
        for (Entry entry : entries()) {
            if (container.is(entry.empty()) && available.is(entry.fluid())
                    && available.getAmount() >= FluidType.BUCKET_VOLUME) {
                return new ItemStack(entry.filled());
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * BuildCraft {@code Utils.consumeItem}: a single item turns into its container (bucket), a larger stack loses one
     * item – the empty container is lost then, as in the original.
     */
    public static ItemStack consumeItem(ItemStack stack) {
        if (stack.getCount() == 1) {
            return stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.shrink(1);
        return result;
    }
}
