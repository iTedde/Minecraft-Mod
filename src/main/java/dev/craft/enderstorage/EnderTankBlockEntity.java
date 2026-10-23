package dev.craft.enderstorage;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * EnderStorage {@code TileEnderTank} (spec 27 §5): frequency and owner, the shared liquid storage, the pressure valve
 * that pushes up to 100 mB per tick into every neighbour while the redstone signal (XOR {@code invert}) is on, and
 * the liquid level and valve shown on the client.
 */
public class EnderTankBlockEntity extends BlockEntity {
    public static final int EJECT_PER_TICK = 100;

    private Frequency frequency = Frequency.GLOBAL;
    /** {@code invert_redstone} (NBT {@code ir}). */
    private boolean invert;
    /** {@code a_pressure}: redstone XOR invert, sent to the client. */
    private boolean pressure;
    private final EnderTankState liquid = new EnderTankState();
    /** Client: valve angle in degrees, -90 with pressure, 90 without. */
    private double valve;
    private double prevValve;
    private boolean described;

    public EnderTankBlockEntity(BlockPos pos, BlockState state) {
        super(EsBlockEntityTypes.ENDER_TANK.get(), pos, state);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    /** {@code setFreq}/{@code setOwner}: switches to another storage. */
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        setChanged();
        sync();
    }

    /** The shared storage; server only. */
    public EnderLiquidStorage getStorage() {
        return EnderStorageData.get(((ServerLevel) level).getServer()).liquid(frequency);
    }

    @Nullable
    public IFluidHandler getFluidHandler() {
        return level instanceof ServerLevel ? getStorage() : null;
    }

    public boolean isInverted() {
        return invert;
    }

    public boolean hasPressure() {
        return pressure;
    }

    /** {@code PressureState.invert}: the valve knob. */
    public void invert() {
        invert = !invert;
        setChanged();
    }

    public void serverTick() {
        boolean now = level.hasNeighborSignal(worldPosition) ^ invert;
        if (now != pressure) {
            pressure = now;
            sync();
        }
        if (pressure) {
            ejectLiquid();
        }
        FluidStack stored = getStorage().getFluid();
        if (liquid.serverUpdate(stored)) {
            sync();
        }
        int light = stored.isEmpty() ? 0 : stored.getFluidType().getLightLevel(stored);
        BlockState state = getBlockState();
        if (state.getValue(EnderTankBlock.LIGHT) != light) {
            level.setBlock(worldPosition, state.setValue(EnderTankBlock.LIGHT, light), Block.UPDATE_ALL);
        }
    }

    /** Every side with a liquid handler gets what it takes of a simulated 100 mB drain. */
    private void ejectLiquid() {
        EnderLiquidStorage storage = getStorage();
        for (Direction side : Direction.values()) {
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                    worldPosition.relative(side), side.getOpposite());
            if (target == null) {
                continue;
            }
            FluidStack offer = storage.drain(EJECT_PER_TICK, IFluidHandler.FluidAction.SIMULATE);
            if (offer.isEmpty()) {
                continue;
            }
            int filled = target.fill(offer, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                storage.drain(filled, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    /** Valve turns half the way per tick, at most 20°; the shown liquid glides. */
    public void clientTick() {
        prevValve = valve;
        valve = EnderTankState.approachExp(valve, targetValve(), 0.5, 20.0);
        liquid.clientUpdate();
    }

    private double targetValve() {
        return pressure ? -90.0 : 90.0;
    }

    /** Valve angle in radians. */
    public float getValveAngle(float partialTick) {
        return (float) Mth.lerp(partialTick, prevValve, valve) * 0.01745F;
    }

    /** Client: the liquid to draw. */
    public FluidStack getShownLiquid() {
        return liquid.getShown();
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- components, saving and client sync ---

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        frequency = input.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(EsDataComponents.FREQUENCY.get(), frequency);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("frequency");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("frequency", frequency.save());
        tag.putBoolean("ir", invert);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        frequency = Frequency.load(tag.getCompound("frequency"));
        invert = tag.getBoolean("ir");
        // Only in the update tag: liquid and pressure as the server sees them.
        if (tag.contains("pressure")) {
            pressure = tag.getBoolean("pressure");
            FluidStack sent = FluidStack.parseOptional(registries, tag.getCompound("liquid"));
            if (described) {
                liquid.sync(sent);
            } else {
                liquid.jump(sent);
                valve = prevValve = targetValve();
                described = true;
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.putBoolean("pressure", pressure);
        // writeToPacket: s_liquid, the storage's liquid right now.
        FluidStack stored = level instanceof ServerLevel ? getStorage().getFluid() : FluidStack.EMPTY;
        tag.put("liquid", stored.saveOptional(registries));
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
