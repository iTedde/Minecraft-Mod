package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.PumpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerPump}: container slot (empty cells only) and battery; syncs charge and energy. */
public class PumpMenu extends MachineMenu<PumpBlockEntity> {
    private static final int CHARGE = 0;
    private static final int ENERGY = 1;

    public PumpMenu(int id, Inventory inventory, PumpBlockEntity pump) {
        this(id, inventory, pump, pump.getBlockPos(), new SyncedInts(pump::getPumpCharge, pump::getEnergy));
    }

    public PumpMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private PumpMenu(int id, Inventory inventory, @Nullable PumpBlockEntity pump, BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.PUMP), id, pump, pos, data);
        IItemHandler items = items(pump, 2);
        // IC2 SlotCustom with Ic2Items.cell: buckets only get in by automation.
        addMachineSlot(new FilteredSlot(items, PumpBlockEntity.SLOT_CONTAINER, 62, 17,
                stack -> stack.is(Ic2Items.EMPTY_CELL.get())));
        addMachineSlot(new FilteredSlot(items, PumpBlockEntity.SLOT_BATTERY, 62, 53,
                SlotFilters.discharge(PumpBlockEntity.TIER)));
        addPlayerInventory(inventory);
    }

    public int getCharge() {
        return data.getInt(CHARGE);
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    /** IC2 {@code getInput} returns 1 (the battery slot). */
    @Override
    protected int getInputSlot() {
        return PumpBlockEntity.SLOT_BATTERY;
    }
}
