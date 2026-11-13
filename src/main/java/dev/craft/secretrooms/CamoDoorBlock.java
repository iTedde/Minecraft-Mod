package dev.craft.secretrooms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Camouflaged wooden / iron door ({@code BlockCamoDoor}, spec 50 §4): a vanilla door whose both halves wear the
 * camouflage of the lower half (chosen from the wall around it). The iron door only opens by redstone.
 */
public class CamoDoorBlock extends DoorBlock implements EntityBlock {
    public CamoDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) {
            return;
        }
        BlockState camo = CamoSelection.full(level, pos);
        for (BlockPos half : new BlockPos[]{pos, pos.above()}) {
            if (level.getBlockEntity(half) instanceof CamoBlockEntity entity) {
                entity.setCamo(camo);
            }
        }
    }

    public static boolean isLower(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER;
    }
}
