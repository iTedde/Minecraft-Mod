package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.item.DataLinkCardItem;
import dev.craft.mffs.item.SecurityLinkCardItem;
import dev.craft.mffs.menu.ControlSystemMenu;
import dev.craft.mffs.security.SecurityRight;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityControlSystem} (spec 48 §7.4): 40 slots of stack size 1 – 0 security link, 1 reader (data link
 * card), 4–31 storage for data link cards. Active while a station is linked. Every 20 ticks the reader's target is
 * looked up over kind and ID and its state shown; buttons switch the target's switch mode and value and open its
 * GUI (target within 61 blocks).
 */
public class ControlSystemBlockEntity extends MffsMachineBlockEntity {
    public static final int SLOT_SECURITY_LINK = 0;
    public static final int SLOT_READER = 1;
    public static final int EVENT_REMOTE_MODE = 101;
    public static final int EVENT_REMOTE_VALUE = 102;
    public static final int EVENT_REMOTE_GUI = 103;
    private static final double GUI_RANGE = 61.0;

    private MffsMachineBlockEntity remote;
    private String remoteName = "";
    private String remoteKind = "";
    private boolean remoteActive;
    private boolean remoteSwitchValue;
    private int remoteSwitchMode;
    private boolean remoteStationLink;
    private boolean remoteHasPowerSource;
    private boolean remoteGuiInRange;
    private int remotePowerLeft;

    public ControlSystemBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.CONTROL_SYSTEM.get(), pos, state, 40);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.CONTROL_SYSTEM;
    }

    @Override
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return SecurityLinkCardItem.getLinkedStation(this, SLOT_SECURITY_LINK);
    }

    /** Slot 0 takes a security link card or (original fall-through) a data link card; the rest data link cards. */
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == SLOT_SECURITY_LINK && stack.getItem() instanceof SecurityLinkCardItem) {
            return true;
        }
        return stack.getItem() instanceof DataLinkCardItem;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 1;
    }

    @Override
    public List<SecurityRight> getOpenRights() {
        return List.of(SecurityRight.UCS, SecurityRight.EB);
    }

    @Override
    protected void tickMachine() {
        if (getTicker() == 20) {
            boolean linked = getLinkedSecurityStation() != null;
            if (linked && !isActive()) {
                setActive(true);
            }
            if (!linked && isActive()) {
                setActive(false);
            }
            refreshRemoteData();
            setTicker(0);
        }
        setTicker(getTicker() + 1);
    }

    public MffsMachineBlockEntity getRemote() {
        return remote;
    }

    /** {@code getTargetMaschine}: the reader's target; a card whose target is gone becomes a blank card. */
    private MffsMachineBlockEntity findTarget() {
        ItemStack card = getStack(SLOT_READER);
        if (!(card.getItem() instanceof DataLinkCardItem)) {
            return null;
        }
        MffsMachineBlockEntity device = DataLinkCardItem.getTarget(card, level);
        if (device != null) {
            return device;
        }
        setStack(SLOT_READER, new ItemStack(MffsItems.CARD_BLANK.get()));
        return null;
    }

    private void refreshRemoteData() {
        remote = findTarget();
        boolean changed;
        if (remote != null) {
            double distance = Math.sqrt(worldPosition.distSqr(remote.getBlockPos()));
            changed = update(remote.getDeviceName(), remote.getDeviceKind().translationKey(), remote.isActive(),
                    remote.getSwitchValue(), remote.getSwitchMode(), remote.getLinkedSecurityStation() != null,
                    remote.hasPowerSource(), remote.getPercentageCapacity(), distance <= GUI_RANGE);
        } else {
            // The original only resets name, kind, active and switch mode.
            changed = update("-", "-", false, remoteSwitchValue, 0, remoteStationLink, remoteHasPowerSource,
                    remotePowerLeft, remoteGuiInRange);
        }
        if (changed) {
            markForUpdate();
        }
    }

    private boolean update(String name, String kind, boolean active, boolean value, int mode, boolean station,
                           boolean power, int percent, boolean inRange) {
        boolean changed = !name.equals(remoteName) || !kind.equals(remoteKind) || active != remoteActive
                || value != remoteSwitchValue || mode != remoteSwitchMode || station != remoteStationLink
                || power != remoteHasPowerSource || percent != remotePowerLeft || inRange != remoteGuiInRange;
        remoteName = name;
        remoteKind = kind;
        remoteActive = active;
        remoteSwitchValue = value;
        remoteSwitchMode = mode;
        remoteStationLink = station;
        remoteHasPowerSource = power;
        remotePowerLeft = percent;
        remoteGuiInRange = inRange;
        return changed;
    }

    @Override
    public void handleEvent(int key, String value, ServerPlayer player) {
        if (key == EVENT_REMOTE_GUI && remote != null && remoteGuiInRange && !remote.isRemoved()) {
            BlockPos pos = remote.getBlockPos();
            player.openMenu(remote, buf -> buf.writeBlockPos(pos));
            return;
        }
        handleEvent(key, value);
    }

    @Override
    public void handleEvent(int key, String value) {
        if (key == EVENT_REMOTE_VALUE && remote != null) {
            remote.toggleSwitchValue();
        } else if (key == EVENT_REMOTE_MODE && remote != null) {
            remote.toggleSwitchMode();
        } else {
            super.handleEvent(key, value);
        }
    }

    public String getRemoteName() {
        return remoteName;
    }

    public String getRemoteKind() {
        return remoteKind;
    }

    public boolean isRemoteActive() {
        return remoteActive;
    }

    public boolean getRemoteSwitchValue() {
        return remoteSwitchValue;
    }

    public int getRemoteSwitchMode() {
        return remoteSwitchMode;
    }

    public boolean hasRemoteStationLink() {
        return remoteStationLink;
    }

    public boolean remoteHasPowerSource() {
        return remoteHasPowerSource;
    }

    public int getRemotePowerLeft() {
        return remotePowerLeft;
    }

    public boolean isRemoteGuiInRange() {
        return remoteGuiInRange;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ControlSystemMenu(id, inventory, this);
    }

    /** The remote data goes to the client like the original's networked fields (and is saved with it). */
    @Override
    protected void saveState(CompoundTag tag) {
        super.saveState(tag);
        tag.putString("RemoteDeviceName", remoteName);
        tag.putString("RemoteDeviceTyp", remoteKind);
        tag.putBoolean("RemoteActive", remoteActive);
        tag.putBoolean("RemoteSwitchValue", remoteSwitchValue);
        tag.putInt("RemoteSwitchModi", remoteSwitchMode);
        tag.putBoolean("RemoteSecurityStationlink", remoteStationLink);
        tag.putBoolean("RemotehasPowersource", remoteHasPowerSource);
        tag.putInt("RemotePowerleft", remotePowerLeft);
        tag.putBoolean("RemoteGUIinRange", remoteGuiInRange);
    }

    @Override
    protected void loadState(CompoundTag tag) {
        super.loadState(tag);
        if (tag.contains("RemoteDeviceName")) {
            remoteName = tag.getString("RemoteDeviceName");
            remoteKind = tag.getString("RemoteDeviceTyp");
            remoteActive = tag.getBoolean("RemoteActive");
            remoteSwitchValue = tag.getBoolean("RemoteSwitchValue");
            remoteSwitchMode = tag.getInt("RemoteSwitchModi");
            remoteStationLink = tag.getBoolean("RemoteSecurityStationlink");
            remoteHasPowerSource = tag.getBoolean("RemotehasPowersource");
            remotePowerLeft = tag.getInt("RemotePowerleft");
            remoteGuiInRange = tag.getBoolean("RemoteGUIinRange");
        }
    }
}
