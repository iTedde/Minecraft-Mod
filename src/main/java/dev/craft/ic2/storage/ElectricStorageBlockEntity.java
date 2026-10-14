package dev.craft.ic2.storage;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.menu.ElectricStorageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BatBox, MFE and MFSU, 1:1 with IC2 {@code TileEntityElectricBlock}: input on five sides, output on the front,
 * charge slot 0 and discharge slot 1, six redstone modes.
 */
public class ElectricStorageBlockEntity extends MachineBlockEntity implements EnergySink, EnergySource {
    public static final int REDSTONE_MODES = 6;
    private static final String[] REDSTONE_MODE_MESSAGES = {
            "Redstone Behavior: Nothing",
            "Redstone Behavior: Emit if full",
            "Redstone Behavior: Emit if partially filled",
            "Redstone Behavior: Emit if empty",
            "Redstone Behavior: Do not output energy",
            "Redstone Behavior: Do not output energy unless full"
    };

    private final int tier;
    private final int output;
    private final int maxStorage;
    private int energy;
    private byte redstoneMode;
    private boolean isEmittingRedstone;
    private int redstoneUpdateInhibit = 5;

    public ElectricStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tier, int output,
                                      int maxStorage) {
        super(type, pos, state, 2);
        this.tier = tier;
        this.output = output;
        this.maxStorage = maxStorage;
    }

    public int getTier() {
        return tier;
    }

    public int getOutput() {
        return output;
    }

    public int getMaxStorage() {
        return maxStorage;
    }

    public int getEnergy() {
        return energy;
    }

    public int getRedstoneMode() {
        return redstoneMode;
    }

    public boolean isEmittingRedstone() {
        return isEmittingRedstone;
    }

    @Override
    public void serverTick() {
        boolean needsInvUpdate = false;
        ItemStack chargeStack = inventory.get(0);
        if (energy > 0 && ElectricItems.isElectric(chargeStack)) {
            int sent = ElectricItems.charge(chargeStack, energy, tier, false, false);
            energy -= sent;
            needsInvUpdate = sent > 0;
        }
        ItemStack dischargeStack = inventory.get(1);
        if (demandsEnergy() > 0 && !dischargeStack.isEmpty()) {
            if (dischargeStack.getItem() instanceof ElectricItem item) {
                if (item.canProvideEnergy()) {
                    int gain = ElectricItems.discharge(dischargeStack, maxStorage - energy, tier, false, false);
                    energy += gain;
                    needsInvUpdate = gain > 0;
                }
            } else {
                int gain = 0;
                if (dischargeStack.is(Items.REDSTONE)) {
                    gain = 500;
                }
                if (dischargeStack.is(Ic2Items.SU_BATTERY.get())) {
                    gain = 1000;
                }
                if (gain > 0 && gain <= maxStorage - energy) {
                    dischargeStack.shrink(1);
                    energy += gain;
                }
            }
        }
        boolean powered = level.getDirectSignalTo(worldPosition) > 0;
        if (energy >= output && (redstoneMode != 4 || !powered)
                && (redstoneMode != 5 || !powered || energy >= maxStorage)) {
            int rest = EnergyNets.emit(level, this, output);
            energy -= output - rest;
        }
        boolean shouldEmitRedstone = shouldEmitRedstone();
        if (shouldEmitRedstone != isEmittingRedstone) {
            isEmittingRedstone = shouldEmitRedstone;
            setActive(isEmittingRedstone);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        }
    }

    private boolean shouldEmitRedstone() {
        boolean should = switch (redstoneMode) {
            case 1 -> energy >= maxStorage;
            case 2 -> energy > output && energy < maxStorage;
            case 3 -> energy < output;
            default -> false;
        };
        if (isEmittingRedstone != should && redstoneUpdateInhibit != 0) {
            redstoneUpdateInhibit--;
            return isEmittingRedstone;
        }
        redstoneUpdateInhibit = 5;
        return should;
    }

    /** GUI button: next redstone mode, reported in chat. */
    public void cycleRedstoneMode(Player player) {
        redstoneMode++;
        if (redstoneMode >= REDSTONE_MODES) {
            redstoneMode = 0;
        }
        player.sendSystemMessage(Component.literal(REDSTONE_MODE_MESSAGES[redstoneMode]));
        setChanged();
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
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return direction != getFacing();
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return direction == getFacing();
    }

    @Override
    public int getMaxEnergyOutput() {
        return output;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : maxStorage - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > output) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        int need = amount;
        if (energy + amount >= maxStorage + output) {
            need = maxStorage + output - energy - 1;
        }
        energy += need;
        return amount - need;
    }

    @Override
    public int getMaxSafeInput() {
        return output;
    }

    // --- wrench ---

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return getFacing() != side;
    }

    /** Re-registers in the energy net so that input/output sides are recomputed. */
    @Override
    public void setFacing(Direction facing) {
        EnergyNets.remove(level, this);
        super.setFacing(facing);
        EnergyNets.add(level, this);
    }

    /** Bottom → discharge slot 1, other sides → charge slot 0. */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN ? 1 : 0;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ElectricStorageMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        redstoneMode = tag.getByte("redstoneMode");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putByte("redstoneMode", redstoneMode);
    }
}
