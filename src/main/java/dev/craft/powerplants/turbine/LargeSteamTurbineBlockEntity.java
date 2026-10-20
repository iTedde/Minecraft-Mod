package dev.craft.powerplants.turbine;

import dev.craft.powerplants.PpBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Large steam turbine (spec 23 §2): 5×5×9 of large turbine casings around an axis of seven large rotors with air
 * around it. Works like the small turbine with a 64 000 mB tank, up to 512 mB steam per tick and packets of up to
 * 512 EU (HV), enough for three high-pressure boilers.
 */
public class LargeSteamTurbineBlockEntity extends SteamTurbineBlockEntity {
    public LargeSteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.LARGE_STEAM_TURBINE.get(), pos, state);
    }

    @Override
    public SteamTurbineTier getTier() {
        return SteamTurbineTier.LARGE;
    }
}
