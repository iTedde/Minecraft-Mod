package dev.craft.powerconverters.block;

import dev.craft.powerconverters.power.ChargerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Universal Charger ({@code converterBlockCommon} meta 2): the collision box is 0.125 lower at the top, so a player
 * standing on it is inside the block and gets remembered by the charger.
 */
public class ChargerBlock extends PowerConverterBlock {
    private static final VoxelShape COLLISION = Block.box(0, 0, 0, 16, 14, 16);

    public ChargerBlock(Properties properties) {
        super(null, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargerBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && entity instanceof Player player
                && level.getBlockEntity(pos) instanceof ChargerBlockEntity charger) {
            charger.setPlayer(player);
        }
    }
}
