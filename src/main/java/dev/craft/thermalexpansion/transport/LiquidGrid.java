package dev.craft.thermalexpansion.transport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * {@code GridLiquid} + {@code LiquidTankGrid}: one tank for all connected liquiducts, {@code 5 · viscosity} mB per
 * conduit (CoFH {@code LiquidRegistry}: lava 80, water and everything else 100). Output nodes hand out
 * {@code min(amount / outputs, throughput)} per tick – the throughput falls from the viscosity (tank ≥ 75 % full) to
 * half of it (≤ 25 %); input nodes with a redstone signal pull up to the viscosity per side and tick. Conduits with
 * different liquids do not join.
 */
public final class LiquidGrid {
    /** {@code minLiquidTime}: a liquid that passed through stays visible this long. */
    private static final int MIN_LIQUID_TIME = 100;

    private final List<LiquiductBlockEntity> members = new ArrayList<>();
    private final List<LiquiductBlockEntity> outputs = new ArrayList<>();
    private final List<LiquiductBlockEntity> inputs = new ArrayList<>();
    private final Level level;
    private FluidStack liquid = FluidStack.EMPTY;
    private int capacity;
    private int liquidThroughput = 100;
    private int liquidPerConduit = 500;
    private boolean dissolved;
    // GUI/render state of the network (updateRender)
    private int conduitTick;
    private long lastFill;
    private boolean recentUpdate;
    private Fluid recentLiquid = Fluids.EMPTY;
    private Fluid renderLiquid = Fluids.EMPTY;
    private int renderLevel;

    private LiquidGrid(Level level) {
        this.level = level;
    }

    /** {@code LiquidRegistry.getLiquidViscosity}, 0 → 100, at most 200. */
    static int viscosity(FluidStack fluid) {
        return !fluid.isEmpty() && fluid.getFluid().isSame(Fluids.LAVA) ? 80 : 100;
    }

    /** {@code LiquidRegistry.getLiquidLightLevel}: lava 15, the rest 0. */
    static int lightLevel(Fluid fluid) {
        return fluid.isSame(Fluids.LAVA) ? 15 : 0;
    }

    public FluidStack getLiquid() {
        return liquid;
    }

    public int getLiquidAmount() {
        return liquid.getAmount();
    }

    public int getCapacity() {
        return capacity;
    }

    public Fluid getRenderLiquid() {
        return renderLiquid;
    }

    public int getRenderLevel() {
        return renderLevel;
    }

    /** Flood fill over conduits whose liquid matches (or is empty); networks met on the way are taken over. */
    static void build(LiquiductBlockEntity start) {
        LiquidGrid grid = new LiquidGrid(start.getLevel());
        Fluid type = start.getPendingLiquid().getFluid();
        Deque<LiquiductBlockEntity> queue = new ArrayDeque<>();
        Set<LiquiductBlockEntity> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            LiquiductBlockEntity conduit = queue.poll();
            if (conduit.grid != null) {
                conduit.grid.dissolve();
            }
            conduit.grid = grid;
            grid.members.add(conduit);
            for (Direction side : Direction.values()) {
                if (!(conduit.neighbour(side) instanceof LiquiductBlockEntity next) || next.isRemoved()
                        || seen.contains(next)) {
                    continue;
                }
                Fluid other = next.grid != null ? next.grid.liquid.getFluid() : next.getPendingLiquid().getFluid();
                if (type != Fluids.EMPTY && other != Fluids.EMPTY && !other.isSame(type)) {
                    continue;
                }
                if (type == Fluids.EMPTY) {
                    type = other;
                }
                seen.add(next);
                queue.add(next);
            }
        }
        grid.renderLiquid = start.getRenderFluid();
        grid.renderLevel = start.getRenderLevel();
        for (LiquiductBlockEntity conduit : grid.members) {
            conduit.refreshSides();
            if (conduit.isNode()) {
                (conduit.getMode() == ConduitBlockEntity.MODE_OUTPUT ? grid.outputs : grid.inputs).add(conduit);
            }
            grid.fillWithoutLoss(conduit.takePendingLiquid());
        }
        grid.recalcTank();
        for (LiquiductBlockEntity conduit : grid.members) {
            conduit.syncSides();
        }
    }

    /** {@code destroy}: every conduit keeps an equal part, the first one the remainder. */
    void dissolve() {
        if (dissolved) {
            return;
        }
        dissolved = true;
        int count = members.size();
        int share = count > 0 ? liquid.getAmount() / count : 0;
        int overflow = count > 0 ? liquid.getAmount() % count : 0;
        for (LiquiductBlockEntity conduit : members) {
            conduit.grid = null;
            conduit.setPendingLiquid(liquid.isEmpty() ? FluidStack.EMPTY : liquid.copyWithAmount(share + overflow));
            overflow = 0;
        }
    }

    /** {@code getSharedAmount}: what a conduit saves. */
    FluidStack share(LiquiductBlockEntity conduit) {
        if (liquid.isEmpty() || members.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int amount = liquid.getAmount() / members.size();
        if (members.get(0) == conduit) {
            amount += liquid.getAmount() % members.size();
        }
        return liquid.copyWithAmount(amount);
    }

    private void recalcTank() {
        capacity = members.size() * liquidPerConduit;
        if (liquid.getAmount() > capacity) {
            liquid.setAmount(capacity);
        }
    }

    /** {@code setLiquidData}: throughput and tank size follow the liquid. */
    private void setLiquidData(FluidStack fluid, boolean isFill) {
        int viscosity = viscosity(fluid);
        liquidThroughput = viscosity;
        liquidPerConduit = 5 * viscosity;
        recalcTank();
        if (!fluid.isEmpty() && isFill) {
            lastFill = level.getGameTime();
            recentUpdate = true;
            recentLiquid = fluid.getFluid();
        }
    }

    private void fillWithoutLoss(FluidStack resource) {
        if (resource.isEmpty()) {
            return;
        }
        if (liquid.isEmpty()) {
            setLiquidData(resource, true);
            liquid = resource.copy();
        } else if (FluidStack.isSameFluidSameComponents(liquid, resource)) {
            liquid.grow(resource.getAmount());
        }
    }

    /** {@code LiquidTankGrid.fill}; an empty tank takes the liquid's data even when only simulating (original). */
    int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        if (liquid.isEmpty()) {
            setLiquidData(resource, true);
            int amount = Math.min(resource.getAmount(), capacity);
            if (action.execute() && amount > 0) {
                liquid = resource.copyWithAmount(amount);
            }
            return amount;
        }
        if (!FluidStack.isSameFluidSameComponents(liquid, resource)) {
            return 0;
        }
        int amount = Math.min(resource.getAmount(), capacity - liquid.getAmount());
        if (action.execute()) {
            liquid.grow(amount);
        }
        return amount;
    }

    FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (liquid.isEmpty() || maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        int used = Math.min(maxDrain, liquid.getAmount());
        FluidStack drained = liquid.copyWithAmount(used);
        if (action.execute()) {
            liquid.shrink(used);
            if (liquid.isEmpty()) {
                liquid = FluidStack.EMPTY;
                setLiquidData(FluidStack.EMPTY, false);
            }
        }
        return drained;
    }

    /** {@code getLiquidThroughput}. */
    public int getLiquidThroughput() {
        if (liquid.isEmpty()) {
            return 0;
        }
        int amount = liquid.getAmount();
        if (amount >= capacity * 3 / 4) {
            return liquidThroughput;
        }
        if (amount <= capacity / 4) {
            return liquidThroughput >> 1;
        }
        return (liquidThroughput >> 1) + (liquidThroughput >> 1) * (amount - (capacity >> 2)) / (capacity >> 1);
    }

    int getBaseThroughput() {
        return liquidThroughput;
    }

    void changeMode(LiquiductBlockEntity conduit) {
        outputs.remove(conduit);
        inputs.remove(conduit);
        (conduit.getMode() == ConduitBlockEntity.MODE_OUTPUT ? outputs : inputs).add(conduit);
    }

    /** {@code transferLiquid}: the first output node distributes, otherwise the first input node pulls. */
    void tick(LiquiductBlockEntity conduit) {
        if (!outputs.isEmpty() && outputs.get(0) == conduit && !liquid.isEmpty()) {
            int distAmount = Math.min(liquid.getAmount() / outputs.size(), getLiquidThroughput());
            int overflow = liquid.getAmount() % outputs.size();
            if (distAmount > 0) {
                for (LiquiductBlockEntity output : outputs) {
                    distribute(output, distAmount);
                }
            } else if (overflow > 0) {
                int[] left = {overflow};
                int start = level.random.nextInt(outputs.size());
                for (int a = 0; a < outputs.size() && left[0] > 0; a++) {
                    LiquiductBlockEntity output = outputs.get((start + a) % outputs.size());
                    left[0] -= distribute(output, left[0]);
                }
            }
        } else if (!inputs.isEmpty() && inputs.get(0) == conduit && liquid.getAmount() < capacity) {
            int start = level.random.nextInt(inputs.size());
            for (int a = 0; a < inputs.size(); a++) {
                LiquiductBlockEntity input = inputs.get((start + a) % inputs.size());
                for (int i = input.outputTracker; i < 6; i++) {
                    input.extractLiquid(Direction.from3DDataValue(i));
                }
                for (int i = 0; i < input.outputTracker; i++) {
                    input.extractLiquid(Direction.from3DDataValue(i));
                }
                input.increaseTracker();
            }
        }
    }

    /** One output node: up to {@code amount} into its tanks, from its current side on; returns what it gave. */
    private int distribute(LiquiductBlockEntity output, int amount) {
        int left = amount;
        for (int i = output.outputTracker; i < 6 && left > 0; i++) {
            left -= output.transferLiquid(Direction.from3DDataValue(i), left);
        }
        for (int i = 0; i < output.outputTracker && left > 0; i++) {
            left -= output.transferLiquid(Direction.from3DDataValue(i), left);
        }
        output.increaseTracker();
        return amount - left;
    }

    /** {@code updateRender}: once per round over all conduits; the level in six steps with hysteresis. */
    void updateRender() {
        conduitTick++;
        if (conduitTick < members.size()) {
            return;
        }
        conduitTick = 0;
        if (level.getGameTime() - lastFill >= MIN_LIQUID_TIME && recentUpdate) {
            recentUpdate = false;
            lastFill = level.getGameTime();
        }
        if (!liquid.isEmpty() && capacity > 0) {
            renderLiquid = liquid.getFluid();
            double fullPercent = 10000L * liquid.getAmount() / capacity;
            if (fullPercent <= (renderLevel == 2 ? 500 : 700)) {
                renderLevel = 1;
            } else if (fullPercent >= 500 && fullPercent <= (renderLevel == 3 ? 2000 : 2500)) {
                renderLevel = 2;
            } else if (fullPercent >= 2000 && fullPercent <= (renderLevel == 4 ? 4000 : 4500)) {
                renderLevel = 3;
            } else if (fullPercent >= 4000 && fullPercent <= (renderLevel == 5 ? 6000 : 6500)) {
                renderLevel = 4;
            } else if (fullPercent >= 6000 && fullPercent <= (renderLevel == 6 ? 8000 : 8500)) {
                renderLevel = 5;
            } else {
                renderLevel = 6;
            }
        } else if (renderLevel != 0 && !recentUpdate) {
            renderLiquid = Fluids.EMPTY;
            renderLevel = 0;
        } else if (recentUpdate && renderLiquid != recentLiquid) {
            renderLiquid = recentLiquid;
            renderLevel = 1;
        }
    }
}
