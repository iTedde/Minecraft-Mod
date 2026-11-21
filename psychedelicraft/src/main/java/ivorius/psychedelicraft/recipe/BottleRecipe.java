package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.BeaconBeamBlock;

public class BottleRecipe extends ShapedRecipe {
    public static final MapCodec<BottleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(BottleRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(BottleRecipe::category),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.raw),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(BottleRecipe::showNotification)
    ).apply(instance, BottleRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BottleRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BottleRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, BottleRecipe::category,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.raw,
            ItemStack.OPTIONAL_STREAM_CODEC, recipe -> recipe.result,
            ByteBufCodecs.BOOL, BottleRecipe::showNotification,
            BottleRecipe::new
    );

    private final ShapedRecipePattern raw;
    private final ItemStack result;

    public BottleRecipe(String group, CraftingBookCategory category, ShapedRecipePattern raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_SHAPED;
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, Provider registries) {
        ItemStack output = RecipeUtils.copyInputFluidToResult(getResultItem(registries).copy(), inventory.items());
        inventory.items().stream().mapToInt(stack -> {
                if (stack.getItem() instanceof BlockItem i && i.getBlock() instanceof BeaconBeamBlock s) {
                    return s.getColor().getTextColor();
                }
                return DyedItemColor.getOrDefault(stack, CommonColors.WHITE);
            })
            .filter(color -> color != CommonColors.WHITE)
            .findFirst()
            .ifPresent(color -> output.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true)));
        return output;
    }
}
