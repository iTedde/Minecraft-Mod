package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.pos;
import static dev.craft.ic2.terraformer.Terraforming.set;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code ItemTFBPChilling}: water freezes, ice grows down, snow is laid and turns into snow blocks. */
public final class ChillingBlueprint implements TerraformerBlueprint {
    @Override
    public int getConsume() {
        return 2000;
    }

    @Override
    public int getRange() {
        return 50;
    }

    @Override
    public boolean terraform(ServerLevel level, int x, int z, int yCoord) {
        int y = Terraforming.getFirstBlockFrom(level, x, z, yCoord + 10);
        if (y == Terraforming.NONE) {
            return false;
        }
        BlockState state = get(level, x, y, z);
        if (Terraforming.isWater(state)) {
            set(level, x, y, z, Blocks.ICE);
            return true;
        }
        if (state.is(Blocks.ICE) && Terraforming.isWater(get(level, x, y - 1, z))) {
            set(level, x, y - 1, z, Blocks.ICE);
            return true;
        }
        if (state.is(Blocks.SNOW) && isSurroundedBySnow(level, x, y, z)) {
            set(level, x, y, z, Blocks.SNOW_BLOCK);
            return true;
        }
        if (canPlaceSnow(level, x, y + 1, z) || state.is(Blocks.ICE)) {
            set(level, x, y + 1, z, Blocks.SNOW);
        }
        return false;
    }

    /** 1.4.7 {@code BlockSnow.canPlaceBlockAt}: replaceable spot on a solid block or leaves. */
    static boolean canPlaceSnow(ServerLevel level, int x, int y, int z) {
        BlockPos pos = pos(x, y, z);
        return level.getBlockState(pos).canBeReplaced() && Blocks.SNOW.defaultBlockState().canSurvive(level, pos);
    }

    private static boolean isSurroundedBySnow(ServerLevel level, int x, int y, int z) {
        return isSnowHere(level, x + 1, y, z) && isSnowHere(level, x - 1, y, z) && isSnowHere(level, x, y, z + 1)
                && isSnowHere(level, x, y, z - 1);
    }

    private static boolean isSnowHere(ServerLevel level, int x, int y, int z) {
        int saveY = y;
        y = Terraforming.getFirstBlockFrom(level, x, z, y + 16);
        if (y == Terraforming.NONE || saveY > y) {
            return false;
        }
        BlockState state = get(level, x, y, z);
        if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
            return true;
        }
        if (canPlaceSnow(level, x, y + 1, z) || state.is(Blocks.ICE)) {
            set(level, x, y + 1, z, Blocks.SNOW);
        }
        return false;
    }
}
