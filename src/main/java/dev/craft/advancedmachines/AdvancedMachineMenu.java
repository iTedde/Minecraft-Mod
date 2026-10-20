package dev.craft.advancedmachines;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code ContainerRotaryMacerator}, {@code ContainerSingularityCompressor}, {@code ContainerCentrifugeExtractor}:
 * plain slots for energy, input and upgrades (no filters, as in the original), furnace-like output slots.
 */
public class AdvancedMachineMenu extends MachineMenu<AdvancedMachineBlockEntity> {
    public static final int SCALE = 32767;
    private static final int CHARGE = 0;
    private static final int PROGRESS = 1;
    private static final int SPEED = 2;
    /** Output slot positions per machine (spec 25 §4). */
    private static final int[][] OUTPUT_POSITIONS = {{115, 25, 115, 46}, {115, 35}, {115, 17, 115, 35, 115, 53}};

    private final AdvancedMachineType type;

    public AdvancedMachineMenu(int id, Inventory inventory, AdvancedMachineBlockEntity machine) {
        this(machine.getMachineType(), id, inventory, machine, machine.getBlockPos(), new SyncedInts(
                () -> (int) Math.min(SCALE, (long) Math.max(0, machine.getEnergy()) * SCALE / machine.getMaxEnergy()),
                machine::getProgress,
                machine::getSpeed));
    }

    public AdvancedMachineMenu(Ic2Machine machine, int id, Inventory inventory, BlockPos pos) {
        this(AdvancedMachineType.of(machine), id, inventory, null, pos, new SyncedInts(3));
    }

    private AdvancedMachineMenu(AdvancedMachineType type, int id, Inventory inventory,
                                @Nullable AdvancedMachineBlockEntity machine, BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(type.getMachine()), id, machine, pos, data);
        this.type = type;
        IItemHandler items = items(machine, type.getSlotCount());
        addMachineSlot(new MachineSlot(items, AdvancedMachineType.SLOT_ENERGY, 56, 53));
        addMachineSlot(new MachineSlot(items, AdvancedMachineType.SLOT_INPUT, 56, 17));
        int[] outputs = type.getOutputs();
        int[] positions = OUTPUT_POSITIONS[type.ordinal()];
        for (int i = 0; i < outputs.length; i++) {
            addMachineSlot(new OutputSlot(items, outputs[i], positions[2 * i], positions[2 * i + 1], inventory.player,
                    false));
        }
        for (int i = 0; i < AdvancedMachineType.UPGRADE_SLOTS; i++) {
            addMachineSlot(new MachineSlot(items, type.getFirstUpgradeSlot() + i, 152, 6 + i * 18));
        }
        if (type == AdvancedMachineType.ROTARY_MACERATOR) {
            addMachineSlot(new MachineSlot(items, RotaryMaceratorBlockEntity.SLOT_SUPPLEMENT, 75, 17));
        }
        addPlayerInventory(inventory);
    }

    public AdvancedMachineType getMachineType() {
        return type;
    }

    public float getChargeLevel() {
        return data.getInt(CHARGE) / (float) SCALE;
    }

    public int getProgress() {
        return data.getInt(PROGRESS);
    }

    public int getSpeed() {
        return data.getInt(SPEED);
    }

    @Override
    protected int getInputSlot() {
        return AdvancedMachineType.SLOT_INPUT;
    }
}
