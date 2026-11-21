package ivorius.psychedelicraft.fluid.container;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;

public interface FluidCauldronBehavior {
    CauldronInteraction AIR = (state, world, pos, player, hand, stack) -> {
        ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());
        ItemFluids fluid = t.fluids();
        @Nullable
        Block cauldron = fluid.fluid().getPhysical().getCauldron();

        if (cauldron != null && t.fluids().amount() >= FluidVolumes.GLASS_BOTTLE) {
            int levels = Math.min(t.fluids().amount() / FluidVolumes.GLASS_BOTTLE, LayeredCauldronBlock.MAX_FILL_LEVEL);
            Item item = stack.getItem();
            t.withdraw(levels * FluidVolumes.GLASS_BOTTLE);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            setCauldronState(world, pos, fluid, levels);
            world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
            world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    };
    CauldronInteraction WATER = createCauldronInteraction((w, pos) -> SimpleFluid.of(Fluids.WATER).getDefaultStack());
    CauldronInteraction LAVA = createCauldronInteraction((w, pos) -> SimpleFluid.of(Fluids.LAVA).getDefaultStack());
    CauldronInteraction OTHER_FLUID = createCauldronInteraction((w, pos) -> w.getBlockEntity(pos, PSBlockEntities.CAULDRON).map(FluidCauldronBlock.Data::getFluid).orElse(ItemFluids.EMPTY));

    static void register(Item item) {
        CauldronInteraction.EMPTY.map().put(item, FluidCauldronBehavior.AIR);
        CauldronInteraction.LAVA.map().put(item, FluidCauldronBehavior.LAVA);
        CauldronInteraction.WATER.map().put(item, FluidCauldronBehavior.WATER);
        FluidCauldronBlock.BEHAVIOUR.map().put(item, FluidCauldronBehavior.OTHER_FLUID);
    }

    static CauldronInteraction createCauldronInteraction(BiFunction<Level, BlockPos, ItemFluids> fluidTypeSupplier) {
        return (state, world, pos, player, hand, stack) -> {
            Item item = stack.getItem();
            ItemFluids fluidType = fluidTypeSupplier.apply(world, pos);
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack.copy());

            int maxTransferred = FluidCapacity.get(stack);

            if (t.fluids().isEmpty() && maxTransferred >= FluidVolumes.GLASS_BOTTLE) {
                int levelChange = maxTransferred / FluidVolumes.GLASS_BOTTLE;
                ItemFluids fluid = fluidType.ofAmount(levelChange * FluidVolumes.GLASS_BOTTLE);

                if (!t.canAccept(fluid)) {
                    return ItemInteractionResult.FAIL;
                }
                if (!world.isClientSide) {
                    t.deposit(fluid);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(item));
                    decrementFluidLevel(state, world, pos, fluidType);
                    setCauldronState(world, pos, fluidType, getFluidLevel(state) - levelChange);
                    world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                    world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
                    world.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
                return ItemInteractionResult.SUCCESS;
            }

            if (!t.fluids().canCombine(fluidType) || t.fluids().amount() < FluidVolumes.GLASS_BOTTLE || !incrementFluidLevel(state, world, pos, fluidType)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            t.withdraw(FluidVolumes.GLASS_BOTTLE);
            world.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, t.toItemStack()));
            return ItemInteractionResult.SUCCESS;
        };
    }

    private static int getFluidLevel(BlockState state) {
        return state.getOptionalValue(LayeredCauldronBlock.LEVEL).orElse(state.is(Blocks.CAULDRON) ? 0 : LayeredCauldronBlock.MAX_FILL_LEVEL);
    }

    private static boolean incrementFluidLevel(BlockState state, Level world, BlockPos pos, ItemFluids fluidType) {
        int level = getFluidLevel(state);
        if (level < LayeredCauldronBlock.MAX_FILL_LEVEL) {
            setCauldronState(world, pos, fluidType, level + 1);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
            return true;
        }
        return false;
    }

    private static void decrementFluidLevel(BlockState state, Level world, BlockPos pos, ItemFluids fluidType) {
        setCauldronState(world, pos, fluidType, getFluidLevel(state) - 1);
        world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    private static void setCauldronState(Level world, BlockPos pos, ItemFluids fluidType, int level) {
        world.setBlockAndUpdate(pos, getCauldronState(fluidType, level));
        world.getBlockEntity(pos, PSBlockEntities.CAULDRON).ifPresent(data -> data.setFluid(fluidType));
    }

    private static BlockState getCauldronState(ItemFluids fluidType, int level) {
        if (level <= 0) {
            return Blocks.CAULDRON.defaultBlockState();
        }
        if (level >= LayeredCauldronBlock.MAX_FILL_LEVEL && fluidType.isOf(Fluids.LAVA)) {
            return Blocks.LAVA_CAULDRON.defaultBlockState();
        }

        return fluidType.fluid().getPhysical().getCauldron().defaultBlockState().trySetValue(LayeredCauldronBlock.LEVEL, level);
    }
}
