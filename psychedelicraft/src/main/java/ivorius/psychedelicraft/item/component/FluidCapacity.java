package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.FluidTransferUtils;
import ivorius.psychedelicraft.fluid.container.RecepticalHandler;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;

public record FluidCapacity(int capacity) {
    private static final Interner<FluidCapacity> INTERNER = Interners.newStrongInterner();
    public static final FluidCapacity EMPTY = create(0);

    public static final Codec<FluidCapacity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("capacity").forGetter(FluidCapacity::capacity)
    ).apply(instance, FluidCapacity::create));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidCapacity> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, FluidCapacity::capacity,
            FluidCapacity::create
    );

    public static FluidCapacity create(int capacity) {
        return INTERNER.intern(new FluidCapacity(capacity));
    }

    public static int get(ItemStack stack) {
        FluidCapacity capacity = stack.get(PSComponents.FLUID_CAPACITY);
        if (capacity == null) {

            int maxLevel = (int)FluidTransferUtils.getCapacity(stack);

            if (maxLevel == 0) {
                ItemStack filledStack = RecepticalHandler.get(stack).toFilled(stack, ItemFluids.of(FluidVariant.of(Fluids.WATER), 1));
                if (filledStack != stack && filledStack.getItem() != stack.getItem()) {
                    capacity = filledStack.get(PSComponents.FLUID_CAPACITY);
                    if (capacity != null) {
                        return capacity.capacity();
                    }
                }
            }

            return maxLevel;
        }
        return capacity == null ? 0 : capacity.capacity();
    }

    public static void appendTooltip(ItemStack stack, TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        tooltip.accept(Component.translatable("psychedelicraft.container.levels",
                FluidVolumes.format(ItemFluids.of(stack).amount()),
                FluidVolumes.format(FluidCapacity.get(stack))
        ).withStyle(ChatFormatting.DARK_PURPLE));
    }

    public static float getPercentage(ItemStack stack) {
        int capacity = get(stack);
        return capacity == 0 ? 0 : ItemFluids.of(stack).amount() / (float)capacity;
    }

    public record Predicate(Ints capacity) implements SingleComponentItemPredicate<FluidCapacity> {
        public static final Codec<Predicate> CODEC = Ints.CODEC.xmap(Predicate::new, Predicate::capacity);

        @Override
        public DataComponentType<FluidCapacity> componentType() {
            return PSComponents.FLUID_CAPACITY;
        }

        @Override
        public boolean matches(ItemStack stack, FluidCapacity capacity) {
            return this.capacity.matches(capacity.capacity());
        }
    }

}
