/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.BurnerBlockEntity;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.contents.EmptyContents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BurnerBlock extends BaseEntityBlock {
    public static final MapCodec<BurnerBlock> CODEC = simpleCodec(BurnerBlock::new);
    public static final VoxelShape SHAPE = ShapeUtil.createCenteredShape(5, 2, 5);
    private static final Map<ResourceLocation, VoxelShape> SHAPE_CACHE = new HashMap<>(Map.of(EmptyContents.ID, SHAPE));

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BurnerBlock(Properties settings) {
        super(settings
                .noOcclusion()
                .dynamicShape()
                .emissiveRendering((state, world, pos) -> state.getOptionalValue(LIT).orElse(false))
        );
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BurnerBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BurnerBlockEntity be = world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).orElse(null);
        return be == null || be.getContents().getId() == EmptyContents.ID
                ? SHAPE
                : SHAPE_CACHE.computeIfAbsent(be.getContents().getId(), id -> Shapes.or(SHAPE, be.getContents().getOutlineShape()));
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            world.addParticle(ParticleTypes.SMOKE,
                    world.random.triangle(pos.getX() + 0.5, 0.2),
                    world.random.triangle(pos.getY() + 0.2, 0.2),
                    world.random.triangle(pos.getZ() + 0.5, 0.2),
                    0, 0, 0);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ItemTags.CREEPER_IGNITERS) && state.getValue(LIT)) {
            SoundEvent sound = stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
            world.playSound(player, pos, sound, SoundSource.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            if (!world.isClientSide) {
                if (!stack.isDamageableItem()) {
                    stack.consume(1, player);
                } else {
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                world.setBlockAndUpdate(pos, state.cycle(LIT));
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (world.getBlockEntity(pos, PSBlockEntities.BUNSEN_BURNER).filter(data -> data.interact(stack, player, hand, hit.getDirection())).isPresent()) {
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = isReceivingPower(world, pos);
        if (powered != state.getValue(LIT)) {
            world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, world.random.nextFloat() * 0.4F + 0.8F);
            world.setBlockAndUpdate(pos, state.cycle(LIT));
        }
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        Containers.dropContentsOnDestroy(state, newState, world, pos);
        super.onRemove(state, world, pos, newState, moved);
    }

    private boolean isReceivingPower(Level world, BlockPos pos) {
        return world.hasNeighborSignal(pos)
            || DistilleryBlockEntity.getBlockHeat(world.getBlockState(pos.below())) > 0;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && !entity.isShiftKeyDown() && entity.tickCount % 10 == 0 && entity.isSupportedBy(pos)) {
            entity.hurt(entity.damageSources().inFire(), 1);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(LIT, isReceivingPower(ctx.getLevel(), ctx.getClickedPos()));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BurnerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(Level world, BlockState state, BlockEntityType<Q> type) {
        return world.isClientSide
                ? createTickerHelper(type, PSBlockEntities.BUNSEN_BURNER, (w, p, s, entity) -> entity.clientTick(w))
                : createTickerHelper(type, PSBlockEntities.BUNSEN_BURNER, (w, p, s, entity) -> entity.tick((ServerLevel)w));
    }
}
