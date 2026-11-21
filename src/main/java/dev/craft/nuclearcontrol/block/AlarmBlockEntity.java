package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import dev.craft.ic2.machine.Wrenchable;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.NcSounds;
import dev.craft.nuclearcontrol.menu.AlarmMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Howler/industrial alarm ({@code TileEntityHowlerAlarm}, {@code TileEntityIndustrialAlarm}; spec 46 §4). Powered by
 * redstone at the block or a powered wire beside it ({@code RedstoneHelper}); while powered the client loops the
 * chosen sound with the set range. The industrial alarm's light changes every 3 ticks.
 */
public class AlarmBlockEntity extends BlockEntity implements Wrenchable, MenuProvider {
    /** {@code alarmRange} of the pack config. */
    public static final int DEFAULT_RANGE = 64;
    /** {@code maxAlarmRange}: the slider's maximum. */
    public static final int MAX_RANGE = 128;
    /** {@code SMPMaxAlarmRange}: the range the client plays with at most. */
    public static final int SMP_MAX_RANGE = 256;
    public static final int TICK_RATE = 2;
    /** {@code lightSteps}. */
    private static final byte[] LIGHT_STEPS = {0, 7, 15, 7, 0};

    private boolean powered;
    private int range = DEFAULT_RANGE;
    private String soundName = "default";
    private int updateTicker;
    private boolean initialized;
    private byte internalFire;
    private int lightLevel;

    public AlarmBlockEntity(BlockPos pos, BlockState state) {
        super(NcBlockEntityTypes.ALARM.get(), pos, state);
    }

    public boolean isIndustrial() {
        return getBlockState().getBlock() instanceof AlarmBlock block && block.isIndustrial();
    }

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            checkPowered();
        }
        if (updateTicker-- > 0) {
            return;
        }
        updateTicker = TICK_RATE;
        if (isIndustrial()) {
            updateLight();
        }
    }

    /**
     * {@code TileEntityIndustrialAlarm.checkStatus}. The original computes {@code (internalFire + 1) % 5 * 2}, so the
     * lamp runs 7, 7, 15, 0 instead of 0/7/15/7/0 – kept as it is.
     */
    private void updateLight() {
        if (!powered) {
            lightLevel = 0;
            internalFire = 0;
        } else {
            internalFire = (byte) ((internalFire + 1) % LIGHT_STEPS.length * 2);
            lightLevel = LIGHT_STEPS[internalFire / 2];
        }
        int lamp = AlarmBlock.lampOf(lightLevel);
        BlockState state = getBlockState();
        if (state.getValue(AlarmBlock.LAMP) != lamp) {
            level.setBlock(worldPosition, state.setValue(AlarmBlock.LAMP, lamp), Block.UPDATE_CLIENTS);
        }
    }

    public void clientTick() {
        if (updateTicker-- > 0) {
            return;
        }
        updateTicker = TICK_RATE;
        AlarmSoundHooks.update(this);
    }

    /** {@code RedstoneHelper.checkPowered}: signal at the block or a powered wire on one of the four sides. */
    public void checkPowered() {
        boolean signal = level.hasNeighborSignal(worldPosition);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState wire = level.getBlockState(worldPosition.relative(side));
            if (wire.getBlock() instanceof RedStoneWireBlock && wire.getValue(RedStoneWireBlock.POWER) > 0) {
                signal = true;
            }
        }
        setPowered(signal);
    }

    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            setChanged();
            sync();
        }
    }

    public boolean isPowered() {
        return powered;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = Math.max(0, Math.min(MAX_RANGE, range));
        setChanged();
        sync();
    }

    public String getSoundName() {
        return soundName;
    }

    public void setSoundName(String soundName) {
        if (NcSounds.ALARM_NAMES.contains(soundName)) {
            this.soundName = soundName;
            setChanged();
            sync();
        }
    }

    public int getLightLevel() {
        return lightLevel;
    }

    /** Client: {@code getNormalizedRange} = range / 16 (the volume; the sound fades linearly over the range). */
    public float getNormalizedRange() {
        return Math.min(range, SMP_MAX_RANGE) / 16.0F;
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide()) {
            AlarmSoundHooks.stop(this);
        }
    }

    // --- wrench: dismantle only ---

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
        return new AlarmMenu(id, inventory, worldPosition);
    }

    // --- persistence and sync ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("range")) {
            range = tag.getInt("range");
        }
        if (tag.contains("soundName")) {
            soundName = tag.getString("soundName");
        }
        powered = tag.getBoolean("powered");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("range", range);
        tag.putString("soundName", soundName);
        tag.putBoolean("powered", powered);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** Client: a switched-off alarm stops its sound at once ({@code setPowered(false)}). */
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        if (!powered) {
            AlarmSoundHooks.stop(this);
        }
    }

    /** Client sound handling, set by the client setup; does nothing on a dedicated server. */
    public static final class AlarmSoundHooks {
        private static Handler handler = new Handler() {
            @Override
            public void update(AlarmBlockEntity alarm) {
            }

            @Override
            public void stop(AlarmBlockEntity alarm) {
            }
        };

        private AlarmSoundHooks() {
        }

        public static void setHandler(Handler newHandler) {
            handler = newHandler;
        }

        static void update(AlarmBlockEntity alarm) {
            handler.update(alarm);
        }

        static void stop(AlarmBlockEntity alarm) {
            handler.stop(alarm);
        }

        public interface Handler {
            void update(AlarmBlockEntity alarm);

            void stop(AlarmBlockEntity alarm);
        }
    }
}
