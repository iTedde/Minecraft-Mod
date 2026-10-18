package dev.craft.ic2.machine;

import java.util.Optional;

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
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.FilledFuelCanItem;
import dev.craft.ic2.item.FilledTinCanItem;
import dev.craft.ic2.menu.CanningMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code TileEntityCanner}: cans food into tin cans, fills fuel cans and jetpacks with fuel cells (spec 04, canning
 * machine). Mode 3 (CF pack) follows with the construction foam.
 */
public class CanningMachineBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_CONTAINER = 3;
    public static final int MAX_ENERGY = 631;
    public static final int MAX_INPUT = 32;
    public static final int TIER = 1;
    public static final int ENERGY_PER_TICK = 1;
    public static final int OPERATION_LENGTH = 600;
    public static final int COALFUEL_VALUE = 2548;
    public static final int BIOFUEL_VALUE = 868;

    public static final int MODE_NONE = 0;
    public static final int MODE_FOOD = 1;
    public static final int MODE_FUEL = 2;
    public static final int MODE_CF_PACK = 3;
    /** Ticks per pellet in CF pack mode. */
    public static final int CF_PACK_LENGTH = 50;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    /** IC2 keeps the progress in a {@code short}. */
    private short progress;
    private int fuelQuality;

    public CanningMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.CANNING_MACHINE), pos, state, 4);
    }

    public int getEnergy() {
        return energy;
    }

    public int getProgress() {
        return progress;
    }

    public int getFuelQuality() {
        return fuelQuality;
    }

    @Override
    public void serverTick() {
        boolean needsInvUpdate = false;
        boolean canOperate = canOperate();
        if (energy <= ENERGY_PER_TICK * OPERATION_LENGTH && canOperate) {
            needsInvUpdate = provideEnergy();
        }
        boolean newActive = isActive();
        int mode = getMode();
        if (canOperate && (mode == MODE_FOOD && progress >= getFoodValue(inventory.get(SLOT_INPUT)) * 50
                || mode == MODE_FUEL && progress > 0 && progress % 100 == 0
                || mode == MODE_CF_PACK && progress >= CF_PACK_LENGTH)) {
            if (mode == MODE_FUEL && progress < OPERATION_LENGTH) {
                operate(true);
            } else {
                operate(false);
                fuelQuality = 0;
                progress = 0;
                newActive = false;
            }
            needsInvUpdate = true;
        }
        if (newActive && progress != 0) {
            if (!canOperate || energy < ENERGY_PER_TICK) {
                if (!canOperate && getMode() != MODE_FUEL) {
                    fuelQuality = 0;
                    progress = 0;
                }
                newActive = false;
            }
        } else if (canOperate) {
            if (energy >= ENERGY_PER_TICK) {
                newActive = true;
            }
        } else if (getMode() != MODE_FUEL) {
            fuelQuality = 0;
            progress = 0;
        }
        if (newActive) {
            progress++;
            energy -= ENERGY_PER_TICK;
            consumptionMeter.record(level, ENERGY_PER_TICK);
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        } else {
            setChanged();
        }
        if (newActive != isActive()) {
            setActive(newActive);
        }
    }

    /**
     * IC2 {@code TileEntityElecMachine.provideEnergy}: electric items discharge, redstone adds the full capacity without
     * a cap, single-use batteries 1000.
     */
    private boolean provideEnergy() {
        ItemStack stack = inventory.get(SLOT_BATTERY);
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ElectricItem item) {
            if (!item.canProvideEnergy()) {
                return false;
            }
            int transfer = ElectricItems.discharge(stack, MAX_ENERGY - energy, TIER, false, false);
            energy += transfer;
            return transfer > 0;
        }
        if (stack.is(Items.REDSTONE)) {
            energy += MAX_ENERGY;
            stack.shrink(1);
            return true;
        }
        if (stack.is(Ic2Items.SU_BATTERY.get())) {
            energy += 1000;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private void operate(boolean incremental) {
        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack container = inventory.get(SLOT_CONTAINER);
        switch (getMode()) {
            case MODE_FOOD -> {
                int food = getFoodValue(input);
                int meta = getFoodMeta(input);
                ItemStack remainder = getFoodRemainder(input);
                input.shrink(1);
                if (input.isEmpty()) {
                    inventory.set(SLOT_INPUT, remainder);
                }
                container.shrink(food);
                ItemStack output = inventory.get(SLOT_OUTPUT);
                if (output.isEmpty()) {
                    inventory.set(SLOT_OUTPUT, FilledTinCanItem.create(meta, food));
                } else {
                    output.grow(food);
                }
            }
            case MODE_FUEL -> {
                int fuel = getFuelValue(input);
                input.shrink(1);
                fuelQuality += fuel;
                if (!incremental) {
                    if (container.is(Ic2Items.EMPTY_FUEL_CAN.get())) {
                        container.shrink(1);
                        inventory.set(SLOT_OUTPUT, FilledFuelCanItem.create(fuelQuality));
                    } else {
                        int damage = Math.max(1, container.getDamageValue() - fuelQuality);
                        inventory.set(SLOT_CONTAINER, ItemStack.EMPTY);
                        ItemStack jetpack = new ItemStack(Ic2Items.JETPACK.get());
                        jetpack.setDamageValue(damage);
                        inventory.set(SLOT_OUTPUT, jetpack);
                    }
                }
            }
            case MODE_CF_PACK -> {
                input.shrink(1);
                container.setDamageValue(container.getDamageValue() - 2);
                if (input.isEmpty() || container.getDamageValue() <= 1) {
                    inventory.set(SLOT_OUTPUT, container);
                    inventory.set(SLOT_CONTAINER, ItemStack.EMPTY);
                }
            }
            default -> {
            }
        }
    }

    private boolean canOperate() {
        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack output = inventory.get(SLOT_OUTPUT);
        return switch (getMode()) {
            case MODE_FOOD -> {
                int food = getFoodValue(input);
                yield food > 0 && food <= inventory.get(SLOT_CONTAINER).getCount()
                        && (output.isEmpty() || output.getCount() + food <= output.getMaxStackSize()
                        && output.is(Ic2Items.FILLED_TIN_CAN.get())
                        && FilledTinCanItem.getMeta(output) == getFoodMeta(input));
            }
            case MODE_FUEL -> getFuelValue(input) > 0 && output.isEmpty();
            case MODE_CF_PACK -> inventory.get(SLOT_CONTAINER).getDamageValue() > 2
                    && input.is(Ic2Items.CONSTRUCTION_FOAM_PELLET.get()) && output.isEmpty();
            default -> false;
        };
    }

    /** Mode from the container slot: tin cans 1, empty fuel can or fuel jetpack 2, CF pack 3, else 0. */
    public int getMode() {
        ItemStack container = inventory.get(SLOT_CONTAINER);
        if (container.isEmpty()) {
            return MODE_NONE;
        }
        if (container.is(Ic2Items.TIN_CAN.get())) {
            return MODE_FOOD;
        }
        if (container.is(Ic2Items.EMPTY_FUEL_CAN.get()) || container.is(Ic2Items.JETPACK.get())) {
            return MODE_FUEL;
        }
        if (container.is(Ic2Items.CF_PACK.get())) {
            return MODE_CF_PACK;
        }
        return MODE_NONE;
    }

    /** {@code getFoodValue}: {@code ceil(heal / 2)} for food, 6 for cake, 0 for filled tin cans and the rest. */
    public static int getFoodValue(ItemStack stack) {
        if (stack.is(Ic2Items.FILLED_TIN_CAN.get())) {
            return 0;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            return (int) Math.ceil(food.nutrition() / 2.0);
        }
        return stack.is(Items.CAKE) ? 6 : 0;
    }

    /** {@code specialFood}: filled tin can variant of the food. */
    public static int getFoodMeta(ItemStack stack) {
        if (stack.is(Items.ROTTEN_FLESH)) {
            return 1;
        }
        if (stack.is(Items.SPIDER_EYE) || stack.is(Items.POISONOUS_POTATO)) {
            return 2;
        }
        if (stack.is(Items.CHICKEN)) {
            return 3;
        }
        if (stack.is(Items.GOLDEN_APPLE)) {
            return 4;
        }
        return stack.is(Items.ENCHANTED_GOLDEN_APPLE) ? 5 : 0;
    }

    /**
     * Container left by a canned food: the crafting remainder (GT {@code getContainerItem}, e.g. the honey bottle's
     * glass bottle), else what eating leaves (the bowl of mushroom stew and the 1.21 soups).
     */
    public static ItemStack getFoodRemainder(ItemStack stack) {
        if (stack.hasCraftingRemainingItem()) {
            return stack.getCraftingRemainingItem();
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        Optional<ItemStack> remainder = food == null ? Optional.empty() : food.usingConvertsTo();
        return remainder.map(ItemStack::copy).orElse(ItemStack.EMPTY);
    }

    /** {@code getFuelValue}: fuel cells, and redstone/glowstone/gunpowder as boosters once fuel is collected. */
    public int getFuelValue(ItemStack stack) {
        if (stack.is(Ic2Items.COALFUEL_CELL.get())) {
            return COALFUEL_VALUE;
        }
        if (stack.is(Ic2Items.BIOFUEL_CELL.get())) {
            return BIOFUEL_VALUE;
        }
        if (stack.is(Items.REDSTONE) && fuelQuality > 0) {
            return (int) (fuelQuality * 0.2);
        }
        if (stack.is(Items.GLOWSTONE_DUST) && fuelQuality > 0) {
            return (int) (fuelQuality * 0.3);
        }
        return stack.is(Items.GUNPOWDER) && fuelQuality > 0 ? (int) (fuelQuality * 0.4) : 0;
    }

    // --- inventory ---

    /** IC2 {@code getStartInventorySide}: left of the front → battery, bottom → container, top → input, else output. */
    @Override
    protected int getSlotForSide(Direction side) {
        if (side == getFacing().getCounterClockWise()) {
            return SLOT_BATTERY;
        }
        return switch (side) {
            case DOWN -> SLOT_CONTAINER;
            case UP -> SLOT_INPUT;
            default -> SLOT_OUTPUT;
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CanningMachineMenu(id, playerInventory, this);
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
        energy = tag.getInt("energy");
        progress = tag.getShort("progress");
        fuelQuality = tag.getInt("fuelQuality");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putShort("progress", progress);
        tag.putInt("fuelQuality", fuelQuality);
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
