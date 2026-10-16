package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ScannerItem;
import dev.craft.ic2.machine.MinerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerMiner}: battery, scanner, pipe (any block) and drill slot; syncs mining ticker and energy. */
public class MinerMenu extends MachineMenu<MinerBlockEntity> {
    private static final int TICKER = 0;
    private static final int ENERGY = 1;

    public MinerMenu(int id, Inventory inventory, MinerBlockEntity miner) {
        this(id, inventory, miner, miner.getBlockPos(), new SyncedInts(miner::getMiningTicker, miner::getEnergy));
    }

    public MinerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private MinerMenu(int id, Inventory inventory, @Nullable MinerBlockEntity miner, BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.MINER), id, miner, pos, data);
        IItemHandler items = items(miner, 4);
        addMachineSlot(new FilteredSlot(items, MinerBlockEntity.SLOT_BATTERY, 81, 59,
                SlotFilters.discharge(MinerBlockEntity.TIER)));
        addMachineSlot(new FilteredSlot(items, MinerBlockEntity.SLOT_SCANNER, 117, 22,
                stack -> stack.getItem() instanceof ScannerItem));
        addMachineSlot(new FilteredSlot(items, MinerBlockEntity.SLOT_PIPE, 81, 22,
                stack -> stack.getItem() instanceof BlockItem));
        addMachineSlot(new FilteredSlot(items, MinerBlockEntity.SLOT_DRILL, 45, 22,
                stack -> stack.getItem() instanceof DrillItem));
        addPlayerInventory(inventory);
    }

    public int getMiningTicker() {
        return data.getInt(TICKER);
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    /** IC2 {@code getInput} returns 0 (the battery slot). */
    @Override
    protected int getInputSlot() {
        return MinerBlockEntity.SLOT_BATTERY;
    }
}
