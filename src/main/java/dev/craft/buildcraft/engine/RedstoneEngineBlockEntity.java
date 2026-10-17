package dev.craft.buildcraft.engine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Redstone engine, 1:1 with BuildCraft {@code EngineWood}: +1 MJ every 20 ticks while powered, at most 1 MJ per
 * stroke. The energy is added without a cap or explosion check.
 */
public class RedstoneEngineBlockEntity extends EngineBlockEntity {
    private static final float[] PISTON_SPEEDS = {0.01F, 0.02F, 0.04F, 0.08F};

    public RedstoneEngineBlockEntity(BlockPos pos, BlockState state) {
        super(EngineType.REDSTONE, pos, state, 1000, 1, 0);
    }

    @Override
    public int explosionRange() {
        return 1;
    }

    @Override
    public int minEnergyReceived() {
        return 1;
    }

    @Override
    public int maxEnergyReceived() {
        return 50;
    }

    @Override
    protected float[] pistonSpeeds() {
        return PISTON_SPEEDS;
    }

    @Override
    protected void update() {
        super.update();
        if (isRedstonePowered() && level.getGameTime() % 20L == 0L) {
            energy++;
            outputMeter.record(level, 1.0F);
        }
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered();
    }

    @Override
    protected void burn() {
    }
}
