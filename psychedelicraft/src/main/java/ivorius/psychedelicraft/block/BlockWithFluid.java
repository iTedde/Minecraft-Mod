/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.Processable;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

/**
 * @author Sollace
 * @since 3 Jan 2023
 */
public abstract class BlockWithFluid<T extends FlaskBlockEntity> extends BaseEntityBlock {
    public static final ResourceLocation CONTENTS_DYNAMIC_DROP_ID = ResourceLocation.withDefaultNamespace("contents");

    protected BlockWithFluid(Properties settings) {
        super(settings);
    }

    protected abstract BlockEntityType<T> getBlockEntityType();

    protected abstract MenuType<FluidContraptionScreenHandler<T>> getScreenHandlerType();

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!world.isClientSide && FluidCapacity.get(stack) > 0) {
            world.getBlockEntity(pos, getBlockEntityType()).ifPresent(be -> {
                be.getPrimaryTank().deposit(stack);
            });
            ((ServerLevel)world).getChunkSource().blockChanged(pos);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        appendDroppedStacks(asItem().getDefaultInstance(), state, builder);
        return super.getDrops(state, builder);
    }

    @Deprecated
    public static void appendDroppedStacks(ItemStack defaultStack, BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof DirectionalFluidResovoir container) {
            builder = builder.withDynamicDrop(CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                List<ItemStack> dynamicStacks = container.getDroppedStacks(defaultStack);
                if (dynamicStacks.isEmpty()) {
                    if (!defaultStack.isEmpty()) {
                        lootConsumer.accept(defaultStack);
                    }
                } else {
                    dynamicStacks.forEach(stack -> {
                        lootConsumer.accept(stack);
                    });
                }
            });
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
            InteractionResult result = onInteract(be.getBlockState(), world, be.getBlockPos(), player, be);
            if (result != InteractionResult.PASS) {
                return result;
            }
            InteractionData data = new InteractionData(be.getBlockPos(), hit.getDirection());
            player.openMenu(new SimpleMenuProvider(
                    (syncId, inv, p) -> new FluidContraptionScreenHandler<>(getScreenHandlerType(), syncId, inv, be, hit.getDirection()),
                    BlockWithFluid.this.getName()
            ), buf -> InteractionData.PACKET_CODEC.encode(buf, data));
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.FAIL);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return world.getBlockEntity(pos, getBlockEntityType()).map(be -> {
            return onInteractWithItem(stack, be.getBlockState(), world, be.getBlockPos(), player, hand, be);
        }).orElse(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
    }

    protected ItemInteractionResult onInteractWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, T blockEntity) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected InteractionResult onInteract(BlockState state, Level world, BlockPos pos, Player player, T blockEntity) {
        return InteractionResult.PASS;
    }

    @Override
    @Nullable
    public final <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(Level world, BlockState state, BlockEntityType<Q> type) {
        return world.isClientSide
                ? createTickerHelper(type, getBlockEntityType(), (w, p, s, entity) -> entity.clientTick(w))
                : createTickerHelper(type, getBlockEntityType(), (w, p, s, entity) -> entity.tick((ServerLevel)w));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        return toRedstoneSignal(world.getBlockEntity(pos, getBlockEntityType())
                .map(FlaskBlockEntity::getPrimaryTank)
                .map(tank -> Mth.clamp(tank.getAmount() / (float)tank.getCapacity(), 0, 1))
                .orElse(0F), 15);
    }

    static int toRedstoneSignal(float percentage, int maxSignal) {
        return percentage > 0 ? Mth.clamp((int)(percentage * maxSignal), 1, maxSignal) : 0;
    }

    public record InteractionData(BlockPos pos, Direction side) {
        public static final StreamCodec<FriendlyByteBuf, InteractionData> PACKET_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, InteractionData::pos,
                Direction.STREAM_CODEC, InteractionData::side,
                InteractionData::new
        );
    }

    public interface DirectionalFluidResovoir extends WorldlyContainer, Processable.Context {

        List<ItemStack> getDroppedStacks(ItemStack container);

        void tick(ServerLevel world);

        void clientTick(Level world);

        default IFluidHandler getFluidHandler(@Nullable Direction side) {
            return getTankOnSide(side);
        }
    }
}
