package dev.craft.advancedmachines;

import java.util.function.BiFunction;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** {@code BlockAdvancedMachines}: smoke above the rotary macerator and the singularity compressor while active. */
public class AdvancedMachineBlock extends MachineBlock {
    public AdvancedMachineBlock(Properties properties, Ic2Machine machine,
                                BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> factory) {
        super(properties, machine, factory);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (getMachine() == Ic2Machine.CENTRIFUGE_EXTRACTOR || !state.getValue(ACTIVE)) {
            return;
        }
        float x = pos.getX() + 1.0F;
        float y = pos.getY() + 1.0F;
        float z = pos.getZ() + 1.0F;
        for (int i = 0; i < 4; i++) {
            float dx = -0.2F - random.nextFloat() * 0.6F;
            float dy = -0.1F + random.nextFloat() * 0.2F;
            float dz = -0.2F - random.nextFloat() * 0.6F;
            level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0, 0.0, 0.0);
        }
    }
}
