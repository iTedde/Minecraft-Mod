package dev.craft.thermalexpansion.transport;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * {@code BlockTeleport}: Material.iron, hardness 15, {@code setResistance(200)} = 120, drawn by its block entity
 * renderer, light 7 while tuned ({@link #ACTIVE}). The placer owns it. Sneaking with a wrench dismantles it (owner or
 * operator only); a multimeter reads frequency, owner, mode and partners; otherwise right click opens the GUI.
 */
public class TesseractBlock extends BaseEntityBlock {
    /** {@code isActive}: a frequency is set. */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final TesseractType type;
    private final BiFunction<BlockPos, BlockState, ? extends TesseractBlockEntity> factory;

    public TesseractBlock(Properties properties, TesseractType type,
                          BiFunction<BlockPos, BlockState, ? extends TesseractBlockEntity> factory) {
        super(properties.lightLevel(state -> state.getValue(ACTIVE) ? 7 : 0));
        this.type = type;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    public TesseractType getType() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof TesseractBlockEntity tesseract) {
                tesseract.serverTick();
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof TesseractBlockEntity tesseract) {
            tesseract.setOwner(player);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!CrescentHammerItem.isWrench(stack)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TesseractBlockEntity tesseract
                    && (tesseract.isOwner(player) || player.hasPermissions(2))) {
                dismantle(level, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (stack.is(TeItems.MULTIMETER.get())) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TesseractBlockEntity tesseract) {
                describe(tesseract, player);
            }
            // The original returns false here, so the multimeter's own use follows.
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TesseractBlockEntity tesseract
                && player instanceof ServerPlayer serverPlayer) {
            if (tesseract.canAccess(player)) {
                TesseractPayloads.sendNames(serverPlayer, tesseract);
                serverPlayer.openMenu(tesseract, pos);
            } else {
                player.sendSystemMessage(Component.translatable("message.craft.tesseract.private",
                        tesseract.getOwnerName()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** The multimeter lines of {@code BlockTeleport.onBlockActivated}. */
    private static void describe(TesseractBlockEntity tesseract, Player player) {
        String owner = tesseract.getOwnerName();
        if (tesseract.getFrequency() == -1) {
            player.sendSystemMessage(Component.translatable("message.craft.tesseract.disabled", owner));
        } else if (tesseract.isPrivate()) {
            player.sendSystemMessage(Component.translatable("message.craft.tesseract.frequency_private",
                    tesseract.getFrequency(), owner));
        } else {
            player.sendSystemMessage(Component.translatable("message.craft.tesseract.frequency",
                    tesseract.getFrequency(), owner));
        }
        switch (tesseract.getMode()) {
            case TesseractBlockEntity.MODE_SEND -> player.sendSystemMessage(Component.translatable(
                    "message.craft.tesseract.mode_send", tesseract.getValidOutputs().size()));
            case TesseractBlockEntity.MODE_RECEIVE -> player.sendSystemMessage(Component.translatable(
                    "message.craft.tesseract.mode_receive", tesseract.getValidInputs().size()));
            default -> player.sendSystemMessage(Component.translatable("message.craft.tesseract.mode_both",
                    tesseract.getValidInputs().size(), tesseract.getValidOutputs().size()));
        }
    }

    /** {@code BlockTERoot.dismantleBlock}: the plain block pops out. */
    private static void dismantle(Level level, BlockPos pos) {
        ItemStack drop = new ItemStack(level.getBlockState(pos).getBlock());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        float f = 0.3F;
        ItemEntity entity = new ItemEntity(level, pos.getX() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getY() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getZ() + level.random.nextFloat() * f + (1.0F - f) * 0.5, drop);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
