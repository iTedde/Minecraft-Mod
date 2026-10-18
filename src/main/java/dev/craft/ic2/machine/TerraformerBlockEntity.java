package dev.craft.ic2.machine;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.terraformer.TerraformerBlueprint;
import dev.craft.ic2.terraformer.TerraformerBlueprintItem;
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
 * IC2 {@code TileEntityTerra}: runs the inserted blueprint at random columns around itself (near the last success,
 * otherwise in a radius growing with the failures) as long as the energy suffices (spec 04, terraformer).
 */
public class TerraformerBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int MAX_ENERGY = 100000;
    public static final int MAX_INPUT = 512;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    private int failedAttempts;
    private boolean hasLast;
    private int lastX;
    private int lastZ;
    private int inactiveTicks;

    public TerraformerBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.TERRAFORMER), pos, state, 1);
    }

    public int getEnergy() {
        return energy;
    }

    public ItemStack getBlueprintStack() {
        return inventory.get(0);
    }

    @Override
    public void serverTick() {
        boolean newActive = false;
        if (inventory.get(0).getItem() instanceof TerraformerBlueprintItem item) {
            TerraformerBlueprint blueprint = item.getBlueprint();
            if (energy >= blueprint.getConsume()) {
                newActive = true;
                int x = worldPosition.getX();
                int z = worldPosition.getZ();
                int range;
                if (hasLast) {
                    range = blueprint.getRange() / 10;
                    x = lastX - level.random.nextInt(range + 1) + level.random.nextInt(range + 1);
                    z = lastZ - level.random.nextInt(range + 1) + level.random.nextInt(range + 1);
                } else {
                    if (failedAttempts > 4) {
                        failedAttempts = 4;
                    }
                    range = blueprint.getRange() * (failedAttempts + 1) / 5;
                    x = x - level.random.nextInt(range + 1) + level.random.nextInt(range + 1);
                    z = z - level.random.nextInt(range + 1) + level.random.nextInt(range + 1);
                }
                if (blueprint.terraform((ServerLevel) level, x, z, worldPosition.getY())) {
                    energy -= blueprint.getConsume();
                    consumptionMeter.record(level, blueprint.getConsume());
                    failedAttempts = 0;
                    lastX = x;
                    lastZ = z;
                    hasLast = true;
                } else {
                    energy -= blueprint.getConsume() / 10;
                    consumptionMeter.record(level, blueprint.getConsume() / 10);
                    failedAttempts++;
                    hasLast = false;
                }
                setChanged();
            }
        }
        if (newActive) {
            inactiveTicks = 0;
            setActive(true);
        } else if (isActive() && inactiveTicks++ > 30) {
            setActive(false);
        }
    }

    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, Ic2Sounds.TERRAFORMER_LOOP.get(), isActive());
    }

    /** IC2 {@code ejectBlueprint}: drops the blueprint as an item at the terraformer. */
    public boolean ejectBlueprint() {
        ItemStack stack = inventory.get(0);
        if (stack.isEmpty()) {
            return false;
        }
        if (level != null && !level.isClientSide()) {
            StackUtil.dropAsEntity(level, worldPosition, stack);
            inventory.set(0, ItemStack.EMPTY);
        }
        return true;
    }

    public void insertBlueprint(ItemStack blueprint) {
        ejectBlueprint();
        inventory.set(0, blueprint);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
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
        setChanged();
        if (energy + amount > MAX_ENERGY) {
            int unused = energy + amount - MAX_ENERGY;
            energy = MAX_ENERGY;
            return unused;
        }
        energy += amount;
        return 0;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    // --- persistence (IC2 keeps only the energy and the inventory) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
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
