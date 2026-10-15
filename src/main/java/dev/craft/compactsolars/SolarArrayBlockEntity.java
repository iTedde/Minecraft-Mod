package dev.craft.compactsolars;

import java.util.HashSet;
import java.util.Set;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Controller of a solar array multiblock (Compact Solars {@code TileEntityCompactSolar} as a flat structure, spec 11
 * §4). Every {@link #CHECK_INTERVAL} ticks it validates the structure, leases its panels and lower controllers and
 * counts the panels that see the sun; each tick it delivers {@code min(cap, sunny panels)} EU: first into the charge
 * slot, the rest as one packet into the net. Energy the net does not take is lost; there is no buffer.
 */
public class SolarArrayBlockEntity extends MachineBlockEntity implements EnergySource {
    public static final int CHECK_INTERVAL = 20;
    public static final int LEASE_DURATION = 40;

    private final SolarArrayType type;
    private final MultiblockController structure;
    private final MultiblockLease lease = new MultiblockLease();
    private int sunnyPanels;
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
                sunnyPanels = match == null ? 0 : claimParts(match);
            }
        });
    }

    public SolarArrayType getArrayType() {
        return type;
    }

    public boolean isFormed() {
        return structure.isFormed();
    }

    /** Sunny panels counted at the last check (not capped). */
    public int getSunnyPanels() {
        return sunnyPanels;
    }

    /** True while the array delivers energy (sun symbol in the GUI). */
    public boolean isDelivering() {
        return delivering;
    }

    /** Held by the controller of a higher array this one is built into. */
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
        int radius = type.getRadius();
        if (!level.hasChunksAt(worldPosition.offset(-radius, 0, -radius), worldPosition.offset(radius, 0, radius))) {
            return;
        }
        structure.tick(level, worldPosition);
        if (!structure.isFormed()) {
            return;
        }
        int energy = Math.min(type.getOutput(), sunnyPanels);
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
        if (energy > 0) {
            EnergyNets.emit(level, this, energy);
        }
    }

    /**
     * Renews the leases on the lower controllers (highest tier first, so a controller may take over the parts of
     * the controllers it has just claimed) and on the panels; returns the number of claimed panels that see the sun.
     * Parts leased by a foreign array are skipped.
     */
    private int claimParts(MultiblockPattern.Match match) {
        long now = level.getGameTime();
        Set<BlockPos> owners = new HashSet<>();
        owners.add(worldPosition);
        for (int i = type.ordinal() - 1; i >= 0; i--) {
            for (BlockPos pos : match.get(SolarArrayType.values()[i].getSymbol())) {
                if (level.getBlockEntity(pos) instanceof SolarArrayBlockEntity lower
                        && lower.lease.claim(worldPosition, owners, now, LEASE_DURATION)) {
                    owners.add(pos);
                }
            }
        }
        int sunny = 0;
        for (BlockPos pos : match.get(SolarArrayPatterns.PANEL)) {
            if (level.getBlockEntity(pos) instanceof SolarPanelBlockEntity panel
                    && panel.getArrayLease().claim(worldPosition, owners, now, LEASE_DURATION)
                    && panel.isSunVisible()) {
                sunny++;
            }
        }
        return sunny;
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
        return type.getOutput();
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
}
