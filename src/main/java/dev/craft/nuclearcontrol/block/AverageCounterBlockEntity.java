package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.menu.CounterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Average counter ({@code TileEntityAverageCounter}, spec 46 §7.2): ring buffer of 11 measuring points of 20 ticks
 * each; the average over the last 1/3/5/10 complete points is {@code sum / (period × 20)} EU/t, recomputed whenever the
 * buffer advances. The first point after loading or placing lasts 21 ticks ({@code updateTicker-- == 0} from 20).
 */
public class AverageCounterBlockEntity extends CounterBlockEntity {
    public static final int POINTS = 11;
    public static final int TICK_RATE = 20;

    private final long[] data = new long[POINTS];
    private int index;
    private int updateTicker = TICK_RATE;
    private int period = 1;
    private int average = -1;

    public AverageCounterBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.AVERAGE_COUNTER.get(), pos, state);
    }

    @Override
    public void serverTick() {
        if (updateTicker-- == 0) {
            updateTicker = TICK_RATE - 1;
            index = (index + 1) % POINTS;
            data[index] = 0L;
            computeAverage();
        }
        super.serverTick();
    }

    @Override
    protected void onSent(int amount) {
        data[index] += amount;
    }

    private void computeAverage() {
        int start = POINTS + index - period;
        long sum = 0L;
        for (int i = 0; i < period; i++) {
            sum += data[(start + i) % POINTS];
        }
        average = (int) (sum / ((long) period * TICK_RATE));
    }

    /** {@code getClientAverage}: the last computed average, computed now if there is none yet. */
    public int getAverage() {
        if (average == -1) {
            computeAverage();
        }
        return average;
    }

    public int getPeriod() {
        return period;
    }

    /** Buttons 1/3/5/10 s. */
    public void setPeriod(int period) {
        if (period == 1 || period == 3 || period == 5 || period == 10) {
            this.period = period;
            setChanged();
        }
    }

    /** Event 0 (no button in the GUI, kept): clears the buffer; the shown average stays until the next point. */
    public void reset() {
        java.util.Arrays.fill(data, 0L);
        updateTicker = TICK_RATE;
        index = 0;
        setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return CounterMenu.average(id, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        index = tag.getInt("dataIndex");
        updateTicker = tag.getInt("updateTicker");
        period = tag.contains("period") ? tag.getInt("period") : 1;
        for (int i = 0; i < POINTS; i++) {
            data[i] = tag.getLong("point-" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("dataIndex", index);
        tag.putInt("updateTicker", updateTicker);
        tag.putInt("period", period);
        for (int i = 0; i < POINTS; i++) {
            tag.putLong("point-" + i, data[i]);
        }
    }
}
