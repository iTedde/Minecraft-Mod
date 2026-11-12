package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.NcDataComponents;
import dev.craft.nuclearcontrol.NcItems;
import dev.craft.nuclearcontrol.menu.RemoteThermalMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Remote thermal monitor ({@code TileEntityRemoteThermo}, spec 46 §3.2): an EU sink (600 EU + 10 000 per storage
 * upgrade, packets up to {@code 32 · 4^transformer upgrades}, 1 EU per tick) that reads the hull heat of the reactor
 * on its sensor location card. Slots: 0 battery, 1 card, 2–4 upgrades.
 */
public class RemoteThermalMonitorBlockEntity extends ThermalMonitorBlockEntity implements EnergySink,
        ConsumptionReadout {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_CARD = 1;
    public static final int BASE_STORAGE = 600;
    public static final int STORAGE_PER_UPGRADE = 10000;
    public static final int ENERGY_SU_BATTERY = 1000;
    /** {@code remoteThermalMonitorEnergyConsumption} of the pack config. */
    public static final int CONSUMPTION = 1;
    public static final float EXPLOSION_POWER = 0.8F;

    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return RemoteThermalMonitorBlockEntity.this.isItemValid(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                updateUpgrades();
            }
        }
    };
    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    private int maxStorage = BASE_STORAGE;
    private int tier = 1;
    private int maxPacketSize = 32;
    private BlockPos delta = BlockPos.ZERO;
    /** Display rotation on the front, 0–3 ({@code IRotation}). */
    private int rotation;

    public RemoteThermalMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.REMOTE_THERMAL_MONITOR.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /** {@code isItemValid}: battery up to the own tier or single-use battery; reactor card; IC2/range upgrades. */
    public boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_BATTERY -> stack.is(Ic2Items.SU_BATTERY.get())
                    || stack.getItem() instanceof ElectricItem item && item.canProvideEnergy() && item.getTier() <= tier;
            case SLOT_CARD -> stack.is(NcItems.REACTOR_SENSOR_LOCATION_CARD.get());
            default -> stack.is(Ic2Items.TRANSFORMER_UPGRADE.get()) || stack.is(Ic2Items.ENERGY_STORAGE_UPGRADE.get())
                    || stack.is(NcItems.RANGE_UPGRADE.get());
        };
    }

    @Override
    public void serverTick() {
        super.serverTick();
        ItemStack battery = inventory.getStackInSlot(SLOT_BATTERY);
        if (!battery.isEmpty() && energy < maxStorage) {
            if (battery.getItem() instanceof ElectricItem item) {
                if (item.canProvideEnergy()) {
                    energy += ElectricItems.discharge(battery, maxStorage - energy, tier, false, false);
                }
            } else if (battery.is(Ic2Items.SU_BATTERY.get())
                    && (ENERGY_SU_BATTERY <= maxStorage - energy || energy == 0)) {
                battery.shrink(1);
                energy = Math.min(maxStorage, energy + ENERGY_SU_BATTERY);
            }
        }
        if (energy >= CONSUMPTION) {
            energy -= CONSUMPTION;
            consumptionMeter.record(level, CONSUMPTION);
        } else {
            energy = 0;
        }
        setChanged();
    }

    @Override
    protected void checkStatus() {
        updateUpgrades();
        int fire;
        if (energy >= CONSUMPTION) {
            NuclearReactorBlockEntity reactor = targetReactor();
            if (reactor != null) {
                startTicking();
                fire = reactor.getHeat();
            } else {
                fire = -1;
            }
        } else {
            fire = -2;
        }
        setOnFire(fire);
    }

    /**
     * {@code getReactorAt(card target)}. Unlike 1.4.7, an unloaded target chunk is not loaded but counts as "no
     * reactor".
     */
    @Nullable
    private NuclearReactorBlockEntity targetReactor() {
        BlockPos target = worldPosition.offset(delta);
        if (!level.isLoaded(target)) {
            return null;
        }
        return level.getBlockEntity(target) instanceof NuclearReactorBlockEntity reactor ? reactor : null;
    }

    /** Remote: redstone at {@code onFire >= heatLevel}. */
    @Override
    protected boolean isTriggered() {
        return onFire >= heatLevel;
    }

    /** {@code onInventoryChanged}: tier, packet size, storage and the card target within range. */
    public void updateUpgrades() {
        int transformers = 0;
        int storage = 0;
        int range = 0;
        for (int slot = 2; slot < 5; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.is(Ic2Items.TRANSFORMER_UPGRADE.get())) {
                transformers += stack.getCount();
            } else if (stack.is(Ic2Items.ENERGY_STORAGE_UPGRADE.get())) {
                storage += stack.getCount();
            } else if (stack.is(NcItems.RANGE_UPGRADE.get())) {
                range += stack.getCount();
            }
        }
        BlockPos target = inventory.getStackInSlot(SLOT_CARD).get(NcDataComponents.SENSOR_TARGET.get());
        delta = BlockPos.ZERO;
        if (target != null) {
            int reach = range(range);
            BlockPos offset = target.subtract(worldPosition);
            if (Math.abs(offset.getX()) <= reach && Math.abs(offset.getY()) <= reach
                    && Math.abs(offset.getZ()) <= reach) {
                delta = offset;
            }
        }
        transformers = Math.min(transformers, 4);
        tier = transformers + 1;
        maxPacketSize = 32 * (int) Math.pow(4.0, transformers);
        int oldMax = maxStorage;
        maxStorage = BASE_STORAGE + STORAGE_PER_UPGRADE * storage;
        energy = Math.min(energy, maxStorage);
        if (oldMax != maxStorage) {
            sync();
        }
    }

    /** {@code 8 · 2^n} blocks per axis, at most 7 range upgrades count. */
    public static int range(int upgrades) {
        return 8 * (int) Math.pow(2.0, Math.min(upgrades, 7));
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide()) {
            updateUpgrades();
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
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : maxStorage - energy;
    }

    /** Too large a packet removes the block and causes a plain explosion of strength 0.8. */
    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > maxPacketSize) {
            BlockPos pos = worldPosition;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
            return 0;
        }
        energy += amount;
        int left = 0;
        if (energy > maxStorage) {
            left = energy - maxStorage;
            energy = maxStorage;
        }
        setChanged();
        return left;
    }

    @Override
    public int getMaxSafeInput() {
        return maxPacketSize;
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public int getTier() {
        return tier;
    }

    public int getRotation() {
        return rotation;
    }

    /** {@code IRotation.rotate}: 0 → 1 → 3 → 2 → 0. */
    public void rotate() {
        rotation = switch (rotation) {
            case 0 -> 1;
            case 1 -> 3;
            case 3 -> 2;
            default -> 0;
        };
        setChanged();
        sync();
    }

    // --- wrench: turn with a plain click, dismantle without sneaking ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(RemoteThermalMonitorBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return !player.isShiftKeyDown() && getFacing() != side;
    }

    @Override
    public void setFacing(Direction facing) {
        level.setBlock(worldPosition, getBlockState().setValue(RemoteThermalMonitorBlock.FACING, facing),
                Block.UPDATE_ALL);
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        return !player.isShiftKeyDown();
    }

    // --- menu ---

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new RemoteThermalMonitorMenu(id, playerInventory, this);
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        maxStorage = tag.contains("maxStorage") ? tag.getInt("maxStorage") : BASE_STORAGE;
        rotation = tag.getInt("rotation");
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putInt("maxStorage", maxStorage);
        tag.putInt("rotation", rotation);
        tag.put("Items", inventory.serializeNBT(registries));
    }
}
