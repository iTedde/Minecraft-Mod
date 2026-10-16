package dev.craft.buildcraft.transport;

import java.util.Arrays;

import javax.annotation.Nullable;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.factory.MiningWellBlockEntity;
import dev.craft.buildcraft.factory.PumpBlockEntity;
import dev.craft.buildcraft.factory.QuarryBlockEntity;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.power.SafeTimeTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * BuildCraft {@code PipeTransportPower} with {@code PipePowerWood}: double-buffered power and requests per side,
 * switched once per world tick; the wooden pipe takes MJ from engines and pushes it into the connected pipes.
 */
public class PowerPipeBlockEntity extends PipeBlockEntity {
    public static final int MAX_POWER_INTERNAL = 10000;
    public static final int OVERLOAD_LIMIT = 7500;
    public static final int MAX_DISPLAY = 100;
    private static final float DISPLAY_POWER_FACTOR = 0.1F;
    /** {@code 2 · BuildCraftCore.updateFactor}. */
    private static final int UPDATE_DELAY = 20;
    private static final int MAX_OVERHEAT_TICKS = 100;

    private final BlockEntity[] tiles = new BlockEntity[6];
    private boolean needsInit = true;
    private int[] powerQuery = new int[6];
    private int[] nextPowerQuery = new int[6];
    private double[] internalPower = new double[6];
    private double[] internalNextPower = new double[6];
    private long currentDate;
    private short[] displayPower = new short[6];
    private boolean overload;
    private int overheatTicks;
    private final SafeTimeTracker tracker = new SafeTimeTracker();

    public PowerPipeBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.POWER_PIPE.get(), pos, state);
    }

    /** {@code powerResistance}: 0.005 for gold, 0.05 otherwise. */
    private double powerResistance() {
        return getPipeType().getMaterial() == PipeMaterial.GOLD ? 0.005 : 0.05;
    }

    /** {@code PipePowerWood.getPowerProvider}: nothing while overheated, so engines stop delivering. */
    @Nullable
    @Override
    public PowerProvider getPowerProvider() {
        return overheatTicks > 0 ? null : provider;
    }

    /** {@code isPipeConnected}: BC machines and every MJ receiver (even without a provider). */
    @Override
    protected boolean acceptsNonPipe(BlockEntity other, Direction side) {
        return other instanceof QuarryBlockEntity || other instanceof MiningWellBlockEntity
                || other instanceof PumpBlockEntity || other instanceof RefineryBlockEntity
                || receptor(other, side) != null;
    }

    @Nullable
    private PowerReceptor receptor(@Nullable BlockEntity tile, Direction side) {
        if (tile == null || tile instanceof PipeBlockEntity) {
            return null;
        }
        if (tile instanceof PowerReceptor receptor) {
            return receptor;
        }
        return level.getCapability(BcCapabilities.POWER_RECEPTOR, tile.getBlockPos(), side.getOpposite());
    }

    @Override
    protected void onNeighborBlockChange() {
        super.onNeighborBlockChange();
        updateTiles();
    }

    private void updateTiles() {
        for (Direction side : Direction.values()) {
            tiles[side.get3DDataValue()] = PipeConnections.canConnect(level, worldPosition, side)
                    ? level.getBlockEntity(worldPosition.relative(side)) : null;
        }
    }

    @Override
    protected void tickTransport() {
        if (level.isClientSide()) {
            return;
        }
        updatePower();
        if (!isRemoved() && provider != null) {
            updateWood();
        }
    }

    private void updatePower() {
        step();
        if (needsInit) {
            needsInit = false;
            updateTiles();
        }
        Arrays.fill(displayPower, (short) 0);
        for (int i = 0; i < 6; i++) {
            if (internalPower[i] <= 0.0) {
                continue;
            }
            double div = 0.0;
            for (int j = 0; j < 6; j++) {
                if (j != i && powerQuery[j] > 0 && (tiles[j] instanceof PipeBlockEntity
                        || receptor(tiles[j], Direction.from3DDataValue(j)) != null)) {
                    div += powerQuery[j];
                }
            }
            double totalWatt = internalPower[i];
            for (int j = 0; j < 6; j++) {
                if (j == i || powerQuery[j] <= 0) {
                    continue;
                }
                double watts = totalWatt / div * powerQuery[j];
                Direction side = Direction.from3DDataValue(j);
                if (tiles[j] instanceof PowerPipeBlockEntity pipe) {
                    pipe.receiveEnergy(side.getOpposite(), watts);
                } else {
                    PowerReceptor receptor = receptor(tiles[j], side);
                    PowerProvider target = receptor != null ? receptor.getPowerProvider() : null;
                    if (target == null) {
                        continue;
                    }
                    target.receiveEnergy((float) watts, side.getOpposite());
                }
                displayPower[j] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);
                displayPower[i] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);
                internalPower[i] -= watts;
            }
        }
        double highestPower = 0.0;
        for (int i = 0; i < 6; i++) {
            highestPower = Math.max(highestPower, internalPower[i]);
            displayPower[i] = (short) Math.max(displayPower[i], Math.ceil(internalPower[i] * DISPLAY_POWER_FACTOR));
            displayPower[i] = (short) Math.min(displayPower[i], MAX_DISPLAY);
        }
        overload = highestPower > OVERLOAD_LIMIT;

        for (int i = 0; i < 6; i++) {
            PowerReceptor receptor = receptor(tiles[i], Direction.from3DDataValue(i));
            if (receptor != null) {
                int request = receptor.powerRequest();
                if (request > 0) {
                    requestEnergy(Direction.from3DDataValue(i), request);
                }
            }
        }
        int[] transferQuery = new int[6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (j != i) {
                    transferQuery[i] += powerQuery[j];
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            if (transferQuery[i] != 0 && tiles[i] instanceof PowerPipeBlockEntity pipe) {
                pipe.requestEnergy(Direction.from3DDataValue(i).getOpposite(), transferQuery[i]);
            }
        }

        if (tracker.markTimeIfDelay(level, UPDATE_DELAY) && level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(worldPosition),
                    new PipePowerPayload(worldPosition, displayBytes(), overload));
        }
    }

    /** {@code PipePowerWood.updateEntity}: overheating, then push into every connected pipe. */
    private void updateWood() {
        if (provider.getEnergyStored() == provider.getMaxEnergyStored()) {
            overheatTicks += overheatTicks < MAX_OVERHEAT_TICKS ? 1 : 0;
        } else {
            overheatTicks -= overheatTicks > 0 ? 1 : 0;
        }
        for (Direction side : Direction.values()) {
            if (!isConnected(side)
                    || !(level.getBlockEntity(worldPosition.relative(side)) instanceof PowerPipeBlockEntity pipe)) {
                continue;
            }
            float stored = provider.getEnergyStored();
            float energyToRemove;
            if (stored > 40.0F) {
                energyToRemove = stored / 40.0F + 4.0F;
            } else if (stored > 10.0F) {
                energyToRemove = stored / 10.0F;
            } else {
                energyToRemove = 1.0F;
            }
            float energyUsed = provider.useEnergy(1.0F, energyToRemove, true);
            pipe.receiveEnergy(side.getOpposite(), energyUsed);
        }
    }

    /** Switches the buffers once per world tick; sides without a request lose 1 of their rest. */
    private void step() {
        long date = level.getGameTime();
        if (currentDate == date) {
            return;
        }
        currentDate = date;
        powerQuery = nextPowerQuery;
        nextPowerQuery = new int[6];
        double[] next = Arrays.copyOf(internalPower, 6);
        internalPower = internalNextPower;
        internalNextPower = next;
        for (int i = 0; i < 6; i++) {
            if (powerQuery[i] == 0 && internalNextPower[i] > 0.0) {
                internalNextPower[i]--;
            }
        }
    }

    /** At {@value #MAX_POWER_INTERNAL} on one side the pipe explodes (strength 3, no block damage) without drop. */
    public void receiveEnergy(Direction from, double val) {
        if (isRemoved()) {
            return;
        }
        step();
        int i = from.get3DDataValue();
        internalNextPower[i] += val * (1.0 - powerResistance());
        if (internalNextPower[i] >= MAX_POWER_INTERNAL) {
            level.explode(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 3.0F,
                    Level.ExplosionInteraction.NONE);
            level.removeBlock(worldPosition, false);
        }
    }

    public void requestEnergy(Direction from, int amount) {
        if (isRemoved()) {
            return;
        }
        step();
        nextPowerQuery[from.get3DDataValue()] += amount;
    }

    /** Power is lost when the pipe is broken. */
    @Override
    public void dropContents() {
    }

    // --- display ---

    public short[] getDisplayPower() {
        return displayPower;
    }

    public boolean isOverloaded() {
        return overload;
    }

    public double getInternalPower(Direction side) {
        return internalPower[side.get3DDataValue()];
    }

    public int getPowerQuery(Direction side) {
        return powerQuery[side.get3DDataValue()];
    }

    public int getOverheatTicks() {
        return overheatTicks;
    }

    private byte[] displayBytes() {
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) displayPower[i];
        }
        return bytes;
    }

    void handlePowerPacket(PipePowerPayload payload) {
        for (int i = 0; i < 6; i++) {
            displayPower[i] = payload.displayPower()[i];
        }
        overload = payload.overload();
    }

    // --- saving ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < 6; i++) {
            tag.putInt("powerQuery[" + i + "]", powerQuery[i]);
            tag.putInt("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
            tag.putDouble("internalPower[" + i + "]", internalPower[i]);
            tag.putDouble("internalNextPower[" + i + "]", internalNextPower[i]);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < 6; i++) {
            powerQuery[i] = tag.getInt("powerQuery[" + i + "]");
            nextPowerQuery[i] = tag.getInt("nextPowerQuery[" + i + "]");
            internalPower[i] = tag.getDouble("internalPower[" + i + "]");
            internalNextPower[i] = tag.getDouble("internalNextPower[" + i + "]");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("displayPower", displayBytes());
        tag.putBoolean("overload", overload);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        byte[] bytes = tag.getByteArray("displayPower");
        for (int i = 0; i < 6 && i < bytes.length; i++) {
            displayPower[i] = bytes[i];
        }
        overload = tag.getBoolean("overload");
    }
}
