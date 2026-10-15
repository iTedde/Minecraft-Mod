package dev.craft.buildcraft.fluid;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.BcItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * BuildCraft liquids: oil ({@code oilStill}/{@code oilMoving}, Material.water) and fuel, which has no block and only
 * exists in containers and tanks.
 */
public final class BcFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Craft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Craft.MODID);

    /** Material.water: entities swim, drown, are extinguished and take no fall damage like in water. */
    public static final DeferredHolder<FluidType, FluidType> OIL_TYPE = FLUID_TYPES.register("oil",
            () -> new FluidType(FluidType.Properties.create()
                    .canSwim(true).canDrown(true).canExtinguish(true).canHydrate(true).supportsBoating(true)
                    .fallDistanceModifier(0.0F).motionScale(0.014D).canPushEntity(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));
    public static final DeferredHolder<FluidType, FluidType> FUEL_TYPE = FLUID_TYPES.register("fuel",
            () -> new FluidType(FluidType.Properties.create()
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> OIL = FLUIDS.register("oil",
            () -> new BaseFlowingFluid.Source(BcFluids.oilProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_OIL = FLUIDS.register("flowing_oil",
            () -> new BaseFlowingFluid.Flowing(BcFluids.oilProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> FUEL = FLUIDS.register("fuel",
            () -> new BaseFlowingFluid.Source(BcFluids.fuelProperties()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_FUEL = FLUIDS.register(
            "flowing_fuel", () -> new BaseFlowingFluid.Flowing(BcFluids.fuelProperties()));

    private static BaseFlowingFluid.Properties oilProperties;
    private static BaseFlowingFluid.Properties fuelProperties;

    private BcFluids() {
    }

    /** Flows like 1.4.7 water: 5 ticks, 7 blocks, slope search 4, no new sources. */
    private static BaseFlowingFluid.Properties oilProperties() {
        if (oilProperties == null) {
            oilProperties = new BaseFlowingFluid.Properties(OIL_TYPE, OIL, FLOWING_OIL).bucket(BcItems.OIL_BUCKET)
                    .block(BcBlocks.OIL).tickRate(5).slopeFindDistance(4).levelDecreasePerBlock(1)
                    .explosionResistance(100.0F);
        }
        return oilProperties;
    }

    private static BaseFlowingFluid.Properties fuelProperties() {
        if (fuelProperties == null) {
            fuelProperties = new BaseFlowingFluid.Properties(FUEL_TYPE, FUEL, FLOWING_FUEL)
                    .bucket(BcItems.FUEL_BUCKET);
        }
        return fuelProperties;
    }

    /**
     * Lava next to oil hardens like next to water (1.4.7 {@code BlockFluid.checkForHarden} checks Material.water):
     * source → obsidian, levels 1–4 → cobblestone, weaker or falling lava stays.
     */
    public static void registerInteractions() {
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(),
                new FluidInteractionRegistry.InteractionInformation(
                        (level, pos, relativePos, state) -> level.getFluidState(relativePos).getFluidType()
                                == OIL_TYPE.get() && hardens(state),
                        (level, pos, relativePos, state) -> harden(level, pos, state)));
    }

    private static boolean hardens(FluidState lava) {
        return lava.isSource() || !lava.getValue(FlowingFluid.FALLING) && lava.getAmount() >= 4;
    }

    private static void harden(LevelAccessor level, BlockPos pos, FluidState lava) {
        Block block = lava.isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
        BlockState state = EventHooks.fireFluidPlaceBlockEvent(level, pos, pos, block.defaultBlockState());
        level.setBlock(pos, state, Block.UPDATE_ALL);
        level.levelEvent(1501, pos, 0);
    }
}
