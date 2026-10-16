package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.pos;
import static dev.craft.ic2.terraformer.Terraforming.set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code ItemTFBPMushroom}: spreads mycelium (and the mushroom biome), plants mushrooms on it and grows them into
 * huge mushrooms. The neighbour loops only cover x−1..x and z−1..z, as in the original.
 */
public final class MushroomBlueprint implements TerraformerBlueprint {
    private enum Goal {
        HUGE_MUSHROOM,
        MUSHROOM
    }

    private enum Dependency {
        MUSHROOM,
        MYCELIUM
    }

    @Override
    public int getConsume() {
        return 8000;
    }

    @Override
    public int getRange() {
        return 25;
    }

    @Override
    public boolean terraform(ServerLevel level, int x, int z, int yCoord) {
        int y = Terraforming.getFirstSolidBlockFrom(level, x, z, yCoord + 20);
        return y != Terraforming.NONE && grow(level, x, y, z, Goal.HUGE_MUSHROOM, Dependency.MUSHROOM);
    }

    private static boolean isCap(BlockState state) {
        return state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK);
    }

    private static boolean isMushroom(BlockState state) {
        return state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM);
    }

    /** IC2 {@code growBlockWithDependancy}. */
    private static boolean grow(ServerLevel level, int x, int y, int z, Goal goal, Dependency dependency) {
        for (int xm = x - 1; xm < x + 1; xm++) {
            for (int zm = z - 1; zm < z + 1; zm++) {
                for (int ym = y + 5; ym > y - 2; ym--) {
                    BlockState block = get(level, xm, ym, zm);
                    if (dependency == Dependency.MYCELIUM) {
                        if (block.is(Blocks.MYCELIUM) || isCap(block)) {
                            break;
                        }
                        if (block.isAir()) {
                            continue;
                        }
                        if (block.is(Blocks.DIRT) || block.is(Blocks.GRASS_BLOCK)) {
                            set(level, xm, ym, zm, Blocks.MYCELIUM);
                            Terraforming.setBiomeAt(level, x, z, Biomes.MUSHROOM_FIELDS);
                            return true;
                        }
                        continue;
                    }
                    if (isMushroom(block)) {
                        break;
                    }
                    if (!block.isAir() && grow(level, xm, ym, zm, Goal.MUSHROOM, Dependency.MYCELIUM)) {
                        return true;
                    }
                }
            }
        }
        if (goal == Goal.MUSHROOM) {
            BlockState base = get(level, x, y, z);
            if (!base.is(Blocks.MYCELIUM)) {
                if (!isCap(base)) {
                    return false;
                }
                set(level, x, y, z, Blocks.MYCELIUM);
            }
            BlockState above = get(level, x, y + 1, z);
            if (!above.isAir() && !Terraforming.isTallGrass(above)) {
                return false;
            }
            set(level, x, y + 1, z, level.random.nextBoolean() ? Blocks.RED_MUSHROOM : Blocks.BROWN_MUSHROOM);
            return true;
        }
        BlockPos mushroomPos = pos(x, y + 1, z);
        BlockState mushroom = level.getBlockState(mushroomPos);
        if (mushroom.getBlock() instanceof MushroomBlock block
                && block.growMushroom(level, mushroomPos, mushroom, level.random)) {
            for (int xm = x - 1; xm < x + 1; xm++) {
                for (int zm = z - 1; zm < z + 1; zm++) {
                    if (isMushroom(get(level, xm, y + 1, zm))) {
                        set(level, xm, y + 1, zm, Blocks.AIR);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
