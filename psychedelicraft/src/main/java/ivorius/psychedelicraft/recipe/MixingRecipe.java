/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import java.util.Arrays;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;

/**
 * Created from "RecipeFillDrink" by Sollace on 5 Jan 2023
 * Original by lukas on 21.10.14.
 * Recipe that takes as a config:
 * - Input Ingrediences (unshaped)
 * - Input Container
 * - Preconfigured fluid+level
 *
 * Outputs:
 * - Original Container filled with assigned fluid and level
 */
public class MixingRecipe extends ShapelessRecipe implements MultiResultRecipe<MixingRecipe> {
    public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MixingRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(MixingRecipe::category),
            ItemFluids.CODEC.fieldOf("result").forGetter(MixingRecipe::getOutputFluid),
            Ingredient.CODEC.fieldOf("receptical").forGetter(i -> i.receptical),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(i -> i.input)
    ).apply(instance, MixingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MixingRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MixingRecipe::category,
            ItemFluids.PACKET_CODEC, MixingRecipe::getOutputFluid,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.receptical,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, i -> i.input,
            MixingRecipe::new
    );
    private final Ingredient receptical;
    private final NonNullList<Ingredient> input;
    private final ItemFluids output;

    public MixingRecipe(
            String group,
            CraftingBookCategory category,
            ItemFluids output,
            Ingredient receptical,
            NonNullList<Ingredient> input) {
        super(group, category, ItemStack.EMPTY, RecipeUtils.checkLength(RecipeUtils.union(input, receptical)));
        this.receptical = receptical;
        this.input = input;
        this.output = output;
    }

    public ItemFluids getOutputFluid() {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.FILL_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        return getOutputRecepticals(inventory).count() == 1
                && inventory.stackedContents().canCraft(this, null);
    }

    private Stream<ItemStack> getOutputRecepticals(CraftingInput inventory) {
        return RecipeUtils.recepticals(inventory.items()
                .stream())
                .filter(receptical)
                .filter(receptical -> input.stream().noneMatch(i -> i.test(receptical))
                        && ItemFluids.of(receptical).isOf(Fluids.WATER)
                        && FluidCapacity.getPercentage(receptical) >= 1);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, Provider registries) {
        return getOutputRecepticals(inventory)
                .findFirst()
                .map(receptical -> output.amount() <= 1 ? output.ofFilling(receptical.copy()) : ItemFluids.set(receptical.copy(), output.ofAmount(Math.min(output.amount(), FluidCapacity.get(receptical)))))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public Stream<MixingRecipe> flatten() {
        return Arrays.stream(receptical.getItems()).map(result -> {
            return new MixingRecipe(getGroup(), category(), output, Ingredient.of(result), input);
        });
    }
}
