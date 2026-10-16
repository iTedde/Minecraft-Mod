package dev.craft.gregtech.menu;

import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.GtMenus;
import dev.craft.gregtech.machine.GtTankMachineBlockEntity;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_Container_Electrolyzer} / {@code GT_Container_Centrifuge} (spec 13 §4, §9): two inputs, four outputs,
 * the tank holo slot, progress scale and the error code. Only the slot positions differ.
 */
public class GtTankMachineMenu extends MachineMenu<GtTankMachineBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int ERROR = 1;
    private static final int MACHINE_SLOTS = 7;
    /** Slot positions 0–6 (x, y) of the electrolyzer. */
    private static final int[][] ELECTROLYZER_SLOTS = {{80, 46}, {50, 46}, {50, 16}, {70, 16}, {90, 16}, {110, 16},
            {110, 46}};
    /** Slot positions 0–6 (x, y) of the centrifuge: outputs around the input, cells top left. */
    private static final int[][] CENTRIFUGE_SLOTS = {{80, 35}, {50, 5}, {80, 5}, {110, 35}, {80, 65}, {50, 35},
            {110, 65}};

    public GtTankMachineMenu(int id, Inventory inventory, GtTankMachineBlockEntity machine) {
        this(id, inventory, machine, machine.getBlockPos(), machine.getGtMachine(),
                new SyncedInts(machine::getProgressScale, machine::getDisplayErrorCode));
    }

    /** Client side. */
    public GtTankMachineMenu(int id, Inventory inventory, BlockPos pos, GtMachine type) {
        this(id, inventory, null, pos, type, new SyncedInts(2));
    }

    private GtTankMachineMenu(int id, Inventory inventory, @Nullable GtTankMachineBlockEntity machine, BlockPos pos,
                              GtMachine type, SyncedInts data) {
        super(menuType(type), id, machine, pos, data);
        int[][] positions = type == GtMachine.INDUSTRIAL_CENTRIFUGE ? CENTRIFUGE_SLOTS : ELECTROLYZER_SLOTS;
        IItemHandler items = items(machine, MACHINE_SLOTS);
        addMachineSlot(new MachineSlot(items, GtTankMachineBlockEntity.SLOT_INPUT, positions[0][0], positions[0][1]));
        addMachineSlot(new MachineSlot(items, GtTankMachineBlockEntity.SLOT_CELLS, positions[1][0], positions[1][1]));
        for (int i = 0; i < GtTankMachineBlockEntity.OUTPUTS; i++) {
            int slot = GtTankMachineBlockEntity.SLOT_OUTPUT_1 + i;
            addMachineSlot(new OutputSlot(items, slot, positions[slot][0], positions[slot][1], inventory.player,
                    false));
        }
        int tank = GtTankMachineBlockEntity.SLOT_TANK;
        addMachineSlot(new HoloSlot(items, tank, positions[tank][0], positions[tank][1]));
        addPlayerInventory(inventory);
    }

    private static MenuType<GtTankMachineMenu> menuType(GtMachine type) {
        return type == GtMachine.INDUSTRIAL_CENTRIFUGE ? GtMenus.CENTRIFUGE.get() : GtMenus.ELECTROLYZER.get();
    }

    /** Progress scale 0–10. */
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

    /** {@code GT_Slot_Holo}: shows the tank contents, cannot be taken or filled. */
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
