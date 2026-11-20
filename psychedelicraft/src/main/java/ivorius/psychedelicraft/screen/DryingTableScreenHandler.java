package ivorius.psychedelicraft.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
* Created by lukas on 08.11.14.
*/
public class DryingTableScreenHandler extends AbstractContainerMenu {
    private final Container inventory;

    private final ContainerData properties;

    public DryingTableScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, new SimpleContainerData(3), new SimpleContainer(10));
    }

    public DryingTableScreenHandler(int syncId, Inventory inventory, ContainerData properties, Container container) {
        super(PSScreenHandlers.DRYING_TABLE, syncId);
        this.inventory = inventory;
        this.properties = properties;
        checkContainerDataCount(properties, 3);
        container.startOpen(inventory.player);

        addSlot(new SlotDryingTableResult(inventory.player, container, 0, 124, 35));

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                addSlot(new Slot(container, 1 + x * 3 + y, 30 + x * 18, 17 + y * 18) {
                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(properties);
    }

    public float getHeatRatio() {
        return properties.get(0) / 1000F;
    }

    public float getProgress() {
        return getCookingTime() == 0 ? 0 : (float)properties.get(1) / getCookingTime();
    }

    public int getCookingTime() {
        return properties.get(2);
    }

    public int getTimeRemaining() {
        float progress = properties.get(1);
        float timeRemaining = getCookingTime() - progress;
        return (int)(timeRemaining / getHeatRatio());
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            originalStack = stack.copy();

            if (index < 10) {
                if (!moveItemStackTo(stack, 10, 46, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(stack, originalStack);
            } else if (index >= 10 && index < 37) {
                if (!moveItemStackTo(stack, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 37 && index < 46) {
                if (!moveItemStackTo(stack, 10, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 10, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == originalStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, originalStack);
        }

        return originalStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }
}
