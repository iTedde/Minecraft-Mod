package dev.craft.core.energy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class EnergyNetTest {
    private final List<ShockTarget> entities = new ArrayList<>();
    private final EnergyNet net = new EnergyNet(area -> entities.stream()
            .filter(e -> e.bounds().intersects(area)).toList(), new Random(1));

    @Test
    void shortTinLineDeliversWholePacket() {
        Source source = add(new Source(pos(0), 10));
        for (int i = 1; i <= 5; i++) {
            add(new Cable(pos(i), 0.025, 3, 6));
        }
        Sink sink = add(new Sink(pos(6), 1000, 32));

        assertEquals(0, net.emitEnergyFrom(source, 10));
        assertEquals(10, sink.received);
    }

    @Test
    void lossIsAccumulatedAsDoubleAndFloored() {
        Source source = add(new Source(pos(0), 10));
        for (int i = 1; i <= 10; i++) {
            add(new Cable(pos(i), 0.2, 32, 33));
        }
        Sink sink = add(new Sink(pos(11), 1000, 32));

        // 10 x 0.2 accumulates to 1.9999999999999998 -> floor 1, as in IC2
        assertEquals(0, net.emitEnergyFrom(source, 10));
        assertEquals(9, sink.received);
    }

    @Test
    void elevenCopperCablesLoseTwo() {
        Source source = add(new Source(pos(0), 10));
        for (int i = 1; i <= 11; i++) {
            add(new Cable(pos(i), 0.2, 32, 33));
        }
        Sink sink = add(new Sink(pos(12), 1000, 32));

        net.emitEnergyFrom(source, 10);
        assertEquals(8, sink.received);
    }

    @Test
    void packetIsSplitInverselyToLoss() {
        Source source = add(new Source(pos(0), 32));
        add(new Cable(pos(1), 0.2, 32, 33));
        Sink near = add(new Sink(pos(2), 1000, 32));
        add(new Cable(new BlockPos(0, 1, 0), 0.2, 32, 33));
        add(new Cable(new BlockPos(0, 2, 0), 0.2, 32, 33));
        add(new Cable(new BlockPos(0, 3, 0), 0.2, 32, 33));
        Sink far = add(new Sink(new BlockPos(0, 4, 0), 1000, 32));

        assertEquals(0, net.emitEnergyFrom(source, 30));
        // losses 0.2 and 0.6 -> 22 and 7 EU; the remaining 1 EU goes to one random path in a later round
        assertEquals(30, near.received + far.received);
        assertTrue(near.received == 22 && far.received == 8 || near.received == 23 && far.received == 7);
    }

    @Test
    void undeliveredEnergyIsReturned() {
        Source source = add(new Source(pos(0), 32));
        add(new Cable(pos(1), 0.2, 32, 33));
        Sink sink = add(new Sink(pos(2), 5, 32));

        assertEquals(27, net.emitEnergyFrom(source, 32));
        assertEquals(5, sink.received);
    }

    @Test
    void sinkAddedLaterInvalidatesCache() {
        Source source = add(new Source(pos(0), 32));
        add(new Cable(pos(1), 0.2, 32, 33));
        assertEquals(10, net.emitEnergyFrom(source, 10));

        Sink sink = add(new Sink(pos(2), 1000, 32));
        assertEquals(0, net.emitEnergyFrom(source, 10));
        assertEquals(10, sink.received);
    }

    @Test
    void removedConductorBreaksPath() {
        Source source = add(new Source(pos(0), 32));
        Cable cable = add(new Cable(pos(1), 0.2, 32, 33));
        Sink sink = add(new Sink(pos(2), 1000, 32));
        net.emitEnergyFrom(source, 10);

        net.remove(cable);
        assertEquals(10, net.emitEnergyFrom(source, 10));
        assertEquals(10, sink.received);
    }

    @Test
    void overloadedCableMeltsAndShocks() {
        Source source = add(new Source(pos(0), 32));
        Cable tin = add(new Cable(pos(1), 0.025, 3, 6));
        Sink sink = add(new Sink(pos(2), 1000, 32));
        Object entity = new Object();
        entities.add(new ShockTarget(entity, new AABB(1.2, 1, 0.2, 1.8, 2.8, 0.8)));

        net.emitEnergyFrom(source, 10);

        assertEquals(10, sink.received);
        assertTrue(tin.melted);
        assertFalse(net.contains(tin));
        assertEquals(Map.of(entity, 7), net.drainShockEnergy());
        assertTrue(net.drainShockEnergy().isEmpty());
    }

    @Test
    void lossLimitStopsDiscovery() {
        Source source = add(new Source(pos(0), 1));
        add(new Cable(pos(1), 1.0, 0, 2049));
        Sink sink = add(new Sink(pos(2), 1000, 32));

        assertEquals(1, net.emitEnergyFrom(source, 1));
        assertEquals(0, sink.received);
    }

    @Test
    void conductorTracksEmittedEnergy() {
        Source source = add(new Source(pos(0), 32));
        Cable cable = add(new Cable(pos(1), 0.2, 32, 33));
        add(new Sink(pos(2), 1000, 32));

        net.emitEnergyFrom(source, 10);
        net.emitEnergyFrom(source, 10);
        assertEquals(20, net.getTotalEnergyEmitted(cable));
    }

    private static BlockPos pos(int x) {
        return new BlockPos(x, 0, 0);
    }

    private <T extends EnergyTile> T add(T tile) {
        net.add(tile);
        return tile;
    }

    private record Source(BlockPos getBlockPos, int getMaxEnergyOutput) implements EnergySource {
        @Override
        public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
            return true;
        }
    }

    private static final class Sink implements EnergySink {
        final BlockPos pos;
        final int capacity;
        final int maxSafeInput;
        int received;

        Sink(BlockPos pos, int capacity, int maxSafeInput) {
            this.pos = pos;
            this.capacity = capacity;
            this.maxSafeInput = maxSafeInput;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
            return true;
        }

        @Override
        public int demandsEnergy() {
            return capacity - received;
        }

        @Override
        public int injectEnergy(Direction direction, int amount) {
            int accepted = Math.min(amount, capacity - received);
            received += accepted;
            return amount - accepted;
        }

        @Override
        public int getMaxSafeInput() {
            return maxSafeInput;
        }
    }

    private final class Cable implements EnergyConductor {
        final BlockPos pos;
        final double loss;
        final int insulation;
        final int breakdown;
        boolean melted;

        Cable(BlockPos pos, double loss, int insulation, int breakdown) {
            this.pos = pos;
            this.loss = loss;
            this.insulation = insulation;
            this.breakdown = breakdown;
        }

        @Override
        public BlockPos getBlockPos() {
            return pos;
        }

        @Override
        public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
            return true;
        }

        @Override
        public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
            return true;
        }

        @Override
        public double getConductionLoss() {
            return loss;
        }

        @Override
        public int getInsulationEnergyAbsorption() {
            return insulation;
        }

        @Override
        public int getInsulationBreakdownEnergy() {
            return 9001;
        }

        @Override
        public int getConductorBreakdownEnergy() {
            return breakdown;
        }

        @Override
        public void removeInsulation() {
        }

        @Override
        public void removeConductor() {
            melted = true;
            net.remove(this);
        }
    }
}
