package dev.craft.thermalexpansion.transport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Direction;

/**
 * {@code GridEnergy}: all connected energy conduits share one {@link GridPowerProvider} of 500 MJ per node. Each tick
 * (driven by the first output node) every output node may hand out the per-node share ({@code stored / nodes}, only
 * from 1 MJ on) to the receivers around it that ask for energy. Input nodes only ask for energy ({@code powerRequest}).
 */
public final class EnergyGrid {
    private final List<EnergyConduitBlockEntity> members = new ArrayList<>();
    private final List<EnergyConduitBlockEntity> outputs = new ArrayList<>();
    private final List<EnergyConduitBlockEntity> inputs = new ArrayList<>();
    private final GridPowerProvider provider = new GridPowerProvider();
    private boolean dissolved;

    private EnergyGrid() {
    }

    public GridPowerProvider getProvider() {
        return provider;
    }

    public int getNodeCount() {
        return outputs.size() + inputs.size();
    }

    /** Flood fill from {@code start}; networks met on the way are broken up and taken over. */
    static void build(EnergyConduitBlockEntity start) {
        EnergyGrid grid = new EnergyGrid();
        Deque<EnergyConduitBlockEntity> queue = new ArrayDeque<>();
        Set<EnergyConduitBlockEntity> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            EnergyConduitBlockEntity conduit = queue.poll();
            if (conduit.grid != null) {
                conduit.grid.dissolve();
            }
            conduit.grid = grid;
            grid.members.add(conduit);
            for (Direction side : Direction.values()) {
                if (conduit.neighbour(side) instanceof EnergyConduitBlockEntity next && !next.isRemoved()
                        && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        float pending = 0.0F;
        for (EnergyConduitBlockEntity conduit : grid.members) {
            conduit.refreshSides();
            if (conduit.isNode()) {
                (conduit.getMode() == ConduitBlockEntity.MODE_OUTPUT ? grid.outputs : grid.inputs).add(conduit);
            }
            pending += conduit.takePendingEnergy();
        }
        grid.provider.setNodeCount(grid.getNodeCount());
        grid.provider.addEnergyWithoutLoss(pending);
        grid.provider.setNodeCount(grid.getNodeCount());
        for (EnergyConduitBlockEntity conduit : grid.members) {
            conduit.syncSides();
        }
    }

    /** {@code destroy}: every node keeps its share; the conduits rebuild their network on their next tick. */
    void dissolve() {
        if (dissolved) {
            return;
        }
        dissolved = true;
        float share = getNodeEnergy();
        for (EnergyConduitBlockEntity conduit : members) {
            conduit.grid = null;
            conduit.setPendingEnergy(conduit.isNode() ? share : 0.0F);
        }
    }

    /** {@code getNodeEnergy}: the per-node share, 0 below 1 MJ. */
    public float getNodeEnergy() {
        int nodes = getNodeCount();
        if (nodes <= 0) {
            return 0.0F;
        }
        float share = provider.getEnergyStored() / nodes;
        return share >= 1.0F ? share : 0.0F;
    }

    /** {@code changeMode}. */
    void changeMode(EnergyConduitBlockEntity conduit) {
        outputs.remove(conduit);
        inputs.remove(conduit);
        (conduit.getMode() == ConduitBlockEntity.MODE_OUTPUT ? outputs : inputs).add(conduit);
    }

    /** {@code transferEnergy}: once per tick, from the first output node. */
    void tick(EnergyConduitBlockEntity conduit) {
        if (outputs.isEmpty() || outputs.get(0) != conduit || provider.getEnergyStored() <= 0.0F) {
            return;
        }
        float distAmount = getNodeEnergy();
        if (distAmount <= 0.0F) {
            return;
        }
        for (EnergyConduitBlockEntity output : outputs) {
            float left = distAmount;
            for (int i = output.outputTracker; i < 6 && left > 0.0F; i++) {
                left -= output.transferEnergy(Direction.from3DDataValue(i), left);
            }
            for (int i = 0; i < output.outputTracker && left > 0.0F; i++) {
                left -= output.transferEnergy(Direction.from3DDataValue(i), left);
            }
            output.trackSent(distAmount - left);
            output.increaseTracker();
        }
    }
}
