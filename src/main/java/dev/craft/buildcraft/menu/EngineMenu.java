package dev.craft.buildcraft.menu;

import java.util.function.IntSupplier;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.engine.StirlingEngineBlockEntity;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Stirling and combustion engine container (BuildCraft {@code ContainerEngine}): one fuel slot and the player
 * inventory. The values for the GUI and the engine ledger are rounded like BuildCraft's progress bar updates.
 */
public class EngineMenu extends AbstractContainerMenu {
    private static final int ENERGY = 0;
    private static final int OUTPUT = 1;
    private static final int HEAT = 2;
    private static final int BURN_TIME = 3;
    private static final int TOTAL_BURN_TIME = 4;
    private static final int FUEL_AMOUNT = 5;
    private static final int FUEL_ID = 6;
    private static final int COOLANT_AMOUNT = 7;
    private static final int COOLANT_ID = 8;
    private static final int VALUES = 9;

    private final EngineType type;
    @Nullable
    private final EngineBlockEntity engine;
    private final BlockPos pos;
    private final SyncedInts data;

    public EngineMenu(int id, Inventory inventory, EngineBlockEntity engine) {
        this(engine.getEngineType(), id, inventory, engine, engine.getBlockPos(), serverData(engine));
    }

    public EngineMenu(EngineType type, int id, Inventory inventory, BlockPos pos) {
        this(type, id, inventory, null, pos, new SyncedInts(VALUES));
    }

    private EngineMenu(EngineType type, int id, Inventory inventory, @Nullable EngineBlockEntity engine, BlockPos pos,
                       SyncedInts data) {
        super(BcMenus.get(type), id);
        this.type = type;
        this.engine = engine;
        this.pos = pos;
        this.data = data;
        ItemStackHandler items = engine != null ? engine.getInventory() : new ItemStackHandler(1);
        addSlot(new SlotItemHandler(items, 0, type == EngineType.STIRLING ? 80 : 52, 41));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    private static SyncedInts serverData(EngineBlockEntity engine) {
        IntSupplier energy;
        IntSupplier output;
        IntSupplier burnTime = () -> 0;
        IntSupplier totalBurnTime = () -> 0;
        IntSupplier fuelAmount = () -> 0;
        IntSupplier fuelId = () -> 0;
        IntSupplier coolantAmount = () -> 0;
        IntSupplier coolantId = () -> 0;
        if (engine instanceof CombustionEngineBlockEntity combustion) {
            // EngineIron sends tenths and divides them as ints on the client.
            energy = () -> Math.round(combustion.getEnergyStored() * 10.0F) / 10;
            output = () -> Math.round(combustion.getCurrentOutput() * 10.0F) / 10;
            fuelAmount = () -> combustion.getFuelTank().getFluidAmount();
            fuelId = () -> fluidId(combustion.getFuelTank().getFluid());
            coolantAmount = () -> combustion.getCoolantTank().getFluidAmount();
            coolantId = () -> fluidId(combustion.getCoolantTank().getFluid());
        } else {
            energy = () -> Math.round(engine.getEnergyStored());
            output = () -> Math.round(engine.getCurrentOutput());
            if (engine instanceof StirlingEngineBlockEntity stirling) {
                burnTime = stirling::getBurnTime;
                totalBurnTime = stirling::getTotalBurnTime;
            }
        }
        return new SyncedInts(energy, output, engine::getHeat, burnTime, totalBurnTime, fuelAmount, fuelId,
                coolantAmount, coolantId);
    }

    private static int fluidId(FluidStack stack) {
        return stack.isEmpty() ? 0 : BuiltInRegistries.FLUID.getId(stack.getFluid());
    }

    public EngineType getEngineType() {
        return type;
    }

    public BlockPos getPos() {
        return pos;
    }

    public float getEnergy() {
        return data.getInt(ENERGY);
    }

    public float getCurrentOutput() {
        return data.getInt(OUTPUT);
    }

    public int getHeat() {
        return data.getInt(HEAT);
    }

    /** BuildCraft {@code EngineStone.getScaledBurnTime}. */
    public int getScaledBurnTime(int scale) {
        int total = data.getInt(TOTAL_BURN_TIME);
        return total == 0 ? 0 : (int) ((float) data.getInt(BURN_TIME) / total * scale);
    }

    /** BuildCraft {@code EngineIron.getScaledBurnTime}: fuel tank level. */
    public int getScaledFuel(int scale) {
        return (int) ((float) data.getInt(FUEL_AMOUNT) / CombustionEngineBlockEntity.MAX_LIQUID * scale);
    }

    public int getScaledCoolant(int scale) {
        return (int) ((float) data.getInt(COOLANT_AMOUNT) / CombustionEngineBlockEntity.MAX_LIQUID * scale);
    }

    public Fluid getFuelFluid() {
        return BuiltInRegistries.FLUID.byId(data.getInt(FUEL_ID));
    }

    public Fluid getCoolantFluid() {
        return BuiltInRegistries.FLUID.byId(data.getInt(COOLANT_ID));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 1) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return engine == null || engine.stillValid(player);
    }
}
