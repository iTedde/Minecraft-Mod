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

/** Energy counter ({@code TileEntityEnergyCounter}, spec 46 §7.1): counts the EU that actually left. GUI: reset. */
public class EnergyCounterBlockEntity extends CounterBlockEntity {
    private long counter;

    public EnergyCounterBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.ENERGY_COUNTER.get(), pos, state);
    }

    @Override
    protected void onSent(int amount) {
        counter += amount;
    }

    public long getCounter() {
        return counter;
    }

    /** {@code onNetworkEvent}: the only GUI button. */
    public void reset() {
        counter = 0L;
        setChanged();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return CounterMenu.energy(id, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        counter = tag.getLong("counter");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("counter", counter);
    }
}
