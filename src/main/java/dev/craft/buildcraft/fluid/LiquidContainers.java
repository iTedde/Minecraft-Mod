package dev.craft.buildcraft.fluid;

import java.util.List;

import dev.craft.buildcraft.BcItems;
import dev.craft.ic2.Ic2Fluids;
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
 * the pack (vanilla buckets, BuildCraft oil/fuel buckets, IC2 water/lava cells with 1000 mB, IC2 coolant cells with
 * 1000/3000/6000 mB coolant). The registry keys on item and damage, so only undamaged (unheated) coolant cells count.
 */
public final class LiquidContainers {
    /** One registered container: {@code amount} mB of {@code fluid} in {@code filled}, leaving {@code empty}. */
    public record Entry(Fluid fluid, int amount, Item filled, Item empty) {
    }

    private LiquidContainers() {
    }

    /** All registered containers in registration order (Thermal Expansion builds its transposer recipes from it). */
    public static List<Entry> entries() {
        int bucket = FluidType.BUCKET_VOLUME;
        Item cell = Ic2Items.EMPTY_CELL.get();
        Fluid coolant = Ic2Fluids.COOLANT.get();
        // Filling an empty cell with coolant gives the 10k cell: Forge keeps the last registration (simple cell).
        return List.of(new Entry(Fluids.WATER, bucket, Items.WATER_BUCKET, Items.BUCKET),
                new Entry(Fluids.LAVA, bucket, Items.LAVA_BUCKET, Items.BUCKET),
                new Entry(BcFluids.OIL.get(), bucket, BcItems.OIL_BUCKET.get(), Items.BUCKET),
                new Entry(BcFluids.FUEL.get(), bucket, BcItems.FUEL_BUCKET.get(), Items.BUCKET),
                new Entry(Fluids.WATER, bucket, Ic2Items.WATER_CELL.get(), cell),
                new Entry(Fluids.LAVA, bucket, Ic2Items.LAVA_CELL.get(), cell),
                new Entry(coolant, 1000, Ic2Items.COOLANT_CELL_10K.get(), cell),
                new Entry(coolant, 3000, Ic2Items.COOLANT_CELL_30K.get(), cell),
                new Entry(coolant, 6000, Ic2Items.COOLANT_CELL_60K.get(), cell));
    }

    /** {@code getLiquidForFilledItem}: the liquid in a filled container, or empty. */
    public static FluidStack getLiquidForFilledItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (Entry entry : entries()) {
            if (stack.is(entry.filled()) && stack.getDamageValue() == 0) {
                return new FluidStack(entry.fluid(), entry.amount());
            }
        }
        return FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
    }

    /** The empty container left over from a filled one (bucket, empty cell), or empty if unknown. */
    public static ItemStack emptyContainer(ItemStack filled) {
        for (Entry entry : entries()) {
            if (filled.is(entry.filled()) && filled.getDamageValue() == 0) {
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
                    && available.getAmount() >= entry.amount()) {
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
