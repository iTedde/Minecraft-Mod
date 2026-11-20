package ivorius.psychedelicraft.compat.emi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.item.component.PSComponents;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Conversions between Psychedelicraft fluids and EMI stacks (upstream: {@code compat/tia/RecipeUtil}).
 * Psychedelicraft counts droplets internally; EMI on NeoForge shows millibuckets.
 */
interface EmiUtil {
    static EmiStack toEmiStack(ItemFluids fluids) {
        if (fluids.isEmpty()) {
            return EmiStack.EMPTY;
        }
        FluidVariant variant = fluids.toVariant();
        return EmiStack.of(variant.fluid(), variant.components(), Math.max(1, fluids.amount() / FluidVariant.DROPLETS_PER_MB));
    }

    static EmiStack toEmiStack(ItemStack receptical, ItemFluids fluids) {
        return EmiStack.of(ItemFluids.set(receptical.copy(), fluids.ofAmount(FluidCapacity.get(receptical))));
    }

    static EmiIngredient toFilled(EmiStack receptical, ItemFluids fluids) {
        return toEmiStack(receptical.getItemStack(), fluids);
    }

    static EmiIngredient toIngredient(FluidIngredient ingredient, int amount) {
        return toEmiStack(ingredient.getAsItemFluid(amount));
    }

    static ItemFluids toItemFluids(EmiStack stack) {
        var fluids = stack.getComponentChanges().get(PSComponents.FLUIDS);
        if (fluids != null && fluids.isPresent()) {
            return fluids.get();
        }
        return ItemFluids.EMPTY;
    }

    static List<ItemFluids> getMatchingFluids(ItemFluids.Predicate predicate, int amount) {
        List<SimpleFluid> fluids = predicate.fluid().filter(l -> !l.isEmpty()).orElseGet(() -> BuiltInRegistries.FLUID.stream().map(SimpleFluid::of).toList());
        if (predicate.amount().max().isPresent()) {
            amount = Math.min(amount, predicate.amount().max().get());
        }
        if (predicate.amount().min().isPresent()) {
            amount = Math.max(amount, predicate.amount().min().get());
        }
        final int a = amount;
        return fluids.stream().map(fluid -> fluid.getDefaultStack(a)).toList();
    }

    /** Equal ingredients are shown once with the number of times they are needed. */
    static List<EmiIngredient> grouped(Stream<Ingredient> ingredients) {
        Map<Ingredient, Integer> counts = new LinkedHashMap<>();
        ingredients.filter(i -> !i.isEmpty()).forEach(i -> counts.merge(i, 1, Integer::sum));
        return counts.entrySet().stream().map(e -> EmiIngredient.of(e.getKey(), e.getValue())).toList();
    }

    static RegistryAccess registries() {
        var level = Minecraft.getInstance().level;
        return level == null ? RegistryAccess.EMPTY : level.registryAccess();
    }

    static String formatTicks(int ticks) {
        var level = Minecraft.getInstance().level;
        return StringUtil.formatTickDuration(ticks, level == null ? 20 : level.tickRateManager().tickrate());
    }

    record Contents(ItemFluids type, EmiIngredient contents, EmiIngredient empty, EmiIngredient filled) {
        static Contents of(EmiIngredient recepticals, ItemFluids fluid) {
            return new Contents(
                    fluid,
                    toEmiStack(fluid),
                    EmiIngredient.of(recepticals.getEmiStacks().stream().map(r -> toFilled(r, ItemFluids.EMPTY)).toList()),
                    EmiIngredient.of(recepticals.getEmiStacks().stream().map(r -> toFilled(r, fluid)).toList())
            );
        }
    }
}
