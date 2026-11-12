package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.ic2.reactor.ReactorChamberBlockEntity;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.menu.ThermalMonitorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Thermal monitor ({@code TileEntityIC2Thermo}, spec 46 §3.1): watches a reactor next to it (a chamber first, then a
 * reactor) and compares its hull heat with {@link #heatLevel}. Checks every {@code reactor tick rate / 2} ticks, which
 * the original's {@code updateTicker-- > 0} turns into every 11 ticks; every tick until a reactor was found.
 */
public class ThermalMonitorBlockEntity extends BlockEntity implements Wrenchable, MenuProvider {
    public static final int DEFAULT_HEAT_LEVEL = 500;
    public static final int MAX_HEAT_LEVEL = 1000000;

    protected int heatLevel = DEFAULT_HEAT_LEVEL;
    /** −2 no energy (remote), −1 no reactor, otherwise 0/1 (monitor) or the hull heat (remote). */
    protected int onFire;
    protected boolean invertRedstone;
    protected int tickRate = -1;
    private int updateTicker;

    public ThermalMonitorBlockEntity(BlockPos pos, BlockState state) {
        this(NcBlockEntityTypes.THERMAL_MONITOR.get(), pos, state);
    }

    protected ThermalMonitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static InteractionResult openMenu(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ThermalMonitorBlockEntity monitor
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(monitor, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public void serverTick() {
        if (tickRate != -1 && updateTicker-- > 0) {
            return;
        }
        updateTicker = tickRate;
        checkStatus();
    }

    protected void checkStatus() {
        NuclearReactorBlockEntity reactor = adjacentReactor();
        int fire;
        if (reactor != null) {
            startTicking();
            fire = reactor.getHeat() >= heatLevel ? 1 : 0;
        } else {
            fire = -1;
        }
        setOnFire(fire);
    }

    /** First reactor found: from now on every {@code TICK_RATE / 2} ticks. */
    protected void startTicking() {
        if (tickRate == -1) {
            tickRate = Math.max(1, NuclearReactorBlockEntity.TICK_RATE / 2);
            updateTicker = tickRate;
        }
    }

    /** {@code getReactorChamberAroundCoord}, then {@code getReactorAroundCoord}. */
    @Nullable
    private NuclearReactorBlockEntity adjacentReactor() {
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ReactorChamberBlockEntity chamber
                    && chamber.getReactor() != null) {
                return chamber.getReactor();
            }
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof NuclearReactorBlockEntity reactor) {
                return reactor;
            }
        }
        return null;
    }

    /** Status change: front texture, block update for redstone ({@code notifyBlocksOfNeighborChange}), sync. */
    protected void setOnFire(int fire) {
        if (fire == onFire) {
            return;
        }
        onFire = fire;
        setChanged();
        BlockState state = getBlockState();
        if (state.hasProperty(ThermalMonitorBlock.STATUS)) {
            int status = ThermalMonitorBlock.statusOf(fire);
            if (state.getValue(ThermalMonitorBlock.STATUS) != status) {
                level.setBlock(worldPosition, state.setValue(ThermalMonitorBlock.STATUS, status), Block.UPDATE_CLIENTS);
            }
        }
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        sync();
    }

    /** Redstone: {@code onFire > 0} XOR inverted, never towards a reactor or reactor chamber. */
    public boolean emitsSignalTowards(BlockPos target) {
        BlockEntity entity = level.getBlockEntity(target);
        if (entity instanceof NuclearReactorBlockEntity || entity instanceof ReactorChamberBlockEntity) {
            return false;
        }
        return isTriggered() ^ invertRedstone;
    }

    protected boolean isTriggered() {
        return onFire > 0;
    }

    public int getOnFire() {
        return onFire;
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    /** GUI value; the GUI keeps it within 0…1 000 000. */
    public void setHeatLevel(int heatLevel) {
        this.heatLevel = Math.max(0, Math.min(MAX_HEAT_LEVEL, heatLevel));
        setChanged();
        sync();
    }

    public boolean isInvertRedstone() {
        return invertRedstone;
    }

    /** Like the original, the neighbours only see the change with the next status change. */
    public void setInvertRedstone(boolean invert) {
        this.invertRedstone = invert;
        setChanged();
        sync();
    }

    protected void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- wrench: the monitor cannot be turned, only dismantled (always drops itself) ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(AttachedBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return false;
    }

    @Override
    public void setFacing(Direction facing) {
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    // --- menu ---

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ThermalMonitorMenu(id, inventory, worldPosition);
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("heatLevel")) {
            heatLevel = tag.getInt("heatLevel");
        }
        invertRedstone = tag.getBoolean("invert");
        onFire = tag.getInt("onFire");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("heatLevel", heatLevel);
        tag.putBoolean("invert", invertRedstone);
        tag.putInt("onFire", onFire);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
