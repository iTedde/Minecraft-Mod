package dev.craft.ic2.machine;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.UpgradeModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Standard electric machine, 1:1 with IC2 {@code TileEntityElecMachine} + {@code TileEntityElectricMachine}: slots
 * 0 input, 1 battery, 2 output, 3–6 upgrades. Used by the electric furnace, macerator, extractor, compressor and
 * recycler.
 */
public abstract class ElectricMachineBlockEntity extends MachineBlockEntity implements EnergySink {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int FIRST_UPGRADE_SLOT = 3;
    public static final int UPGRADE_SLOTS = 4;

    /** Block event ids, IC2 {@code EventStart/EventInterrupt/EventStop}. */
    public static final int EVENT_START = 0;
    public static final int EVENT_INTERRUPT = 1;
    public static final int EVENT_STOP = 2;

    protected int energy;
    protected int maxEnergy;
    protected int maxInput;
    protected int tier = 1;
    protected int progress;

    protected final int defaultEnergyConsume;
    protected final int defaultOperationLength;
    protected final int defaultMaxInput;
    protected final int defaultEnergyStorage;
    protected int energyConsume;
    protected int operationLength;

    protected ElectricMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int energyPerTick,
                                         int length, int maxInput) {
        super(type, pos, state, 3 + UPGRADE_SLOTS);
        this.maxEnergy = energyPerTick * length + maxInput - 1;
        this.maxInput = maxInput;
        this.defaultEnergyConsume = this.energyConsume = energyPerTick;
        this.defaultOperationLength = this.operationLength = length;
        this.defaultMaxInput = maxInput;
        this.defaultEnergyStorage = energyPerTick * length;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getProgress() {
        return progress;
    }

    public int getOperationLength() {
        return operationLength;
    }

    public int getEnergyConsume() {
        return energyConsume;
    }

    public int getTier() {
        return tier;
    }

    /** Charge display: energy relative to the storage without the input headroom, capped at 1. */
    public float getChargeLevel() {
        return Math.min(1.0F, (float) energy / (maxEnergy - maxInput + 1));
    }

    public float getProgressLevel() {
        return Math.min(1.0F, (float) progress / operationLength);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            setOverclockRates();
        }
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
    protected void onInventoryChanged() {
        super.onInventoryChanged();
        if (level != null && !level.isClientSide()) {
            setOverclockRates();
        }
    }

    @Override
    public void serverTick() {
        boolean canOperate = canOperate();
        boolean needsInvUpdate = false;
        if (energy <= energyConsume * operationLength && canOperate) {
            needsInvUpdate = provideEnergy();
        }
        boolean newActive = isActive();
        if (progress >= operationLength) {
            operate();
            needsInvUpdate = true;
            progress = 0;
            newActive = false;
            sendEvent(EVENT_STOP);
        }
        canOperate = canOperate();
        if (newActive && progress != 0) {
            if (!canOperate || energy < energyConsume) {
                if (!canOperate) {
                    progress = 0;
                }
                newActive = false;
                sendEvent(EVENT_INTERRUPT);
            }
        } else if (canOperate) {
            if (energy >= energyConsume) {
                newActive = true;
                sendEvent(EVENT_START);
            }
        } else {
            progress = 0;
        }
        if (newActive) {
            progress++;
            energy -= energyConsume;
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        }
        if (newActive != isActive()) {
            setActive(newActive);
        }
    }

    private void sendEvent(int event) {
        level.blockEvent(worldPosition, getBlockState().getBlock(), event, 0);
    }

    /** IC2 {@code setOverclockRates}: applies the upgrades in slots 3–6. */
    public void setOverclockRates() {
        int overclockers = 0;
        int transformers = 0;
        int energyStorages = 0;
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            ItemStack stack = inventory.get(FIRST_UPGRADE_SLOT + i);
            if (stack.getItem() instanceof UpgradeModuleItem upgrade) {
                switch (upgrade.getType()) {
                    case OVERCLOCKER -> overclockers += stack.getCount();
                    case TRANSFORMER -> transformers += stack.getCount();
                    case ENERGY_STORAGE -> energyStorages += stack.getCount();
                }
            }
        }
        overclockers = Math.min(overclockers, 32);
        transformers = Math.min(transformers, 10);
        double previousProgress = (double) progress / operationLength;
        energyConsume = (int) (defaultEnergyConsume * Math.pow(1.6, overclockers));
        operationLength = (int) Math.round(defaultOperationLength * Math.pow(0.7, overclockers));
        maxInput = defaultMaxInput * (int) Math.pow(4.0, transformers);
        maxEnergy = defaultEnergyStorage + energyStorages * 10000 + maxInput - 1;
        tier = transformers + 1;
        if (operationLength < 1) {
            operationLength = 1;
        }
        progress = (short) Math.round(previousProgress * operationLength);
    }

    /** Takes energy from the battery slot: redstone (one operation), electric items or a single-use battery. */
    public boolean provideEnergy() {
        ItemStack stack = inventory.get(SLOT_BATTERY);
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.REDSTONE)) {
            energy += defaultEnergyConsume * defaultOperationLength;
            stack.shrink(1);
            return true;
        }
        if (stack.getItem() instanceof ElectricItem item) {
            if (!item.canProvideEnergy()) {
                return false;
            }
            int transfer = ElectricItems.discharge(stack, maxEnergy - energy, tier, false, false);
            energy += transfer;
            return transfer > 0;
        }
        if (stack.is(Ic2Items.SU_BATTERY.get())) {
            energy += 1000;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    public void operate() {
        if (!canOperate()) {
            return;
        }
        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack result;
        if (input.hasCraftingRemainingItem()) {
            result = getResultFor(input, false).copy();
            inventory.set(SLOT_INPUT, input.getCraftingRemainingItem());
        } else {
            result = getResultFor(input, true).copy();
        }
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, result);
        } else {
            output.grow(result.getCount());
        }
    }

    public boolean canOperate() {
        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack result = getResultFor(input, false);
        if (result == null || result.isEmpty()) {
            return false;
        }
        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    /**
     * Recipe result for {@code input}, or {@code null}. With {@code adjustInput} the input stack is reduced by the
     * recipe amount.
     */
    @Nullable
    public abstract ItemStack getResultFor(ItemStack input, boolean adjustInput);

    // --- energy net ---

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        if (isRemoved()) {
            return 0;
        }
        return maxEnergy - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > maxInput) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        energy += amount;
        int rest = 0;
        if (energy > maxEnergy) {
            rest = energy - maxEnergy;
            energy = maxEnergy;
        }
        return rest;
    }

    @Override
    public int getMaxSafeInput() {
        return maxInput;
    }

    // --- client events (sounds) ---

    @Override
    public boolean triggerEvent(int id, int param) {
        if (level != null && level.isClientSide()) {
            MachineSoundHooks.onMachineEvent(this, id);
        }
        return true;
    }

    /** Sound played when an operation starts, or {@code null}. */
    @Nullable
    public SoundEvent getStartSound() {
        return null;
    }

    /** Sound played when an operation is interrupted, or {@code null}. */
    @Nullable
    public SoundEvent getInterruptSound() {
        return null;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        progress = tag.getShort("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putShort("progress", (short) progress);
    }
}
