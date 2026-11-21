package dev.craft.nuclearcontrol.menu;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.AverageCounterBlockEntity;
import dev.craft.nuclearcontrol.block.CounterBlockEntity;
import dev.craft.nuclearcontrol.block.EnergyCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * {@code ContainerEnergyCounter}/{@code ContainerAverageCounter}: transformer upgrade slot at 8, 18, player inventory
 * from y 84. Synced: kind (0 energy, 1 average), the counter reading as two ints, the average and the period.
 */
public class CounterMenu extends NcMenu {
    private final SyncedInts data;

    public static CounterMenu energy(int id, Inventory inventory, EnergyCounterBlockEntity counter) {
        return new CounterMenu(id, inventory, counter, new SyncedInts(() -> 0, () -> (int) counter.getCounter(),
                () -> (int) (counter.getCounter() >>> 32), () -> 0, () -> 0));
    }

    public static CounterMenu average(int id, Inventory inventory, AverageCounterBlockEntity counter) {
        return new CounterMenu(id, inventory, counter, new SyncedInts(() -> 1, () -> 0, () -> 0,
                counter::getAverage, counter::getPeriod));
    }

    private CounterMenu(int id, Inventory inventory, CounterBlockEntity counter, SyncedInts data) {
        this(id, inventory, counter.getBlockPos(), counter.getInventory(), data);
    }

    /** Client. */
    public CounterMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.is(Ic2Items.TRANSFORMER_UPGRADE.get());
            }
        }, new SyncedInts(5));
    }

    private CounterMenu(int id, Inventory inventory, BlockPos pos, IItemHandler items, SyncedInts data) {
        super(NcMenus.COUNTER.get(), id, pos);
        this.data = data;
        addSlot(new SlotItemHandler(items, 0, 8, 18));
        addPlayerInventory(inventory, 84);
        addDataSlots(data);
    }

    public boolean isAverage() {
        return data.getInt(0) == 1;
    }

    public long getCounter() {
        return data.getInt(1) & 0xFFFFFFFFL | (long) data.getInt(2) << 32;
    }

    public int getAverage() {
        return data.getInt(3);
    }

    public int getPeriod() {
        return data.getInt(4);
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof CounterBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveOwn(1, index);
    }
}
