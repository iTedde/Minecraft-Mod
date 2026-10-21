package dev.craft.thermalexpansion.energy;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.ic2.machine.MachineInventory;
import dev.craft.thermalexpansion.TeRedstoneControl;
import dev.craft.thermalexpansion.transport.GridPowerProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEngineRoot} (spec 26 §6.1): burns fuel into its MJ buffer at {@link #getPower()} per tick and hands at most
 * {@code maxCycleEnergy} per piston stroke (at half stroke, 25 ticks per stroke) to the MJ receiver in front of it.
 * It only runs while a receiver is in front and the redstone condition holds; idle it loses {@code minPower} per tick
 * and 10 MJ of burning fuel every 40 ticks. A full buffer breaks it ({@code shutdownEngine}: fuel gone, buffer −20 %,
 * −{@code minPower} per tick) until it is repaired with a wrench. Light 7 while active.
 */
public abstract class TeEngineBlockEntity extends BlockEntity implements MenuProvider, PowerReceptor,
        TeRedstoneControl {
    /** Stroke progress per tick. */
    private static final float CYCLE_STEP = 0.04F;

    protected final TeEngineType type;
    protected final MachineInventory inventory;
    protected final PowerProvider provider = new PowerProvider();
    protected boolean active;
    protected boolean shutdown;
    private boolean canCycle;
    private boolean stageCycle;
    protected float fuelMJ;
    private float cycleProgress;
    private boolean redstoneDisable;
    private boolean redstoneState;

    protected TeEngineBlockEntity(BlockEntityType<?> blockEntityType, TeEngineType type, int slots, BlockPos pos,
                                  BlockState state) {
        super(blockEntityType, pos, state);
        this.type = type;
        this.inventory = new MachineInventory(slots, this::setChanged);
        // PowerProviderAdv.configure(maxCycleEnergy / 2, maxEnergy): no loss, no work callback.
        provider.configure(0, type.getMaxCycleEnergy() / 2, 0, type.getMaxEnergy());
        provider.configurePowerPerdition(0, 72000);
    }

    public TeEngineType getEngineType() {
        return type;
    }

    public MachineInventory getInventory() {
        return inventory;
    }

    public Direction getFacing() {
        return getBlockState().getValue(TeEngineBlock.FACING);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public float getCycleProgress() {
        return cycleProgress;
    }

    public float getEnergy() {
        return provider.getEnergyStored();
    }

    public float getFuel() {
        return fuelMJ;
    }

    /** {@code getScaledDuration}: fuel left of the current fuel item (or lava portion). */
    public abstract float getDurationLevel();

    /** The tank for the GUI. */
    public abstract FluidStack getTankFluid();

    /** The fluid capability; nothing can be drained, the front takes nothing ({@code null} = unknown side). */
    @Nullable
    public abstract IFluidHandler getFluidHandler(@Nullable Direction side);

    protected abstract boolean canProcess();

    protected abstract void processActive();

    // --- power ---

    protected void addEnergy(float amount) {
        provider.setEnergyStored(Math.min(provider.getMaxEnergyStored(), provider.getEnergyStored() + amount));
    }

    protected void subtractEnergy(float amount) {
        provider.setEnergyStored(Math.max(0.0F, provider.getEnergyStored() - amount));
    }

    /** {@code getPower}: full power below 10 % of the buffer, minimum above 90 %, linear (0.1 steps) in between. */
    public float getPower() {
        if (!active || shutdown) {
            return 0.0F;
        }
        float stored = provider.getEnergyStored();
        if (stored < type.getMaxPowerLevel()) {
            return type.getMaxPower();
        }
        if (stored > type.getMinPowerLevel()) {
            return type.getMinPower();
        }
        int intPower = (int) (10.0F * (provider.getMaxEnergyStored() - stored) / type.getEnergyRamp());
        return intPower / 10.0F;
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    @Override
    public void doWork() {
    }

    @Nullable
    private PowerReceptor receptorInFront() {
        Direction facing = getFacing();
        return level.getCapability(BcCapabilities.POWER_RECEPTOR, worldPosition.relative(facing),
                facing.getOpposite());
    }

    /** {@code canCycle}: an MJ receiver in front. */
    private boolean updateCanCycle() {
        canCycle = receptorInFront() != null;
        return canCycle;
    }

    /**
     * {@code transferEnergy}: fill the receiver in front, at most {@code maxCycleEnergy}. An energy conduit network
     * ({@code isPoweredTileLoss}) gets what it asks for, but at least {@code 25 · minPower}, whatever fits.
     */
    private void transferEnergy() {
        PowerReceptor receptor = receptorInFront();
        PowerProvider target = receptor != null ? receptor.getPowerProvider() : null;
        if (target == null) {
            return;
        }
        if (target instanceof GridPowerProvider) {
            float adjustedEnergyRequest = Math.max(receptor.powerRequest(), 25.0F * type.getMinPower());
            float energy = provider.useEnergy(0.0F, Math.min(adjustedEnergyRequest, type.getMaxCycleEnergy()), true);
            target.receiveEnergy(energy, getFacing().getOpposite());
            return;
        }
        float energyMax = Math.min(target.getMaxEnergyStored() - target.getEnergyStored(), type.getMaxCycleEnergy());
        float energy = provider.useEnergy(0.0F, energyMax, true);
        target.receiveEnergy(energy, getFacing().getOpposite());
    }

    /** {@code shutdownEngine}. */
    protected void shutdownEngine() {
        active = false;
        shutdown = true;
        fuelMJ = 0.0F;
        provider.setEnergyStored(provider.getEnergyStored() * 0.8F);
    }

    /** {@code repairEngine} (wrench on a broken engine). */
    public void repair() {
        shutdown = false;
        cycleProgress = 0.0F;
        sync();
    }

    /** {@code rotateBlock}: the next side (from the current one) with an MJ receiver. */
    public boolean rotate() {
        int f = getFacing().get3DDataValue();
        for (int i = f + 1; i < f + 6; i++) {
            Direction side = Direction.from3DDataValue(i % 6);
            PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, worldPosition.relative(side),
                    side.getOpposite());
            if (receptor != null && receptor.getPowerProvider() != null) {
                level.setBlock(worldPosition, getBlockState().setValue(TeEngineBlock.FACING, side), Block.UPDATE_ALL);
                sync();
                return true;
            }
        }
        return false;
    }

    // --- redstone ---

    @Override
    public boolean getRedstoneDisable() {
        return redstoneDisable;
    }

    @Override
    public boolean getRedstoneState() {
        return redstoneState;
    }

    @Override
    public void setRedstone(boolean disable, boolean state) {
        redstoneDisable = disable;
        redstoneState = state;
        sync();
    }

    private boolean redstoneControlOrDisable() {
        return redstoneDisable || level.hasNeighborSignal(worldPosition) == redstoneState;
    }

    // --- ticks ---

    public void serverTick() {
        if (shutdown) {
            subtractEnergy(type.getMinPower());
            setChanged();
            return;
        }
        if (cycleProgress > 0.0F || active && canCycle) {
            cycleProgress += CYCLE_STEP;
            if (cycleProgress >= 1.0F) {
                cycleProgress = 0.0F;
                stageCycle = false;
            } else if (cycleProgress >= 0.5F && !stageCycle) {
                transferEnergy();
                stageCycle = true;
            }
        }
        boolean curActive = active;
        boolean curCycle = canCycle;
        if (!redstoneControlOrDisable() || !updateCanCycle()) {
            active = false;
        } else if (canProcess()) {
            active = true;
            processActive();
        } else {
            active = false;
        }
        if (!active) {
            subtractEnergy(type.getMinPower());
            if (level.getGameTime() % 40L == 0L && fuelMJ > 0.0F) {
                fuelMJ = Math.max(0.0F, fuelMJ - 10.0F);
            }
        }
        if (curActive != active || curCycle != canCycle || shutdown) {
            BlockState state = getBlockState();
            if (state.getValue(TeEngineBlock.ACTIVE) != active) {
                level.setBlock(worldPosition, state.setValue(TeEngineBlock.ACTIVE, active), Block.UPDATE_ALL);
            }
            sync();
        }
        setChanged();
    }

    /** The client animates the stroke from the synced flags. */
    public void clientTick() {
        if (!shutdown && (cycleProgress > 0.0F || active && canCycle)) {
            cycleProgress += CYCLE_STEP;
            if (cycleProgress >= 1.0F) {
                cycleProgress = 0.0F;
            }
        }
    }

    // --- inventory, menu, persistence ---

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

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeEngineMenu(id, playerInventory, this);
    }

    protected void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag);
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        stageCycle = tag.getBoolean("flag.stage");
        cycleProgress = tag.getFloat("cycle.progress");
    }

    private void loadClientData(CompoundTag tag) {
        float energy = tag.getFloat("energy.stored");
        provider.setEnergyStored(Float.isNaN(energy) ? 0.0F : Math.min(energy, provider.getMaxEnergyStored()));
        active = tag.getBoolean("flag.active");
        shutdown = tag.getBoolean("flag.shutdown");
        fuelMJ = tag.getFloat("qty.fuel");
        canCycle = tag.getBoolean("flag.cycle");
        redstoneDisable = tag.getBoolean("red.disable");
        redstoneState = tag.getBoolean("red.state");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag);
        tag.put("Items", inventory.serializeNBT(registries));
        tag.putBoolean("flag.stage", stageCycle);
        tag.putFloat("cycle.progress", cycleProgress);
    }

    private void saveClientData(CompoundTag tag) {
        tag.putFloat("energy.stored", provider.getEnergyStored());
        tag.putBoolean("flag.active", active);
        tag.putBoolean("flag.shutdown", shutdown);
        tag.putFloat("qty.fuel", fuelMJ);
        tag.putBoolean("flag.cycle", canCycle);
        tag.putBoolean("red.disable", redstoneDisable);
        tag.putBoolean("red.state", redstoneState);
    }

    /** The description packet ({@code getDescriptionPacket}). */
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

    /** A broken engine shows its piston a quarter out. */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag);
        if (shutdown) {
            cycleProgress = 0.25F;
        }
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
