/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.entity.PSBlockEntities;
import ivorius.psychedelicraft.block.entity.TrayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrayBlock extends BaseEntityBlock {
    public static final MapCodec<TrayBlock> CODEC = simpleCodec(TrayBlock::new);

    private static final VoxelShape X_SHAPE = ShapeUtil.createCenteredShape(9, 2, 6);
    private static final VoxelShape Z_SHAPE = ShapeUtil.createCenteredShape(6, 2, 9);

    private static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public TrayBlock(Properties settings) {
        super(settings.noOcclusion());
        this.registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends TrayBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(AXIS, ctx.getHorizontalDirection().getClockWise().getAxis());
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof TrayBlockEntity be) {
            builder = builder.withDynamicDrop(BlockWithFluid.CONTENTS_DYNAMIC_DROP_ID, lootConsumer -> {
                be.getCraftingResult().ifPresent(result -> {
                    lootConsumer.accept(result);
                });
            });
        }
        return super.getDrops(state, builder);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrayBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <Q extends BlockEntity> BlockEntityTicker<Q> getTicker(Level world, BlockState state, BlockEntityType<Q> type) {
        return world.isClientSide
                ? null
                : createTickerHelper(type, PSBlockEntities.TRAY, (w, p, s, entity) -> entity.tick((ServerLevel)w));
    }
}
