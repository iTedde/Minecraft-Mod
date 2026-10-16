package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Water mill after IC2 {@code TileEntityWaterGenerator}: water containers (bucket 500 EU, cell 1000 EU) 1:1, the passive
 * generation follows the approved balancing (spec 14 §6): water in the surrounding 5×5×3 area, flowing water yields
 * twice as much as sources, shared between overlapping mills, biome bonus, capped at 2 EU/t.
 */
public class WaterMillBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorWater} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_WATER = 100;
    public static final int MAX_WATER = 2000;
    public static final int WATER_PER_CONTAINER = 500;
    public static final int TICK_RATE = 128;
    public static final int RADIUS = 2;
    /** Passive yield per water block in 1/100 EU/t. */
    public static final double SOURCE_YIELD = 2.0;
    public static final double FLOWING_YIELD = 4.0;
    public static final double RIVER_FACTOR = 1.5;
    public static final double OCEAN_FACTOR = 1.25;
    /** Passive output cap in 1/100 EU/t. */
    public static final double MAX_YIELD = 200.0;

    private int ticker;
    private int water;
    private double flow;
    private double microStorage;

    public WaterMillBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.WATER_MILL), pos, state, 2, 2, 4);
        this.ticker = RANDOM.nextInt(TICK_RATE);
    }

    public int gaugeFuelScaled(int scale) {
        return fuel <= 0 ? 0 : fuel * scale / MAX_WATER;
    }

    public int getWaterBlocks() {
        return water;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            updateWaterCount();
        }
    }

    @Override
    public boolean gainFuel() {
        ItemStack stack = inventory.get(1);
        if (!stack.isEmpty() && MAX_WATER - fuel >= WATER_PER_CONTAINER) {
            if (stack.is(Items.WATER_BUCKET)) {
                production = 1;
                fuel += WATER_PER_CONTAINER;
                inventory.set(1, stack.transmuteCopy(Items.BUCKET, stack.getCount()));
                return true;
            }
            if (stack.is(Ic2Items.WATER_CELL.get())) {
                production = 2;
                fuel += WATER_PER_CONTAINER;
                stack.shrink(1);
                inventory.set(1, stack.isEmpty() ? ItemStack.EMPTY : stack);
                return true;
            }
            if (FuelValues.containsWater(stack)) {
                fuel += WATER_PER_CONTAINER;
                if (stack.hasCraftingRemainingItem()) {
                    production = 1;
                } else {
                    production = 2;
                }
                inventory.set(1, consumeOne(stack));
                return true;
            }
            // Original quirk: any other item here blocks the passive generation.
        } else if (fuel <= 0) {
            flowPower();
            production = (int) (microStorage / 100);
            microStorage -= production * 100;
            if (production > 0) {
                fuel++;
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean needsFuel() {
        return fuel <= MAX_WATER;
    }

    private void flowPower() {
        if (ticker++ % TICK_RATE == 0) {
            updateWaterCount();
        }
        microStorage += flow;
    }

    /** Recounts the water around the mill and recomputes the passive yield ({@link #flow}, 1/100 EU/t). */
    public void updateWaterCount() {
        List<BlockPos> mills = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-2 * RADIUS, -2, -2 * RADIUS),
                worldPosition.offset(2 * RADIUS, 2, 2 * RADIUS))) {
            if (level.getBlockEntity(pos) instanceof WaterMillBlockEntity) {
                mills.add(pos.immutable());
            }
        }
        int count = 0;
        double yield = 0.0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-RADIUS, -1, -RADIUS),
                worldPosition.offset(RADIUS, 1, RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.WATER)) {
                continue;
            }
            count++;
            int sharing = 0;
            for (BlockPos mill : mills) {
                if (Math.abs(mill.getX() - pos.getX()) <= RADIUS && Math.abs(mill.getY() - pos.getY()) <= 1
                        && Math.abs(mill.getZ() - pos.getZ()) <= RADIUS) {
                    sharing++;
                }
            }
            double blockYield = state.getFluidState().isSource() ? SOURCE_YIELD : FLOWING_YIELD;
            yield += blockYield / Math.max(sharing, 1);
        }
        Holder<Biome> biome = level.getBiome(worldPosition);
        if (biome.is(BiomeTags.IS_RIVER)) {
            yield *= RIVER_FACTOR;
        } else if (biome.is(BiomeTags.IS_OCEAN)) {
            yield *= OCEAN_FACTOR;
        }
        water = count;
        flow = Math.min(yield, MAX_YIELD) * ENERGY_GENERATOR_WATER / 100.0;
    }

    public double getFlow() {
        return flow;
    }

    @Override
    public boolean delayActiveUpdate() {
        return true;
    }

    @Override
    public SoundEvent getOperationSound() {
        return Ic2Sounds.WATERMILL_LOOP.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GeneratorMenu(id, playerInventory, this);
    }
}
