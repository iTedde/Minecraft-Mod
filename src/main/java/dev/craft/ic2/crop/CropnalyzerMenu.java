package dev.craft.ic2.crop;

import java.util.List;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.menu.FilteredSlot;
import dev.craft.ic2.menu.SlotFilters;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Cropnalyzer GUI (IC2 {@code ContainerCropnalyzer} + {@code HandHeldCropnalyzer}): seed input, scanned output and a
 * discharge slot above the hotbar. The three slots live in the held item's container component. While the GUI is open
 * the server scans every tick: a fully scanned bag moves straight to the output, otherwise the next level costs
 * 10/90/900/9000 EU from an electric item (tier 2, transfer limit ignored). IC2 guards against moving the open
 * cropnalyzer with a random uid; here its hotbar slot is simply locked.
 */
public class CropnalyzerMenu extends AbstractContainerMenu {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BATTERY = 2;
    private static final int SIZE = 3;

    private final Player player;
    private final int heldSlot;
    private final ItemStackHandler items;

    public CropnalyzerMenu(int id, Inventory inventory, int heldSlot) {
        super(Ic2Menus.CROPNALYZER.get(), id);
        this.player = inventory.player;
        this.heldSlot = heldSlot;
        this.items = new ItemStackHandler(SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                save();
            }
        };
        if (!player.level().isClientSide()) {
            NonNullList<ItemStack> stored = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            getCropnalyzer().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(stored);
            for (int slot = 0; slot < SIZE; slot++) {
                items.setStackInSlot(slot, stored.get(slot));
            }
        }
        addSlot(new FilteredSlot(items, SLOT_INPUT, 8, 7, stack -> stack.getItem() instanceof CropSeedItem));
        addSlot(new FilteredSlot(items, SLOT_OUTPUT, 41, 7, SlotFilters.NONE));
        addSlot(new FilteredSlot(items, SLOT_BATTERY, 152, 7, SlotFilters.discharge(Integer.MAX_VALUE)));
        for (int column = 0; column < 9; column++) {
            if (column == heldSlot) {
                addSlot(new Slot(inventory, column, 8 + column * 18, 142) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            } else {
                addSlot(new Slot(inventory, column, 8 + column * 18, 142));
            }
        }
    }

    private ItemStack getCropnalyzer() {
        return player.getInventory().getItem(heldSlot);
    }

    private void save() {
        ItemStack cropnalyzer = getCropnalyzer();
        if (!player.level().isClientSide() && cropnalyzer.is(Ic2Items.CROPNALYZER.get())) {
            cropnalyzer.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(
                    items.getStackInSlot(0), items.getStackInSlot(1), items.getStackInSlot(2))));
        }
    }

    /** The scanned bag shown on the screen (synced through the output slot). */
    public ItemStack getScannedStack() {
        return slots.get(SLOT_OUTPUT).getItem();
    }

    /** IC2 {@code tickCallback}, run from the server's per-tick menu sync. */
    void scan() {
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (!items.getStackInSlot(SLOT_OUTPUT).isEmpty() || !input.is(Ic2Items.CROP_SEED_BAG.get())) {
            return;
        }
        int level = CropSeedItem.getSeed(input).scan();
        if (level != 4) {
            ItemStack battery = items.getStackInSlot(SLOT_BATTERY);
            if (!(battery.getItem() instanceof ElectricItem)) {
                return;
            }
            int need = energyForLevel(level);
            if (ElectricItems.discharge(battery, need, 2, true, true) < need) {
                return;
            }
            ElectricItems.discharge(battery, need, 2, true, false);
            input = input.copy();
            CropSeedItem.incrementScan(input);
            items.setStackInSlot(SLOT_BATTERY, battery.copy());
        }
        items.setStackInSlot(SLOT_OUTPUT, input);
        items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
    }

    public static int energyForLevel(int level) {
        return switch (level) {
            case 1 -> 90;
            case 2 -> 900;
            case 3 -> 9000;
            default -> 10;
        };
    }

    @Override
    public void broadcastChanges() {
        if (!player.level().isClientSide()) {
            scan();
        }
        super.broadcastChanges();
    }

    /** IC2 {@code ContainerIC2} shift click: GUI slots to the hotbar; hotbar to the input first, then other slots. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        int count = stack.getCount();
        if (index < SIZE) {
            moveItemStackTo(stack, SIZE, slots.size(), false);
        } else {
            if (slots.get(SLOT_INPUT).mayPlace(stack)) {
                moveItemStackTo(stack, SLOT_INPUT, SLOT_INPUT + 1, false);
            }
            if (!stack.isEmpty() && slots.get(SLOT_BATTERY).mayPlace(stack)) {
                moveItemStackTo(stack, SLOT_BATTERY, SLOT_BATTERY + 1, false);
            }
        }
        if (stack.getCount() == count) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        source.onTake(player, stack);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return getCropnalyzer().is(Ic2Items.CROPNALYZER.get());
    }
}
