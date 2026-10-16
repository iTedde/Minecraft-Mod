package dev.craft.ic2.reactor;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Nuclear reactor block (IC2 {@code BlockGenerator} meta 5): no GUI while holding a reactor chamber so it can be
 * placed against the reactor; smoke and flames by hull heat.
 */
public class NuclearReactorBlock extends MachineBlock {
    public NuclearReactorBlock(Properties properties) {
        super(properties, Ic2Machine.NUCLEAR_REACTOR, NuclearReactorBlockEntity::new);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Ic2Blocks.REACTOR_CHAMBER.get().asItem())) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof NuclearReactorBlockEntity reactor) {
            puff(level, pos, random, reactor.getHeat());
        }
    }

    /** IC2: {@code heat / 1000} upper bound of smoke puffs, flames for the puffs beyond 3–6. */
    static void puff(Level level, BlockPos pos, RandomSource random, int heat) {
        int puffs = heat / 1000;
        if (puffs <= 0) {
            return;
        }
        puffs = level.random.nextInt(puffs);
        for (int n = 0; n < puffs; n++) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + random.nextFloat(), pos.getY() + 0.95F,
                    pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        }
        puffs -= level.random.nextInt(4) + 3;
        for (int n = 0; n < puffs; n++) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + random.nextFloat(), pos.getY() + 1.0F,
                    pos.getZ() + random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }
}
