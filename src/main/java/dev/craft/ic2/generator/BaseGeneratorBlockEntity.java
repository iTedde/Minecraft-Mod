package dev.craft.ic2.generator;

import java.util.Random;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.machine.MachineSoundHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Common generator logic, 1:1 with IC2 {@code TileEntityBaseGenerator}: fuel → internal storage → charge slot and
 * one packet per tick into the energy net.
 */
public abstract class BaseGeneratorBlockEntity extends MachineBlockEntity implements EnergySource {
    protected static final Random RANDOM = new Random();
    public static final int TIER = 1;

    protected int fuel;
    protected int storage;
    protected final int maxStorage;
    protected int production;
    private int ticksSinceLastActiveUpdate;
    private int activityMeter;

    protected BaseGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots,
                                       int production, int maxStorage) {
        super(type, pos, state, slots);
        this.production = production;
        this.maxStorage = maxStorage;
        this.ticksSinceLastActiveUpdate = RANDOM.nextInt(256);
    }

    public int getFuel() {
        return fuel;
    }

    public int getStorage() {
        return storage;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public int getProduction() {
        return production;
    }

    @Override
    public void serverTick() {
        boolean needsInvUpdate = false;
        if (needsFuel()) {
            needsInvUpdate = gainFuel();
        }
        boolean newActive = gainEnergy();
        if (isRemoved()) {
            return;
        }
        if (storage > maxStorage) {
            storage = maxStorage;
        }
        if (storage > 0) {
            ItemStack chargeStack = inventory.get(0);
            if (ElectricItems.isElectric(chargeStack)) {
                int used = ElectricItems.charge(chargeStack, storage, TIER, false, false);
                storage -= used;
                if (used > 0) {
                    needsInvUpdate = true;
                }
            }
            int output = Math.min(production, storage);
            if (output > 0) {
                storage += EnergyNets.emit(level, this, output) - output;
            }
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        }
        if (!delayActiveUpdate()) {
            setActive(newActive);
        } else {
            if (ticksSinceLastActiveUpdate % 256 == 0) {
                setActive(activityMeter > 0);
                activityMeter = 0;
            }
            if (newActive) {
                activityMeter++;
            } else {
                activityMeter--;
            }
            ticksSinceLastActiveUpdate++;
        }
    }

    public boolean gainEnergy() {
        if (isConverting()) {
            storage += production;
            fuel--;
            return true;
        }
        return false;
    }

    public boolean isConverting() {
        return fuel > 0 && storage + production <= maxStorage;
    }

    public boolean needsFuel() {
        return fuel <= 0 && storage + production <= maxStorage;
    }

    public abstract boolean gainFuel();

    /** Loop sound while the generator is active (IC2 {@code getOperationSoundFile}), or {@code null}. */
    @Nullable
    public SoundEvent getOperationSound() {
        return null;
    }

    @Override
    public void clientTick() {
        SoundEvent sound = getOperationSound();
        if (sound != null) {
            MachineSoundHooks.updateLoop(this, sound, isActive());
        }
    }

    /** Solar panel, water mill and wind mill only switch their active texture every 256 ticks. */
    public boolean delayActiveUpdate() {
        return false;
    }

    /** IC2 {@code getContainerItemStack}: the stack that replaces a used-up fuel container, or empty. */
    protected static ItemStack consumeOne(ItemStack stack) {
        if (stack.hasCraftingRemainingItem()) {
            return stack.getCraftingRemainingItem();
        }
        stack.shrink(1);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
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
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return production;
    }

    // --- wrench / sides ---

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    /** IC2 generators: bottom → slot 1 (fuel), other sides → slot 0 (charge). */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN && inventory.getSlots() > 1 ? 1 : 0;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel = tag.getInt("fuel");
        storage = tag.getShort("storage");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fuel", fuel);
        tag.putShort("storage", (short) storage);
    }

    /** True for the lava cell, which generators consume without returning an empty cell. */
    protected static boolean isLavaCell(ItemStack stack) {
        return stack.is(Ic2Items.LAVA_CELL.get());
    }
}
