package dev.craft.forestry.recipe;

import java.util.List;

import dev.craft.forestry.FrFluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/** Forestry {@code MachineFabricator.Smelting} ({@code MachineFabricator.initialize}): what melts at which heat. */
public record FabricatorSmelting(Item resource, int amount, int meltingPoint) {
    private static final List<FabricatorSmelting> SMELTINGS = List.of(
            new FabricatorSmelting(Items.GLASS, 1000, 1000),
            new FabricatorSmelting(Items.GLASS_PANE, 375, 1000),
            new FabricatorSmelting(Items.SAND, 1000, 3000));

    public static List<FabricatorSmelting> all() {
        return SMELTINGS;
    }

    public FluidStack product() {
        return new FluidStack(FrFluids.LIQUID_GLASS.get(), amount);
    }

    /** {@code findMatchingSmelting(ItemStack)}. */
    @Nullable
    public static FabricatorSmelting find(ItemStack resource) {
        if (resource.isEmpty()) {
            return null;
        }
        for (FabricatorSmelting smelting : SMELTINGS) {
            if (resource.is(smelting.resource)) {
                return smelting;
            }
        }
        return null;
    }

    /** {@code findMatchingSmelting(LiquidStack)}: the first smelting producing this liquid. */
    @Nullable
    public static FabricatorSmelting find(FluidStack product) {
        if (product.isEmpty()) {
            return null;
        }
        for (FabricatorSmelting smelting : SMELTINGS) {
            if (product.is(smelting.product().getFluid())) {
                return smelting;
            }
        }
        return null;
    }
}
