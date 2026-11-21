package dev.craft.secretrooms;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * Camouflaged trapdoor ({@code BlockCamoTrapDoor}, spec 50 §4): a vanilla trapdoor wearing the look of the block at its
 * hinge side (the original reads the texture of that neighbour), otherwise of its surroundings.
 */
public class CamoTrapDoorBlock extends TrapDoorBlock implements EntityBlock {
    public CamoTrapDoorBlock(BlockSetType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CamoBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof CamoBlockEntity entity)) {
            return;
        }
        BlockState hinge = CamoSelection.lookOf(level, pos.relative(state.getValue(FACING).getOpposite()));
        entity.setCamo(hinge != null ? hinge : CamoSelection.full(level, pos));
    }
}
