package ivorius.psychedelicraft.item.component;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.recipe.FluidMound;

public record ItemFluidsMixture(List<ItemFluids> fluids) implements TooltipProvider {
    private static final ItemFluidsMixture EMPTY = new ItemFluidsMixture(List.of());
    public static final Codec<ItemFluidsMixture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemFluids.CODEC.listOf().fieldOf("fluids").forGetter(ItemFluidsMixture::fluids)
    ).apply(instance, ItemFluidsMixture::of));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemFluidsMixture> PACKET_CODEC = StreamCodec.composite(
            ItemFluids.PACKET_CODEC.apply(ByteBufCodecs.list()), ItemFluidsMixture::fluids,
            ItemFluidsMixture::of
    );

    public static ItemFluidsMixture of(List<ItemFluids> fluids) {
        fluids = removeEmpty(fluids);
        return fluids.isEmpty() ? EMPTY : new ItemFluidsMixture(fluids);
    }

    @NotNull
    public static ItemFluidsMixture of(ItemStack stack) {
        ItemFluidsMixture fluids = stack.get(PSComponents.FLUIDS_MIXTURE);
        return fluids == null ? EMPTY : fluids;
    }

    public static ItemStack set(ItemStack stack, List<ItemFluids> fluids) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            ItemFluidsMixture mixture = of(fluids);
            if (mixture.fluids.size() < 2) {
                stack.remove(PSComponents.FLUIDS_MIXTURE);
                return ItemFluids.set(stack, mixture.getFirstFluid());
            }
            stack = ItemFluids.getItemForFluids(stack, mixture.getFirstFluid());
            stack.set(PSComponents.FLUIDS_MIXTURE, mixture);
        }
        return stack;
    }

    private static List<ItemFluids> removeEmpty(List<ItemFluids> fluids) {
        return FluidMound.of(fluids).getFluids();
    }

    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    public ItemFluids getFirstFluid() {
        return isEmpty() ? ItemFluids.EMPTY : fluids.get(0);
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        if (isEmpty()) {
            return;
        }
        int total = fluids().stream().mapToInt(ItemFluids::amount).sum();
        tooltip.accept(Component.translatable("psychedelicraft.container.mixture").withStyle(ChatFormatting.DARK_GRAY));
        fluids().forEach(fluid -> {
            tooltip.accept(Component.translatable("psychedelicraft.container.mixture.fluid", getPercentage(fluid.amount(), total), fluid.getName()));
        });
    }

    static int getPercentage(int amount, int total) {
        return Mth.floor(100 * ((float)amount / total));
    }
}
