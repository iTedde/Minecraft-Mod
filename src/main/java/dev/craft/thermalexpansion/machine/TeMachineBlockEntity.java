package dev.craft.thermalexpansion.machine;

import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeConnections;
import dev.craft.buildcraft.transport.TravelingItem;
import dev.craft.ic2.machine.MachineInventory;
import dev.craft.thermalexpansion.TeRedstoneControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Thermal Expansion {@code TileMachineRoot} + {@code TileMachinePower} (spec 26 §4): side groups, redstone control,
 * the MJ buffer ({@code PowerProviderAdv}) and the process loop. Subclasses implement the recipe hooks. Machines
 * without power (extruder, accumulator) override {@link #serverTick()}.
 */
public abstract class TeMachineBlockEntity extends BlockEntity implements MenuProvider, PowerReceptor,
        TeRedstoneControl {
    /** Side groups for the model: one byte per direction, packed. */
    public static final ModelProperty<Long> SIDES = new ModelProperty<>();
    /** {@code TileMachineRoot.updateIfChanged}: the client sees "inactive" only 200 ticks after the machine stopped. */
    private static final int INACTIVE_DELAY = 200;

    protected final TeMachineType type;
    protected final MachineInventory inventory;
    protected final PowerProvider provider = new PowerProvider();
    protected byte[] sides;
    protected boolean redstoneDisable;
    protected boolean redstoneState;
    protected boolean active;
    private boolean wasActive;
    private long inactiveSince;
    protected float processCur;
    protected float processEnd;
    protected ItemStack processInput = ItemStack.EMPTY;

    protected TeMachineBlockEntity(BlockEntityType<?> blockEntityType, TeMachineType type, BlockPos pos,
                                   BlockState state) {
        super(blockEntityType, pos, state);
        this.type = type;
        this.inventory = new MachineInventory(type.getSlots(), this::setChanged);
        this.sides = type.getDefaultSides();
        // PowerProviderAdv: no minimum, no activation energy, no power loss (72000 ticks regularity, loss 0).
        provider.configure(0, 10 * type.getMaxPower(), 0, type.getMaxEnergy());
        provider.configurePowerPerdition(0, 72000);
    }

    public TeMachineType getMachineType() {
        return type;
    }

    public MachineInventory getInventory() {
        return inventory;
    }

    public Direction getFacing() {
        return getBlockState().getValue(TeMachineBlock.FACING);
    }

    public boolean isActive() {
        return active;
    }

    public float getProcessCur() {
        return processCur;
    }

    public float getProcessEnd() {
        return processEnd;
    }

    public float getEnergy() {
        return provider.getEnergyStored();
    }

    public byte getSide(Direction side) {
        return sides[side.get3DDataValue()];
    }

    public boolean getRedstoneDisable() {
        return redstoneDisable;
    }

    public boolean getRedstoneState() {
        return redstoneState;
    }

    // --- energy (PowerProviderAdv) ---

    protected void addEnergy(float amount) {
        provider.setEnergyStored(Math.min(provider.getMaxEnergyStored(), provider.getEnergyStored() + amount));
    }

    protected void subtractEnergy(float amount) {
        provider.setEnergyStored(Math.max(0.0F, provider.getEnergyStored() - amount));
    }

    /** {@code getPower}: max above 80 % of the buffer, min below 20 %, linear (0.1 steps) in between. */
    public float getPower() {
        if (!active) {
            return 0.0F;
        }
        float stored = provider.getEnergyStored();
        if (stored > type.getMaxPowerLevel()) {
            return type.getMaxPower();
        }
        if (stored < type.getMinPowerLevel()) {
            return type.getMinPower();
        }
        int intPower = (int) (10.0F * stored / type.getEnergyRamp());
        return intPower / 10.0F;
    }

    /** {@code getScaledSpeed}: the power the buffer would allow, for the GUI flame. */
    public float getSpeedLevel() {
        float power = provider.getEnergyStored() / type.getEnergyRamp();
        power = Math.max(type.getMinPower(), Math.min(type.getMaxPower(), power));
        return power / type.getMaxPower();
    }

    /** {@code null} for the extruder and the accumulator ({@code TileMachineRoot} has no provider). */
    @Override
    public PowerProvider getPowerProvider() {
        return type.isPowered() ? provider : null;
    }

    @Override
    public void doWork() {
    }

    @Override
    public int powerRequest() {
        if (provider.getEnergyStored() == provider.getMaxEnergyStored()) {
            return 0;
        }
        return (int) Math.ceil(Math.min(provider.getMaxEnergyReceived(),
                provider.getMaxEnergyStored() - provider.getEnergyStored()));
    }

    // --- redstone ({@code TileTERoot}) ---

    /** {@code redstoneControlOrDisable}: ignored, or the signal equals the wanted state. */
    protected boolean redstoneControlOrDisable() {
        return redstoneDisable || level.hasNeighborSignal(worldPosition) == redstoneState;
    }

    public void setRedstone(boolean disable, boolean state) {
        redstoneDisable = disable;
        redstoneState = state;
        sync();
    }

    // --- process ({@code TileMachinePower.updateEntity}) ---

    protected abstract boolean canStart();

    protected abstract boolean canFinish();

    protected abstract void processStart();

    protected abstract void processFinish();

    protected abstract void transferProducts();

    public void serverTick() {
        provider.update(this, level);
        boolean curActive = active;
        if (active) {
            if (processCur < processEnd) {
                float energy = getPower();
                subtractEnergy(energy);
                processCur += energy;
            }
            if (canFinish()) {
                processFinish();
                transferProducts();
                addEnergy(processCur % processEnd);
                provider.setEnergyStored(Math.round(provider.getEnergyStored()));
                processCur = 0.0F;
                if (redstoneControlOrDisable() && canStart()) {
                    processStart();
                    processCur += provider.useEnergy(0.0F, getPower(), true);
                } else {
                    processCur = 0.0F;
                    markInactive();
                }
            }
        } else if (redstoneControlOrDisable()) {
            if (level.getGameTime() % 40L == 0L) {
                transferProducts();
            }
            if (canStart()) {
                processStart();
                active = true;
                processCur += provider.useEnergy(0.0F, getPower(), true);
            }
        }
        updateIfChanged(curActive);
        setChanged();
    }

    /** The process stopped: {@code isActive = false; wasActive = true; tracker.markTime}. */
    protected void markInactive() {
        active = false;
        wasActive = true;
        inactiveSince = level.getGameTime();
    }

    protected void updateIfChanged(boolean curActive) {
        if (curActive != active && active) {
            setActiveState(true);
        } else if (wasActive && level.getGameTime() - inactiveSince >= INACTIVE_DELAY) {
            wasActive = false;
            setActiveState(active);
        }
    }

    private void setActiveState(boolean value) {
        BlockState state = getBlockState();
        if (state.getValue(TeMachineBlock.ACTIVE) != value) {
            level.setBlock(worldPosition, state.setValue(TeMachineBlock.ACTIVE, value), Block.UPDATE_ALL);
        }
    }

    /** {@code getScaledProgress}. */
    public float getProgressLevel() {
        return processEnd == 0.0F ? 0.0F : processCur / processEnd;
    }

    // --- liquids and GUI data of the subclasses ---

    /** The fluid capability on {@code side} ({@code null} = unknown side, e.g. a bucket), or {@code null}. */
    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return null;
    }

    /** {@code BlockMachine.onBlockActivated}: extruder and precipitator take liquid from a held container. */
    public boolean acceptsContainers() {
        return false;
    }

    /** Tank {@code index} for the GUI (0 = the gauge on the right). */
    public FluidStack getDisplayFluid(int index) {
        return FluidStack.EMPTY;
    }

    /** Machine-specific GUI state (transposer mode, selected product), synced as one int. */
    public int getModeBits() {
        return 0;
    }

    // --- output ({@code TileInventory.ejectItem}) ---

    /** Up to {@code qty} items of {@code slot} into an adjacent item pipe, else an adjacent inventory. */
    protected boolean ejectItem(int slot, int qty, Direction side) {
        ItemStack stack = inventory.get(slot);
        if (stack.isEmpty()) {
            return false;
        }
        int toSend = Math.min(qty, stack.getCount());
        BlockPos adjacent = worldPosition.relative(side);
        if (level.getBlockEntity(adjacent) instanceof ItemPipeBlockEntity pipe
                && PipeConnections.canConnect(level, adjacent, side.getOpposite())) {
            ItemStack sent = stack.copyWithCount(toSend);
            pipe.entityEntering(new TravelingItem(sent, worldPosition.getX() + 0.5 + side.getStepX() * 0.5,
                    worldPosition.getY() + ItemPipeBlockEntity.PIPE_FLOOR + side.getStepY() * 0.5,
                    worldPosition.getZ() + 0.5 + side.getStepZ() * 0.5), side);
            stack.shrink(toSend);
            inventory.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            return true;
        }
        IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, adjacent, side.getOpposite());
        if (target == null || target.getSlots() == 0) {
            return false;
        }
        ItemStack rest = ItemHandlerHelper.insertItemStacked(target, stack.copyWithCount(toSend), false);
        stack.shrink(toSend - rest.getCount());
        inventory.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        return true;
    }

    /** True if {@code stack} fits into {@code slot} completely (null-safe {@code isItemEqual} + stack limit). */
    protected boolean fits(int slot, ItemStack stack) {
        ItemStack inSlot = inventory.get(slot);
        if (inSlot.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(inSlot, stack)
                && inSlot.getCount() + stack.getCount() <= stack.getMaxStackSize();
    }

    protected void addToSlot(int slot, ItemStack stack) {
        ItemStack inSlot = inventory.get(slot);
        if (inSlot.isEmpty()) {
            inventory.set(slot, stack);
        } else {
            inSlot.grow(stack.getCount());
            inventory.set(slot, inSlot);
        }
    }

    // --- sides ({@code IReconfigurableSides}, {@code ISidedInventory}) ---

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        int group = sides[side.get3DDataValue()];
        int size = type.getAccessSize(group);
        if (size <= 0) {
            return EmptyItemHandler.INSTANCE;
        }
        int start = type.getStart(group);
        return new RangedWrapper(inventory, start, start + size);
    }

    public boolean hasSide(int group) {
        for (byte side : sides) {
            if (side == group) {
                return true;
            }
        }
        return false;
    }

    /** {@code incrSide}/{@code decrSide}; the front cannot be changed. */
    public boolean changeSide(Direction side, boolean increment) {
        if (side == getFacing()) {
            return false;
        }
        int n = type.getNumGroups();
        int index = side.get3DDataValue();
        sides[index] = (byte) ((sides[index] + (increment ? 1 : n - 1)) % n);
        sidesChanged();
        return true;
    }

    /** {@code setSide(side, 0)} (shift-click in the configuration tab); the front cannot be changed. */
    public boolean clearSide(Direction side) {
        int index = side.get3DDataValue();
        if (side == getFacing() || sides[index] == 0) {
            return false;
        }
        sides[index] = 0;
        sidesChanged();
        return true;
    }

    /** Syncs the new side groups and lets adjacent pipes recompute their connections. */
    private void sidesChanged() {
        sync();
        if (level != null && !level.isClientSide()) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    /** {@code resetSides}: every side back to group 0. */
    public boolean resetSides() {
        boolean update = false;
        for (int i = 0; i < 6; i++) {
            if (sides[i] > 0) {
                sides[i] = 0;
                update = true;
            }
        }
        if (update) {
            sidesChanged();
        }
        return update;
    }

    /** {@code setFacing} on placement: front group 0, left and back input groups (1, smelter 5/6), rest the defaults. */
    public void initFacing(Direction facing) {
        int f = facing.get3DDataValue();
        sides[f] = 0;
        sides[TeSides.LEFT[f]] = type.getLeftGroup();
        sides[TeSides.OPPOSITE[f]] = type.getBackGroup();
        sync();
    }

    /** {@code rotateBlock}: not while active; the side groups turn with the machine. */
    public boolean rotate() {
        if (active) {
            return false;
        }
        byte tempWest = sides[4];
        sides[4] = sides[3];
        sides[3] = sides[5];
        sides[5] = sides[2];
        sides[2] = tempWest;
        Direction facing = Direction.from3DDataValue(TeSides.LEFT[getFacing().get3DDataValue()]);
        level.setBlock(worldPosition, getBlockState().setValue(TeMachineBlock.FACING, facing), Block.UPDATE_ALL);
        sync();
        return true;
    }

    protected void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            if (level.isClientSide()) {
                requestModelDataUpdate();
            }
        }
    }

    @Override
    public ModelData getModelData() {
        long packed = 0;
        for (int i = 0; i < 6; i++) {
            packed |= (long) (sides[i] & 0xFF) << (8 * i);
        }
        return ModelData.builder().with(SIDES, packed).build();
    }

    public void dropContents() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inventory.set(i, ItemStack.EMPTY);
            }
        }
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- persistence and client sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag);
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        active = tag.getBoolean("flag.active");
        processCur = tag.getFloat("process.cur");
        processEnd = tag.getFloat("process.end");
        processInput = ItemStack.parseOptional(registries, tag.getCompound("process.inv"));
        float energy = tag.getFloat("energy.stored");
        provider.setEnergyStored(Float.isNaN(energy) ? 0.0F : Math.min(energy, provider.getMaxEnergyStored()));
    }

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("side.array", Tag.TAG_BYTE_ARRAY)) {
            byte[] stored = tag.getByteArray("side.array");
            for (int i = 0; i < 6 && i < stored.length; i++) {
                sides[i] = stored[i] < type.getNumGroups() ? stored[i] : 0;
            }
        }
        redstoneDisable = tag.getBoolean("red.disable");
        redstoneState = tag.getBoolean("red.state");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag);
        tag.put("Items", inventory.serializeNBT(registries));
        tag.putBoolean("flag.active", active);
        tag.putFloat("process.cur", processCur);
        tag.putFloat("process.end", processEnd);
        if (!processInput.isEmpty()) {
            tag.put("process.inv", processInput.save(registries));
        }
        tag.putFloat("energy.stored", provider.getEnergyStored());
    }

    private void saveClientData(CompoundTag tag) {
        tag.putByteArray("side.array", sides);
        tag.putBoolean("red.disable", redstoneDisable);
        tag.putBoolean("red.state", redstoneState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveClientData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag);
        requestModelDataUpdate();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
