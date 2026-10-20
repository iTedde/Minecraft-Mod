package dev.craft.compactsolars;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.core.multiblock.MultiblockController;
import dev.craft.core.multiblock.MultiblockLease;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Centre of a solar array multiblock (Compact Solars {@code TileEntityCompactSolar} as a 3×3 structure, spec 11 §4).
 * Every {@link #CHECK_INTERVAL} ticks it validates the structure, leases the panels and arrays of its ring and adds up
 * the base values of the parts that see the sun, itself included; each tick it delivers that sum times its bonus:
 * first into the charge slot, the rest as packets of at most the tier's packet size into the net. Energy the net
 * does not take is lost; there is no buffer.
 */
public class SolarArrayBlockEntity extends MachineBlockEntity implements EnergySource, OutputReadout {
    public static final int CHECK_INTERVAL = 20;
    public static final int LEASE_DURATION = 40;

    private final SolarArrayType type;
    private final MultiblockController structure;
    private final MultiblockLease lease = new MultiblockLease();
    private final OutputMeter outputMeter = new OutputMeter();
    private int sunnyValue;
    private boolean delivering;

    public SolarArrayBlockEntity(BlockPos pos, BlockState state) {
        super(CsBlockEntityTypes.SOLAR_ARRAY.get(), pos, state, 1);
        this.type = ((SolarArrayBlock) state.getBlock()).getArrayType();
        this.structure = new MultiblockController(SolarArrayPatterns.pattern(type), CHECK_INTERVAL,
                () -> Direction.NORTH, new MultiblockController.Listener() {
            @Override
            public void onFormed(MultiblockPattern.Match match) {
            }

            @Override
            public void onBroken(MultiblockPattern.Match match) {
            }

            @Override
            public void onChecked(@Nullable MultiblockPattern.Match match) {
                sunnyValue = match == null ? 0 : claimParts(match);
            }
        });
    }

    public SolarArrayType getArrayType() {
        return type;
    }

    public boolean isFormed() {
        return structure.isFormed();
    }

    /** Base values of the sunny parts at the last check, before the bonus. */
    public int getSunnyValue() {
        return sunnyValue;
    }

    /** True while the array delivers energy (sun symbol in the GUI). */
    public boolean isDelivering() {
        return delivering;
    }

    /** Held by the centre of the array whose ring this one is part of. */
    public MultiblockLease getLease() {
        return lease;
    }

    @Override
    public void serverTick() {
        delivering = false;
        long now = level.getGameTime();
        if (lease.isHeld(now)) {
            return;
        }
        int radius = SolarArrayPatterns.RADIUS;
        if (!level.hasChunksAt(worldPosition.offset(-radius, 0, -radius), worldPosition.offset(radius, 0, radius))) {
            return;
        }
        structure.tick(level, worldPosition);
        if (!structure.isFormed()) {
            return;
        }
        int energy = type.output(sunnyValue);
        outputMeter.record(level, Math.max(energy, 0));
        if (energy <= 0) {
            return;
        }
        delivering = true;
        ItemStack chargeStack = inventory.get(0);
        if (ElectricItems.isElectric(chargeStack)) {
            int used = ElectricItems.charge(chargeStack, energy, type.getTier(), false, false);
            energy -= used;
            if (used > 0) {
                onInventoryChanged();
            }
        }
        while (energy > 0) {
            int packet = Math.min(energy, type.getPacketSize());
            energy -= packet;
            if (EnergyNets.emit(level, this, packet) > 0) {
                break;
            }
        }
    }

    /**
     * Renews the leases on the arrays of the ring (highest tier first, so this array may take over the parts of the
     * arrays it is about to claim) and on its panels; returns the summed base values of itself and of the parts that
     * see the sun. Only the 9 blocks of one field work together (user, 2026-10-20): if any part of the ring is held
     * by a foreign field, nothing is claimed and the field delivers nothing.
     */
    private int claimParts(MultiblockPattern.Match match) {
        long now = level.getGameTime();
        List<BlockPos> ring = new ArrayList<>(match.get(SolarArrayPatterns.RING));
        ring.sort(Comparator.comparingInt(pos -> level.getBlockEntity(pos) instanceof SolarArrayBlockEntity array
                ? -array.type.ordinal() : 1));
        Set<BlockPos> owners = new HashSet<>();
        owners.add(worldPosition);
        List<MultiblockLease> leases = new ArrayList<>();
        int sunny = sunVisible(worldPosition) ? type.getBaseValue() : 0;
        for (BlockPos pos : ring) {
            BlockEntity part = level.getBlockEntity(pos);
            MultiblockLease partLease;
            if (part instanceof SolarArrayBlockEntity array) {
                partLease = array.lease;
                if (sunVisible(pos)) {
                    sunny += array.type.getBaseValue();
                }
            } else if (part instanceof SolarPanelBlockEntity panel) {
                partLease = panel.getArrayLease();
                if (panel.isSunVisible()) {
                    sunny++;
                }
            } else {
                return 0;
            }
            if (!partLease.canClaim(owners, now)) {
                return 0;
            }
            if (part instanceof SolarArrayBlockEntity) {
                owners.add(pos);
            }
            leases.add(partLease);
        }
        for (MultiblockLease partLease : leases) {
            partLease.claim(worldPosition, owners, now, LEASE_DURATION);
        }
        return sunny;
    }

    /** Same sun check as the IC2 solar panel, for this array and the arrays of its ring. */
    private boolean sunVisible(BlockPos pos) {
        return SolarPanelBlockEntity.isSunVisible(level, pos.above());
    }

    // --- energy net ---

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

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return true;
    }

    @Override
    public int getMaxEnergyOutput() {
        return type.getPacketSize();
    }

    // --- no facing, no active state ---

    @Override
    public Ic2Machine getMachine() {
        throw new UnsupportedOperationException("solar arrays are not IC2 machines");
    }

    @Override
    public Direction getFacing() {
        return Direction.NORTH;
    }

    @Override
    public void setFacing(Direction facing) {
    }

    @Override
    public boolean isActive() {
        return delivering;
    }

    @Override
    public void setActive(boolean active) {
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    protected int getSlotForSide(Direction side) {
        return 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new SolarArrayMenu(id, playerInventory, this);
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.EU);
    }
}
