package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.CanningMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerCanner}: input, battery, output and container slot; syncs progress, energy and the gauge length. */
public class CanningMachineMenu extends MachineMenu<CanningMachineBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int ENERGY = 1;
    private static final int LENGTH = 2;

    public CanningMachineMenu(int id, Inventory inventory, CanningMachineBlockEntity canner) {
        this(id, inventory, canner, canner.getBlockPos(), new SyncedInts(canner::getProgress, canner::getEnergy,
                () -> getGaugeLength(canner)));
    }

    public CanningMachineMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private CanningMachineMenu(int id, Inventory inventory, @Nullable CanningMachineBlockEntity canner, BlockPos pos,
                               SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.CANNING_MACHINE), id, canner, pos, data);
        IItemHandler items = items(canner, 4);
        addMachineSlot(new MachineSlot(items, CanningMachineBlockEntity.SLOT_INPUT, 69, 17));
        addMachineSlot(new FilteredSlot(items, CanningMachineBlockEntity.SLOT_BATTERY, 30, 45,
                SlotFilters.discharge(CanningMachineBlockEntity.TIER)));
        addMachineSlot(new OutputSlot(items, CanningMachineBlockEntity.SLOT_OUTPUT, 119, 35, inventory.player,
                false));
        addMachineSlot(new MachineSlot(items, CanningMachineBlockEntity.SLOT_CONTAINER, 69, 53));
        addPlayerInventory(inventory);
    }

    /** {@code gaugeProgressScaled}: the divisor is 50 per food value in food mode, 50 for CF packs, else 600. */
    private static int getGaugeLength(CanningMachineBlockEntity canner) {
        if (canner.getMode() == CanningMachineBlockEntity.MODE_FOOD) {
            int food = CanningMachineBlockEntity.getFoodValue(
                    canner.getInventory().getStackInSlot(CanningMachineBlockEntity.SLOT_INPUT));
            if (food > 0) {
                return 50 * food;
            }
        }
        if (canner.getMode() == CanningMachineBlockEntity.MODE_CF_PACK) {
            return CanningMachineBlockEntity.CF_PACK_LENGTH;
        }
        return CanningMachineBlockEntity.OPERATION_LENGTH;
    }

    public int getProgressScaled(int width) {
        return width * data.getInt(PROGRESS) / Math.max(1, data.getInt(LENGTH));
    }

    /** {@code gaugeFuelScaled}: relative to one full operation (600 EU), capped. */
    public int getEnergyScaled(int height) {
        return Math.min(height, data.getInt(ENERGY) * height
                / (CanningMachineBlockEntity.OPERATION_LENGTH * CanningMachineBlockEntity.ENERGY_PER_TICK));
    }

    public boolean hasEnergy() {
        return data.getInt(ENERGY) > 0;
    }

    /** IC2 {@code getInput} = {@code firstEmptyFrom(0, 2)}: first empty of slots 0–2 (the output accepts nothing). */
    @Override
    protected int getInputSlot() {
        for (int slot = CanningMachineBlockEntity.SLOT_INPUT; slot <= CanningMachineBlockEntity.SLOT_OUTPUT; slot++) {
            if (!slots.get(slot).hasItem()) {
                return slot;
            }
        }
        return -1;
    }
}
