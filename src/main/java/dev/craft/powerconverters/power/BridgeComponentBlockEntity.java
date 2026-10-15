package dev.craft.powerconverters.power;

import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerconverters.block.PowerConverterBlock;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Power Converters {@code TileEntityBridgeComponent}: caches the adjacent energy bridges and the adjacent tiles of
 * the matching kind ({@code A}) on the first tick and on every neighbour change. The "connected" side textures are
 * driven by block state properties instead of a client-side class check.
 */
public abstract class BridgeComponentBlockEntity<A> extends BlockEntity {
    private Map<Direction, EnergyBridgeBlockEntity> adjacentBridges = new EnumMap<>(Direction.class);
    private Map<Direction, A> adjacentTiles = new EnumMap<>(Direction.class);
    protected PowerSystem powerSystem;
    private final int voltageIndex;
    private boolean initialized;

    protected BridgeComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                         PowerSystem powerSystem, int voltageIndex) {
        super(type, pos, state);
        this.powerSystem = powerSystem;
        this.voltageIndex = voltageIndex;
    }

    /** The adjacent tile of the matching kind on side {@code side}, or {@code null}. */
    protected abstract @Nullable A findAdjacent(Direction side, BlockPos neighbor);

    public void serverTick() {
        if (!initialized) {
            onNeighborChanged();
            initialized = true;
        }
        tick();
    }

    protected void tick() {
    }

    public void onNeighborChanged() {
        if (level == null) {
            return;
        }
        Map<Direction, EnergyBridgeBlockEntity> bridges = new EnumMap<>(Direction.class);
        Map<Direction, A> tiles = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(side);
            if (level.getBlockEntity(neighbor) instanceof EnergyBridgeBlockEntity bridge) {
                bridges.put(side, bridge);
            } else {
                A tile = findAdjacent(side, neighbor);
                if (tile != null) {
                    tiles.put(side, tile);
                }
            }
        }
        adjacentBridges = bridges;
        adjacentTiles = tiles;
        updateConnectedSides();
    }

    /** Sets the side properties of the block state to {@link #isSideConnected}. */
    protected void updateConnectedSides() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        BlockState updated = state;
        for (Direction side : Direction.values()) {
            updated = updated.setValue(PowerConverterBlock.property(side), isSideConnected(side));
        }
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
    }

    public PowerSystem getPowerSystem() {
        return powerSystem;
    }

    public int getVoltageIndex() {
        return voltageIndex;
    }

    public boolean isConnected() {
        return !adjacentTiles.isEmpty();
    }

    public boolean isSideConnected(Direction side) {
        return adjacentTiles.containsKey(side);
    }

    /** {@code getFirstBridge} (direction order instead of hash map order). */
    public @Nullable EnergyBridgeBlockEntity getFirstBridge() {
        for (EnergyBridgeBlockEntity bridge : adjacentBridges.values()) {
            if (!bridge.isRemoved()) {
                return bridge;
            }
        }
        return null;
    }

    protected Map<Direction, EnergyBridgeBlockEntity> getBridges() {
        return adjacentBridges;
    }

    protected Map<Direction, A> getTiles() {
        return adjacentTiles;
    }
}
