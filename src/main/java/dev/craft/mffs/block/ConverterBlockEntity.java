package dev.craft.mffs.block;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsConfig;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.energy.PowerLinkItem;
import dev.craft.mffs.menu.ConverterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityConverter} (spec 48 §4): force energy → EU, 3 FE per EU, packets of 1/32/128/512/2048 EU, 1–9 per
 * tick, to every side; settings only change while the output is off. The UE part is left out (no UE in the pack).
 */
public class ConverterBlockEntity extends FePoweredBlockEntity implements EnergySource {
    public static final int SLOT_POWER_LINK = 0;
    public static final int EVENT_IC_OUTPUT = 100;
    public static final int EVENT_PACKET_SIZE = 200;
    public static final int EVENT_PACKET_AMOUNT = 201;
    private static final int[] PACKET_SIZES = {1, 32, 128, 512, 2048};

    private boolean icOutput;
    private int packetSize = 1;
    private int packetAmount = 1;
    private int linkPower;

    public ConverterBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.CONVERTER.get(), pos, state, 4);
    }

    /** {@code getLinkedSecurityStation}: the station of the linked capacitor. */
    @Override
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return level != null && MffsDevices.get(level, MffsDevices.Kind.CAPACITOR,
                getPowerSourceId()) instanceof CapacitorBlockEntity capacitor
                ? capacitor.getLinkedSecurityStation() : null;
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.CONVERTER;
    }

    @Override
    public int getPowerLinkSlot() {
        return SLOT_POWER_LINK;
    }

    @Override
    public int getMinSwitchMode() {
        return 1;
    }

    @Override
    public int getMaxSwitchMode() {
        return 3;
    }

    /** Slot 0 only power link items; the hidden slots 1–3 take anything (only slot 1 is reachable by hoppers). */
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot != SLOT_POWER_LINK || stack.getItem() instanceof PowerLinkItem;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 1;
    }

    @Override
    protected void tickMachine() {
        linkPower = hasPowerSource() ? getAvailablePower() : 0;
        followRedstone();
        if (getSwitchValue() && hasPowerSource() && !isActive()) {
            setActive(true);
        }
        if ((!getSwitchValue() || !hasPowerSource()) && isActive()) {
            setActive(false);
        }
        if (isActive() && icOutput) {
            emitIcPower(packetSize, packetAmount);
        }
    }

    /** {@code EmitICpower}: per packet 3 · size FE must be there; only the delivered part costs FE. */
    private void emitIcPower(int amount, int packets) {
        if (!hasPowerSource()) {
            return;
        }
        for (int i = 0; i < packets; i++) {
            if (consumePower(MffsConfig.FE_PER_EU * amount, true)) {
                int left = EnergyNets.emit(level, this, amount);
                consumePower(MffsConfig.FE_PER_EU * (amount - left), false);
            }
        }
        setChanged();
    }

    @Override
    public void handleEvent(int key, String value) {
        if (key == EVENT_IC_OUTPUT) {
            icOutput = !icOutput;
        }
        if (!icOutput) {
            if (key == EVENT_PACKET_SIZE) {
                int index = indexOf(packetSize);
                if ("+".equals(value)) {
                    packetSize = PACKET_SIZES[(index + 1) % PACKET_SIZES.length];
                } else if ("-".equals(value)) {
                    packetSize = PACKET_SIZES[(index + PACKET_SIZES.length - 1) % PACKET_SIZES.length];
                }
            }
            if (key == EVENT_PACKET_AMOUNT) {
                if ("+".equals(value)) {
                    packetAmount = packetAmount == 9 ? 1 : packetAmount + 1;
                } else if ("-".equals(value)) {
                    packetAmount = packetAmount == 1 ? 9 : packetAmount - 1;
                }
            }
        }
        setChanged();
        super.handleEvent(key, value);
    }

    private static int indexOf(int size) {
        for (int i = 0; i < PACKET_SIZES.length; i++) {
            if (PACKET_SIZES[i] == size) {
                return i;
            }
        }
        return 0;
    }

    public boolean isIcOutput() {
        return icOutput;
    }

    public void setIcOutput(boolean icOutput) {
        this.icOutput = icOutput;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getPacketAmount() {
        return packetAmount;
    }

    public int getLinkPower() {
        return linkPower;
    }

    // --- EU source ---

    @Override
    public int getMaxEnergyOutput() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return receiver instanceof EnergySink;
    }

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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("ICOutputpacketamount", packetAmount);
        tag.putInt("ICOutputpacketsize", packetSize);
        tag.putBoolean("ICOutput", icOutput);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        packetAmount = Math.max(1, tag.getInt("ICOutputpacketamount"));
        packetSize = tag.contains("ICOutputpacketsize") ? tag.getInt("ICOutputpacketsize") : 1;
        icOutput = tag.getBoolean("ICOutput");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ConverterMenu(id, playerInventory, this);
    }
}
