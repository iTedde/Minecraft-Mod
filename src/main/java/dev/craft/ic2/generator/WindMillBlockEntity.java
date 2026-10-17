package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wind mill after IC2 {@code TileEntityWindGenerator}: output depends on global wind, height above y 64 and
 * obstructing blocks. Factors and buffer follow the approved balancing (spec 14 §7); unlike IC2 it never breaks
 * down (user, 2026-10-17).
 */
public class WindMillBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorWind} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_WIND = 100;
    public static final int TICK_RATE = 128;
    public static final int MAX_ENERGY_OUTPUT = 16;
    /** Balancing: output multiplier on the IC2 formula. */
    public static final double OUTPUT_FACTOR = 2.5;
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

    /** Wind output before the config factor; not positive means no generation. */
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
