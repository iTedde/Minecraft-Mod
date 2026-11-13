package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.MffsItems;
import dev.craft.mffs.item.AccessCardItem;
import dev.craft.mffs.item.AccessLicense;
import dev.craft.mffs.item.BlankCardItem;
import dev.craft.mffs.item.IdCardData;
import dev.craft.mffs.item.PersonalIdCardItem;
import dev.craft.mffs.item.multitool.DebuggerMultitoolItem;
import dev.craft.mffs.menu.SecurityStationMenu;
import dev.craft.mffs.security.SecurityRight;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileEntityAdvSecurityStation} (spec 48 §7.2): 40 slots of stack size 1. Slot 0 holds the master's ID card
 * (station active while one lies there, the master gets CSR), slot 1 is the editing slot (ID card: toggle rights;
 * blank card → access license 5 min; license: ±5 min), 2–33 storage (ID cards there count for the rights), 39 + 38
 * copy an ID card onto a blank card.
 */
public class SecurityStationBlockEntity extends MffsMachineBlockEntity {
    public static final int SLOT_MASTER = 0;
    public static final int SLOT_EDIT = 1;
    public static final int SLOT_COPY_TARGET = 38;
    public static final int SLOT_COPY_SOURCE = 39;
    public static final int EVENT_TOGGLE_RIGHT = 100;
    public static final int EVENT_VALIDITY_DOWN = 101;
    public static final int EVENT_VALIDITY_UP = 102;
    private static final int LICENSE_STEP = 5;

    private String mainUser = "";
    private UUID mainUserId;

    public SecurityStationBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.SECURITY_STATION.get(), pos, state, 40);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.SECURITY_STATION;
    }

    @Override
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return this;
    }

    public String getMainUser() {
        return mainUser;
    }

    private void setMainUser(String name, UUID id) {
        if (!mainUser.equals(name) || !java.util.Objects.equals(mainUserId, id)) {
            mainUser = name;
            mainUserId = id;
            markForUpdate();
        }
    }

    /** Only ID cards and blank cards; slot 38 only blank cards, slot 39 only ID cards (no licenses). */
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == SLOT_COPY_TARGET && !(stack.getItem() instanceof BlankCardItem)) {
            return false;
        }
        if (slot == SLOT_COPY_SOURCE && (stack.getItem() instanceof AccessCardItem
                || !(stack.getItem() instanceof PersonalIdCardItem))) {
            return false;
        }
        return stack.getItem() instanceof PersonalIdCardItem || stack.getItem() instanceof BlankCardItem;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return 1;
    }

    @Override
    public List<SecurityRight> getOpenRights() {
        return isActive() ? List.of(SecurityRight.CSR, SecurityRight.EB) : List.of(SecurityRight.EB);
    }

    @Override
    protected boolean canBeTurned() {
        return false;
    }

    @Override
    public boolean canBeRemovedByMultitool() {
        return !isActive();
    }

    @Override
    protected void tickMachine() {
        if (getTicker() == 10) {
            if (!mainUser.isEmpty()) {
                if (!isActive()) {
                    setActive(true);
                }
            } else if (isActive()) {
                setActive(false);
            }
            checkSlots();
            setTicker(0);
        }
        setTicker(getTicker() + 1);
    }

    /** {@code checkslots}: master from slot 0 (gets CSR), copy 39 → 38. */
    public void checkSlots() {
        ItemStack master = getStack(SLOT_MASTER);
        if (master.is(MffsItems.CARD_PERSONAL_ID.get())) {
            IdCardData data = PersonalIdCardItem.getData(master);
            setMainUser(data.owner(), data.ownerId().orElse(null));
            if (!data.hasRight(SecurityRight.CSR)) {
                PersonalIdCardItem.setRight(master, SecurityRight.CSR, true);
            }
        } else {
            setMainUser("", null);
        }
        ItemStack source = getStack(SLOT_COPY_SOURCE);
        if (!source.isEmpty() && getStack(SLOT_COPY_TARGET).getItem() instanceof BlankCardItem
                && source.getItem() instanceof PersonalIdCardItem) {
            setStack(SLOT_COPY_TARGET, source.copy());
        }
    }

    /**
     * {@code isAccessGranted}: everything while inactive; the master; else the first ID card of the player in slots
     * 35…1; else a valid access license of this station (or a debugger) in the player's inventory. Expired licenses
     * found on the way become blank cards.
     */
    public boolean isAccessGranted(Player player, SecurityRight right) {
        if (!isActive()) {
            return true;
        }
        if (mainUserId != null && mainUserId.equals(player.getUUID())) {
            return true;
        }
        return storedCardGrants(player, right) || playerInventoryGrants(player, right);
    }

    private boolean storedCardGrants(Player player, SecurityRight right) {
        for (int slot = 35; slot >= 1; slot--) {
            ItemStack stack = getStack(slot);
            if (stack.is(MffsItems.CARD_PERSONAL_ID.get())) {
                IdCardData data = PersonalIdCardItem.getData(stack);
                if (data.isOwner(player)) {
                    return data.hasRight(right);
                }
            }
        }
        return false;
    }

    private boolean playerInventoryGrants(Player player, SecurityRight right) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof AccessCardItem) {
                AccessLicense license = AccessCardItem.getLicense(stack);
                if (license.validity() > 0) {
                    if (license.stationId() == getDeviceId() && PersonalIdCardItem.hasRight(stack, right)) {
                        if (!license.areaName().equals(getDeviceName())) {
                            AccessCardItem.setLicense(stack, license.withAreaName(getDeviceName()));
                        }
                        return true;
                    }
                } else {
                    player.sendSystemMessage(Component.translatable("message.craft.mffs.license_expired"));
                    inventory.setItem(slot, new ItemStack(MffsItems.CARD_BLANK.get()));
                    continue;
                }
            }
            if (stack.getItem() instanceof DebuggerMultitoolItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEvent(int key, String value) {
        ItemStack edit = getStack(SLOT_EDIT);
        switch (key) {
            case EVENT_TOGGLE_RIGHT -> {
                SecurityRight right = SecurityRight.byKey(value);
                if (right != null && edit.getItem() instanceof PersonalIdCardItem) {
                    PersonalIdCardItem.setRight(edit, right, !PersonalIdCardItem.hasRight(edit, right));
                    setChanged();
                }
            }
            case EVENT_VALIDITY_DOWN -> {
                if (edit.getItem() instanceof AccessCardItem) {
                    if (AccessCardItem.getValidity(edit) <= LICENSE_STEP) {
                        setStack(SLOT_EDIT, new ItemStack(MffsItems.CARD_BLANK.get()));
                    } else {
                        AccessCardItem.setValidity(edit, AccessCardItem.getValidity(edit) - LICENSE_STEP);
                        setChanged();
                    }
                }
            }
            case EVENT_VALIDITY_UP -> {
                if (edit.getItem() instanceof BlankCardItem) {
                    ItemStack license = new ItemStack(MffsItems.CARD_ACCESS.get());
                    AccessCardItem.link(license, this, LICENSE_STEP);
                    setStack(SLOT_EDIT, license);
                } else if (edit.getItem() instanceof AccessCardItem) {
                    AccessCardItem.setValidity(edit, AccessCardItem.getValidity(edit) + LICENSE_STEP);
                    setChanged();
                }
            }
            default -> super.handleEvent(key, value);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SecurityStationMenu(id, inventory, this);
    }

    @Override
    protected void saveState(CompoundTag tag) {
        super.saveState(tag);
        tag.putString("MainUser", mainUser);
        if (mainUserId != null) {
            tag.putUUID("MainUserId", mainUserId);
        }
    }

    @Override
    protected void loadState(CompoundTag tag) {
        super.loadState(tag);
        mainUser = tag.getString("MainUser");
        mainUserId = tag.hasUUID("MainUserId") ? tag.getUUID("MainUserId") : null;
    }
}
