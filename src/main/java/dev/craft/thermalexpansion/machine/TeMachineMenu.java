package dev.craft.thermalexpansion.machine;

import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.thermalexpansion.TeMenus;
import dev.craft.thermalexpansion.recipe.TeFluidRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Container of the Thermal Expansion machines ({@code ContainerFurnace}, {@code ContainerPulverizer} …). The GUI tabs
 * talk to the server through {@link #clickMenuButton}: redstone modes 0–2, side changes 10 + 2·side (+1 backwards),
 * 22 + side sets a side to group 0, 30 resets all sides, 40 toggles the transposer mode, 41–43 choose the product
 * of the precipitator and the extruder.
 */
public class TeMachineMenu extends AbstractContainerMenu {
    public static final int BUTTON_REDSTONE_IGNORED = 0;
    public static final int BUTTON_REDSTONE_LOW = 1;
    public static final int BUTTON_REDSTONE_HIGH = 2;
    public static final int BUTTON_SIDE = 10;
    public static final int BUTTON_CLEAR_SIDE = 22;
    public static final int BUTTON_RESET_SIDES = 30;
    public static final int BUTTON_MODE = 40;
    public static final int BUTTON_SELECTION = 41;
    /** Synced values ×100 where fractions matter (MJ and process progress are floats). */
    private static final int ACTIVE = 0;
    private static final int PROCESS_CUR = 1;
    private static final int PROCESS_END = 2;
    private static final int ENERGY = 3;
    private static final int SPEED = 4;
    private static final int POWER = 5;
    private static final int FLUID = 6;
    private static final int FLUID_AMOUNT = 7;
    /** {@link TeMachineBlockEntity#getModeBits()}. */
    private static final int MODE = 8;
    /** Second tank (extruder lava). */
    private static final int FLUID2_AMOUNT = 9;

    @Nullable
    private final TeMachineBlockEntity machine;
    private final TeMachineType type;
    private final BlockPos pos;
    private final SyncedInts data;
    private final int machineSlots;

    public TeMachineMenu(int id, Inventory inventory, TeMachineBlockEntity machine) {
        this(id, inventory, machine, machine.getMachineType(), machine.getBlockPos(), new SyncedInts(
                () -> machine.isActive() ? 1 : 0,
                () -> Math.round(machine.getProcessCur() * 100),
                () -> Math.round(machine.getProcessEnd() * 100),
                () -> Math.round(machine.getEnergy() * 100),
                () -> Math.round(machine.getSpeedLevel() * 1000),
                () -> Math.round(machine.getPower() * 100),
                () -> BuiltInRegistries.FLUID.getId(machine.getDisplayFluid(0).getFluid()),
                () -> machine.getDisplayFluid(0).getAmount(),
                machine::getModeBits,
                () -> machine.getDisplayFluid(1).getAmount()));
    }

    public TeMachineMenu(int id, Inventory inventory, TeMachineType type, BlockPos pos) {
        this(id, inventory, null, type, pos, new SyncedInts(10));
    }

    private TeMachineMenu(int id, Inventory inventory, @Nullable TeMachineBlockEntity machine, TeMachineType type,
                          BlockPos pos, SyncedInts data) {
        super(TeMenus.MACHINE.get(), id);
        this.machine = machine;
        this.type = type;
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
        IItemHandler items = machine != null ? machine.getInventory() : new ItemStackHandler(type.getSlots());
        Player player = inventory.player;
        switch (type) {
            case POWERED_FURNACE -> {
                addSlot(new MachineSlot(items, 0, 56, 26));
                addSlot(new OutputSlot(items, 1, 116, 35, player, false));
            }
            case PULVERIZER, SAWMILL -> {
                addSlot(new MachineSlot(items, 0, 56, 26));
                addSlot(new OutputSlot(items, 1, 116, 26, player, false));
                addSlot(new OutputSlot(items, 2, 134, 26, player, false));
                addSlot(new OutputSlot(items, 3, 116, 53, player, false));
            }
            case INDUCTION_SMELTER -> {
                addSlot(new MachineSlot(items, 0, 32, 26));
                addSlot(new MachineSlot(items, 1, 56, 26));
                addSlot(new OutputSlot(items, 2, 116, 26, player, false));
                addSlot(new OutputSlot(items, 3, 134, 26, player, false));
                addSlot(new OutputSlot(items, 4, 116, 53, player, false));
            }
            case MAGMA_CRUCIBLE -> addSlot(new MachineSlot(items, 0, 56, 26));
            case LIQUID_TRANSPOSER -> {
                addSlot(new MachineSlot(items, 0, 80, 19));
                addSlot(new OutputSlot(items, 1, 80, 49, player, false));
            }
            // ContainerIceGen/ContainerRockGen: a plain slot; the three products are drawn by the screen.
            case GLACIAL_PRECIPITATOR, IGNEOUS_EXTRUDER -> addSlot(new MachineSlot(items, 0, 80, 49));
            case AQUEOUS_ACCUMULATOR -> {
            }
        }
        machineSlots = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public TeMachineType getMachineType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isActive() {
        return data.getInt(ACTIVE) != 0;
    }

    public float getProgressLevel() {
        int end = data.getInt(PROCESS_END);
        return end == 0 ? 0.0F : (float) data.getInt(PROCESS_CUR) / end;
    }

    public float getEnergy() {
        return data.getInt(ENERGY) / 100.0F;
    }

    public float getSpeedLevel() {
        return data.getInt(SPEED) / 1000.0F;
    }

    public float getPower() {
        return data.getInt(POWER) / 100.0F;
    }

    /** Tank of the crucible and the transposer. */
    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(data.getInt(FLUID));
    }

    public int getFluidAmount() {
        return data.getInt(FLUID_AMOUNT);
    }

    public boolean isReverse() {
        return (data.getInt(MODE) & 1) != 0;
    }

    public boolean getReverseFlag() {
        return (data.getInt(MODE) & 2) != 0;
    }

    public int getCurSelection() {
        return data.getInt(MODE) & 3;
    }

    public int getPrevSelection() {
        return data.getInt(MODE) >> 2 & 3;
    }

    /** Extruder lava tank. */
    public int getFluid2Amount() {
        return data.getInt(FLUID2_AMOUNT);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (machine == null) {
            return false;
        }
        switch (id) {
            case BUTTON_REDSTONE_IGNORED -> machine.setRedstone(true, false);
            case BUTTON_REDSTONE_LOW -> machine.setRedstone(false, false);
            case BUTTON_REDSTONE_HIGH -> machine.setRedstone(false, true);
            case BUTTON_RESET_SIDES -> machine.resetSides();
            case BUTTON_MODE -> {
                if (!(machine instanceof TransposerBlockEntity transposer)
                        || transposer.isReverse() != transposer.getReverseFlag()) {
                    return false;
                }
                transposer.setMode(!transposer.isReverse());
            }
            case BUTTON_SELECTION, BUTTON_SELECTION + 1, BUTTON_SELECTION + 2 -> {
                if (!(machine instanceof SelectionMachineBlockEntity selection)) {
                    return false;
                }
                selection.setSelection(id - BUTTON_SELECTION);
            }
            default -> {
                if (id >= BUTTON_SIDE && id < BUTTON_SIDE + 12) {
                    int side = (id - BUTTON_SIDE) / 2;
                    machine.changeSide(Direction.from3DDataValue(side), (id - BUTTON_SIDE) % 2 == 0);
                } else if (id >= BUTTON_CLEAR_SIDE && id < BUTTON_CLEAR_SIDE + 6) {
                    machine.clearSide(Direction.from3DDataValue(id - BUTTON_CLEAR_SIDE));
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /** {@code transferStackInSlot}: machine → player; player → input slot if a recipe exists, else hotbar ↔ inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int playerStart = machineSlots;
        int hotbarStart = playerStart + 27;
        int end = playerStart + 36;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, playerStart, end, true)) {
                return ItemStack.EMPTY;
            }
        } else if (hasRecipe(player, stack)) {
            if (!moveItemStackTo(stack, 0, type == TeMachineType.INDUCTION_SMELTER ? 2 : 1, false)) {
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

    private boolean hasRecipe(Player player, ItemStack stack) {
        Level level = player.level();
        return switch (type) {
            case POWERED_FURNACE -> TeRecipes.find(level, TeRecipe.Kind.FURNACE, stack) != null;
            case PULVERIZER -> TeRecipes.find(level, TeRecipe.Kind.PULVERIZER, stack) != null;
            case SAWMILL -> TeRecipes.find(level, TeRecipe.Kind.SAWMILL, stack) != null;
            case INDUCTION_SMELTER -> TeRecipes.isSmelterInput(level, stack);
            case MAGMA_CRUCIBLE -> TeRecipes.find(level, TeFluidRecipe.Kind.CRUCIBLE, stack) != null;
            case LIQUID_TRANSPOSER -> TeRecipes.isTransposerInput(level, stack);
            case GLACIAL_PRECIPITATOR, IGNEOUS_EXTRUDER, AQUEOUS_ACCUMULATOR -> false;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return machine == null || machine.stillValid(player);
    }
}
