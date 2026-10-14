package dev.craft.core.energy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/**
 * Packet based EU distribution for one dimension, following IC2 1.115 (see {@code docs/spec/01-eu-system.md} §2).
 * <p>
 * The net is independent of the level: tiles register themselves, neighbours are looked up among the registered
 * tiles, and living entities for electric shocks come from a {@link ShockTargetQuery}. Shock damage is accumulated
 * here and applied by the owner at the start of the next tick via {@link #drainShockEnergy()}.
 */
public final class EnergyNet {
    public static final double MIN_CONDUCTION_LOSS = 1.0E-4;

    /** IC2 neighbour order: -X, +X, -Y, +Y, -Z, +Z. */
    private static final Direction[] DIRECTIONS = {
            Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH
    };

    private final Map<BlockPos, EnergyTile> tiles = new HashMap<>();
    private final Map<EnergySource, List<EnergyPath>> pathCache = new IdentityHashMap<>();
    private final Map<Object, Integer> shockEnergy = new LinkedHashMap<>();
    private final ShockTargetQuery shockTargets;
    private final Random random;

    public EnergyNet(ShockTargetQuery shockTargets, Random random) {
        this.shockTargets = shockTargets;
        this.random = random;
    }

    public boolean contains(EnergyTile tile) {
        return tiles.get(tile.getBlockPos()) == tile;
    }

    public EnergyTile getTile(BlockPos pos) {
        return tiles.get(pos);
    }

    public void add(EnergyTile tile) {
        if (contains(tile)) {
            return;
        }
        EnergyTile previous = tiles.get(tile.getBlockPos());
        if (previous != null) {
            remove(previous);
        }
        if (tile instanceof EnergyAcceptor) {
            invalidateReachingSources(tile, true);
        }
        tiles.put(tile.getBlockPos().immutable(), tile);
    }

    public void remove(EnergyTile tile) {
        if (!contains(tile)) {
            return;
        }
        if (tile instanceof EnergyAcceptor) {
            invalidateReachingSources(tile, tile instanceof EnergyConductor);
        }
        if (tile instanceof EnergySource source) {
            pathCache.remove(source);
        }
        tiles.remove(tile.getBlockPos());
    }

    /**
     * Drops cached paths of every source that reaches {@code tile}. For a removed plain sink only the path to that
     * sink is dropped ({@code dropWholeCache == false}).
     */
    private void invalidateReachingSources(EnergyTile tile, boolean dropWholeCache) {
        for (EnergyPath reversePath : discover(tile, true, Integer.MAX_VALUE)) {
            EnergySource source = (EnergySource) reversePath.target;
            List<EnergyPath> paths = pathCache.get(source);
            if (paths == null || source.getMaxEnergyOutput() <= reversePath.loss) {
                continue;
            }
            if (dropWholeCache) {
                pathCache.remove(source);
            } else {
                for (int i = 0; i < paths.size(); i++) {
                    if (paths.get(i).target == tile) {
                        paths.remove(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Distributes one packet of {@code amount} EU from {@code source}.
     *
     * @return EU that could not be delivered
     */
    public int emitEnergyFrom(EnergySource source, int amount) {
        if (!contains(source)) {
            return amount;
        }
        List<EnergyPath> paths = pathCache.computeIfAbsent(source, s -> discover(s, false, s.getMaxEnergyOutput()));

        List<EnergyPath> active = new ArrayList<>();
        double totalInvLoss = 0.0;
        for (EnergyPath path : paths) {
            EnergySink sink = (EnergySink) path.target;
            if (sink.demandsEnergy() > 0 && path.loss < amount) {
                totalInvLoss += 1.0 / path.loss;
                active.add(path);
            }
        }

        Collections.shuffle(active, random);
        for (int i = active.size() - amount; i > 0; i--) {
            EnergyPath removed = active.remove(active.size() - 1);
            totalInvLoss -= 1.0 / removed.loss;
        }

        Map<EnergyPath, Integer> supplied = new LinkedHashMap<>();
        while (!active.isEmpty() && amount > 0) {
            int consumed = 0;
            double newTotalInvLoss = 0.0;
            List<EnergyPath> current = active;
            active = new ArrayList<>();

            for (EnergyPath path : current) {
                EnergySink sink = (EnergySink) path.target;
                int provided = (int) Math.floor(Math.round(amount / totalInvLoss / path.loss * 100000.0) / 100000.0);
                int loss = (int) Math.floor(path.loss);
                if (provided > loss) {
                    int returned = sink.injectEnergy(path.targetDirection, provided - loss);
                    if (returned == 0 && sink.demandsEnergy() > 0) {
                        active.add(path);
                        newTotalInvLoss += 1.0 / path.loss;
                    } else if (returned >= provided - loss) {
                        // sink accepted nothing although it demanded energy
                        returned = provided - loss;
                    }
                    consumed += provided - returned;
                    supplied.merge(path, provided - loss - returned, Integer::sum);
                } else {
                    active.add(path);
                    newTotalInvLoss += 1.0 / path.loss;
                }
            }

            if (consumed == 0 && !active.isEmpty()) {
                EnergyPath removed = active.remove(active.size() - 1);
                newTotalInvLoss -= 1.0 / removed.loss;
            }
            totalInvLoss = newTotalInvLoss;
            amount -= consumed;
        }

        for (Map.Entry<EnergyPath, Integer> entry : supplied.entrySet()) {
            applyPacketEffects(entry.getKey(), entry.getValue());
        }
        return amount;
    }

    private void applyPacketEffects(EnergyPath path, int injected) {
        path.totalEnergyConducted += injected;

        if (injected > path.minInsulationEnergyAbsorption) {
            AABB area = new AABB(path.minX - 1, path.minY - 1, path.minZ - 1,
                    path.maxX + 2, path.maxY + 2, path.maxZ + 2);
            for (ShockTarget target : shockTargets.find(area)) {
                int maxShock = 0;
                for (EnergyConductor conductor : path.conductors) {
                    BlockPos pos = conductor.getBlockPos();
                    AABB around = new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                            pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
                    if (target.bounds().intersects(around)) {
                        int shock = injected - conductor.getInsulationEnergyAbsorption();
                        if (shock > maxShock) {
                            maxShock = shock;
                        }
                        if (conductor.getInsulationEnergyAbsorption() == path.minInsulationEnergyAbsorption) {
                            break;
                        }
                    }
                }
                shockEnergy.merge(target.key(), maxShock, Integer::sum);
            }

            if (injected >= path.minInsulationBreakdownEnergy) {
                for (EnergyConductor conductor : path.conductors) {
                    if (injected >= conductor.getInsulationBreakdownEnergy()) {
                        conductor.removeInsulation();
                        path.minInsulationEnergyAbsorption = Math.min(path.minInsulationEnergyAbsorption,
                                conductor.getInsulationEnergyAbsorption());
                    }
                }
            }
        }

        if (injected >= path.minConductorBreakdownEnergy) {
            for (EnergyConductor conductor : path.conductors) {
                if (injected >= conductor.getConductorBreakdownEnergy()) {
                    conductor.removeConductor();
                }
            }
        }
    }

    /**
     * Returns the accumulated shock energy per entity key and clears it. Damage is {@code (energy + 63) / 64}.
     */
    public Map<Object, Integer> drainShockEnergy() {
        if (shockEnergy.isEmpty()) {
            return Map.of();
        }
        Map<Object, Integer> result = new LinkedHashMap<>(shockEnergy);
        shockEnergy.clear();
        return result;
    }

    /** EU that went through {@code tile} (conductor) or was emitted by it (source) since the paths were cached. */
    public long getTotalEnergyEmitted(EnergyTile tile) {
        long total = 0;
        if (tile instanceof EnergyConductor conductor && contains(tile)) {
            for (EnergyPath reversePath : discover(tile, true, Integer.MAX_VALUE)) {
                EnergySource source = (EnergySource) reversePath.target;
                List<EnergyPath> paths = pathCache.get(source);
                if (paths == null || source.getMaxEnergyOutput() <= reversePath.loss) {
                    continue;
                }
                for (EnergyPath path : paths) {
                    if (path.conductors.contains(conductor)) {
                        total += path.totalEnergyConducted;
                    }
                }
            }
        }
        if (tile instanceof EnergySource source) {
            List<EnergyPath> paths = pathCache.get(source);
            if (paths != null) {
                for (EnergyPath path : paths) {
                    total += path.totalEnergyConducted;
                }
            }
        }
        return total;
    }

    /** EU that went into {@code tile} (sink) or through it (conductor). */
    public long getTotalEnergySunken(EnergyTile tile) {
        long total = 0;
        if ((tile instanceof EnergyConductor || tile instanceof EnergySink) && contains(tile)) {
            for (EnergyPath reversePath : discover(tile, true, Integer.MAX_VALUE)) {
                EnergySource source = (EnergySource) reversePath.target;
                List<EnergyPath> paths = pathCache.get(source);
                if (paths == null || source.getMaxEnergyOutput() <= reversePath.loss) {
                    continue;
                }
                for (EnergyPath path : paths) {
                    if (path.target == tile || tile instanceof EnergyConductor c && path.conductors.contains(c)) {
                        total += path.totalEnergyConducted;
                    }
                }
            }
        }
        return total;
    }

    /** Targets reachable from {@code emitter}: sinks (forward) or sources (reverse). */
    public List<EnergyTile> discoverTargets(EnergyTile emitter, boolean reverse, int lossLimit) {
        List<EnergyTile> targets = new ArrayList<>();
        for (EnergyPath path : discover(emitter, reverse, lossLimit)) {
            targets.add(path.target);
        }
        return targets;
    }

    private List<EnergyPath> discover(EnergyTile emitter, boolean reverse, int lossLimit) {
        Map<EnergyTile, BlockLink> reached = new LinkedHashMap<>();
        Deque<EnergyTile> toCheck = new ArrayDeque<>();
        toCheck.add(emitter);

        while (!toCheck.isEmpty()) {
            EnergyTile current = toCheck.remove();
            if (current != emitter && !contains(current)) {
                continue;
            }
            double currentLoss = current == emitter ? 0.0 : reached.get(current).loss;

            for (Direction direction : DIRECTIONS) {
                EnergyTile neighbour = validNeighbour(current, direction, reverse);
                if (neighbour == null || neighbour == emitter) {
                    continue;
                }
                double additionalLoss = 0.0;
                if (neighbour instanceof EnergyConductor conductor) {
                    additionalLoss = Math.max(conductor.getConductionLoss(), MIN_CONDUCTION_LOSS);
                    if (currentLoss + additionalLoss >= lossLimit) {
                        continue;
                    }
                }
                BlockLink link = reached.get(neighbour);
                if (link == null || link.loss > currentLoss + additionalLoss) {
                    reached.put(neighbour, new BlockLink(direction.getOpposite(), currentLoss + additionalLoss));
                    if (neighbour instanceof EnergyConductor) {
                        toCheck.remove(neighbour);
                        toCheck.add(neighbour);
                    }
                }
            }
        }

        List<EnergyPath> paths = new ArrayList<>();
        nextTarget:
        for (Map.Entry<EnergyTile, BlockLink> entry : reached.entrySet()) {
            EnergyTile tile = entry.getKey();
            if (reverse ? !(tile instanceof EnergySource) : !(tile instanceof EnergySink)) {
                continue;
            }
            BlockLink link = entry.getValue();
            EnergyPath path = new EnergyPath(tile, link.direction, Math.max(link.loss, 0.1));

            if (!reverse && emitter instanceof EnergySource) {
                while (true) {
                    tile = tiles.get(tile.getBlockPos().relative(link.direction));
                    if (tile == emitter) {
                        break;
                    }
                    if (!(tile instanceof EnergyConductor conductor)) {
                        continue nextTarget;
                    }
                    path.addConductor(conductor);
                    link = reached.get(tile);
                    if (link == null) {
                        continue nextTarget;
                    }
                }
            }
            paths.add(path);
        }
        return paths;
    }

    private EnergyTile validNeighbour(EnergyTile current, Direction direction, boolean reverse) {
        EnergyTile target = tiles.get(current.getBlockPos().relative(direction));
        if (target == null) {
            return null;
        }
        Direction inverse = direction.getOpposite();
        EnergyTile from = reverse ? target : current;
        EnergyTile to = reverse ? current : target;
        Direction outward = reverse ? inverse : direction;
        boolean valid = from instanceof EnergyEmitter emitter && emitter.emitsEnergyTo(to, outward)
                && to instanceof EnergyAcceptor acceptor && acceptor.acceptsEnergyFrom(from, outward.getOpposite());
        return valid ? target : null;
    }

    private record BlockLink(Direction direction, double loss) {
    }

    private static final class EnergyPath {
        final EnergyTile target;
        final Direction targetDirection;
        final double loss;
        final Set<EnergyConductor> conductors = new LinkedHashSet<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minInsulationEnergyAbsorption = Integer.MAX_VALUE;
        int minInsulationBreakdownEnergy = Integer.MAX_VALUE;
        int minConductorBreakdownEnergy = Integer.MAX_VALUE;
        long totalEnergyConducted;

        EnergyPath(EnergyTile target, Direction targetDirection, double loss) {
            this.target = target;
            this.targetDirection = targetDirection;
            this.loss = loss;
        }

        void addConductor(EnergyConductor conductor) {
            BlockPos pos = conductor.getBlockPos();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
            conductors.add(conductor);
            minInsulationEnergyAbsorption = Math.min(minInsulationEnergyAbsorption,
                    conductor.getInsulationEnergyAbsorption());
            minInsulationBreakdownEnergy = Math.min(minInsulationBreakdownEnergy,
                    conductor.getInsulationBreakdownEnergy());
            minConductorBreakdownEnergy = Math.min(minConductorBreakdownEnergy,
                    conductor.getConductorBreakdownEnergy());
        }
    }
}
