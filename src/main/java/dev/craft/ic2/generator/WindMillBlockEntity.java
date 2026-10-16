package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wind mill after IC2 {@code TileEntityWindGenerator}: output depends on global wind, height above y 64 and
 * obstructing blocks; too much wind can break it down into a plain generator. Factors, overload threshold and buffer
 * follow the approved balancing (spec 14 §7).
 */
public class WindMillBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorWind} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_WIND = 100;
    public static final int TICK_RATE = 128;
    public static final int MAX_ENERGY_OUTPUT = 16;
    /** Balancing: output multiplier on the IC2 formula. */
    public static final double OUTPUT_FACTOR = 2.5;
    /** Balancing: overload starts above this subproduction (IC2: 5). */
    public static final double OVERLOAD_THRESHOLD = 12.5;
    public static final double THUNDER_FACTOR = 3.0;
    public static final double RAIN_FACTOR = 1.5;

    private double subproduction;
    private double substorage;
    private int ticker = RANDOM.nextInt(TICK_RATE);
    private int obscuratedBlockCount;

    public WindMillBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.WIND_MILL), pos, state, 1, 4, MAX_ENERGY_OUTPUT);
    }

    public double getSubproduction() {
        return subproduction;
    }

    public int getObscuratedBlockCount() {
        return obscuratedBlockCount;
    }

    public int gaugeFuelScaled(int scale) {
        return gaugePowerScaled(subproduction, scale);
    }

    public static int gaugePowerScaled(double subproduction, int scale) {
        int result = (int) (subproduction / 3.0 * scale);
        return result < 0 ? 0 : Math.min(result, scale);
    }

    /** Wind output before overload check and config factor; not positive means no generation. */
    public static double subproduction(int wind, int y, int obstructed, boolean thunder, boolean rain) {
        double sub = wind * (y - 64 - obstructed) / 750.0 * OUTPUT_FACTOR;
        if (sub <= 0.0) {
            return sub;
        }
        if (thunder) {
            return sub * THUNDER_FACTOR;
        }
        return rain ? sub * RAIN_FACTOR : sub;
    }

    public static int getOverheatScaled(double subproduction, int scale) {
        if (subproduction <= OVERLOAD_THRESHOLD) {
            return 0;
        }
        return subproduction >= 2 * OVERLOAD_THRESHOLD ? scale
                : (int) ((subproduction - OVERLOAD_THRESHOLD) / OVERLOAD_THRESHOLD * scale);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            updateObscuratedBlockCount();
        }
    }

    @Override
    public boolean gainEnergy() {
        ticker++;
        if (ticker % TICK_RATE == 0) {
            if (ticker % (8 * TICK_RATE) == 0) {
                updateObscuratedBlockCount();
            }
            subproduction = subproduction(Wind.getStrength(), worldPosition.getY(), obscuratedBlockCount,
                    level.isThundering(), level.isRaining());
            if (subproduction <= 0.0) {
                return false;
            }
            if (subproduction > OVERLOAD_THRESHOLD
                    && level.random.nextInt(5000) <= subproduction - OVERLOAD_THRESHOLD) {
                subproduction = 0.0;
                breakDown();
                return false;
            }
            subproduction = subproduction * ENERGY_GENERATOR_WIND;
            subproduction /= 100.0;
        }
        substorage += subproduction;
        production = (int) substorage;
        if (storage + production >= maxStorage) {
            substorage = 0.0;
            return false;
        }
        storage += production;
        substorage -= production;
        return true;
    }

    /** The overloaded wind mill turns into a plain generator and drops 0–4 iron ingots. */
    private void breakDown() {
        BlockState generator = Ic2Blocks.machine(Ic2Machine.GENERATOR).get().defaultBlockState()
                .setValue(MachineBlock.FACING, getFacing());
        BlockPos pos = worldPosition;
        level.setBlock(pos, generator, Block.UPDATE_ALL);
        for (int i = level.random.nextInt(5); i > 0; i--) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.IRON_INGOT));
        }
    }

    public void updateObscuratedBlockCount() {
        obscuratedBlockCount = -1;
        for (int x = -4; x < 5; x++) {
            for (int y = -2; y < 5; y++) {
                for (int z = -4; z < 5; z++) {
                    if (!level.getBlockState(worldPosition.offset(x, y, z)).isAir()) {
                        obscuratedBlockCount++;
                    }
                }
            }
        }
    }

    @Override
    public boolean gainFuel() {
        return false;
    }

    @Override
    public boolean needsFuel() {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return MAX_ENERGY_OUTPUT;
    }

    @Override
    public boolean delayActiveUpdate() {
        return true;
    }

    @Override
    protected int getSlotForSide(Direction side) {
        return 0;
    }

    @Override
    public SoundEvent getOperationSound() {
        return Ic2Sounds.WINDGEN_LOOP.get();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GeneratorMenu(id, playerInventory, this);
    }
}
