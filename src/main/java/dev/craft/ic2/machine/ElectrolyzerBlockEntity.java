package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.menu.ElectrolyzerMenu;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code TileEntityElectrolyzer}: stores surplus energy of an adjacent BatBox/MFE/MFSU in electrolyzed water cells
 * and gives it back when the storage runs low (spec 04, electrolyzer). No energy net connection of its own.
 */
public class ElectrolyzerBlockEntity extends MachineBlockEntity {
    public static final int SLOT_WATER = 0;
    public static final int SLOT_ELECTROLYZED = 1;
    public static final int ENERGY_PER_CELL = 20000;
    /** IC2 searches below, above, −X, +X, −Z, +Z. */
    private static final Direction[] SEARCH_ORDER = {Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST,
            Direction.NORTH, Direction.SOUTH};

    /** IC2 keeps the buffer in a {@code short}. */
    private short energy;
    @Nullable
    private ElectricStorageBlockEntity storage;
    private int ticker = RandomSource.create().nextInt(16);

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.ELECTROLYZER), pos, state, 2);
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public void serverTick() {
        if (ticker++ % 16 == 0) {
            storage = lookForStorage();
        }
        if (storage == null) {
            return;
        }
        if (storage.isRemoved()) {
            storage = null;
            return;
        }
        boolean changed = false;
        boolean active = false;
        if (shouldDrain() && canDrain()) {
            changed = drain();
            active = true;
        }
        if (shouldPower() && (canPower() || energy > 0)) {
            changed = power();
            active = true;
        }
        if (isActive() != active) {
            setActive(active);
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean shouldDrain() {
        return (double) storage.getEnergy() / storage.getMaxStorage() >= 0.7;
    }

    private boolean shouldPower() {
        return (double) storage.getEnergy() / storage.getMaxStorage() <= 0.3;
    }

    private boolean canDrain() {
        ItemStack water = inventory.get(SLOT_WATER);
        ItemStack electrolyzed = inventory.get(SLOT_ELECTROLYZED);
        return water.is(Ic2Items.WATER_CELL.get()) && (electrolyzed.isEmpty()
                || electrolyzed.is(Ic2Items.ELECTROLYZED_WATER_CELL.get())
                && electrolyzed.getCount() < electrolyzed.getMaxStackSize());
    }

    private boolean canPower() {
        ItemStack water = inventory.get(SLOT_WATER);
        return (water.isEmpty() || water.is(Ic2Items.WATER_CELL.get()) && water.getCount() < water.getMaxStackSize())
                && inventory.get(SLOT_ELECTROLYZED).is(Ic2Items.ELECTROLYZED_WATER_CELL.get());
    }

    /** Charges the buffer from the storage; every 20 000 EU turns a water cell into an electrolyzed one. */
    private boolean drain() {
        storage.setEnergy(storage.getEnergy() - processRate());
        energy = (short) (energy + processRate());
        if (energy < ENERGY_PER_CELL) {
            return false;
        }
        energy = (short) (energy - ENERGY_PER_CELL);
        convert(SLOT_WATER, SLOT_ELECTROLYZED, Ic2Items.ELECTROLYZED_WATER_CELL.get().getDefaultInstance());
        return true;
    }

    /** Feeds the buffer back into the storage; an empty buffer is refilled from one electrolyzed cell. */
    private boolean power() {
        if (energy > 0) {
            int out = Math.min(processRate(), energy);
            energy = (short) (energy - out);
            storage.setEnergy(storage.getEnergy() + out);
            return false;
        }
        energy = (short) (energy + 12000 + 2000 * storage.getTier());
        convert(SLOT_ELECTROLYZED, SLOT_WATER, Ic2Items.WATER_CELL.get().getDefaultInstance());
        return true;
    }

    private void convert(int from, int to, ItemStack product) {
        ItemStack source = inventory.get(from);
        source.shrink(1);
        inventory.set(from, source.isEmpty() ? ItemStack.EMPTY : source);
        ItemStack target = inventory.get(to);
        if (target.isEmpty()) {
            inventory.set(to, product);
        } else {
            target.grow(1);
            inventory.set(to, target);
        }
    }

    /** BatBox 2, MFE 8, MFSU 32 EU per tick. */
    private int processRate() {
        return switch (storage.getTier()) {
            case 2 -> 8;
            case 3 -> 32;
            default -> 2;
        };
    }

    @Nullable
    private ElectricStorageBlockEntity lookForStorage() {
        for (Direction direction : SEARCH_ORDER) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ElectricStorageBlockEntity found) {
                return found;
            }
        }
        return null;
    }

    /** Energy gauge width 0–24 ({@code gaugeEnergyScaled}). */
    public int gaugeEnergyScaled(int width) {
        return energy <= 0 ? 0 : Math.min(width, energy * width / ENERGY_PER_CELL);
    }

    /** IC2 {@code getStartInventorySide}: bottom → electrolyzed cells, all other sides → water cells. */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN ? SLOT_ELECTROLYZED : SLOT_WATER;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectrolyzerMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getShort("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("energy", energy);
    }

    /** Test hook. */
    public void setEnergyForTest(int energy) {
        this.energy = (short) energy;
    }
}
