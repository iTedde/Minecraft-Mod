package dev.craft.ic2.crop;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.menu.CropMatronMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code TileEntityCropmatron}: with at least 31 EU it checks one position of the 9×3×9 area around it per tick
 * and feeds a crop there with fertilizer, hydration cells and Weed-EX (spec 04, Crop-Matron).
 */
public class CropMatronBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int SLOT_FERTILIZER = 0;
    public static final int SLOT_HYDRATION = 3;
    public static final int SLOT_WEED_EX = 6;
    public static final int MAX_ENERGY = 1000;
    public static final int MAX_INPUT = 32;
    public static final int MIN_ENERGY = 31;
    public static final int APPLY_COST = 10;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    /** Scan offsets; IC2 does not save them, so a reload starts over. */
    private int scanX = -4;
    private int scanY = -1;
    private int scanZ = -4;

    public CropMatronBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.CROP_MATRON), pos, state, 9);
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public void serverTick() {
        if (energy >= MIN_ENERGY) {
            scan();
        }
    }

    /** Advances to the next position (x first, then z, then y; the first check is x −3) and tends a crop there. */
    public void scan() {
        if (++scanX > 4) {
            scanX = -4;
            if (++scanZ > 4) {
                scanZ = -4;
                if (++scanY > 1) {
                    scanY = -1;
                }
            }
        }
        useEnergy(1);
        setChanged();
        if (!(level.getBlockEntity(worldPosition.offset(scanX, scanY, scanZ)) instanceof CropBlockEntity crop)) {
            return;
        }
        updateSlots();
        ItemStack fertilizer = inventory.get(SLOT_FERTILIZER);
        if (fertilizer.is(Ic2Items.FERTILIZER.get()) && crop.applyFertilizer(false)) {
            useEnergy(APPLY_COST);
            fertilizer.shrink(1);
            clearIfEmpty(SLOT_FERTILIZER);
        }
        ItemStack cell = inventory.get(SLOT_HYDRATION);
        if (cell.is(Ic2Items.HYDRATING_CELL.get()) && crop.applyHydration(false, cell)) {
            useEnergy(APPLY_COST);
            clearIfEmpty(SLOT_HYDRATION);
        }
        ItemStack weedEx = inventory.get(SLOT_WEED_EX);
        if (weedEx.is(Ic2Items.WEED_EX.get()) && crop.applyWeedEx(false)) {
            useEnergy(APPLY_COST);
            weedEx.setDamageValue(weedEx.getDamageValue() + 1);
            if (weedEx.getDamageValue() >= weedEx.getMaxDamage()) {
                weedEx.shrink(1);
            }
            clearIfEmpty(SLOT_WEED_EX);
        }
    }

    private void useEnergy(int amount) {
        energy -= amount;
        consumptionMeter.record(level, amount);
    }

    /** IC2 {@code updateSlots}: each column moves up by one slot into an empty slot (2→1, then 1→0). */
    private void updateSlots() {
        for (int first = 0; first < 9; first += 3) {
            moveFrom(first + 1, first);
            moveFrom(first + 2, first + 1);
        }
    }

    private void moveFrom(int from, int to) {
        if (!inventory.isEmpty(from) && inventory.isEmpty(to)) {
            inventory.set(to, inventory.get(from));
            inventory.set(from, ItemStack.EMPTY);
        }
    }

    private void clearIfEmpty(int slot) {
        if (inventory.get(slot).isEmpty()) {
            inventory.set(slot, ItemStack.EMPTY);
        } else {
            inventory.notifyChanged(slot);
        }
    }

    /** IC2 {@code getStartInventorySide}/{@code getSizeInventorySide}: three slots per side. */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        int start = switch (side) {
            case DOWN -> SLOT_WEED_EX;
            case UP -> SLOT_HYDRATION;
            default -> SLOT_FERTILIZER;
        };
        return new RangedWrapper(inventory, start, start + 3);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CropMatronMenu(id, playerInventory, this);
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : MAX_ENERGY - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > MAX_INPUT) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        energy += amount;
        int rest = 0;
        if (energy > MAX_ENERGY) {
            rest = energy - MAX_ENERGY;
            energy = MAX_ENERGY;
        }
        setChanged();
        return rest;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getShort("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("energy", (short) energy);
    }

    /** Test hook. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }
}
