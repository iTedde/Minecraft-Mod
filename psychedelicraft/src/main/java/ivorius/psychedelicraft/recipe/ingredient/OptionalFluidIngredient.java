package ivorius.psychedelicraft.recipe.ingredient;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record OptionalFluidIngredient (
        Optional<FluidIngredient> fluid,
        Optional<Ingredient> receptical
) implements ICustomIngredient, Predicate<ItemStack> {
    public static final OptionalFluidIngredient EMPTY = new OptionalFluidIngredient(Optional.empty(), Optional.empty());
    public static final MapCodec<OptionalFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(OptionalFluidIngredient::fluid),
            Ingredient.CODEC_NONEMPTY.optionalFieldOf("receptical").forGetter(OptionalFluidIngredient::receptical)
    ).apply(i, OptionalFluidIngredient::new));
    public static final Codec<NonNullList<OptionalFluidIngredient>> LIST_CODEC = CODEC.codec().listOf().xmap(
            values -> NonNullList.of(EMPTY, values.toArray(OptionalFluidIngredient[]::new)),
            defaultedList -> new ArrayList<>(defaultedList)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, OptionalFluidIngredient> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(FluidIngredient.PACKET_CODEC), OptionalFluidIngredient::fluid,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), OptionalFluidIngredient::receptical,
            OptionalFluidIngredient::new
    );

    public static OptionalFluidIngredient of(FluidIngredient fluid) {
        return new OptionalFluidIngredient(Optional.of(fluid), Optional.empty());
    }

    public static OptionalFluidIngredient of(Ingredient receptical) {
        return new OptionalFluidIngredient(Optional.empty(), Optional.of(receptical));
    }

    public static OptionalFluidIngredient of(FluidIngredient fluid, Ingredient receptical) {
        return new OptionalFluidIngredient(Optional.of(fluid), Optional.of(receptical));
    }

    public boolean isEmpty() {
        return fluid.isEmpty() && receptical.isEmpty();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Stream<ItemStack> getItems() {
        return receptical
                .map(ingredient -> Arrays.stream(ingredient.getItems()))
                .orElseGet(() -> FluidIngredient.allRecepticals(fluid.flatMap(FluidIngredient::fluid)))
                .map(fluid
                        .map(fluid -> (Function<ItemStack, ItemStack>)(receptical -> ItemFluids.set(receptical, fluid.getAsItemFluid(FluidCapacity.get(receptical)))))
                        .orElse(Function.identity()));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return PSIngredients.OPTIONAL_FLUID;
    }

    @Override
    public boolean test(ItemStack stack) {
        return fluid.map(f -> f.test(stack)).orElse(true)
            && receptical.map(r -> r.test(stack)).orElse(true);
    }
}

