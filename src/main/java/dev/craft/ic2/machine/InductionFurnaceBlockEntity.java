package dev.craft.ic2.machine;

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
import dev.craft.ic2.menu.InductionFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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
 * IC2 {@code TileEntityInduction}: smelts two furnace inputs at once; speed grows with the heat, which rises by one per
 * tick while working or while powered by redstone (spec 04, induction furnace).
 */
public class InductionFurnaceBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_OUTPUT_1 = 3;
    public static final int SLOT_OUTPUT_2 = 4;
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_INPUT = 128;
    public static final int TIER = 2;
    public static final int MAX_HEAT = 10000;
    public static final int OPERATION_PROGRESS = 4000;
    public static final int ENERGY_PER_TICK = 15;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    /** IC2 keeps heat and progress in {@code short}s. */
    private short heat;
    private short progress;

    public InductionFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.INDUCTION_FURNACE), pos, state, 5);
    }

    public int getEnergy() {
        return energy;
    }

    public int getHeat() {
        return heat;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void serverTick() {
        boolean needsInvUpdate = false;
        if (energy <= MAX_ENERGY) {
            needsInvUpdate = provideEnergy();
        }
        boolean newActive = isActive();
        if (heat == 0) {
            newActive = false;
        }
        if (progress >= OPERATION_PROGRESS) {
            operate(SLOT_INPUT_1, SLOT_OUTPUT_1);
            operate(SLOT_INPUT_2, SLOT_OUTPUT_2);
            needsInvUpdate = true;
            progress = 0;
            newActive = false;
        }
        boolean canOperate = canOperate();
        if (energy > 0 && (canOperate || level.hasNeighborSignal(worldPosition))) {
            energy--;
            consumptionMeter.record(level, 1);
            if (heat < MAX_HEAT) {
                heat++;
            }
            newActive = true;
        } else {
            heat = (short) (heat - Math.min(heat, 4));
        }
        if (newActive && progress != 0) {
            if (!canOperate || energy < ENERGY_PER_TICK) {
                if (!canOperate) {
                    progress = 0;
                }
                newActive = false;
            }
        } else if (canOperate) {
            if (energy >= ENERGY_PER_TICK) {
                newActive = true;
            }
        } else {
            progress = 0;
        }
        if (newActive && canOperate) {
            progress = (short) (progress + heat / 30);
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
     * IC2 {@code TileEntityElecMachine.provideEnergy}: redstone adds the full capacity without a cap (original
     * behaviour, consumed every tick while the energy is at most 10 000), single-use batteries 1000, electric items
     * discharge.
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

    private void operate(int input, int output) {
        if (!canOperate(input, output)) {
            return;
        }
        ItemStack source = inventory.get(input);
        ItemStack result = ElectricFurnaceBlockEntity.getSmeltingResult(level, source);
        ItemStack target = inventory.get(output);
        if (target.isEmpty()) {
            inventory.set(output, result.copy());
        } else {
            target.grow(result.getCount());
            inventory.set(output, target);
        }
        if (source.hasCraftingRemainingItem()) {
            inventory.set(input, source.getCraftingRemainingItem());
        } else {
            source.shrink(1);
            inventory.set(input, source.isEmpty() ? ItemStack.EMPTY : source);
        }
    }

    private boolean canOperate() {
        return canOperate(SLOT_INPUT_1, SLOT_OUTPUT_1) || canOperate(SLOT_INPUT_2, SLOT_OUTPUT_2);
    }

    private boolean canOperate(int input, int output) {
        ItemStack source = inventory.get(input);
        if (source.isEmpty()) {
            return false;
        }
        ItemStack result = ElectricFurnaceBlockEntity.getSmeltingResult(level, source);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack target = inventory.get(output);
        return target.isEmpty() || ItemStack.isSameItemSameComponents(target, result)
                && target.getCount() + result.getCount() <= result.getMaxStackSize();
    }

    /** Heat in percent ({@code getHeat}). */
    public int getHeatPercent() {
        return heat * 100 / MAX_HEAT;
    }

    // --- inventory ---

    /**
     * IC2 {@code getStartInventorySide}/{@code getSizeInventorySide}: top → inputs, bottom → battery, sides → outputs.
     * The sides also sort inserted items: energy items into the battery slot, smeltables into the inputs (deviation,
     * see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT_1, SLOT_INPUT_2 + 1);
            case DOWN -> new RangedWrapper(inventory, SLOT_BATTERY, SLOT_BATTERY + 1);
            default -> new SideAccess(inventory)
                    .inputs(SLOT_INPUT_1, SLOT_INPUT_2, stack -> !ElectricItems.isMachineEnergySource(stack)
                            && !ElectricFurnaceBlockEntity.getSmeltingResult(level, stack).isEmpty())
                    .input(SLOT_BATTERY, ElectricItems::isMachineEnergySource)
                    .outputs(SLOT_OUTPUT_1, SLOT_OUTPUT_2);
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new InductionFurnaceMenu(id, playerInventory, this);
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
        heat = tag.getShort("heat");
        progress = tag.getShort("progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putShort("heat", heat);
        tag.putShort("progress", progress);
    }

    /** Test hooks. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }

    public void setHeatForTest(int heat) {
        this.heat = (short) heat;
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }
}
