package dev.craft.secretrooms;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * One-way glass ({@code BlockOneWay}, spec 50 §4): the {@link #FACING} side shows the camouflage of the blocks around
 * it, every other side is clear glass – from behind one looks straight through. Placed like a piston towards the
 * player, then turned round, so the camouflage faces away; the facing key turns that round per player.
 */
public class OneWayGlassBlock extends CamoBlock {
    public static final MapCodec<OneWayGlassBlock> CODEC = simpleCodec(OneWayGlassBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /** Players who switched to "camouflage towards me" ({@code awaySet}). */
    private static final Set<UUID> TOWARDS = new HashSet<>();

    public OneWayGlassBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Toggles the player's facing choice; returns whether the camouflage now faces the player. */
    public static boolean toggleFacing(Player player) {
        if (!TOWARDS.remove(player.getUUID())) {
            TOWARDS.add(player.getUUID());
            return true;
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction towards = context.getNearestLookingDirection().getOpposite();
        Player player = context.getPlayer();
        boolean faceTowards = player != null && TOWARDS.contains(player.getUUID());
        return defaultBlockState().setValue(FACING, faceTowards ? towards : towards.getOpposite());
    }

    @Override
    protected BlockState chooseCamo(BlockState state, Level level, BlockPos pos) {
        return CamoSelection.oneWay(level, pos, state.getValue(FACING));
    }

    /** {@code getLightOpacity}: only an upward-facing pane lets light through (original quirk, meta 1). */
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FACING) == Direction.UP ? 0 : 15;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FACING) == Direction.UP;
    }

    /** Neighbouring one-way glass does not draw the faces between them ({@code shouldSideBeRendered}). */
    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction side) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, side);
    }
}
