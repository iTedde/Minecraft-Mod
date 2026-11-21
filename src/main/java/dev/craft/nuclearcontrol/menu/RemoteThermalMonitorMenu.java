package dev.craft.nuclearcontrol.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * {@code ContainerRemoteThermo}: battery slot left, card and three upgrade slots right, player inventory shifted to
 * x 27; energy and storage are synced. Shift-click from the player puts a stack into the first empty valid slot.
 */
public class RemoteThermalMonitorMenu extends NcMenu {
    private static final int OWN_SLOTS = 5;

    private final SyncedInts data;

    /** Server. */
    public RemoteThermalMonitorMenu(int id, Inventory inventory, RemoteThermalMonitorBlockEntity monitor) {
        this(id, inventory, monitor.getBlockPos(), monitor.getInventory(),
                new SyncedInts(monitor::getEnergy, monitor::getMaxStorage));
    }

    /** Client. */
    public RemoteThermalMonitorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, new ItemStackHandler(OWN_SLOTS), new SyncedInts(2));
    }

    private RemoteThermalMonitorMenu(int id, Inventory inventory, BlockPos pos, IItemHandler items,
                                     SyncedInts data) {
        super(NcMenus.REMOTE_THERMAL_MONITOR.get(), id, pos);
        this.data = data;
        addSlot(new SlotItemHandler(items, RemoteThermalMonitorBlockEntity.SLOT_BATTERY, 13, 53));
        addSlot(new SlotItemHandler(items, RemoteThermalMonitorBlockEntity.SLOT_CARD, 190, 8));
        for (int i = 0; i < 3; i++) {
            addSlot(new SlotItemHandler(items, 2 + i, 190, 26 + i * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 27 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 27 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public int getEnergy() {
        return data.getInt(0);
    }

    public int getMaxStorage() {
        return data.getInt(1);
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof RemoteThermalMonitorBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        if (index < OWN_SLOTS) {
            ItemStack original = stack.copy();
            moveItemStackTo(stack, OWN_SLOTS, slots.size(), false);
            source.setChanged();
            return stack.getCount() != original.getCount() && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
        }
        for (int target = 0; target < OWN_SLOTS; target++) {
            Slot slot = slots.get(target);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                slot.setByPlayer(stack.copy());
                source.setByPlayer(ItemStack.EMPTY);
                break;
            }
        }
        return ItemStack.EMPTY;
    }
}
