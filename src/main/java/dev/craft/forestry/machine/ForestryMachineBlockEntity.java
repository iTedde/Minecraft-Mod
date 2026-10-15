package dev.craft.forestry.machine;

import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.ic2.machine.MachineInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Forestry {@code TileMachine}: a BuildCraft power receptor that runs one machine work step every 5 ticks while the
 * activation energy is stored, and uses exactly that energy when the step did something.
 */
public abstract class ForestryMachineBlockEntity extends BlockEntity implements PowerReceptor, MenuProvider {
    protected final PowerProvider provider = new PowerProvider();
    protected final MachineInventory inventory;

    /** {@code EnergyConfiguration(latency, minReceived, maxReceived, activation, maxEnergy)}; latency is 0 in the pack. */
    protected ForestryMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize,
                                         int minEnergyReceived, int maxEnergyReceived, int activationEnergy,
                                         int maxEnergy) {
        super(type, pos, state);
        this.inventory = new MachineInventory(inventorySize, this::onInventoryChanged);
        provider.configure(minEnergyReceived, maxEnergyReceived, activationEnergy, maxEnergy);
    }

    public MachineInventory getInventory() {
        return inventory;
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    /** {@code TileMachine.doWork}: called every tick by the provider while the activation energy is stored. */
    @Override
    public void doWork() {
        if (level.getGameTime() % 5L == 0L) {
            int activation = provider.getActivationEnergy();
            if (machineWork()) {
                provider.useEnergy(activation, activation, true);
            }
        }
    }

    /** {@code TileForestry.updateEntity}: the power provider first, then the machine. */
    public void serverTick() {
        provider.update(this, level);
        updateServerSide();
    }

    /** {@code Machine.doWork}: one work step; {@code true} uses the activation energy. */
    protected abstract boolean machineWork();

    /** {@code Machine.updateServerSide}: runs every tick. */
    protected abstract void updateServerSide();

    protected void onInventoryChanged() {
        setChanged();
    }

    /** Slots dropped when the block is broken ({@code BlockForestry.breakBlock} drops the {@code IInventory}). */
    protected abstract int[] droppedSlots();

    public void dropContents() {
        for (int slot : droppedSlots()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    inventory.getStackInSlot(slot));
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", inventory.serializeNBT(registries));
        tag.put("power", provider.save());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
    }
}
