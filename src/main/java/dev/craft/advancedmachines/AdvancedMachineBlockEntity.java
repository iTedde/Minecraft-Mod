package dev.craft.advancedmachines;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.UpgradeModuleItem;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.MachineSoundHooks;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced Machines 4.7b {@code TileEntityAdvancedMachine} + {@code TileEntityBaseMachine} (spec 25 §3): the machine
 * spins up to a speed, progress grows by speed / 30 per tick and an item is done at 4000.
 */
public class AdvancedMachineBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int MAX_PROGRESS = 4000;
    public static final int BASE_ENERGY = 5000;
    public static final int BASE_MAX_SPEED = 7500;
    public static final int BASE_MAX_INPUT = 32;
    /** Pack config {@code AdvancedMachines.cfg}, section "opness control". */
    public static final int ENERGY_PER_TICK = 3;
    public static final int ACCELERATION = 2;
    public static final int OVERCLOCK_SPEED_BONUS = 500;
    public static final double OVERCLOCK_ENERGY_RATIO = 2.0;
    public static final double OVERCLOCK_ACCEL_RATIO = 1.6;
    public static final double OVERLOAD_INPUT_RATIO = 4.0;

    /** Block events: sound start, sound interrupt (speed reached 0). */
    public static final int EVENT_START = 0;
    public static final int EVENT_INTERRUPT = 1;

    protected final AdvancedMachineType type;
    private final OutputMeter consumptionMeter = new OutputMeter();
    protected int energy;
    private int maxEnergy = BASE_ENERGY;
    private int maxInput = BASE_MAX_INPUT;
    private int tier = 1;
    private int energyConsume = ENERGY_PER_TICK;
    private int acceleration = 1;
    private int maxSpeed = BASE_MAX_SPEED;
    private short speed;
    private short progress;
    /** Server: the client was told to play the operation sound. */
    private boolean soundOn;
    /** Client: operation sound playing. */
    private boolean clientSound;

    public AdvancedMachineBlockEntity(AdvancedMachineType type, BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(type.getMachine()), pos, state, type.getSlotCount());
        this.type = type;
    }

    public static AdvancedMachineBlockEntity rotaryMacerator(BlockPos pos, BlockState state) {
        return new RotaryMaceratorBlockEntity(pos, state);
    }

    public static AdvancedMachineBlockEntity singularityCompressor(BlockPos pos, BlockState state) {
        return new AdvancedMachineBlockEntity(AdvancedMachineType.SINGULARITY_COMPRESSOR, pos, state);
    }

    public static AdvancedMachineBlockEntity centrifugeExtractor(BlockPos pos, BlockState state) {
        return new AdvancedMachineBlockEntity(AdvancedMachineType.CENTRIFUGE_EXTRACTOR, pos, state);
    }

    public AdvancedMachineType getMachineType() {
        return type;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getSpeed() {
        return speed;
    }

    public int getProgress() {
        return progress;
    }

    public int getTier() {
        return tier;
    }

    /** Energy per tick while spinning up (at full speed the base 3 EU/t). */
    public int getEnergyConsume() {
        return energyConsume;
    }

    public int getMaxSpeed() {
        return maxSpeed;
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
    public void serverTick() {
        boolean newItemProcessing = false;
        if (energy <= maxEnergy) {
            getPowerFromFuelSlot();
        }
        boolean active = isActive();
        if (progress >= MAX_PROGRESS) {
            operate();
            newItemProcessing = true;
            progress = 0;
            active = false;
        }
        boolean canOperate = canOperate();
        if (energy > 0 && (canOperate || level.hasNeighborSignal(worldPosition))) {
            setOverclockRates();
            int used;
            if (speed < maxSpeed) {
                speed = (short) (speed + acceleration);
                used = energyConsume;
            } else {
                speed = (short) maxSpeed;
                used = ENERGY_PER_TICK;
            }
            energy -= used;
            consumptionMeter.record(level, used);
            active = true;
            if (!soundOn) {
                soundOn = true;
                sendEvent(EVENT_START);
            }
        } else {
            boolean wasWorking = speed != 0;
            speed = (short) (speed - Math.min(speed, 4));
            if (wasWorking && speed == 0) {
                soundOn = false;
                sendEvent(EVENT_INTERRUPT);
            }
        }
        if (active && progress != 0) {
            if (!canOperate || speed == 0) {
                if (!canOperate) {
                    progress = 0;
                }
                active = false;
            }
        } else if (canOperate) {
            if (speed != 0) {
                active = true;
            }
        } else {
            progress = 0;
        }
        if (active && canOperate) {
            progress = (short) (progress + speed / 30);
        }
        if (newItemProcessing) {
            setChanged();
        }
        if (active != isActive()) {
            setActive(active);
        }
    }

    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, type.getSound(), clientSound);
    }

    private void sendEvent(int event) {
        level.blockEvent(worldPosition, getBlockState().getBlock(), event, 0);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (level != null && level.isClientSide()) {
            if (id == EVENT_START) {
                clientSound = true;
            } else if (id == EVENT_INTERRUPT) {
                if (clientSound) {
                    level.playLocalSound(worldPosition, Ic2Sounds.INTERRUPT_ONE.get(), SoundSource.BLOCKS, 1.0F,
                            1.0F, false);
                }
                clientSound = false;
            }
        }
        return true;
    }

    /** {@code operate}: spreads a copy of the result over the output slots; a free slot takes the whole rest. */
    private void operate() {
        if (!canOperate()) {
            return;
        }
        ItemStack input = inventory.get(AdvancedMachineType.SLOT_INPUT);
        ItemStack result = getResultFor(input, true).copy();
        int[] outputs = type.getOutputs();
        int max = result.getMaxStackSize();
        int[] space = new int[outputs.length];
        for (int i = 0; i < outputs.length; i++) {
            ItemStack out = inventory.get(outputs[i]);
            if (out.isEmpty()) {
                space[i] = max;
            } else if (ItemStack.isSameItemSameComponents(out, result)) {
                space[i] = max - out.getCount();
            }
        }
        for (int i = 0; i < outputs.length; i++) {
            if (space[i] > 0) {
                int stash = Math.min(result.getCount(), space[i]);
                ItemStack out = inventory.get(outputs[i]);
                if (out.isEmpty()) {
                    inventory.set(outputs[i], result);
                    break;
                }
                out.grow(stash);
                inventory.set(outputs[i], out);
                result.shrink(stash);
                if (result.isEmpty()) {
                    break;
                }
            }
        }
        onFinishedProcessingItem();
        if (input.isEmpty()) {
            inventory.set(AdvancedMachineType.SLOT_INPUT, ItemStack.EMPTY);
        } else {
            inventory.set(AdvancedMachineType.SLOT_INPUT, input);
        }
    }

    protected void onFinishedProcessingItem() {
    }

    protected boolean canOperate() {
        ItemStack input = inventory.get(AdvancedMachineType.SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack result = getResultFor(input, false);
        if (result == null || result.isEmpty()) {
            return false;
        }
        int max = result.getMaxStackSize();
        int free = 0;
        for (int slot : type.getOutputs()) {
            ItemStack out = inventory.get(slot);
            if (out.isEmpty()) {
                free += max;
            } else if (ItemStack.isSameItemSameComponents(out, result)) {
                free += max - out.getCount();
            }
        }
        return free >= result.getCount();
    }

    /** Recipe result for {@code input}, or {@code null}; with {@code adjustInput} the input is reduced. */
    @Nullable
    public ItemStack getResultFor(ItemStack input, boolean adjustInput) {
        ItemStack result = MachineRecipe.getOutputFor(level, type.getKind(), input, adjustInput);
        return result.isEmpty() ? null : result;
    }

    /** {@code setOverclockRates}: upgrades in the four upgrade slots. */
    public void setOverclockRates() {
        int overclockers = 0;
        int transformers = 0;
        int energyStorages = 0;
        for (int i = 0; i < AdvancedMachineType.UPGRADE_SLOTS; i++) {
            ItemStack stack = inventory.get(type.getFirstUpgradeSlot() + i);
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
        energyConsume = (int) (ENERGY_PER_TICK * Math.pow(OVERCLOCK_ENERGY_RATIO, overclockers));
        acceleration = (int) (ACCELERATION * Math.pow(OVERCLOCK_ACCEL_RATIO, overclockers) / 2.0);
        maxSpeed = BASE_MAX_SPEED + overclockers * OVERCLOCK_SPEED_BONUS;
        maxInput = BASE_MAX_INPUT * (int) Math.pow(OVERLOAD_INPUT_RATIO, transformers);
        maxEnergy = BASE_ENERGY + energyStorages * BASE_ENERGY + maxInput - 1;
        tier = 1 + transformers;
    }

    /** Slot 0: electric items, redstone (fills the whole storage) and single-use batteries (+1000). */
    private boolean getPowerFromFuelSlot() {
        ItemStack stack = inventory.get(AdvancedMachineType.SLOT_ENERGY);
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ElectricItem item) {
            if (!item.canProvideEnergy()) {
                return false;
            }
            int charge = ElectricItems.discharge(stack, maxEnergy - energy, tier, false, false);
            energy += charge;
            return charge > 0;
        }
        if (stack.is(Items.REDSTONE)) {
            energy += maxEnergy;
            stack.shrink(1);
            inventory.set(AdvancedMachineType.SLOT_ENERGY, stack);
            return true;
        }
        if (stack.is(Ic2Items.SU_BATTERY.get())) {
            energy += 1000;
            stack.shrink(1);
            inventory.set(AdvancedMachineType.SLOT_ENERGY, stack);
            return true;
        }
        return false;
    }

    // --- sides ---

    /** {@code getStartInventorySide}/{@code getSizeInventorySide}: bottom energy slot, top input, sides outputs. */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        int[] outputs = type.getOutputs();
        return switch (side) {
            case DOWN -> new RangedWrapper(inventory, AdvancedMachineType.SLOT_ENERGY, AdvancedMachineType.SLOT_ENERGY + 1);
            case UP -> new RangedWrapper(inventory, AdvancedMachineType.SLOT_INPUT, AdvancedMachineType.SLOT_INPUT + 1);
            default -> new RangedWrapper(inventory, outputs[0], outputs[0] + outputs.length);
        };
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return side.getAxis().isHorizontal() && side != getFacing();
    }

    // --- energy net ---

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : maxEnergy - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        setOverclockRates();
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

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AdvancedMachineMenu(id, playerInventory, this);
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        speed = tag.getShort("speed");
        progress = tag.getShort("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putShort("speed", speed);
        tag.putShort("progress", progress);
    }

    /** For tests. */
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /** For tests. */
    public void setSpeed(int speed) {
        this.speed = (short) speed;
    }
}
