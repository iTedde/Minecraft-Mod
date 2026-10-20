package dev.craft.powerplants.boiler;

import dev.craft.powerplants.PpBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * High-pressure boiler (spec 23 §1): 5×5×5 of high-pressure boiler casings around a 3×3×3 air core. Works like the
 * small boiler, but up to 1500 °C, 160 mB/t steam, 64 000 mB tanks, shorter burn times (coal 24 000 EU, coke
 * 48 000 EU) and an explosion of power 10.
 */
public class HighPressureBoilerBlockEntity extends BoilerBlockEntity {
    public HighPressureBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.HIGH_PRESSURE_BOILER.get(), pos, state);
    }

    @Override
    public BoilerTier getTier() {
        return BoilerTier.HIGH_PRESSURE;
    }
}
