package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.ElectrolyzerBlockEntity;
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
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_Container_Electrolyzer} (spec 13 §4): two inputs, four outputs, the water holo slot, progress bar and
 * the error code.
 */
public class GtElectrolyzerMenu extends MachineMenu<ElectrolyzerBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int ERROR = 1;
    private static final int MACHINE_SLOTS = 7;

    public GtElectrolyzerMenu(int id, Inventory inventory, ElectrolyzerBlockEntity machine) {
        this(id, inventory, machine, machine.getBlockPos(), new SyncedInts(machine::getProgressScale,
                machine::getDisplayErrorCode));
    }

    public GtElectrolyzerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private GtElectrolyzerMenu(int id, Inventory inventory, @Nullable ElectrolyzerBlockEntity machine, BlockPos pos,
                               SyncedInts data) {
        super(GtMenus.ELECTROLYZER.get(), id, machine, pos, data);
        IItemHandler items = items(machine, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, ElectrolyzerBlockEntity.SLOT_INPUT, 80, 46));
        addMachineSlot(new MachineSlot(items, ElectrolyzerBlockEntity.SLOT_CELLS, 50, 46));
        for (int i = 0; i < ElectrolyzerBlockEntity.OUTPUTS; i++) {
            addMachineSlot(new OutputSlot(items, ElectrolyzerBlockEntity.SLOT_OUTPUT_1 + i, 50 + i * 20, 16,
                    inventory.player, false));
        }
        addMachineSlot(new HoloSlot(items, ElectrolyzerBlockEntity.SLOT_WATER, 110, 46));
        addPlayerInventory(inventory);
    }

    /** Progress bar height 0–10. */
    public int getProgressScale() {
        return Math.min(10, data.getInt(PROGRESS));
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

    /** {@code GT_Slot_Holo}: shows the tank water, cannot be taken or filled. */
    private static final class HoloSlot extends SlotItemHandler {
        HoloSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
