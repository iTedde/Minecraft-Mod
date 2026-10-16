package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.set;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code ItemTFBPFlatification}: levels the ground to the terraformer's height. */
public final class FlatificationBlueprint implements TerraformerBlueprint {
    @Override
    public int getConsume() {
        return 4000;
    }

    @Override
    public int getRange() {
        return 40;
    }

    @Override
    public boolean terraform(ServerLevel level, int x, int z, int yCoord) {
        int y = Terraforming.getFirstBlockFrom(level, x, z, yCoord + 20);
        if (y == Terraforming.NONE) {
            return false;
        }
        if (get(level, x, y, z).is(Blocks.SNOW)) {
            y--;
        }
        if (y == yCoord) {
            return false;
        }
        if (y < yCoord) {
            set(level, x, y + 1, z, Blocks.DIRT);
            return true;
        }
        if (canRemove(get(level, x, y, z))) {
            set(level, x, y, z, Blocks.AIR);
            return true;
        }
        return false;
    }

    /**
     * IC2 {@code removeIDs}: snow, ice, grass, stone, gravel, sand, dirt, leaves, logs, the cultivation plants and the
     * rubber tree. The 1.21 stone and dirt variants of the surface count as stone and dirt.
     */
    private static boolean canRemove(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.ICE) || state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.GRAVEL) || state.is(BlockTags.SAND)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || Terraforming.isLeaves(state) || Terraforming.isLog(state) || Terraforming.isPlant(state);
    }
}
