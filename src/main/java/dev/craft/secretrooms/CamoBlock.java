package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code BlockCamoFull} (spec 50 §4): a full block that takes on the look of its neighbours when placed. Light
 * passes as through the copied block. Subclasses add the behaviour (ghost, redstone, lever, …).
 */
public class CamoBlock extends BaseEntityBlock {
    public static final MapCodec<CamoBlock> CODEC = simpleCodec(CamoBlock::new);

    public CamoBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(pos, state);
    }

    /** {@code onBlockAdded}: choose the look once. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this) && level.getBlockEntity(pos) instanceof CamoBlockEntity camo
                && !camo.isChosen()) {
            camo.setCamo(chooseCamo(state, level, pos));
        }
    }

    protected BlockState chooseCamo(BlockState state, Level level, BlockPos pos) {
        return CamoSelection.full(level, pos);
    }

    protected static BlockState camo(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CamoBlockEntity camo ? camo.getCamo() : null;
    }

    /** {@code getLightOpacity}: as the copied block. */
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState camo = camo(level, pos);
        return camo == null || camo.getBlock() instanceof CamoBlock ? 15 : camo.getLightBlock(level, pos);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState camo = camo(level, pos);
        return camo != null && !(camo.getBlock() instanceof CamoBlock) && camo.propagatesSkylightDown(level, pos);
    }

    /** Placing next to a camouflaged block must not look through to it: no face culling against camouflage. */
    public static Block.Properties camoProperties(Block.Properties properties) {
        return properties.noOcclusion().dynamicShape();
    }
}
