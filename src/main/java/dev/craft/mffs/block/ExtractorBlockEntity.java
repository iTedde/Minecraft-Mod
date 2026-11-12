package dev.craft.mffs.block;

import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsConfig;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.energy.PowerLinkItem;
import dev.craft.mffs.item.ForciciumCellItem;
import dev.craft.mffs.menu.ExtractorMenu;
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

/**
 * {@code TileEntityExtractor} (spec 48 §3): EU (or MJ) + forcicium → force energy for the linked capacitor; with a
 * forcicium cell in slot 4 it fills the cell instead (200 000 EU per forcicium).
 */
public class ExtractorBlockEntity extends FePoweredBlockEntity implements EnergySink, PowerReceptor {
    public static final int SLOT_FORCICIUM = 0;
    public static final int SLOT_POWER_LINK = 1;
    public static final int SLOT_CAPACITY = 2;
    public static final int SLOT_BOOSTER = 3;
    public static final int SLOT_CELL = 4;
    public static final int MAX_SAFE_INPUT = 2048;

    private final PowerProvider powerProvider = new PowerProvider();
    private int workEnergy;
    private int maxWorkEnergy = 4000;
    private int forceEnergyBuffer;
    private int maxForceEnergyBuffer = 1000000;
    private int workCycle;
    private int maxWorkCycle = 125;
    private int workTicker = 20;
    private int workDone;
    private int capacity;
    private int workMode;
    private boolean checkedSlots;

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.EXTRACTOR.get(), pos, state, 5);
        // configure(latency 10, min 2, max/activation/stored = 4000 / 2.5); once, also in cell mode.
        int mj = (int) (4000 / 2.5);
        powerProvider.configure(2, mj, mj, mj);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.EXTRACTOR;
    }

    @Override
    public int getPowerLinkSlot() {
        return SLOT_POWER_LINK;
    }

    @Override
    public int getMinSwitchMode() {
        return 1;
    }

    @Override
    public int getMaxSwitchMode() {
        return 3;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_FORCICIUM -> (stack.is(MffsItems.FORCICIUM.get()) || stack.getItem() instanceof ForciciumCellItem)
                    && getStack(SLOT_CELL).isEmpty();
            case SLOT_POWER_LINK -> stack.getItem() instanceof PowerLinkItem;
            case SLOT_CAPACITY -> stack.is(MffsItems.CAPACITOR_UPGRADE_CAPACITY.get());
            case SLOT_BOOSTER -> stack.is(MffsItems.EXTRACTOR_BOOSTER.get());
            case SLOT_CELL -> stack.getItem() instanceof ForciciumCellItem && getStack(SLOT_FORCICIUM).isEmpty();
            default -> false;
        };
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case SLOT_FORCICIUM -> 64;
            case SLOT_CAPACITY -> 9;
            case SLOT_BOOSTER -> 19;
            default -> 1;
        };
    }

    private void checkSlots() {
        ItemStack capacityStack = getStack(SLOT_CAPACITY);
        maxForceEnergyBuffer = capacityStack.is(MffsItems.CAPACITOR_UPGRADE_CAPACITY.get())
                ? 1000000 + capacityStack.getCount() * 100000 : 1000000;
        ItemStack booster = getStack(SLOT_BOOSTER);
        workTicker = booster.is(MffsItems.EXTRACTOR_BOOSTER.get()) ? 20 - booster.getCount() : 20;
        ItemStack cell = getStack(SLOT_CELL);
        if (!cell.isEmpty()) {
            if (cell.getItem() instanceof ForciciumCellItem) {
                workMode = 1;
                maxWorkEnergy = 200000;
            }
        } else {
            workMode = 0;
            maxWorkEnergy = 4000;
        }
    }

    @Override
    protected void tickMachine() {
        if (!checkedSlots) {
            checkSlots();
            checkedSlots = true;
        }
        followRedstone();
        if (!isActive() && getSwitchValue()) {
            setActive(true);
        }
        if (isActive() && !getSwitchValue()) {
            setActive(false);
        }
        if (isActive()) {
            convertMjToWorkEnergy();
        }
        if (getTicker() >= workTicker) {
            checkSlots();
            if (workMode == 0 && isActive()) {
                workDone = Math.min(100, workEnergy * 100 / maxWorkEnergy);
                capacity = forceEnergyBuffer * 100 / maxForceEnergyBuffer;
                if (hasFreeForceEnergyStorage() && hasStuffToConvert() && hasPowerToConvert()) {
                    workCycle--;
                    forceEnergyBuffer += MffsConfig.EXTRACTOR_PASS_FORCE_ENERGY;
                }
                transferForceEnergy();
                setTicker(0);
                setChanged();
            }
            if (workMode == 1 && isActive()) {
                workDone = workEnergy * 100 / maxWorkEnergy;
                ItemStack cell = getStack(SLOT_CELL);
                if (cell.getItem() instanceof ForciciumCellItem cellItem
                        && ForciciumCellItem.getLevel(cell) < cellItem.getMaxLevel() && hasPowerToConvert()) {
                    ForciciumCellItem.setLevel(cell, ForciciumCellItem.getLevel(cell) + 1);
                }
                setTicker(0);
                setChanged();
            }
        }
        setTicker(getTicker() + 1);
    }

    /** {@code hasPowertoConvert}: a full work energy buffer (max − 1) is used up completely. */
    private boolean hasPowerToConvert() {
        if (workEnergy >= maxWorkEnergy - 1) {
            workEnergy = 0;
            return true;
        }
        return false;
    }

    private boolean hasFreeForceEnergyStorage() {
        return maxForceEnergyBuffer > forceEnergyBuffer;
    }

    /** {@code hasStufftoConvert}: running cycles, else one forcicium (250) or one from a cell in slot 0 (230). */
    private boolean hasStuffToConvert() {
        if (workCycle > 0) {
            return true;
        }
        ItemStack stack = getStack(SLOT_FORCICIUM);
        if (stack.is(MffsItems.FORCICIUM.get())) {
            maxWorkCycle = MffsConfig.FORCICIUM_WORK_CYCLE;
            workCycle = maxWorkCycle;
            inventory.extractItem(SLOT_FORCICIUM, 1, false);
            return true;
        }
        if (stack.getItem() instanceof ForciciumCellItem && ForciciumCellItem.useForcicium(stack, 1)) {
            maxWorkCycle = MffsConfig.FORCICIUM_CELL_WORK_CYCLE;
            workCycle = maxWorkCycle;
            return true;
        }
        return false;
    }

    /** At most capacitor max / 120 per step, limited by the capacitor's free space and the own buffer. */
    private void transferForceEnergy() {
        if (forceEnergyBuffer <= 0 || !hasPowerSource()) {
            return;
        }
        int rate = getMaximumPower() / 120;
        int free = getMaximumPower() - getAvailablePower();
        int amount;
        if (forceEnergyBuffer > free) {
            amount = Math.min(free, rate);
        } else if (free > forceEnergyBuffer) {
            amount = forceEnergyBuffer;
        } else {
            amount = free;
        }
        emitPower(amount, false);
        forceEnergyBuffer -= amount;
    }

    /** {@code converMJtoWorkEnergy}, with the original's missing brackets ({@code max − work / 2.5}). */
    private void convertMjToWorkEnergy() {
        if (workEnergy < maxWorkEnergy) {
            float use = powerProvider.useEnergy(1.0F, (float) (maxWorkEnergy - workEnergy / 2.5), true);
            if (workEnergy + use * 2.5 > maxWorkEnergy) {
                workEnergy = maxWorkEnergy;
            } else {
                workEnergy = (int) (workEnergy + use * 2.5);
            }
        }
    }

    // --- Getters for the GUI and tests ---

    public int getWorkEnergy() {
        return workEnergy;
    }

    /** Tests: fill the work energy buffer directly. */
    public void setWorkEnergy(int value) {
        workEnergy = value;
    }

    public int getMaxWorkEnergy() {
        return maxWorkEnergy;
    }

    public int getForceEnergyBuffer() {
        return forceEnergyBuffer;
    }

    public void setForceEnergyBuffer(int value) {
        forceEnergyBuffer = value;
    }

    public int getMaxForceEnergyBuffer() {
        return maxForceEnergyBuffer;
    }

    public int getWorkCycle() {
        return workCycle;
    }

    public int getWorkDone() {
        return workDone;
    }

    public int getCapacity() {
        return capacity;
    }

    // --- EU ---

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isActive() ? maxWorkEnergy - workEnergy : 0;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (amount > MAX_SAFE_INPUT) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        int free = maxWorkEnergy - workEnergy;
        if (free >= amount) {
            workEnergy += amount;
            return 0;
        }
        workEnergy = maxWorkEnergy;
        return amount - free;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_SAFE_INPUT;
    }

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

    // --- MJ ---

    @Override
    public PowerProvider getPowerProvider() {
        return powerProvider;
    }

    @Override
    public void doWork() {
    }

    @Override
    public int powerRequest() {
        return (int) Math.round(maxWorkEnergy / 2.5 - workEnergy / 2.5);
    }

    // --- Saving ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ForceEnergybuffer", forceEnergyBuffer);
        tag.putInt("WorkEnergy", workEnergy);
        tag.putInt("WorkCylce", workCycle);
        tag.put("power", powerProvider.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        forceEnergyBuffer = tag.getInt("ForceEnergybuffer");
        workEnergy = tag.getInt("WorkEnergy");
        workCycle = tag.getInt("WorkCylce");
        if (tag.contains("power")) {
            powerProvider.load(tag.getCompound("power"));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ExtractorMenu(id, playerInventory, this);
    }
}
