package ivorius.psychedelicraft.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.Processable.ByProductConsumer;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public interface BunsenBurnerRecipe extends Recipe<BunsenBurnerRecipe.Input> {
    /**
     * The number of ticks it takes for this recipe to complete
     */
    int stewTime();

    @Override
    default RecipeType<?> getType() {
        return PSRecipes.CHEMISTRY;
    }

    @Override
    default boolean canCraftInDimensions(int width, int height) {
        return (width * height) > 0;
    }

    public enum ReactionType implements StringRepresentable {
        INGREDIENTS,
        ADDITIONS;

        private final String name = name().toLowerCase(Locale.ROOT);
        public static final Codec<ReactionType> CODEC = StringRepresentable.fromEnum(ReactionType::values);
        public static final StreamCodec<RegistryFriendlyByteBuf, ReactionType> PACKET_CODEC = PacketCodecUtils.ofEnum(ReactionType.class);

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record Input(ReactionType type, FluidMound fluids, ItemMound input, Product consumer, @Nullable Input nextPhase) implements RecipeInput {
        public Input(FluidMound fluids, ItemMound input, Product consumer) {
            this(ReactionType.INGREDIENTS, fluids, input, consumer, new Input(ReactionType.ADDITIONS, fluids, input, consumer, null));
        }

        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return fluids.isEmpty() && input.isEmpty();
        }
    }

    public record Product(FluidMound fluids, List<ItemStack> items, Set<Impurities.Impurity> impurities) implements ByProductConsumer {
        public static final MapCodec<Product> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                FluidMound.CODEC.fieldOf("fluids").forGetter(Product::fluids),
                ItemStack.CODEC.listOf().xmap(l -> (List<ItemStack>)new ArrayList<>(l), Function.identity()).fieldOf("items").forGetter(Product::items),
                CodecUtils.setOf(Impurities.Impurity.CODEC).fieldOf("impurities").forGetter(Product::impurities)
        ).apply(i, Product::new));
        public static final Codec<Product> CODEC = MAP_CODEC.codec();


        @Override
        public void accept(ItemStack stack) {
            items.add(stack);
        }

        @Override
        public void accept(ItemFluids stack) {
            fluids.add(stack);
            if (stack.isOf(PSFluids.GASOLINE)) {
                accept(Impurities.Impurity.GASOLINE);
            }
            if (stack.isOf(PSFluids.ETHANOL)) {
                accept(Impurities.Impurity.ETHANOL);
            }
        }

        public void accept(Impurities.Impurity impurity) {
            impurities.add(impurity);
        }
    }
}
