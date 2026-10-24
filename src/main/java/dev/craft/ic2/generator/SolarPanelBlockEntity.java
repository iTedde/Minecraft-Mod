package dev.craft.ic2.generator;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * Solar panel, IC2 {@code TileEntitySolarGenerator}: 1 EU/t while the sun is visible. Balancing (user, 2026-10-24;
 * {@code docs/balancing.md} §3.1): a panel lying in a complete 9×9 square of panels on its level yields ×1.2, i.e. one
 * extra EU every {@link #FIELD_BONUS_PERIOD} sunny ticks.
 */
public class SolarPanelBlockEntity extends BaseGeneratorBlockEntity {
    /** {@code energyGeneratorSolar} from the pack's IC2.cfg. */
    public static final int ENERGY_GENERATOR_SOLAR = 100;
    public static final int TICK_RATE = 128;
    /** Edge length of the square of panels that earns the field bonus. */
    public static final int FIELD_SIZE = 9;
    /** ×1.2: every fifth sunny tick of a panel in a field yields 2 EU instead of 1. */
    public static final int FIELD_BONUS_PERIOD = 5;

    private int ticker;
    private boolean sunIsVisible;
    private boolean inField;
    private boolean fieldChecked;
    private int bonusTicker;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        // Buffer 2 for the bonus ticks; the packet size follows the field (1 alone, 2 in a field).
        super(Ic2BlockEntityTypes.get(Ic2Machine.SOLAR_PANEL), pos, state, 1, 1, 2);
        this.ticker = RANDOM.nextInt(TICK_RATE);
    }

    public boolean isSunVisible() {
        return sunIsVisible;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            updateSunVisibility();
        }
    }

    /** True if the panel lay in a complete 9×9 square of panels at the last check. */
    public boolean isInField() {
        return inField;
    }

    @Override
    public boolean gainEnergy() {
        if (ticker++ % TICK_RATE == 0 || !fieldChecked) {
            updateSunVisibility();
            inField = isInField(level, worldPosition);
            production = inField ? 2 : 1;
            fieldChecked = true;
        }
        if (!sunIsVisible) {
            return false;
        }
        double gen = ENERGY_GENERATOR_SOLAR / 100.0;
        if (gen >= 1.0 || ticker % (100 - ENERGY_GENERATOR_SOLAR) == 0) {
            storage += (int) Math.ceil(gen);
        }
        if (inField && ++bonusTicker % FIELD_BONUS_PERIOD == 0) {
            storage++;
        }
        return true;
    }

    /**
     * True if {@code pos} lies in at least one complete {@link #FIELD_SIZE}×{@link #FIELD_SIZE} square of solar
     * panels on its level. Blocks in unloaded chunks count as missing.
     */
    public static boolean isInField(Level level, BlockPos pos) {
        int reach = FIELD_SIZE - 1;
        int width = 2 * reach + 1;
        // sums[i][j] = panels in the rectangle of the first i columns and j rows around pos
        int[][] sums = new int[width + 1][width + 1];
        Block panelBlock = Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                cursor.set(pos.getX() - reach + i, pos.getY(), pos.getZ() - reach + j);
                int panel = level.isLoaded(cursor)
                        && level.getBlockState(cursor).is(panelBlock) ? 1 : 0;
                sums[i + 1][j + 1] = panel + sums[i][j + 1] + sums[i + 1][j] - sums[i][j];
            }
        }
        int full = FIELD_SIZE * FIELD_SIZE;
        for (int i = 0; i <= reach; i++) {
            for (int j = 0; j <= reach; j++) {
                int count = sums[i + FIELD_SIZE][j + FIELD_SIZE] - sums[i][j + FIELD_SIZE]
                        - sums[i + FIELD_SIZE][j] + sums[i][j];
                if (count == full) {
                    return true;
                }
            }
        }
        return false;
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
    public boolean delayActiveUpdate() {
        return true;
    }

    public void updateSunVisibility() {
        sunIsVisible = isSunVisible(level, worldPosition.above());
    }

    /** Daytime, sky light, open sky above and no rain – deserts ignore rain. */
    public static boolean isSunVisible(Level level, BlockPos pos) {
        return level.isDay()
                && level.dimensionType().hasSkyLight()
                && level.canSeeSky(pos)
                && (level.getBiome(pos).is(Tags.Biomes.IS_DESERT) || !level.isRaining() && !level.isThundering());
    }

    @Override
    protected int getSlotForSide(Direction side) {
        return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GeneratorMenu(id, playerInventory, this);
    }
}
