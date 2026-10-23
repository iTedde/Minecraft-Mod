package dev.craft.enderstorage;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/**
 * EnderStorage {@code TileEnderChest} (spec 27 §3): frequency and owner, the lid that follows the open count of the
 * shared storage, and item access to that storage.
 */
public class EnderChestBlockEntity extends BlockEntity {
    private static final int EVENT_OPEN = 1;

    private Frequency frequency = Frequency.GLOBAL;
    /** {@code c_numOpen}: -1 until the first sync, so the server sends the count right away. */
    private int numOpen = -1;
    private double lidAngle;
    private double prevLidAngle;

    public EnderChestBlockEntity(BlockPos pos, BlockState state) {
        super(EsBlockEntityTypes.ENDER_CHEST.get(), pos, state);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    /** {@code setFreq}/{@code setOwner}: switches to another storage. */
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** The shared storage; server only. */
    public EnderItemStorage getStorage() {
        return EnderStorageData.get(((ServerLevel) level).getServer()).item(frequency);
    }

    @Nullable
    public IItemHandler getItemHandler() {
        return level instanceof ServerLevel ? new InvWrapper(getStorage()) : null;
    }

    /** Whether the lid is (partly) open; the colour buttons and the latch can only be used on a closed chest. */
    public boolean isLidOpen() {
        return level != null && !level.isClientSide() ? getStorage().getOpenCount() > 0 : lidAngle > 0.0;
    }

    public void serverTick() {
        int open = getStorage().getOpenCount();
        if (level.getGameTime() % 20L == 0L || numOpen != open) {
            numOpen = open;
            level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_OPEN, numOpen);
        }
    }

    /** Lid moves 0.1 per tick; chest sounds when it starts opening and when it closes past half way. */
    public void clientTick() {
        prevLidAngle = lidAngle;
        double target = numOpen > 0 ? 1.0 : 0.0;
        lidAngle = lidAngle < target ? Math.min(target, lidAngle + 0.1) : Math.max(target, lidAngle - 0.1);
        if (prevLidAngle >= 0.5 && lidAngle < 0.5) {
            playSound(true);
        } else if (prevLidAngle == 0.0 && lidAngle > 0.0) {
            playSound(false);
        }
    }

    private void playSound(boolean close) {
        level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                close ? SoundEvents.CHEST_CLOSE : SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F, false);
    }

    /** {@code getRadianLidAngle}: eased lid angle in radians, 0 closed to -π/2 open. */
    public float getLidAngle(float partialTick) {
        double a = 1.0 - Mth.lerp(partialTick, prevLidAngle, lidAngle);
        a = 1.0 - a * a * a;
        return (float) (a * Math.PI * -0.5);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_OPEN) {
            numOpen = param;
            return true;
        }
        return super.triggerEvent(id, param);
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        frequency = Frequency.load(tag.getCompound("frequency"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
