/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public interface RecipeUtils {
    Codec<NonNullList<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.CODEC_NONEMPTY.listOf().flatXmap(ingredients -> {
        Ingredient[] ingredients2 = ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
        if (ingredients2.length == 0) {
            return DataResult.error(() -> "No ingredients for shapeless recipe");
        }
        if (ingredients2.length > 9) {
            return DataResult.error(() -> "Too many ingredients for shapeless recipe");
        }
        return DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients2));
    }, DataResult::success);

    StreamCodec<RegistryFriendlyByteBuf, CraftingBookCategory> CRAFTING_RECIPE_CATEGORY_PACKET_CODEC = PacketCodecUtils.ofEnum(CraftingBookCategory.class);
    StreamCodec<RegistryFriendlyByteBuf, CookingBookCategory> COOKING_RECIPE_CATEGORY_PACKET_CODEC = PacketCodecUtils.ofEnum(CookingBookCategory.class);
    StreamCodec<RegistryFriendlyByteBuf, NonNullList<Ingredient>> INGREDIENTS_PACKET_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(PacketCodecUtils.toDefaultedList());

    static ItemStack copyInputFluidToResult(ItemStack result, List<ItemStack> inputs) {
        return RecipeUtils.recepticals(inputs.stream()).findFirst().map(input -> {
            // copy bottle contents to the new stack
            return ItemFluids.set(input.transmuteCopy(result.getItem(), result.getCount()), ItemFluids.of(input));
        }).orElse(result);
    }

    static Stream<ItemStack> recepticals(Stream<ItemStack> stacks) {
        return stacks.filter(stack -> FluidCapacity.get(stack) > 0);
    }

    static Stream<Entry<ItemStack>> recepticalSlots(RecipeInput input) {
        return slots(input, stack -> FluidCapacity.get(stack) > 0, Function.identity());
    }

    static Stream<ItemStack> stacks(Container inventory) {
        return IntStream.range(0, inventory.getContainerSize())
                .mapToObj(inventory::getItem)
                .filter(s -> !s.isEmpty());
    }

    static <T> Stream<Entry<T>> slots(RecipeInput input, Predicate<ItemStack> filter, Function<ItemStack, T> func) {
        return IntStream.range(0, input.size())
                .filter(i -> filter.test(input.getItem(i)))
                .mapToObj(i -> new Entry<>(func.apply(input.getItem(i)), i));
    }

    static <T> Stream<Slot<T>> slots(Container inventory, Predicate<ItemStack> filter, Function<ItemStack, T> func) {
        return IntStream.range(0, inventory.getContainerSize())
                .filter(i -> filter.test(inventory.getItem(i)))
                .mapToObj(i -> new Slot<>(inventory, func.apply(inventory.getItem(i)), i));
    }

    static NonNullList<Ingredient> union(NonNullList<Ingredient> list, Ingredient...additional) {
        return NonNullList.of(Ingredient.EMPTY, Stream.concat(list.stream(), Arrays.stream(additional)).toArray(Ingredient[]::new));
    }

    static <T> NonNullList<T> checkLength(NonNullList<T> ingredients) {
        if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (ingredients.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
        return ingredients;
    }

    static Optional<ItemStack> consume(Container inventory, Predicate<ItemStack> filter) {
        return IntStream.range(0, inventory.getContainerSize())
                .filter(i -> filter.test(inventory.getItem(i)))
                .mapToObj(i -> inventory.getItem(i).split(1))
                .findFirst();
    }

    static <T> T iDontCareWhich(Either<T, T> either) {
        return either.left().or(either::right).orElseThrow();
    }

    record Entry<T>(T content, int position) {}

    record Slot<T>(Container inventory, T content, int slot) {
        public void set(ItemStack stack) {
            inventory.setItem(slot, stack);
        }

        public <V> V map(Function<T, V> func) {
            return func.apply(content);
        }
    }
}
