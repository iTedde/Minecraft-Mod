package ivorius.psychedelicraft.item.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidTransferUtils;
import ivorius.psychedelicraft.fluid.container.RecepticalHandler;
import ivorius.psychedelicraft.fluid.container.VariantMarshal;
import ivorius.psychedelicraft.util.PacketCodecUtils;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record ItemFluids(SimpleFluid fluid, int amount, Map<String, Integer> attributes) implements TooltipProvider {
    public static final ItemFluids EMPTY = new ItemFluids(PSFluids.EMPTY, 0, Map.of());
    public static final Codec<Map<String, Integer>> ATTRIBUTES_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
    public static final Codec<ItemFluids> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SimpleFluid.CODEC.fieldOf("fluid").forGetter(ItemFluids::fluid),
            Codec.INT.optionalFieldOf("amount", 1).forGetter(ItemFluids::amount),
            ATTRIBUTES_CODEC.optionalFieldOf("attributes", Map.of()).forGetter(ItemFluids::attributes)
    ).apply(instance, ItemFluids::create));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemFluids> PACKET_CODEC = StreamCodec.composite(
            SimpleFluid.PACKET_CODEC, ItemFluids::fluid,
            ByteBufCodecs.INT, ItemFluids::amount,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), ItemFluids::attributes,
            ItemFluids::create
    );

    @NotNull
    public static ItemFluids direct(ItemStack stack) {
        return stack.getOrDefault(PSComponents.FLUIDS, EMPTY);
    }

    @NotNull
    public static ItemFluids of(ItemStack stack) {
        ItemFluids fluids = stack.get(PSComponents.FLUIDS);
        if (fluids == null) {
            var fabricContents = FluidTransferUtils.getContents(stack);
            if (fabricContents.isPresent()) {
                return of(fabricContents.get().getB(), fabricContents.get().getA().intValue());
            }
        }

        return fluids == null ? ItemFluidsMixture.of(stack).getFirstFluid() : fluids;
    }

    public static List<ItemFluids> allOf(ItemStack stack) {
        ItemFluidsMixture mixture = ItemFluidsMixture.of(stack);
        return mixture.isEmpty() ? List.of(of(stack)) : mixture.fluids();
    }

    public static ItemFluids of(FluidVariant variant, int capacity) {
        Optional<? extends ItemFluids> fluidsOptional = variant.getComponents().get(PSComponents.FLUIDS);
        ItemFluids fluids = fluidsOptional == null ? null : fluidsOptional.orElse(null);
        if (fluids == null) {
            return create(SimpleFluid.of(variant.getFluid()), capacity, Map.of());
        }
        return fluids.ofAmount(capacity);
    }

    public FluidVariant toVariant() {
        return FluidVariant.of(fluid().getPhysical().getStandingFluid(), DataComponentPatch.builder().set(PSComponents.FLUIDS, this).build());
    }

    public static ItemFluids create(SimpleFluid fluid, int amount, Map<String, Integer> attributes) {
        if (fluid.isEmpty() || amount <= 0) {
            return EMPTY;
        }
        return new ItemFluids(fluid, amount, Map.copyOf(attributes));
    }

    public static ItemStack set(ItemStack stack, ItemFluids fluids) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0) {
            if (capacity < fluids.amount()) {
                fluids = create(fluids.fluid(), capacity, fluids.attributes());
            }
            return getItemForFluids(stack, fluids);
        }
        return stack;
    }

    public static ItemStack getItemForFluids(ItemStack original, ItemFluids fluids) {
        return fluids.isEmpty()
                ? RecepticalHandler.get(original).toEmpty(original)
                : RecepticalHandler.get(original).toFilled(original, fluids);
    }

    public ItemFluids withAttribute(String attribute, int value) {
        Map<String, Integer> attributes = new HashMap<>(attributes());
        attributes.put(attribute, value);
        return create(fluid, amount, attributes);
    }

    public ItemFluids withAttributes(Map<String, Integer> attributes) {
        return create(fluid(), amount(), attributes);
    }

    public ItemFluids ofAmount(int amount) {
        if (amount == amount()) {
            return this;
        }
        if (amount == 0) {
            return EMPTY;
        }
        return create(fluid(), amount, attributes());
    }

    public ItemStack ofFilling(ItemStack container) {
        return set(container, ofAmount(FluidCapacity.get(container)));
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public boolean canCombine(ItemFluids fluids) {
        return isEmpty() || fluids.isEmpty() || isRoughlyEqual(fluids);
    }

    public int getHash() {
        return fluid.getHash(this);
    }

    public boolean isRoughlyEqual(ItemFluids fluids) {
        return fluid() == fluids.fluid() && getHash() == fluids.getHash();
    }

    public boolean isBaseForm() {
        return isEmpty() || isRoughlyEqual(fluid().getDefaultStack(amount()));
    }

    public boolean isOf(SimpleFluid fluid) {
        return fluid() == fluid;
    }

    public boolean isOf(Fluid fluid) {
        return isOf(SimpleFluid.of(fluid));
    }

    public boolean isIn(TagKey<Fluid> tag) {
        return fluid().getPhysical().isIn(tag);
    }

    public Component getName() {
        return fluid().getName(this);
    }

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltip, TooltipFlag type) {
        fluid().appendTooltip(this, tooltip, type);
        if (type.isAdvanced()) {
            tooltip.accept(Component.literal("Contents:").withStyle(ChatFormatting.DARK_GRAY));
            if (isEmpty()) {
                tooltip.accept(Component.literal(" <empty>").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltip.accept(Component.literal(" " + fluid().getId().toString() + " x" + amount()).withStyle(ChatFormatting.DARK_GRAY));
                for (var attribute : attributes().entrySet()) {
                    tooltip.accept(Component.literal("  " + attribute.getKey() + "=" + attribute.getValue()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    public Tag encode() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public static ItemFluids decode(Tag nbt) {
        return ItemFluids.CODEC.decode(NbtOps.INSTANCE, nbt).result().map(pair -> pair.getFirst()).orElse(ItemFluids.EMPTY);
    }

    public record Predicate(Optional<List<SimpleFluid>> fluid, Ints amount, Map<String, Ints> attributes) implements SingleComponentItemPredicate<ItemFluids> {
        public static final Codec<Predicate> CODEC = RecordCodecBuilder.create(i -> i.group(
                SimpleFluid.CODEC.listOf().optionalFieldOf("fluid").forGetter(Predicate::fluid),
                Ints.CODEC.optionalFieldOf("amount", Ints.ANY).forGetter(Predicate::amount),
                Codec.unboundedMap(Codec.STRING, Ints.CODEC).optionalFieldOf("attributes", Map.of()).forGetter(Predicate::attributes)
        ).apply(i, Predicate::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Predicate> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(SimpleFluid.PACKET_CODEC.apply(ByteBufCodecs.list())), Predicate::fluid,
                PacketCodecUtils.INT_RANGE, Predicate::amount,
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, PacketCodecUtils.INT_RANGE), Predicate::attributes,
                Predicate::new
        );

        @Override
        public DataComponentType<ItemFluids> componentType() {
            return PSComponents.FLUIDS;
        }

        @Override
        public boolean matches(ItemStack stack, ItemFluids fluids) {
            return test(fluids);
        }

        public boolean test(ItemFluids fluids) {
            return (fluid.isEmpty() || fluid.get().contains(fluids.fluid()))
                && amount.matches(fluids.amount())
                && (attributes.isEmpty() || attributes.entrySet().stream().allMatch(entry -> fluids.attributes().containsKey(entry.getKey()) && entry.getValue().matches(fluids.attributes().get(entry.getKey()))));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Optional<List<SimpleFluid>> fluid = Optional.empty();
            private Ints amount = Ints.atLeast(1);
            private final Map<String, Ints> attributes = new HashMap<>();

            public Builder fluid(SimpleFluid fluid) {
                this.fluid = Optional.of(List.of(fluid));
                return this;
            }

            public Builder fluid(SimpleFluid...fluid) {
                this.fluid = Optional.of(List.of(fluid));
                return this;
            }

            public Builder fluid(Collection<SimpleFluid> fluid) {
                this.fluid = Optional.of(List.copyOf(fluid));
                return this;
            }

            public Builder amount(Ints amount) {
                this.amount = amount;
                return this;
            }

            public Builder attribute(String name, Ints range) {
                attributes.put(name, range);
                return this;
            }

            public Predicate build() {
                return new Predicate(fluid, amount, attributes);
            }
        }
    }

    public interface Transaction {
        static Transaction begin(ItemStack initialStack) {
            if (initialStack.get(PSComponents.FLUID_CAPACITY) == null) {
                if (FluidTransferUtils.getCapacity(initialStack) == 0) {
                    ItemStack filledStack = RecepticalHandler.get(initialStack).toFilled(initialStack, ItemFluids.of(FluidVariant.of(Fluids.WATER), 1));
                    if (filledStack != initialStack && filledStack.getItem() != initialStack.getItem()) {
                        if (filledStack.get(PSComponents.FLUID_CAPACITY) != null) {
                            return new DirectTransaction(initialStack);
                        }
                    }
                }

                return new VariantMarshal.FabricTransaction(initialStack);
            }
            return new DirectTransaction(initialStack);
        }

        int capacity();

        ItemFluids withdraw(int amount);

        /**
         * Deposits up to {maxAmount} of the given fluid.
         *
         * @return The remaining fluid that could not be inserted.
         */
        default ItemFluids deposit(ItemFluids fluids) {
            return deposit(fluids, fluids.amount());
        }

        /**
         * Deposits up to {maxAmount} of the given fluid.
         *
         * @return The remaining fluid that could not be inserted.
         */
        ItemFluids deposit(ItemFluids fluids, int maxAmount);

        ItemFluids fluids();

        ItemStack toItemStack();

        default boolean canAccept(ItemFluids fluids) {
            return canAccept(fluids, fluids.amount());
        }

        default boolean canAccept(ItemFluids fluids, int amount) {
            return fluids().canCombine(fluids) && Math.min(fluids.amount(), (capacity() - fluids().amount())) >= amount;
        }
    }

    public static class DirectTransaction implements Transaction {
        private ItemStack stack;
        private final int capacity;
        private ItemFluids fluids;

        public DirectTransaction(ItemStack initialStack) {
            this(initialStack, FluidCapacity.get(initialStack), direct(initialStack));
        }

        public DirectTransaction(ItemStack initialStack, int capacity, ItemFluids fluids) {
            this.stack = initialStack.copy();
            this.capacity = capacity;
            this.fluids = fluids;
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public ItemFluids fluids() {
            return fluids;
        }

        @Override
        public ItemFluids withdraw(int amount) {
            if (capacity <= 0) {
                return EMPTY;
            }
            ItemFluids removed = fluids.ofAmount(Math.min(amount, fluids.amount()));
            this.fluids = fluids.ofAmount(fluids.amount() - removed.amount());
            return removed;
        }

        @Override
        public ItemFluids deposit(ItemFluids fluids, int maxAmount) {
            if (capacity <= 0 || !this.fluids.canCombine(fluids)) {
                return fluids;
            }

            int maxInserted = Math.min(Math.min(capacity - this.fluids.amount(), fluids.amount()), maxAmount);
            if (maxInserted > 0) {
                this.fluids = fluids.ofAmount(this.fluids.amount() + maxInserted);

                if (maxInserted >= fluids.amount()) {
                    return EMPTY;
                }

                return fluids.ofAmount(fluids.amount() - maxInserted);
            }

            return fluids;
        }

        @Override
        public ItemStack toItemStack() {
            return set(stack, fluids);
        }
    }
}
