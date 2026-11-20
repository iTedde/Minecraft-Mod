package ivorius.psychedelicraft.fluid.physical;

import ivorius.psychedelicraft.fluid.container.FluidVariant;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.fluid.SimpleFluid;

public final class PhysicalFluid {
    private final Supplier<Fluid> standing;
    private final Supplier<Fluid> flowing;
    private final Supplier<Block> block;

    @Nullable
    private final SimpleFluid type;
    @Nullable
    private final FluidType fluidType;

    public PhysicalFluid(Supplier<Fluid> standing, Supplier<Fluid> flowing, Supplier<Block> block) {
        this.standing = standing;
        this.flowing = flowing;
        this.block = block;
        this.type = null;
        this.fluidType = null;
    }

    public PhysicalFluid(ResourceLocation id, SimpleFluid type) {
        @SuppressWarnings("unused") Object o = Fluids.EMPTY;
        this.type = type;
        // NeoForge: every fluid needs a FluidType. Water-like properties, but own name/colour/textures.
        fluidType = Registry.register(NeoForgeRegistries.FLUID_TYPES, id, new FluidType(FluidType.Properties.create()
                .descriptionId(Util.makeDescriptionId("fluid", id))
                .canSwim(true)
                .canDrown(true)
                .canExtinguish(true)
                .supportsBoating(true)
                .canConvertToSource(false)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {
            @Override
            public Component getDescription(FluidStack stack) {
                return type.getName(ItemFluids.of(FluidVariant.of(stack), 1));
            }
        });
        standing = Suppliers.ofInstance(Registry.register(BuiltInRegistries.FLUID, id, PlacedFluid.still(this)));
        flowing = Suppliers.ofInstance(Registry.register(BuiltInRegistries.FLUID, id.withPath(p -> "flowing_" + p), PlacedFluid.flowing(this)));
        block = Suppliers.ofInstance(type.isEmpty() ? Blocks.AIR : Registry.register(BuiltInRegistries.BLOCK, id, PlacedFluidBlock.create(this)));
    }

    @Nullable
    public FluidType getFluidType() {
        return fluidType;
    }

    public Fluid getStandingFluid() {
        return standing.get();
    }

    public Fluid getFlowingFluid() {
        return flowing.get();
    }

    public Block getBlock() {
        return block.get();
    }

    @Nullable
    public Block getCauldron() {
        if (isOf(Fluids.WATER)) {
            return Blocks.WATER_CAULDRON;
        }
        if (isOf(Fluids.LAVA) && Blocks.LAVA_CAULDRON.defaultBlockState().hasProperty(LayeredCauldronBlock.LEVEL)) {
            return Blocks.LAVA_CAULDRON;
        }
        return PSBlocks.CAULDRON;
    }

    @Nullable
    public SimpleFluid getType() {
        return type;
    }

    public FluidState getDefaultState() {
        return getStandingFluid().defaultFluidState();
    }

    @SuppressWarnings("deprecation")
    public boolean isIn(TagKey<Fluid> tag) {
        return getStandingFluid().is(tag);
    }

    public boolean isOf(Fluid fluid) {
        return getStandingFluid().isSame(fluid);
    }
}
