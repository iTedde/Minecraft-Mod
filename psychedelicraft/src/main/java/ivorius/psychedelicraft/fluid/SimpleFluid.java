/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.fluid;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import io.netty.buffer.ByteBuf;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import ivorius.psychedelicraft.fluid.Processable.ProcessType;
import ivorius.psychedelicraft.fluid.physical.FluidStateManager;
import ivorius.psychedelicraft.fluid.physical.PhysicalFluid;
import ivorius.psychedelicraft.fluid.physical.PlacedFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.ChatFormatting;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Created by lukas on 29.10.14.
 * Updated by Sollace
 */
public class SimpleFluid implements Combustable {
    public static final ResourceLocation EMPTY_KEY = Psychedelicraft.id("empty");
    public static final ResourceKey<Registry<SimpleFluid>> REGISTRY_KEY = ivorius.psychedelicraft.PSRegistries.FLUIDS_KEY;
    public static final Registry<SimpleFluid> REGISTRY = ivorius.psychedelicraft.PSRegistries.FLUIDS;
    protected static final Map<ResourceLocation, SimpleFluid> ALIASED_IDS = new HashMap<>();

    public static final Codec<SimpleFluid> CODEC = ResourceLocation.CODEC.xmap(SimpleFluid::byId, SimpleFluid::getId);
    public static final StreamCodec<ByteBuf, SimpleFluid> PACKET_CODEC = ResourceLocation.STREAM_CODEC.map(SimpleFluid::byId, SimpleFluid::getId);

    public static SimpleFluid byId(@Nullable ResourceLocation id) {
        if (id == null) {
            return PSFluids.EMPTY;
        }
        SimpleFluid fluid = ALIASED_IDS.get(id);
        if (fluid != null) {
            return fluid;
        }
        return REGISTRY.getOptional(id).orElseGet(() -> BuiltInRegistries.FLUID.getOptional(id).map(SimpleFluid::of).orElse(PSFluids.EMPTY));
    }

    public static SimpleFluid of(@Nullable Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return PSFluids.EMPTY;
        }
        if (fluid instanceof PlacedFluid pf) {
            return pf.getType();
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(VanillaFluid.toStill(fluid));
        SimpleFluid alias = ALIASED_IDS.get(id);
        if (alias != null) {
            return alias;
        }
        return REGISTRY.get(id);
    }

    protected final ResourceLocation id;

    private final ResourceLocation symbol;

    private final boolean custom;
    private final boolean empty;

    private final Settings settings;

    private final PhysicalFluid physical;

    private final Supplier<ItemFluids> defaultStack = Suppliers.memoize(() -> ItemFluids.create(this, 1, Map.of()));

    private final Holder.Reference<SimpleFluid> registryEntry = REGISTRY.createIntrusiveHolder(this);

    protected SimpleFluid(ResourceLocation id, Settings settings) {
        Registry.register(REGISTRY, id, this);
        this.id = id;
        this.settings = settings;
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = true;
        this.empty = false;
        physical = new PhysicalFluid(id, this);
    }

    protected SimpleFluid(ResourceLocation id, int color, PhysicalFluid physical, boolean empty) {
        this.id = id;
        this.empty = empty;
        this.settings = new Settings().color(color);
        this.symbol = id.withPath(p -> "textures/fluid/" + p + ".png");
        this.custom = false;
        this.physical = physical;
    }

    @Deprecated
    public Holder.Reference<SimpleFluid> getRegistryEntry() {
        return registryEntry;
    }

    @SuppressWarnings("unchecked")
    protected <S extends Settings> S getSettings() {
        return (S)settings;
    }

    public final FluidStateManager getStateManager() {
        return settings.stateManager;
    }

    public final boolean isEmpty() {
        return empty;
    }

    public final ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getSymbol(ItemFluids stack) {
        return symbol;
    }

    public Optional<ResourceLocation> getFlowTexture(ItemFluids stack) {
        return settings.flowTexture;
    }

    public Optional<ResourceLocation> getStandingTexture(ItemFluids stack) {
        return settings.stillTexture;
    }

    public final ItemFluids getStack(StateHolder<?, ?> state, int amount) {
        Map<String, Integer> attributes = new HashMap<>();
        getStateManager().writeAttributes(state, attributes);
        return ItemFluids.create(this, amount, attributes);
    }

    public final FluidState getFluidState(ItemFluids stack) {
        return getStateManager().readAttributes(getPhysical().getDefaultState(), stack);
    }

    public PhysicalFluid getPhysical() {
        return physical;
    }

    public boolean isCustomFluid() {
        return custom;
    }

    public int getColor(ItemFluids stack) {
        return settings.color;
    }

    public int getViscocity() {
        return settings.viscocity;
    }

    public int getCondensationTemperature() {
        return settings.condensationPoint;
    }

    protected String getTranslationKey() {
        return Util.makeDescriptionId(isCustomFluid() ? "fluid" : "block", id);
    }

    public final ItemFluids getDefaultStack() {
        return defaultStack.get();
    }

    public final ItemFluids getDefaultStack(int amount) {
        return getDefaultStack().ofAmount(amount);
    }

    public Stream<ItemFluids> getDefaultStacks(int capacity) {
        return Stream.of(getDefaultStack(capacity));
    }

    public String getUniqueKey(ItemFluids fluids) {
        return "";
    }

    public final Stream<ItemStack> getDefaultStacks(ItemStack stack) {
        int capacity = FluidCapacity.get(stack);
        if (capacity > 0 && isSuitableContainer(stack)) {
            return getDefaultStacks(capacity).map(s -> ItemFluids.set(stack.copy(), s));
        }
        return Stream.of();
    }

    public Component getName(ItemFluids stack) {
        return Component.translatable(getTranslationKey());
    }

    public void appendTooltip(ItemFluids stack, Consumer<Component> tooltip, TooltipFlag type) {

    }

    public void appendTankTooltip(ItemFluids stack, @Nullable Level world, List<Component> tooltip, FluidProcessingBlockEntity tank) {
        int ticksProcessed = tank.getTimeProcessed();
        int ticksNeeded = Math.abs(tank.getTimeNeeded());
        String timeRemaining = StringUtil.formatTickDuration(ticksNeeded - ticksProcessed, world == null ? 20 : world.tickRateManager().tickrate());
        ProcessType processType = tank.getActiveProcess();
        tooltip.add(Component.translatable("fluid.status", processType.getStatus()).withStyle(ChatFormatting.GRAY));

        if (processType != ProcessType.IDLE) {
            tooltip.add(Component.translatable(processType.getTimeLabelTranslationKey(), timeRemaining).withStyle(ChatFormatting.GRAY));
        }
    }

    public boolean isSuitableContainer(ItemStack container) {
        return container.is(Tags.Items.BUCKETS);
    }

    public TagKey<Item> getPreferredContainerTag() {
        return PSTags.Items.DRINK_RECEPTICALS;
    }

    public boolean hasRandomTicks() {
        return getFireStrength(getDefaultStack()) > 0;
    }

    public void randomDisplayTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        if (!custom) {
            state.animateTick(world, pos, random);
        }
    }

    public void onRandomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        if (getFireStrength(getDefaultStack()) > 0) {
            for (Direction direction : Direction.values()) {
                BlockPos side = pos.relative(direction);
                BlockState above = world.getBlockState(side);
                if (above.isAir() && !above.is(PSBlocks.FLAMMABLE_GAS)) {
                    world.setBlockAndUpdate(side, PSBlocks.FLAMMABLE_GAS.defaultBlockState());
                }
            }
        }
    }

    public int getHash(ItemFluids stack) {
        return hashCode();
    }

    @Override
    public String toString() {
        return REGISTRY.wrapAsHolder(this).getRegisteredName();
    }

    @Override
    public float getFireStrength(ItemFluids stack) {
        return settings.flammability * stack.amount();
    }

    @Override
    public float getExplosionStrength(ItemFluids stack) {
        return settings.explosiveness * stack.amount();
    }

    @SuppressWarnings("unchecked")
    public static class Settings {
        private int color;
        private int viscocity = 1;
        private float explosiveness;
        private float flammability;
        private int condensationPoint = 15;
        final FluidStateManager stateManager = new FluidStateManager(new HashSet<>());

        private Optional<ResourceLocation> flowTexture = Optional.empty();
        private Optional<ResourceLocation> stillTexture = Optional.empty();

        public <T extends Settings> T condensationPoint(int temperature) {
            Preconditions.checkArgument(temperature >= 0 && temperature <= 15);
            condensationPoint = temperature;
            return (T)this;
        }

        public <T extends Settings> T color(int color) {
            this.color = color;
            return (T)this;
        }

        public <T extends Settings> T viscocity(int viscocity) {
            this.viscocity = viscocity;
            return (T)this;
        }

        public <T extends Settings> T flammability(float flammability, float explosiveness) {
            this.explosiveness = explosiveness;
            this.flammability = flammability;
            return (T)this;
        }

        public <T extends Settings> T with(FluidStateManager.FluidProperty<?> property) {
            stateManager.properties().add(property);
            return (T)this;
        }

        public <T extends Settings> T sprites(ResourceLocation flowing, ResourceLocation still) {
            flowTexture = Optional.ofNullable(flowing);
            stillTexture = Optional.ofNullable(still);
            return (T)this;
        }
    }

    public abstract static class Attribute<T extends Comparable<T>> {

        public abstract ItemFluids set(ItemFluids fluids, T value);

        public abstract T get(ItemFluids fluids);

        public final T get(ItemStack stack) {
            return get(ItemFluids.of(stack));
        }

        public abstract void set(Map<String, Integer> attributes, T value);

        public abstract ItemFluids cycle(ItemFluids fluids);

        public final ItemStack set(ItemStack stack, T value) {
            return ItemFluids.set(stack, set(ItemFluids.of(stack), value));
        }

        public abstract Stream<Tuple<T, T>> steps();

        public abstract String name();

        public static Attribute<Integer> ofInt(String name, int min, int max) {
            return new Attribute<>() {
                @Override
                public ItemFluids set(ItemFluids fluids, Integer value) {
                    return fluids.withAttribute(name, Mth.clamp(value, min, max));
                }

                @Override
                public void set(Map<String, Integer> attributes, Integer value) {
                    attributes.put(name, value);
                }

                @Override
                public Integer get(ItemFluids fluids) {
                    return Mth.clamp(fluids.attributes().getOrDefault(name, 0), min, max);
                }

                @Override
                public Stream<Tuple<Integer, Integer>> steps() {
                    return IntStream.range(min, max).mapToObj(i -> new Tuple<>(i, i + 1));
                }

                @Override
                public ItemFluids cycle(ItemFluids fluids) {
                    int value = get(fluids);
                    return value < max ? set(fluids, value + 1) : fluids;
                }

                @Override
                public String name() {
                    return name;
                }
            };
        }

        public static Attribute<Boolean> ofBoolean(String name) {
            return new Attribute<>() {
                @Override
                public ItemFluids set(ItemFluids fluids, Boolean value) {
                    return fluids.withAttribute(name, value ? 1 : 0);
                }

                @Override
                public void set(Map<String, Integer> attributes, Boolean value) {
                    attributes.put(name, value ? 1 : 0);
                }

                @Override
                public Boolean get(ItemFluids fluids) {
                    return fluids.attributes().getOrDefault(name, 0) != 0;
                }

                @Override
                public Stream<Tuple<Boolean, Boolean>> steps() {
                    return Stream.of(new Tuple<>(false, true));
                }

                @Override
                public ItemFluids cycle(ItemFluids fluids) {
                    return !get(fluids) ? set(fluids, true) : fluids;
                }

                @Override
                public String name() {
                    return name;
                }
            };
        }
    }
}
