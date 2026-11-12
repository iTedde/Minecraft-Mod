package dev.craft.nuclearcontrol.block;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.machine.Wrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Energy flow of the energy and average counter ({@code TileEntityEnergyCounter}/{@code TileEntityAverageCounter},
 * spec 46 §7.1/§7.2): input only on the front ({@link CounterBlock#FACING}), output on the other five sides. Packet
 * size {@code 32 · 4^transformer upgrades} (one slot, at most 4 count), buffer {@code 2 × packet}; every tick one full
 * packet leaves when the buffer holds it. Larger packets remove the block and cause a plain explosion of 0.8.
 */
public abstract class CounterBlockEntity extends BlockEntity implements EnergySink, EnergySource, Wrenchable,
        MenuProvider {
    public static final int BASE_PACKET_SIZE = 32;
    /** {@code getMaxEnergyOutput}: fixed, independent of the packet size. */
    public static final int MAX_OUTPUT = 4096;
    public static final float EXPLOSION_POWER = 0.8F;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Ic2Items.TRANSFORMER_UPGRADE.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updatePacketSize();
        }
    };
    private int storage;
    private int packetSize = BASE_PACKET_SIZE;

    protected CounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getStorage() {
        return storage;
    }

    public void serverTick() {
        if (storage >= packetSize) {
            int sent = packetSize - EnergyNets.emit(level, this, packetSize);
            storage -= sent;
            onSent(sent);
            setChanged();
        }
    }

    /** EU that actually left this tick. */
    protected abstract void onSent(int amount);

    /** {@code onInventoryChanged}: transformer upgrades in the slot, at most 4. */
    private void updatePacketSize() {
        ItemStack stack = inventory.getStackInSlot(0);
        int upgrades = stack.is(Ic2Items.TRANSFORMER_UPGRADE.get()) ? Math.min(stack.getCount(), 4) : 0;
        packetSize = BASE_PACKET_SIZE * (int) Math.pow(4.0, upgrades);
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        updatePacketSize();
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
        return direction == getFacing();
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return direction != getFacing();
    }

    @Override
    public int getMaxEnergyOutput() {
        return MAX_OUTPUT;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : 2 * packetSize - storage;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > packetSize) {
            BlockPos pos = worldPosition;
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.BLOCK);
            return 0;
        }
        storage += amount;
        int left = 0;
        if (storage > 2 * packetSize) {
            left = storage - 2 * packetSize;
            storage = 2 * packetSize;
        }
        setChanged();
        return left;
    }

    @Override
    public int getMaxSafeInput() {
        return packetSize;
    }

    // --- wrench: the front is the clicked side; always drops itself ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(CounterBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return getFacing() != side;
    }

    /** Input and output sides swap, so the counter re-registers in the energy net. */
    @Override
    public void setFacing(Direction facing) {
        EnergyNets.remove(level, this);
        level.setBlock(worldPosition, getBlockState().setValue(CounterBlock.FACING, facing), Block.UPDATE_ALL);
        EnergyNets.add(level, this);
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storage = tag.getInt("storage");
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        updatePacketSize();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("storage", storage);
        tag.put("Items", inventory.serializeNBT(registries));
    }
}
