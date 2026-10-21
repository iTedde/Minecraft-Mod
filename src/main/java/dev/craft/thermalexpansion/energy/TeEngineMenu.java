package dev.craft.thermalexpansion.energy;

import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.thermalexpansion.TeMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code ContainerEngineSteam}/{@code ContainerEngineMagmatic}: the fuel slot of the steam engine (the magmatic
 * engine's slot holds nothing and is left out); buttons 0–2 set the redstone mode.
 */
public class TeEngineMenu extends AbstractContainerMenu {
    private static final int ENERGY = 0;
    private static final int POWER = 1;
    private static final int DURATION = 2;
    private static final int TANK = 3;

    @Nullable
    private final TeEngineBlockEntity engine;
    private final TeEngineType type;
    private final BlockPos pos;
    private final SyncedInts data;
    private final int engineSlots;

    public TeEngineMenu(int id, Inventory inventory, TeEngineBlockEntity engine) {
        this(id, inventory, engine, engine.getEngineType(), engine.getBlockPos(), new SyncedInts(
                () -> Math.round(engine.getEnergy() * 100),
                () -> Math.round(engine.getPower() * 100),
                () -> Math.round(engine.getDurationLevel() * 1000),
                () -> engine.getTankFluid().getAmount()));
    }

    public TeEngineMenu(int id, Inventory inventory, TeEngineType type, BlockPos pos) {
        this(id, inventory, null, type, pos, new SyncedInts(4));
    }

    private TeEngineMenu(int id, Inventory inventory, @Nullable TeEngineBlockEntity engine, TeEngineType type,
                         BlockPos pos, SyncedInts data) {
        super(TeMenus.ENGINE.get(), id);
        this.engine = engine;
        this.type = type;
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
        if (type == TeEngineType.STEAM) {
            addSlot(new MachineSlot(engine != null ? engine.getInventory() : new ItemStackHandler(1), 0, 44, 35));
        }
        engineSlots = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public TeEngineType getEngineType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public float getEnergy() {
        return data.getInt(ENERGY) / 100.0F;
    }

    public float getPower() {
        return data.getInt(POWER) / 100.0F;
    }

    public float getDurationLevel() {
        return data.getInt(DURATION) / 1000.0F;
    }

    public int getTankAmount() {
        return data.getInt(TANK);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (engine == null || id < 0 || id > 2) {
            return false;
        }
        engine.setRedstone(id == 0, id == 2);
        return true;
    }

    /** {@code transferStackInSlot}: engine → player; player inventory ↔ engine slot, hotbar → inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = engineSlots;
        int hotbarStart = playerStart + 27;
        int end = playerStart + 36;
        if (index < engineSlots) {
            if (!moveItemStackTo(stack, playerStart, end, false)) {
                return ItemStack.EMPTY;
            }
        } else if (engineSlots > 0 && SteamEngineBlockEntity.getItemEnergyValue(stack) > 0) {
            if (!moveItemStackTo(stack, 0, engineSlots, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < hotbarStart) {
            if (!moveItemStackTo(stack, hotbarStart, end, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, playerStart, hotbarStart, false)) {
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
        return engine == null || engine.stillValid(player);
    }
}
