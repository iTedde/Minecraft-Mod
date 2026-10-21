package dev.craft.thermalexpansion.energy;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.thermalexpansion.TeMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * {@code ContainerEnergyCell}: only the player inventory. Buttons 0–2 set the redstone mode; {@link #BUTTON_LIMIT} +
 * 4 · (2 · limit + direction) + step changes receive (limit 0) or send (limit 1) down (0) or up (1) by
 * {@link #STEPS}[step].
 */
public class EnergyCellMenu extends AbstractContainerMenu {
    public static final int BUTTON_LIMIT = 10;
    /** {@code GuiEnergyCell}: click 5, right click 1; with shift 100 and 10. */
    public static final int[] STEPS = {1, 5, 10, 100};
    private static final int ENERGY = 0;
    private static final int SEND = 1;
    private static final int RECEIVE = 2;

    @Nullable
    private final EnergyCellBlockEntity cell;
    private final BlockPos pos;
    private final SyncedInts data;

    public EnergyCellMenu(int id, Inventory inventory, EnergyCellBlockEntity cell) {
        this(id, inventory, cell, cell.getBlockPos(), new SyncedInts(() -> Math.round(cell.getEnergy()),
                cell::getEnergySend, cell::getEnergyReceive));
    }

    public EnergyCellMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private EnergyCellMenu(int id, Inventory inventory, @Nullable EnergyCellBlockEntity cell, BlockPos pos,
                           SyncedInts data) {
        super(TeMenus.ENERGY_CELL.get(), id);
        this.cell = cell;
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    public int getSend() {
        return data.getInt(SEND);
    }

    public int getReceive() {
        return data.getInt(RECEIVE);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (cell == null) {
            return false;
        }
        if (id >= 0 && id <= 2) {
            cell.setRedstone(id == 0, id == 2);
            return true;
        }
        int index = id - BUTTON_LIMIT;
        if (index < 0 || index >= 16) {
            return false;
        }
        int step = STEPS[index % 4];
        int change = index / 4 % 2 == 0 ? -step : step;
        if (index / 8 == 0) {
            cell.setLimits(cell.getEnergySend(), cell.getEnergyReceive() + change);
        } else {
            cell.setLimits(cell.getEnergySend() + change, cell.getEnergyReceive());
        }
        return true;
    }

    /** {@code transferStackInSlot}: inventory ↔ hotbar. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 27 ? !moveItemStackTo(stack, 27, 36, false) : !moveItemStackTo(stack, 0, 27, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return cell == null || cell.stillValid(player);
    }
}
