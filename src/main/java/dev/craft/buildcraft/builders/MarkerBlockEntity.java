package dev.craft.buildcraft.builders;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.area.AreaProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Landmark, 1:1 with BuildCraft {@code TileMarker}. Linked landmarks share one {@link Origin}; only the origin
 * landmark saves the group and relinks its partners on the first tick after loading.
 */
public class MarkerBlockEntity extends BlockEntity implements AreaProvider {
    public static final int MAX_SIZE = 64;

    private Origin origin = new Origin();
    private boolean showSignals;
    private boolean initialized;
    @Nullable
    private BlockPos initVectO;
    private final BlockPos[] initVect = new BlockPos[3];

    // client view, filled from the update tag
    private int clientLinkMask;
    private boolean clientIsOrigin;
    private final int[] clientBounds = new int[6];

    public MarkerBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.MARKER.get(), pos, state);
    }

    /** The shared group of linked landmarks; {@code vectO == null} means "not linked". */
    public static final class Origin {
        @Nullable
        BlockPos vectO;
        final BlockPos[] vect = new BlockPos[3];
        int xMin;
        int yMin;
        int zMin;
        int xMax;
        int yMax;
        int zMax;

        boolean isSet() {
            return vectO != null;
        }
    }

    // --- server logic ---

    public void serverTick() {
        if (!initialized) {
            initialized = true;
            initialize();
        }
    }

    private void initialize() {
        updateSignals();
        if (initVectO != null) {
            origin = new Origin();
            origin.vectO = initVectO;
            for (int i = 0; i < 3; i++) {
                if (initVect[i] != null) {
                    linkTo(markerAt(initVect[i]), i);
                }
            }
            initVectO = null;
        }
    }

    public void updateSignals() {
        if (level != null && !level.isClientSide()) {
            showSignals = level.hasNeighborSignal(worldPosition);
            sendNetworkUpdate();
        }
    }

    /** Right click: tries to link every axis that has no partner yet. */
    public void tryConnection() {
        if (level.isClientSide()) {
            return;
        }
        for (int axis = 0; axis < 3; axis++) {
            if (!origin.isSet() || origin.vect[axis] == null) {
                setVect(axis);
            }
        }
        sendNetworkUpdate();
    }

    private void setVect(int axis) {
        if (origin.isSet() && origin.vect[axis] != null) {
            return;
        }
        for (int j = 1; j < MAX_SIZE; j++) {
            if (linkTo(markerAt(offset(axis, j)), axis) || linkTo(markerAt(offset(axis, -j)), axis)) {
                break;
            }
        }
    }

    private BlockPos offset(int axis, int amount) {
        return switch (axis) {
            case 0 -> worldPosition.offset(amount, 0, 0);
            case 1 -> worldPosition.offset(0, amount, 0);
            default -> worldPosition.offset(0, 0, amount);
        };
    }

    @Nullable
    private MarkerBlockEntity markerAt(@Nullable BlockPos pos) {
        if (pos == null || level == null || !level.getBlockState(pos).is(BcBlocks.MARKER.get())) {
            return null;
        }
        return level.getBlockEntity(pos) instanceof MarkerBlockEntity marker ? marker : null;
    }

    private boolean linkTo(@Nullable MarkerBlockEntity marker, int axis) {
        if (marker == null || origin.isSet() && marker.origin.isSet()) {
            return false;
        }
        if (!origin.isSet() && !marker.origin.isSet()) {
            origin = new Origin();
            marker.origin = origin;
            origin.vectO = worldPosition;
            origin.vect[axis] = marker.worldPosition;
        } else if (!origin.isSet()) {
            origin = marker.origin;
            origin.vect[axis] = worldPosition;
        } else {
            marker.origin = origin;
            origin.vect[axis] = marker.worldPosition;
        }
        MarkerBlockEntity originMarker = markerAt(origin.vectO);
        if (originMarker != null) {
            originMarker.computeBounds();
        }
        updateSignals();
        marker.updateSignals();
        for (MarkerBlockEntity member : groupMembers(origin)) {
            member.sendNetworkUpdate();
        }
        return true;
    }

    /** BuildCraft {@code createLasers} on the origin landmark: the box spanned by the linked axes. */
    private void computeBounds() {
        Origin o = origin;
        int[] own = {worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()};
        int[] min = new int[3];
        int[] max = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            int originCoord = coord(o.vectO, axis);
            if (o.vect[axis] == null) {
                min[axis] = originCoord;
                max[axis] = originCoord;
            } else if (coord(o.vect[axis], axis) < own[axis]) {
                min[axis] = coord(o.vect[axis], axis);
                max[axis] = own[axis];
            } else {
                min[axis] = own[axis];
                max[axis] = coord(o.vect[axis], axis);
            }
        }
        o.xMin = min[0];
        o.yMin = min[1];
        o.zMin = min[2];
        o.xMax = max[0];
        o.yMax = max[1];
        o.zMax = max[2];
    }

    private static int coord(BlockPos pos, int axis) {
        return axis == 0 ? pos.getX() : axis == 1 ? pos.getY() : pos.getZ();
    }

    private List<MarkerBlockEntity> groupMembers(Origin o) {
        List<MarkerBlockEntity> members = new ArrayList<>();
        MarkerBlockEntity originMarker = markerAt(o.vectO);
        if (originMarker != null) {
            members.add(originMarker);
        }
        for (BlockPos pos : o.vect) {
            MarkerBlockEntity marker = markerAt(pos);
            if (marker != null && !members.contains(marker)) {
                members.add(marker);
            }
        }
        return members;
    }

    /** BuildCraft {@code destroy}: removing a landmark dissolves its whole group. */
    public void destroy() {
        if (!origin.isSet()) {
            return;
        }
        Origin o = origin;
        MarkerBlockEntity originMarker = markerAt(o.vectO);
        List<MarkerBlockEntity> partners = new ArrayList<>();
        for (BlockPos pos : o.vect) {
            MarkerBlockEntity marker = markerAt(pos);
            if (marker != null) {
                partners.add(marker);
                if (marker != this) {
                    marker.origin = new Origin();
                }
            }
        }
        if (originMarker != null && originMarker != this) {
            originMarker.origin = new Origin();
        }
        for (MarkerBlockEntity marker : partners) {
            marker.updateSignals();
        }
        if (originMarker != null) {
            originMarker.updateSignals();
        }
    }

    // --- AreaProvider ---

    @Override
    public int xMin() {
        return origin.isSet() ? origin.xMin : worldPosition.getX();
    }

    @Override
    public int yMin() {
        return origin.isSet() ? origin.yMin : worldPosition.getY();
    }

    @Override
    public int zMin() {
        return origin.isSet() ? origin.zMin : worldPosition.getZ();
    }

    @Override
    public int xMax() {
        return origin.isSet() ? origin.xMax : worldPosition.getX();
    }

    @Override
    public int yMax() {
        return origin.isSet() ? origin.yMax : worldPosition.getY();
    }

    @Override
    public int zMax() {
        return origin.isSet() ? origin.zMax : worldPosition.getZ();
    }

    /** Removes every landmark of the group and drops it as an item (no break effects, like the original). */
    @Override
    public void removeFromWorld() {
        if (!origin.isSet()) {
            return;
        }
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos pos : origin.vect) {
            if (pos != null) {
                positions.add(pos);
            }
        }
        positions.add(origin.vectO);
        for (BlockPos pos : positions) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new ItemStack(BcBlocks.MARKER.get()));
        }
    }

    public boolean isLinked() {
        return origin.isSet();
    }

    // --- persistence ---

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (origin.isSet() && worldPosition.equals(origin.vectO)) {
            tag.put("vectO", NbtUtils.writeBlockPos(origin.vectO));
            for (int i = 0; i < 3; i++) {
                if (origin.vect[i] != null) {
                    tag.put("vect" + i, NbtUtils.writeBlockPos(origin.vect[i]));
                }
            }
        } else if (initVectO != null) {
            // not initialised yet (unticked chunk): keep the saved group
            tag.put("vectO", NbtUtils.writeBlockPos(initVectO));
            for (int i = 0; i < 3; i++) {
                if (initVect[i] != null) {
                    tag.put("vect" + i, NbtUtils.writeBlockPos(initVect[i]));
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        initVectO = NbtUtils.readBlockPos(tag, "vectO").orElse(null);
        for (int i = 0; i < 3; i++) {
            initVect[i] = NbtUtils.readBlockPos(tag, "vect" + i).orElse(null);
        }
    }

    // --- sync ---

    private void sendNetworkUpdate() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    private CompoundTag writeSync() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("signals", showSignals);
        int mask = 0;
        if (origin.isSet()) {
            mask = 8;
            for (int i = 0; i < 3; i++) {
                if (origin.vect[i] != null) {
                    mask |= 1 << i;
                }
            }
        }
        tag.putInt("links", mask);
        if (origin.isSet() && worldPosition.equals(origin.vectO)) {
            tag.putIntArray("bounds", new int[]{origin.xMin, origin.yMin, origin.zMin, origin.xMax, origin.yMax,
                    origin.zMax});
        }
        return tag;
    }

    private void readSync(CompoundTag tag) {
        showSignals = tag.getBoolean("signals");
        clientLinkMask = tag.getInt("links");
        clientIsOrigin = tag.contains("bounds");
        if (clientIsOrigin) {
            System.arraycopy(tag.getIntArray("bounds"), 0, clientBounds, 0, 6);
        }
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

    // --- client view ---

    public boolean showsSignals() {
        return showSignals;
    }

    /** Client: whether the given axis (0 = X, 1 = Y, 2 = Z) still shows a signal laser. */
    public boolean isAxisFree(int axis) {
        return (clientLinkMask & 8) == 0 || (clientLinkMask & 1 << axis) == 0;
    }

    /** Client: the red laser box, drawn by the origin landmark only; {@code null} for every other landmark. */
    @Nullable
    public int[] getClientBounds() {
        return clientIsOrigin ? clientBounds : null;
    }
}
