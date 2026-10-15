package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.InductionFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerInduction}: two inputs, battery, two furnace outputs; syncs progress, energy and heat. */
public class InductionFurnaceMenu extends MachineMenu<InductionFurnaceBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int ENERGY = 1;
    private static final int HEAT = 2;

    public InductionFurnaceMenu(int id, Inventory inventory, InductionFurnaceBlockEntity furnace) {
        this(id, inventory, furnace, furnace.getBlockPos(), new SyncedInts(furnace::getProgress,
                furnace::getEnergy, furnace::getHeat));
    }

    public InductionFurnaceMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private InductionFurnaceMenu(int id, Inventory inventory, @Nullable InductionFurnaceBlockEntity furnace,
                                 BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.INDUCTION_FURNACE), id, furnace, pos, data);
        IItemHandler items = items(furnace, 5);
        addMachineSlot(new MachineSlot(items, InductionFurnaceBlockEntity.SLOT_INPUT_1, 47, 17));
        addMachineSlot(new MachineSlot(items, InductionFurnaceBlockEntity.SLOT_INPUT_2, 63, 17));
        addMachineSlot(new FilteredSlot(items, InductionFurnaceBlockEntity.SLOT_BATTERY, 56, 53,
                SlotFilters.discharge(InductionFurnaceBlockEntity.TIER)));
        addMachineSlot(new OutputSlot(items, InductionFurnaceBlockEntity.SLOT_OUTPUT_1, 113, 35, inventory.player,
                true));
        addMachineSlot(new OutputSlot(items, InductionFurnaceBlockEntity.SLOT_OUTPUT_2, 131, 35, inventory.player,
                true));
        addPlayerInventory(inventory);
    }

    /** {@code gaugeProgressScaled}. */
    public int getProgressScaled(int width) {
        return width * data.getInt(PROGRESS) / InductionFurnaceBlockEntity.OPERATION_PROGRESS;
    }

    /** {@code gaugeFuelScaled}. */
    public int getEnergyScaled(int height) {
        return height * data.getInt(ENERGY) / InductionFurnaceBlockEntity.MAX_ENERGY;
    }

    public boolean hasEnergy() {
        return data.getInt(ENERGY) > 0;
    }

    public int getHeatPercent() {
        return data.getInt(HEAT) * 100 / InductionFurnaceBlockEntity.MAX_HEAT;
    }

    /** IC2 {@code getInput}: slot 0 only while it holds an item. */
    @Override
    protected int getInputSlot() {
        return slots.get(InductionFurnaceBlockEntity.SLOT_INPUT_1).hasItem() ? 1 : 0;
    }
}
