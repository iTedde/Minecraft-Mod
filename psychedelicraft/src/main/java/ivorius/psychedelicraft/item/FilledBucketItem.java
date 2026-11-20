/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.item;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Created by Sollace on Feb 6 2023
 */
public class FilledBucketItem extends Item {

    public FilledBucketItem(Properties settings) {
        super(settings.craftRemainder(Items.BUCKET));
        registerDispenserBehaviour(this);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemFluids fluids = ItemFluids.of(stack);

        if (!fluids.isEmpty()) {
            return Component.translatable("%s %s", fluids.fluid().getName(fluids), Items.BUCKET.getName(stack));
        }

        return Items.BUCKET.getName(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !ItemFluids.of(stack).isEmpty() && FluidCapacity.getPercentage(stack) < 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int)(MAX_BAR_WIDTH * FluidCapacity.getPercentage(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xAAAAFF;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        ItemFluids fluids = ItemFluids.of(stack);

        FluidState fluid = fluids.fluid().getFluidState(fluids);

        BlockHitResult hit = BucketItem.getPlayerPOVHitResult(world, user, fluid.isEmpty()
                ? ClipContext.Fluid.SOURCE_ONLY
                : ClipContext.Fluid.NONE);

        if (hit.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(stack);
        }

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos3;
            BlockPos blockPos = hit.getBlockPos();
            Direction direction = hit.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!world.mayInteract(user, blockPos) || !user.mayUseItemAt(blockPos2, direction, stack)) {
                return InteractionResultHolder.fail(stack);
            }

            if (fluid.isEmpty()) {
                ItemStack itemStack2;
                BlockState state = world.getBlockState(blockPos);
                if (state.getBlock() instanceof BucketPickup drainable && !(itemStack2 = drainable.pickupBlock(user, world, blockPos, state)).isEmpty()) {
                    user.awardStat(Stats.ITEM_USED.get(this));
                    drainable.getPickupSound().ifPresent(sound -> user.playSound(sound, 1.0f, 1.0f));
                    world.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                    ItemStack itemStack3 = ItemUtils.createFilledResult(stack, user, itemStack2);
                    if (!world.isClientSide) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)user, itemStack2);
                    }
                    return InteractionResultHolder.sidedSuccess(itemStack3, world.isClientSide());
                }
                return InteractionResultHolder.fail(stack);
            }
            BlockState blockState = world.getBlockState(blockPos);
            blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && fluid.is(Fluids.WATER) ? blockPos : blockPos2;
            if (placeFluid(stack, fluids, fluid, user, world, blockPos3, hit)) {
                if (user instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)user, blockPos3, stack);
                }
                user.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(BucketItem.getEmptySuccessItem(stack, user), world.isClientSide());
            }

            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void registerDispenserBehaviour(Item item) {
        DispenserBlock.registerBehavior(item, new DefaultDispenseItemBehavior(){
            @Override
            public ItemStack execute(BlockSource pointer, ItemStack stack) {
                BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                ServerLevel world = pointer.level();
                ItemFluids fluids = ItemFluids.of(stack);
                if (placeFluid(stack, fluids, fluids.fluid().getFluidState(fluids), null, world, blockPos, null)) {
                    return Items.BUCKET.getDefaultInstance();
                }
                return super.execute(pointer, stack);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static boolean placeFluid(ItemStack stack, ItemFluids fluids, FluidState fluidState, @Nullable Player player, Level world, BlockPos pos, @Nullable BlockHitResult hit) {
        if (!(fluidState.getType() instanceof FlowingFluid)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        boolean canPlace = state.canBeReplaced(fluidState.getType());

        if (!(state.isAir() || canPlace || block instanceof LiquidBlockContainer f && f.canPlaceLiquid(player, world, pos, state, fluidState.getType()))) {
            return hit != null && placeFluid(stack, fluids, fluidState, player, world, hit.getBlockPos().relative(hit.getDirection()), null);
        }

        if (world.dimensionType().ultraWarm() && !fluidState.is(FluidTags.LAVA)) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            for (int l = 0; l < 8; ++l) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0, 0.0, 0.0);
            }
            return true;
        }

        if (block instanceof LiquidBlockContainer f && fluids.amount() >= FluidCapacity.get(stack) && f.placeLiquid(world, pos, state, fluidState)) {
            playEmptyingSound(fluidState.getType(), player, world, pos);
            return true;
        }

        if (!world.isClientSide && canPlace && !state.liquid()) {
            world.destroyBlock(pos, true);
        }


        BlockState placedBlockState = fluidState.createLegacyBlock();
        boolean placeFalling = false;
        boolean placeInto = world.getBlockState(pos).is(placedBlockState.getBlock());
        if (fluids.amount() < FluidCapacity.get(stack) && world.isEmptyBlock(pos)) {
            placedBlockState = placedBlockState.setValue(BlockStateProperties.LEVEL, 8);
            placeFalling = true;
        }

        if (placeFalling || placeInto) {
            if (placeFalling && !placeInto) {
                for (var dir : List.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP)) {
                    if (world.isEmptyBlock(pos.relative(dir))) {
                        world.setBlock(pos.relative(dir), placedBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                        break;
                    }
                }
            }
            if (world.setBlock(pos, placedBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE) || state.getFluidState().isSource()) {
               playEmptyingSound(fluidState.getType(), player, world, pos);
               return true;
            }
        } else if (world.isEmptyBlock(pos) || placeInto) {
            if (world.setBlock(pos, placedBlockState, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE) || state.getFluidState().isSource()) {
                playEmptyingSound(fluidState.getType(), player, world, pos);
                return true;
            }
        }
        return false;
    }

    private static void playEmptyingSound(Fluid fluid, @Nullable Player player, LevelAccessor world, BlockPos pos) {
        SoundEvent soundEvent = FluidVariant.of(fluid).getEmptySound();
        world.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1, 1);
        world.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
