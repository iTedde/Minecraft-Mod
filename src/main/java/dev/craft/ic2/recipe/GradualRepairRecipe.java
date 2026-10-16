package dev.craft.ic2.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.ic2.Ic2Recipes;
import dev.craft.ic2.item.reactor.GradualItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * IC2 {@code RecipeGradual}: one gradual item plus any number of a charge material lowers its wear by
 * {@code amount} per material (RSH/LZH condensator with redstone or lapis), clamped to 0 … max.
 */
public class GradualRepairRecipe extends CustomRecipe {
    private final Item item;
    private final Ingredient material;
    private final int amount;

    public GradualRepairRecipe(Item item, Ingredient material, int amount) {
        super(CraftingBookCategory.MISC);
        this.item = item;
        this.material = material;
        this.amount = amount;
    }

    public Item getItem() {
        return item;
    }

    public Ingredient getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack gridItem = ItemStack.EMPTY;
        int chargeMats = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (gridItem.isEmpty() && stack.is(item)) {
                gridItem = stack;
            } else if (material.test(stack)) {
                chargeMats++;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (gridItem.isEmpty() || chargeMats <= 0 || !(item instanceof GradualItem gradual)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = gridItem.copyWithCount(1);
        int damage = gradual.getDamageOfStack(result) - amount * chargeMats;
        damage = Math.max(0, Math.min(gradual.getMaxDamageEx(), damage));
        gradual.setDamageForStack(result, damage);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Ic2Recipes.GRADUAL_REPAIR.get();
    }

    public static final class Serializer implements RecipeSerializer<GradualRepairRecipe> {
        private static final MapCodec<GradualRepairRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(GradualRepairRecipe::getItem),
                Ingredient.CODEC_NONEMPTY.fieldOf("material").forGetter(GradualRepairRecipe::getMaterial),
                com.mojang.serialization.Codec.INT.fieldOf("amount").forGetter(GradualRepairRecipe::getAmount)
        ).apply(instance, GradualRepairRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, GradualRepairRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM),
                        GradualRepairRecipe::getItem,
                        Ingredient.CONTENTS_STREAM_CODEC, GradualRepairRecipe::getMaterial,
                        ByteBufCodecs.VAR_INT, GradualRepairRecipe::getAmount,
                        GradualRepairRecipe::new);

        @Override
        public MapCodec<GradualRepairRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GradualRepairRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
