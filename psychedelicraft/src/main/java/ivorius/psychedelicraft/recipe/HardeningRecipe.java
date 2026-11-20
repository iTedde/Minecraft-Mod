package ivorius.psychedelicraft.recipe;

import java.util.List;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public record HardeningRecipe(
        String hardeningGroup,
        FluidIngredient coreFluid,
        List<FluidIngredient> solutions,
        ImpuritiesPredicate impurities,
        ItemStack result,
        IntProvider amount,
        int hardeningTime
    ) implements Recipe<HardeningRecipe.Input> {
    public static final MapCodec<HardeningRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("group").forGetter(HardeningRecipe::hardeningGroup),
            FluidIngredient.CODEC.fieldOf("core_fluid").forGetter(HardeningRecipe::coreFluid),
            FluidIngredient.CODEC.listOf().fieldOf("solutions").forGetter(HardeningRecipe::solutions),
            ImpuritiesPredicate.CODEC.optionalFieldOf("impurities", ImpuritiesPredicate.EMPTY).forGetter(HardeningRecipe::impurities),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(HardeningRecipe::result),
            IntProvider.POSITIVE_CODEC.fieldOf("amount").forGetter(HardeningRecipe::amount),
            Codec.INT.optionalFieldOf("hardening_time", 20).forGetter(HardeningRecipe::hardeningTime)
    ).apply(i, HardeningRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, HardeningRecipe> STREAM_CODEC = PacketCodecUtils.tuple(
            ByteBufCodecs.STRING_UTF8, HardeningRecipe::hardeningGroup,
            FluidIngredient.PACKET_CODEC, HardeningRecipe::coreFluid,
            FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()), HardeningRecipe::solutions,
            ImpuritiesPredicate.PACKET_CODEC, HardeningRecipe::impurities,
            ItemStack.STREAM_CODEC, HardeningRecipe::result,
            PacketCodecUtils.INT_PROVIDER_VALUE_CODEC, HardeningRecipe::amount,
            ByteBufCodecs.INT, HardeningRecipe::hardeningTime,
            HardeningRecipe::new
    );

    @Override
    public ItemStack getToastSymbol() {
        return PSItems.TRAY.getDefaultInstance();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.HARDENING;
    }

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.TRAY;
    }

    @Override
    public boolean matches(Input input, Level world) {
        FluidMound fluids = FluidMound.of(input.impurities());
        return isCoreFluid(input.coreFluid())
            && solutions.stream().allMatch(i -> fluids.removeMatch(i, -1) > 0)
            && impurities.test(input.cuts());
    }

    public boolean isCoreFluid(ItemFluids fluids) {
        return coreFluid.test(fluids);
    }

    public boolean isValidImpurity(ItemFluids fluids) {
        return solutions.stream().anyMatch(i -> i.test(fluids))
            || fluids.isOf(PSFluids.PETROLIUM)
            || fluids.isOf(PSFluids.ETHANOL)
            || fluids.isOf(PSFluids.GASOLINE);
    }

    @Override
    public String getGroup() {
        return hardeningGroup;
    }

    @Override
    public ItemStack assemble(Input input, Provider lookup) {
        ItemStack result = getResultItem(lookup);
        FluidMound fluids = FluidMound.of(input.impurities());
        fluids.split(fluid -> solutions.stream().anyMatch(i -> i.test(fluid)));

        Impurities inputCuts = impurities.permitted(input.cuts());

        int count = amount().sample(input.random()) * (int)Math.pow(2, inputCuts.impurities().size());
        return Impurities.set(result.copyWithCount(count), inputCuts);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(Provider lookup) {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean isIncomplete() {
        return coreFluid.isEmpty() && solutions.isEmpty();
    }

    public record Input(RandomSource random, ItemFluids coreFluid, FluidMound impurities, Impurities cuts) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return coreFluid.isEmpty() && impurities.isEmpty();
        }
    }

}
