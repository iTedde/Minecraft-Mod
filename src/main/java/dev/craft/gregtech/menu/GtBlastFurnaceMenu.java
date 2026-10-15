package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.BlastFurnaceBlockEntity;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_Container_BlastFurnace} (spec 13 §5): two inputs, two outputs, progress arrow, structure state, heat
 * capacity and the error code.
 */
public class GtBlastFurnaceMenu extends MachineMenu<BlastFurnaceBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int MACHINE = 1;
    private static final int HEAT = 2;
    private static final int ERROR = 3;
    private static final int MACHINE_SLOTS = 4;

    public GtBlastFurnaceMenu(int id, Inventory inventory, BlastFurnaceBlockEntity furnace) {
        this(id, inventory, furnace, furnace.getBlockPos(), new SyncedInts(furnace::getProgressScale,
                () -> furnace.isMachine() ? 1 : 0, furnace::getHeatCapacity, furnace::getDisplayErrorCode));
    }

    public GtBlastFurnaceMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(4));
    }

    private GtBlastFurnaceMenu(int id, Inventory inventory, @Nullable BlastFurnaceBlockEntity furnace, BlockPos pos,
                               SyncedInts data) {
        super(GtMenus.BLAST_FURNACE.get(), id, furnace, pos, data);
        IItemHandler items = items(furnace, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, BlastFurnaceBlockEntity.SLOT_INPUT_1, 34, 16));
        addMachineSlot(new MachineSlot(items, BlastFurnaceBlockEntity.SLOT_INPUT_2, 34, 34));
        addMachineSlot(new OutputSlot(items, BlastFurnaceBlockEntity.SLOT_OUTPUT_1, 86, 25, inventory.player, false));
        addMachineSlot(new OutputSlot(items, BlastFurnaceBlockEntity.SLOT_OUTPUT_2, 104, 25, inventory.player, false));
        addPlayerInventory(inventory);
    }

    /** Progress arrow width 0–20. */
    public int getProgressScale() {
        return Math.min(20, data.getInt(PROGRESS));
    }

    public boolean isMachine() {
        return data.getInt(MACHINE) != 0;
    }

    public int getHeatCapacity() {
        return data.getInt(HEAT);
    }

    /** {@code mDisplayErrorCode} bit 1: insufficient energy. */
    public boolean isMissingEnergy() {
        return (data.getInt(ERROR) & 1) != 0;
    }

    /** {@code GT_Container.transferStackInSlot}: machine → player inventory (from the back), player → the two inputs. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        int before = stack.getCount();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        if (stack.getCount() != before) {
            source.onTake(player, stack);
        }
        return ItemStack.EMPTY;
    }
}
