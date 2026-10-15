package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.ElectricMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Standard electric machine (IC2 {@code ContainerElectricMachine}): input, battery, output and four upgrade slots. */
public class ElectricMachineMenu extends MachineMenu<ElectricMachineBlockEntity> {
    /** IC2 syncs charge and progress as fractions times 32767. */
    public static final int SCALE = 32767;
    private static final int CHARGE = 0;
    private static final int PROGRESS = 1;

    private final Ic2Machine kind;

    public ElectricMachineMenu(int id, Inventory inventory, ElectricMachineBlockEntity machine) {
        this(machine.getMachine(), id, inventory, machine, machine.getBlockPos(), new SyncedInts(
                () -> (short) (machine.getChargeLevel() * SCALE),
                () -> (short) (machine.getProgressLevel() * SCALE)));
    }

    public ElectricMachineMenu(Ic2Machine kind, int id, Inventory inventory, BlockPos pos) {
        this(kind, id, inventory, null, pos, new SyncedInts(2));
    }

    private ElectricMachineMenu(Ic2Machine kind, int id, Inventory inventory,
                                @Nullable ElectricMachineBlockEntity machine, BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(kind), id, machine, pos, data);
        this.kind = kind;
        IItemHandler items = items(machine, 3 + ElectricMachineBlockEntity.UPGRADE_SLOTS);
        addMachineSlot(new MachineSlot(items, ElectricMachineBlockEntity.SLOT_INPUT, 56, 17));
        addMachineSlot(new FilteredSlot(items, ElectricMachineBlockEntity.SLOT_BATTERY, 56, 53,
                SlotFilters.discharge(Integer.MAX_VALUE)));
        addMachineSlot(new OutputSlot(items, ElectricMachineBlockEntity.SLOT_OUTPUT, 116, 35, inventory.player,
                kind == Ic2Machine.ELECTRIC_FURNACE));
        for (int i = 0; i < ElectricMachineBlockEntity.UPGRADE_SLOTS; i++) {
            addMachineSlot(new FilteredSlot(items, ElectricMachineBlockEntity.FIRST_UPGRADE_SLOT + i, 152, 8 + i * 18,
                    SlotFilters.UPGRADE));
        }
        addPlayerInventory(inventory);
    }

    public Ic2Machine getKind() {
        return kind;
    }

    public float getChargeLevel() {
        return data.getInt(CHARGE) / (float) SCALE;
    }

    public float getProgress() {
        return data.getInt(PROGRESS) / (float) SCALE;
    }

    @Override
    protected int getInputSlot() {
        return ElectricMachineBlockEntity.SLOT_INPUT;
    }
}
