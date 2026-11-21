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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by lukas on 10.11.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * A shapeless recipe that preserves a drink bottle's contents between crafting.
 *
 * Used to change the container a fluid is in without losing any of its contents.
 *
 */
public class ChangeRecepticalRecipe extends ShapelessRecipe {
    public static final MapCodec<ChangeRecepticalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ChangeRecepticalRecipe::getGroup),
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ChangeRecepticalRecipe::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ChangeRecepticalRecipe::getIngredients)
    ).apply(instance, ChangeRecepticalRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeRecepticalRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ChangeRecepticalRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ChangeRecepticalRecipe::category,
            ItemStack.OPTIONAL_STREAM_CODEC, recipe -> recipe.output,
            RecipeUtils.INGREDIENTS_PACKET_CODEC, ChangeRecepticalRecipe::getIngredients,
            ChangeRecepticalRecipe::new
    );

    private final ItemStack output;

    public ChangeRecepticalRecipe(String group, CraftingBookCategory category, ItemStack output, NonNullList<Ingredient> input) {
        super(group, category, output, input);
        this.output = output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CHANGE_RECEPTICAL;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        return RecipeUtils.recepticals(inventory.items().stream()).count() == 1 && super.matches(inventory, world);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, Provider registries) {
        return RecipeUtils.copyInputFluidToResult(getResultItem(registries).copy(), inventory.items());
    }
}
