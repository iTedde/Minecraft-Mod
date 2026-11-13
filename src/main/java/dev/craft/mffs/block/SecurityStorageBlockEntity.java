package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.item.SecurityLinkCardItem;
import dev.craft.mffs.menu.SecurityStorageMenu;
import dev.craft.mffs.security.SecurityRight;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/**
 * {@code TileEntitySecStorage} (spec 48 §7.3): 60 slots, 0 security link card, 1–54 storage (stack 64). Active while
 * a station is linked and the switch value is on (switch mode 2–3); automation reaches slots 1–54 only while inactive.
 * Opening needs right OSS.
 */
public class SecurityStorageBlockEntity extends MffsMachineBlockEntity {
    public static final int SLOT_SECURITY_LINK = 0;
    public static final int FIRST_STORAGE = 1;
    public static final int STORAGE_SLOTS = 54;

    private final IItemHandler automation = new RangedWrapper(inventory, FIRST_STORAGE,
            FIRST_STORAGE + STORAGE_SLOTS) {
        @Override
        public int getSlots() {
            return isActive() ? 0 : super.getSlots();
        }
    };

    public SecurityStorageBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.SECURITY_STORAGE.get(), pos, state, 60);
    }

    @Override
    public MffsDevices.Kind getDeviceKind() {
        return MffsDevices.Kind.SECURITY_STORAGE;
    }

    @Override
    public int getMinSwitchMode() {
        return 2;
    }

    @Override
    public int getMaxSwitchMode() {
        return 3;
    }

    @Override
    public SecurityStationBlockEntity getLinkedSecurityStation() {
        return SecurityLinkCardItem.getLinkedStation(this, SLOT_SECURITY_LINK);
    }

    /** Sided access ({@code getStartInventorySide}/{@code getSizeInventorySide}). */
    public IItemHandler getAutomationHandler() {
        return automation;
    }

    /** All storage slots regardless of the state, as the block breaker uses them ({@code addStacksToInventory}). */
    public IItemHandler getStorageHandler() {
        return new RangedWrapper(inventory, FIRST_STORAGE, inventory.getSlots());
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot != SLOT_SECURITY_LINK || stack.getItem() instanceof SecurityLinkCardItem;
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return slot == SLOT_SECURITY_LINK ? 1 : 64;
    }

    @Override
    public List<SecurityRight> getOpenRights() {
        return List.of(SecurityRight.OSS);
    }

    @Override
    protected boolean canBeTurned() {
        return false;
    }

    @Override
    protected void tickMachine() {
        boolean linked = getLinkedSecurityStation() != null;
        if (linked && !isActive() && getSwitchValue()) {
            setActive(true);
        }
        if ((!linked || !getSwitchValue()) && isActive()) {
            setActive(false);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SecurityStorageMenu(id, inventory, this);
    }
}
