package dev.craft.ic2.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.core.crafting.GridPosition;
import dev.craft.ic2.Ic2Recipes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Shaped crafting recipe with IC2 {@code AdvRecipe} semantics: patterns are never mirrored, and the charge of
 * electric ingredients is transferred to an electric result. With a {@link FixedPosition} the pattern was written as
 * a full 3×3 grid with blank rows/columns and only matches at exactly that place in a crafting table (UU matter).
 */
public class Ic2ShapedRecipe extends ShapedRecipe {
    private final ShapedRecipePattern pattern;
    private final ItemStack result;
    private final Optional<FixedPosition> position;

    public Ic2ShapedRecipe(ShapedRecipePattern pattern, ItemStack result) {
        this(pattern, result, Optional.empty());
    }

    public Ic2ShapedRecipe(ShapedRecipePattern pattern, ItemStack result, Optional<FixedPosition> position) {
        super("", CraftingBookCategory.MISC, pattern, result);
        this.pattern = pattern;
        this.result = result;
        this.position = position;
    }

    public ShapedRecipePattern getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return result;
    }

    public Optional<FixedPosition> getPosition() {
        return position;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != pattern.width() || input.height() != pattern.height()) {
            return false;
        }
        if (position.isPresent() && !position.get().matches(GridPosition.of(input))) {
            return false;
        }
        for (int y = 0; y < pattern.height(); y++) {
            for (int x = 0; x < pattern.width(); x++) {
                Ingredient ingredient = pattern.ingredients().get(x + y * pattern.width());
                if (!ingredient.test(input.getItem(x + y * input.width()))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ChargeTransfer.apply(result.copy(), input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Ic2Recipes.SHAPED.get();
    }

    /** Offset of the trimmed pattern inside the 3×3 grid it was written for. */
    public record FixedPosition(int left, int top) {
        public static final Codec<FixedPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("left").forGetter(FixedPosition::left),
                Codec.INT.fieldOf("top").forGetter(FixedPosition::top)
        ).apply(instance, FixedPosition::new));
        public static final StreamCodec<ByteBuf, FixedPosition> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, FixedPosition::left,
                ByteBufCodecs.VAR_INT, FixedPosition::top,
                FixedPosition::new);

        /** Only a 3×3 grid at this offset matches; inputs without a known grid position are accepted. */
        boolean matches(GridPosition.Position grid) {
            return grid == null
                    || grid.gridWidth() == 3 && grid.gridHeight() == 3 && grid.left() == left && grid.top() == top;
        }
    }

    public static final class Serializer implements RecipeSerializer<Ic2ShapedRecipe> {
        private static final MapCodec<Ic2ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(Ic2ShapedRecipe::getPattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Ic2ShapedRecipe::getResult),
                FixedPosition.CODEC.optionalFieldOf("position").forGetter(Ic2ShapedRecipe::getPosition)
        ).apply(instance, Ic2ShapedRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, Ic2ShapedRecipe> STREAM_CODEC = StreamCodec.composite(
                ShapedRecipePattern.STREAM_CODEC, Ic2ShapedRecipe::getPattern,
                ItemStack.STREAM_CODEC, Ic2ShapedRecipe::getResult,
                ByteBufCodecs.optional(FixedPosition.STREAM_CODEC), Ic2ShapedRecipe::getPosition,
                Ic2ShapedRecipe::new);

        @Override
        public MapCodec<Ic2ShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Ic2ShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
