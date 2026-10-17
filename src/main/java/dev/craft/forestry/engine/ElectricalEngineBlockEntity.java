package dev.craft.forestry.engine;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.forestry.FrBlockEntityTypes;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.circuit.CircuitBoard;
import dev.craft.forestry.item.CircuitBoardItem;
import dev.craft.forestry.machine.ErrorState;
import dev.craft.forestry.menu.ElectricalEngineMenu;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.machine.MachineInventory;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry Electrical Engine ({@code TileEngine} with {@code EngineTin}): an EU sink that turns
 * {@code euForCycle} EU into {@code mjPerCycle} MJ per tick while powered by redstone, and hands the MJ to the
 * receiver in front of it on every piston stroke. Overheating forces a cooldown instead of an explosion. The circuit
 * board in the socket changes the EU/MJ per cycle and the EU storage.
 */
public class ElectricalEngineBlockEntity extends BlockEntity
        implements EnergySink, PowerReceptor, MenuProvider, OutputReadout {
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_ENERGY_EXTRACTED = 400;
    public static final int MAX_HEAT = 3000;
    public static final int EU_MAX_ACCEPT = 512;
    private static final int BASE_EU_FOR_CYCLE = 6;
    private static final int BASE_MJ_PER_CYCLE = 2;
    private static final int BASE_EU_STORAGE = 12;
    /** {@code Engine.maxEnergyReceived}. */
    private static final int MAX_ENERGY_RECEIVED = 200;

    private final PowerProvider provider = new PowerProvider();
    private final OutputMeter outputMeter = new OutputMeter();
    private final MachineInventory inventory = new MachineInventory(1, this::setChanged);
    private final MachineInventory socket = new MachineInventory(1, this::onSocketChanged);

    private int storedEnergy;
    private int heat;
    private boolean forceCooldown;
    private float progress;
    private int stagePiston;
    private float pistonSpeed;
    private boolean active;
    private int currentOutput;
    private int euStored;
    /** {@code TileForestry.errorState}: shown by the error ledger, not saved. */
    private ErrorState errorState = ErrorState.OK;
    /** {@code DelayTimer.markedTime} of the fuel check. */
    private long errorCheckTime;
    // EuConfig: rebuilt from the board in the socket, not saved (Forestry re-applies the circuits on load).
    private int euForCycle = BASE_EU_FOR_CYCLE;
    private int mjPerCycle = BASE_MJ_PER_CYCLE;
    private int euStorage = BASE_EU_STORAGE;
    /** {@code onBlockAdded} turns a newly placed engine; the block entity only exists from its first tick on. */
    private boolean newlyPlaced = true;

    public ElectricalEngineBlockEntity(BlockPos pos, BlockState state) {
        super(FrBlockEntityTypes.ELECTRICAL_ENGINE.get(), pos, state);
        provider.configure(10, MAX_ENERGY_RECEIVED, 10, 100000);
    }

    public MachineInventory getInventory() {
        return inventory;
    }

    public MachineInventory getSocket() {
        return socket;
    }

    public Direction getOrientation() {
        return getBlockState().getValue(ElectricalEngineBlock.FACING);
    }

    /** {@code TileForestry.isActivated}: redstone signal at the block. */
    public boolean isActivated() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public TemperatureState getTemperatureState() {
        return TemperatureState.of(heat, MAX_HEAT);
    }

    public int getStoredEnergy() {
        return storedEnergy;
    }

    public int getHeat() {
        return heat;
    }

    public int getEuStored() {
        return euStored;
    }

    public int getEuStorage() {
        return euStorage;
    }

    public int getEuForCycle() {
        return euForCycle;
    }

    public int getMjPerCycle() {
        return mjPerCycle;
    }

    public ErrorState getErrorState() {
        return errorState;
    }

    public boolean isForceCooldown() {
        return forceCooldown;
    }

    public float getProgress() {
        return progress;
    }

    /** {@code Engine.getCurrentOutput}: MJ per tick while burning with redstone. */
    public int getCurrentOutput() {
        return isBurning() && isActivated() ? currentOutput : 0;
    }

    // --- circuit board socket ---

    /** {@code setSocket}: removes the old board's circuits, applies the new one's. */
    public void setSocket(ItemStack stack) {
        socket.setStackInSlot(0, stack);
    }

    private void onSocketChanged() {
        euForCycle = BASE_EU_FOR_CYCLE;
        mjPerCycle = BASE_MJ_PER_CYCLE;
        euStorage = BASE_EU_STORAGE;
        CircuitBoard board = CircuitBoardItem.getCircuitBoard(socket.getStackInSlot(0));
        if (board != null) {
            for (Circuit circuit : board.circuits()) {
                euForCycle += circuit.getEuChange();
                mjPerCycle += circuit.getMjChange();
                euStorage += 2 * circuit.getEuChange();
            }
        }
        setChanged();
    }

    // --- MJ ---

    /** {@code Engine.addEnergy}: capped, no explosion. */
    public void addEnergy(int addition) {
        storedEnergy = Math.min(storedEnergy + addition, MAX_ENERGY);
    }

    /** {@code Engine.extractEnergy}: unlike BuildCraft there is no {@code ceiling < min} check. */
    public int extractEnergy(int min, int max, boolean doExtract) {
        if (storedEnergy < min) {
            return 0;
        }
        int ceiling = Math.min(max, MAX_ENERGY_EXTRACTED);
        int extracted = Math.min(storedEnergy, ceiling);
        if (doExtract) {
            storedEnergy -= extracted;
        }
        return extracted;
    }

    @Override
    public PowerProvider getPowerProvider() {
        return provider;
    }

    /** {@code TileEngine.doWork}: chained engines keep 95 %. */
    @Override
    public void doWork() {
        addEnergy((int) (provider.useEnergy(1.0F, MAX_ENERGY_RECEIVED, true) * 0.95F));
    }

    // --- EU ---

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return euStorage - euStored;
    }

    /** Accepts any voltage up to {@code euStorage + 511}; never explodes. */
    @Override
    public int injectEnergy(Direction direction, int amount) {
        int input = Math.min(euStorage + EU_MAX_ACCEPT - 1 - euStored, amount);
        euStored += input;
        return amount - input;
    }

    @Override
    public int getMaxSafeInput() {
        return Integer.MAX_VALUE;
    }

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

    // --- ticking ---

    public void serverTick() {
        if (newlyPlaced) {
            newlyPlaced = false;
            rotateEngine();
            if (isRemoved()) {
                return;
            }
        }
        provider.update(this, level);
        updateServerSide();
        PowerReceptor receptor = getTarget();
        float newPistonSpeed = getTemperatureState().getPistonSpeed();
        if (newPistonSpeed != pistonSpeed) {
            pistonSpeed = newPistonSpeed;
            sendNetworkUpdate();
        }
        if (stagePiston != 0) {
            progress += pistonSpeed;
            if (progress > 0.5F && stagePiston == 1) {
                stagePiston = 2;
                if (receptor != null) {
                    PowerProvider target = receptor.getPowerProvider();
                    int extracted = extractEnergy(target.getMinEnergyReceived(), target.getMaxEnergyReceived(), true);
                    if (extracted > 0) {
                        target.receiveEnergy(extracted, getOrientation().getOpposite());
                    }
                }
            } else if (progress >= 1.0F) {
                progress = 0.0F;
                stagePiston = 0;
            }
        } else if (receptor != null && isActivated()) {
            PowerProvider target = receptor.getPowerProvider();
            if (extractEnergy(target.getMinEnergyReceived(), target.getMaxEnergyReceived(), false) > 0) {
                stagePiston = 1;
                setActive(true);
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        dissipateHeat();
        generateHeat();
        if (!forceCooldown) {
            burn();
        } else {
            extractEnergy(0, 2, true);
        }
        setChanged();
    }

    /** {@code EngineTin.updateServerSide}. */
    private void updateServerSide() {
        if (getTemperatureState() == TemperatureState.MELTING && heat > 0) {
            forceCooldown = true;
        } else if (forceCooldown && heat <= 0) {
            forceCooldown = false;
        }
        if (forceCooldown) {
            errorState = ErrorState.FORCED_COOLDOWN;
            return;
        }
        if (!inventory.getStackInSlot(0).isEmpty()) {
            replenishFromBattery();
        }
        if (delayPassed(80L)) {
            // Original quirk: without output the state alternates between OK and NO_FUEL every check.
            errorState = currentOutput <= 0 && errorState == ErrorState.OK ? ErrorState.NO_FUEL : ErrorState.OK;
        }
    }

    /** {@code DelayTimer.delayPassed}. */
    private boolean delayPassed(long delay) {
        long time = level.getGameTime();
        if (time < errorCheckTime) {
            errorCheckTime = time;
            return false;
        }
        if (errorCheckTime + delay <= time) {
            errorCheckTime = time;
            return true;
        }
        return false;
    }

    private void replenishFromBattery() {
        if (euStored >= euStorage || !isActivated()) {
            return;
        }
        ItemStack stack = inventory.getStackInSlot(0);
        if (stack.getItem() instanceof ElectricItem item && item.canProvideEnergy()) {
            int space = Math.min(euStorage - euStored, euForCycle * 3);
            euStored += ElectricItems.discharge(stack, space, Integer.MAX_VALUE, true, false);
        }
    }

    private void dissipateHeat() {
        if (heat <= 0) {
            return;
        }
        int loss = 0;
        if (!isBurning() || !isActivated()) {
            loss++;
        }
        TemperatureState state = getTemperatureState();
        if (state == TemperatureState.OVERHEATING || state == TemperatureState.OPERATING_TEMPERATURE) {
            loss++;
        }
        heat -= loss;
    }

    private void generateHeat() {
        int gain = 0;
        if (isActivated() && isBurning()) {
            gain++;
            if ((double) storedEnergy / MAX_ENERGY > 0.5) {
                gain++;
            }
        }
        heat = Math.min(heat + gain, MAX_HEAT);
    }

    private void burn() {
        currentOutput = 0;
        if (isActivated() && euStored >= euForCycle) {
            currentOutput = mjPerCycle;
            addEnergy(mjPerCycle);
            outputMeter.record(level, mjPerCycle);
            euStored -= euForCycle;
        }
    }

    public boolean isBurning() {
        return !forceCooldown && euStored >= euForCycle;
    }

    /** Client half of {@code TileEngine.updateEntity}: the piston follows the synced speed. */
    public void clientTick() {
        if (stagePiston != 0) {
            progress += pistonSpeed;
            if (progress > 1.0F) {
                stagePiston = 0;
                progress = 0.0F;
            }
        } else if (active) {
            stagePiston = 1;
        }
    }

    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            sendNetworkUpdate();
        }
    }

    @Nullable
    private PowerReceptor getTarget() {
        return getReceptor(getOrientation());
    }

    @Nullable
    private PowerReceptor getReceptor(Direction side) {
        return level.getCapability(BcCapabilities.POWER_RECEPTOR, worldPosition.relative(side), side.getOpposite());
    }

    /**
     * {@code TileEngine.rotateEngine}: turns the output to the next side (in direction order, starting after the
     * current one) that has an MJ receiver.
     */
    public void rotateEngine() {
        int current = getOrientation().get3DDataValue();
        for (int i = current + 1; i <= current + 6; i++) {
            Direction side = Direction.from3DDataValue(i % 6);
            if (getReceptor(side) != null) {
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ElectricalEngineBlock.FACING, side));
                break;
            }
        }
    }

    /** {@code BlockForestry.breakBlock}: only the battery slot drops; the board in the socket is lost. */
    public void dropContents() {
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                inventory.getStackInSlot(0));
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    // --- menu ---

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectricalEngineMenu(id, playerInventory, this);
    }

    // --- sync ---

    private void sendNetworkUpdate() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    /** {@code TileEngine.getPacketPayload}: active flag, piston speed and the engine state. */
    private CompoundTag writeSync() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("active", active);
        tag.putFloat("pistonSpeed", pistonSpeed);
        tag.putInt("storedEnergy", storedEnergy);
        tag.putInt("heat", heat);
        return tag;
    }

    private void readSync(CompoundTag tag) {
        active = tag.getBoolean("active");
        pistonSpeed = tag.getFloat("pistonSpeed");
        storedEnergy = tag.getInt("storedEnergy");
        heat = tag.getInt("heat");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return writeSync();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        readSync(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        readSync(packet.getTag());
    }

    // --- persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("power", provider.save());
        tag.putInt("EngineHeat", heat);
        tag.putInt("EngineStoredEnergy", storedEnergy);
        tag.putFloat("EngineProgress", progress);
        tag.putBoolean("ForceCooldown", forceCooldown);
        tag.putInt("stagePiston", stagePiston);
        tag.putInt("EngineEnergyStored", euStored);
        tag.put("Items", inventory.serializeNBT(registries));
        tag.put("sockets", socket.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        newlyPlaced = false;
        if (tag.contains("power")) {
            provider.load(tag.getCompound("power"));
        }
        heat = tag.getInt("EngineHeat");
        storedEnergy = tag.getInt("EngineStoredEnergy");
        progress = tag.getFloat("EngineProgress");
        forceCooldown = tag.getBoolean("ForceCooldown");
        stagePiston = tag.getInt("stagePiston");
        euStored = tag.getInt("EngineEnergyStored");
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
        socket.deserializeNBT(registries, tag.getCompound("sockets"));
        onSocketChanged();
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.MJ);
    }
}
