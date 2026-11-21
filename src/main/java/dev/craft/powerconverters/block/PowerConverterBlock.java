package dev.craft.powerconverters.block;

import com.mojang.serialization.MapCodec;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.power.BcConsumerBlockEntity;
import dev.craft.powerconverters.power.BcProducerBlockEntity;
import dev.craft.powerconverters.power.BridgeComponentBlockEntity;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity;
import dev.craft.powerconverters.power.FeProducerBlockEntity;
import dev.craft.powerconverters.power.Ic2ConsumerBlockEntity;
import dev.craft.powerconverters.power.Ic2ProducerBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Power Converters {@code BlockPowerConverter}: a bridge component. Each side property is true while a matching
 * neighbour is attached there (the "connected" texture). Right-click opens the GUI of the first adjacent bridge.
 */
public class PowerConverterBlock extends BaseEntityBlock {
    private final @Nullable ConverterType type;

    /** {@code type} is {@code null} for the universal charger. */
    public PowerConverterBlock(@Nullable ConverterType type, Properties properties) {
        super(properties);
        this.type = type;
        BlockState state = stateDefinition.any();
        for (Direction side : Direction.values()) {
            state = state.setValue(property(side), false);
        }
        registerDefaultState(state);
    }

    public static BooleanProperty property(Direction side) {
        return PipeBlock.PROPERTY_BY_DIRECTION.get(side);
    }

    public ConverterType getType() {
        if (type == null) {
            throw new IllegalStateException("The universal charger has no converter type");
        }
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction side : Direction.values()) {
            builder.add(property(side));
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        ConverterType converter = getType();
        return switch (converter.getSystem()) {
            case BUILDCRAFT -> converter.isConsumer() ? new BcConsumerBlockEntity(pos, state)
                    : new BcProducerBlockEntity(pos, state);
            case INDUSTRIALCRAFT -> converter.isConsumer() ? new Ic2ConsumerBlockEntity(pos, state)
                    : new Ic2ProducerBlockEntity(pos, state);
            case FE -> new FeProducerBlockEntity(pos, state);
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        if (type == ConverterType.FE_PRODUCER) {
            tooltip.add(Component.translatable("tooltip.craft.fe_producer").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof BridgeComponentBlockEntity<?> component) {
                component.serverTick();
            }
        };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BridgeComponentBlockEntity<?> component) {
            component.onNeighborChanged();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BridgeComponentBlockEntity<?> component) {
            EnergyBridgeBlockEntity bridge = component.getFirstBridge();
            if (bridge != null) {
                player.openMenu(bridge, bridge.getBlockPos());
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
