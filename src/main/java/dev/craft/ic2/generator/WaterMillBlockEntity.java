package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Water mill, 1:1 with IC2 {@code TileEntityWaterGenerator}: water containers (bucket 500 EU, cell 1000 EU) or
 * passively about {@code waterBlocks / 100} EU/t from water in the surrounding 3×3×3 cube.
 */
public class WaterMillBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorWater} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_WATER = 100;
    public static final int MAX_WATER = 2000;
    public static final int WATER_PER_CONTAINER = 500;
    public static final int TICK_RATE = 128;

    private int ticker;
    private int water;
    private int microStorage;

    public WaterMillBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.WATER_MILL), pos, state, 2, 2, 2);
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
            production = microStorage / 100;
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
        water = water * ENERGY_GENERATOR_WATER / 100;
        if (water > 0) {
            microStorage += water;
        }
    }

    public void updateWaterCount() {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 1, 1))) {
            if (level.getBlockState(pos).is(Blocks.WATER)) {
                count++;
            }
        }
        water = count;
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
