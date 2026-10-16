package dev.craft.ic2.terraformer;

import static dev.craft.ic2.terraformer.Terraforming.get;
import static dev.craft.ic2.terraformer.Terraforming.pos;
import static dev.craft.ic2.terraformer.Terraforming.set;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.block.RubberTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;

/** IC2 {@code ItemTFBPIrrigation}: rain, sand → dirt, trees and wheat grow, fire goes out, grass spreads. */
public final class IrrigationBlueprint implements TerraformerBlueprint {
    @Override
    public int getConsume() {
        return 3000;
    }

    @Override
    public int getRange() {
        return 60;
    }

    @Override
    public boolean terraform(ServerLevel level, int x, int z, int yCoord) {
        if (level.random.nextInt(48000) == 0) {
            // IC2 WorldInfo.setRaining(true): the rain timer is left alone.
            if (level.getLevelData() instanceof ServerLevelData data) {
                data.setRaining(true);
            }
            return true;
        }
        int y = Terraforming.getFirstBlockFrom(level, x, z, yCoord + 10);
        if (y == Terraforming.NONE) {
            return false;
        }
        if (Terraforming.switchGround(level, Blocks.SAND, Blocks.DIRT, x, y, z, true)) {
            Terraforming.switchGround(level, Blocks.SAND, Blocks.DIRT, x, y, z, true);
            return true;
        }
        BlockPos pos = pos(x, y, z);
        BlockState state = level.getBlockState(pos);
        if (Terraforming.isTallGrass(state)) {
            return spreadGrass(level, x + 1, y, z) || spreadGrass(level, x - 1, y, z)
                    || spreadGrass(level, x, y, z + 1) || spreadGrass(level, x, y, z - 1);
        }
        if (state.getBlock() instanceof SaplingBlock sapling) {
            // IC2 BlockSapling.growTree: grows at once, whatever the stage.
            BlockState grown = state.setValue(SaplingBlock.STAGE, 1);
            level.setBlock(pos, grown, Block.UPDATE_NONE);
            sapling.advanceTree(level, pos, grown, level.random);
            return true;
        }
        if (state.is(Ic2Blocks.RUBBER_SAPLING.get())) {
            RubberTree.grow(level, pos, level.random);
            return true;
        }
        if (state.is(BlockTags.LOGS_THAT_BURN) && !state.is(Ic2Blocks.RUBBER_WOOD.get())) {
            set(level, x, y + 1, z, state);
            Block leaves = leavesFor(state);
            createLeaves(level, x, y + 2, z, leaves);
            createLeaves(level, x + 1, y + 1, z, leaves);
            createLeaves(level, x - 1, y + 1, z, leaves);
            createLeaves(level, x, y + 1, z + 1, leaves);
            createLeaves(level, x, y + 1, z - 1, leaves);
            return true;
        }
        if (state.is(Blocks.WHEAT)) {
            level.setBlock(pos, state.setValue(CropBlock.AGE, CropBlock.MAX_AGE), Block.UPDATE_ALL);
            return true;
        }
        if (state.is(Blocks.FIRE)) {
            set(level, x, y, z, Blocks.AIR);
            return true;
        }
        return false;
    }

    /** 1.4.7 leaves take the wood type of the log (metadata); 1.21: {@code <name>_log} → {@code <name>_leaves}. */
    private static Block leavesFor(BlockState log) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(log.getBlock());
        String path = id.getPath();
        if (path.endsWith("_log")) {
            ResourceLocation leaves = id.withPath(path.substring(0, path.length() - 4) + "_leaves");
            if (BuiltInRegistries.BLOCK.containsKey(leaves)) {
                return BuiltInRegistries.BLOCK.get(leaves);
            }
        }
        return Blocks.OAK_LEAVES;
    }

    private static void createLeaves(ServerLevel level, int x, int y, int z, Block leaves) {
        BlockPos pos = pos(x, y, z);
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Block.updateFromNeighbourShapes(leaves.defaultBlockState(), level, pos),
                    Block.UPDATE_ALL);
        }
    }

    private static boolean spreadGrass(ServerLevel level, int x, int y, int z) {
        if (level.random.nextBoolean()) {
            return false;
        }
        y = Terraforming.getFirstBlockFrom(level, x, z, y);
        if (y == Terraforming.NONE) {
            return false;
        }
        BlockState state = get(level, x, y, z);
        if (state.is(Blocks.DIRT)) {
            set(level, x, y, z, Blocks.GRASS_BLOCK);
            return true;
        }
        if (state.is(Blocks.GRASS_BLOCK)) {
            set(level, x, y + 1, z, Blocks.SHORT_GRASS);
            return true;
        }
        return false;
    }
}
