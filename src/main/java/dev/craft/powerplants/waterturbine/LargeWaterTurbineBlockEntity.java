package dev.craft.powerplants.waterturbine;

import dev.craft.powerplants.PpBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large water turbine (spec 23 §3): a solid 5×5×5 cube of large water turbine casings. Counts flowing water up to 7
 * blocks out from its sides and 24 above its top and makes 1 EU/t per flowing block from 64 blocks on (× 1.5 in
 * rivers), at most 256 EU/t in packets of up to 256 EU (HV).
 */
public class LargeWaterTurbineBlockEntity extends WaterTurbineBlockEntity {
    public LargeWaterTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.LARGE_WATER_TURBINE.get(), pos, state);
    }

    @Override
    public WaterTurbineTier getTier() {
        return WaterTurbineTier.LARGE;
    }
}
