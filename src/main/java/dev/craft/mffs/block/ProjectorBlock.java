package dev.craft.mffs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * {@code BlockProjector} (spec 48 §5, §9.1): the texture row follows the module type ({@code ProTyp} 0–8, block state
 * {@code module}); a burnt-out projector opens no GUI and smokes.
 */
public class ProjectorBlock extends MffsMachineBlock {
    public static final IntegerProperty MODULE = IntegerProperty.create("module", 0, 8);

    public ProjectorBlock(Properties properties) {
        super(properties, ProjectorBlockEntity::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MODULE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity projector && projector.isBurnedOut()) {
            return InteractionResult.PASS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity projector && projector.isBurnedOut()) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(),
                    pos.getZ() + Math.random(), 0.0, 0.0, 0.0);
        }
    }
}
