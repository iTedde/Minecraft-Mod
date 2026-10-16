package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.set;

import dev.craft.ic2.Ic2Blocks;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** IC2 {@code ItemTFBPCultivation}: sand → dirt, dirt → grass, plants on grass. */
public final class CultivationBlueprint implements TerraformerBlueprint {
    /** IC2 {@code plantIDs} in order: tall grass, rose, dandelion, sapling, wheat, mushrooms, pumpkin, rubber sapling. */
    private static final List<Supplier<Block>> PLANTS = List.of(() -> Blocks.SHORT_GRASS, () -> Blocks.POPPY,
            () -> Blocks.DANDELION, () -> Blocks.OAK_SAPLING, () -> Blocks.WHEAT, () -> Blocks.RED_MUSHROOM,
            () -> Blocks.BROWN_MUSHROOM, () -> Blocks.PUMPKIN, Ic2Blocks.RUBBER_SAPLING::get);

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
        int y = Terraforming.getFirstSolidBlockFrom(level, x, z, yCoord + 10);
        if (y == Terraforming.NONE) {
            return false;
        }
        if (Terraforming.switchGround(level, Blocks.SAND, Blocks.DIRT, x, y, z, true)) {
            return true;
        }
        BlockState state = get(level, x, y, z);
        if (state.is(Blocks.DIRT)) {
            set(level, x, y, z, Blocks.GRASS_BLOCK);
            return true;
        }
        return state.is(Blocks.GRASS_BLOCK) && growPlantsOn(level, x, y + 1, z);
    }

    private static boolean growPlantsOn(ServerLevel level, int x, int y, int z) {
        BlockState state = get(level, x, y, z);
        if (!state.isAir() && !(Terraforming.isTallGrass(state) && level.random.nextInt(4) == 0)) {
            return false;
        }
        Block plant = pickRandomPlant(level.random);
        if (plant == Blocks.WHEAT) {
            set(level, x, y - 1, z, Blocks.FARMLAND);
        }
        set(level, x, y, z, plant);
        return true;
    }

    private static Block pickRandomPlant(RandomSource random) {
        for (Supplier<Block> plant : PLANTS) {
            if (random.nextInt(5) <= 1) {
                return plant.get();
            }
        }
        return Blocks.SHORT_GRASS;
    }
}
