package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code ItemTFBPDesertification}: ground → sand, water/snow/leaves/plants vanish, ice melts, wood burns. */
public final class DesertificationBlueprint implements TerraformerBlueprint {
    @Override
    public int getConsume() {
        return 2500;
    }

    @Override
    public int getRange() {
        return 40;
    }

    @Override
    public boolean terraform(ServerLevel level, int x, int z, int yCoord) {
        int y = Terraforming.getFirstBlockFrom(level, x, z, yCoord + 10);
        if (y == Terraforming.NONE) {
            return false;
        }
        if (Terraforming.switchGround(level, Blocks.DIRT, Blocks.SAND, x, y, z, false)
                || Terraforming.switchGround(level, Blocks.GRASS_BLOCK, Blocks.SAND, x, y, z, false)
                || Terraforming.switchGround(level, Blocks.FARMLAND, Blocks.SAND, x, y, z, false)) {
            Terraforming.switchGround(level, Blocks.DIRT, Blocks.SAND, x, y, z, false);
            return true;
        }
        BlockState state = get(level, x, y, z);
        if (Terraforming.isWater(state) || state.is(Blocks.SNOW) || Terraforming.isLeaves(state)
                || Terraforming.isPlant(state)) {
            set(level, x, y, z, Blocks.AIR);
            return true;
        }
        if (state.is(Blocks.ICE) || state.is(Blocks.SNOW_BLOCK)) {
            set(level, x, y, z, Blocks.WATER);
            return true;
        }
        if ((state.is(BlockTags.PLANKS) || Terraforming.isLog(state)) && level.random.nextInt(15) == 0) {
            set(level, x, y, z, Blocks.FIRE);
            return true;
        }
        return false;
    }
}
