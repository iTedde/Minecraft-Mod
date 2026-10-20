package dev.craft.powerplants.turbine;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.block.PlantShapes;
import net.minecraft.world.level.block.Block;

/**
 * Size of a steam turbine: a box of casings, long axis horizontal, with a row of rotors along the middle and the
 * controller in the middle of one end face. The small turbine (spec 14 §4) has no other inner blocks, the large one
 * (spec 23 §2) air around its rotor axis.
 */
public final class SteamTurbineTier {
    public static final SteamTurbineTier SMALL = new SteamTurbineTier(3, 5, SteamTurbineBlockEntity.TANK_CAPACITY,
            SteamTurbineBlockEntity.MAX_STEAM_PER_TICK, SteamTurbineBlockEntity.MAX_OUTPUT,
            SteamTurbineBlockEntity.BUFFER_CAPACITY, PpBlocks.TURBINE_CASING, PpBlocks.TURBINE_ROTOR,
            PpBlocks.STEAM_TURBINE);
    public static final SteamTurbineTier LARGE = new SteamTurbineTier(5, 9, 64000, 512, 512, 1024,
            PpBlocks.LARGE_TURBINE_CASING, PpBlocks.LARGE_TURBINE_ROTOR, PpBlocks.LARGE_STEAM_TURBINE);

    private final int tankCapacity;
    private final int maxSteamPerTick;
    private final int maxOutput;
    private final int bufferCapacity;
    private final Supplier<MultiblockPattern> pattern;

    private SteamTurbineTier(int size, int length, int tankCapacity, int maxSteamPerTick, int maxOutput,
                             int bufferCapacity, Supplier<? extends Block> casing, Supplier<? extends Block> rotor,
                             Supplier<? extends Block> controller) {
        this.tankCapacity = tankCapacity;
        this.maxSteamPerTick = maxSteamPerTick;
        this.maxOutput = maxOutput;
        this.bufferCapacity = bufferCapacity;
        this.pattern = Suppliers.memoize(() -> PlantShapes.box(size, size, length, (x, y, z) -> {
                    if (PlantShapes.onShell(size, size, length, x, y, z)) {
                        return 'H';
                    }
                    return x == size / 2 && y == size / 2 ? 'R' : 'A';
                })
                .where('H', BlockMatcher.blocks(casing.get()))
                .where('R', BlockMatcher.blocks(rotor.get()))
                .where('A', BlockMatcher.AIR)
                .controller(PlantShapes.CONTROLLER, BlockMatcher.blocks(controller.get()))
                .build());
    }

    public int tankCapacity() {
        return tankCapacity;
    }

    public int maxSteamPerTick() {
        return maxSteamPerTick;
    }

    /** Largest packet; 1 mB steam = 1 EU. */
    public int maxOutput() {
        return maxOutput;
    }

    public int bufferCapacity() {
        return bufferCapacity;
    }

    public MultiblockPattern pattern() {
        return pattern.get();
    }
}
