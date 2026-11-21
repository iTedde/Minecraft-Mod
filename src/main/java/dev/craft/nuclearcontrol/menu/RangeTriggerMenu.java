package dev.craft.nuclearcontrol.menu;

import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.RangeTriggerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * {@code ContainerRangeTrigger}: card slot 8, 21, range upgrade 8, 39, player inventory from y 108. Limits and
 * inversion come from the synced block entity.
 */
public class RangeTriggerMenu extends NcMenu {
    private static final int OWN_SLOTS = 2;

    /** Server. */
    public RangeTriggerMenu(int id, Inventory inventory, RangeTriggerBlockEntity trigger) {
        this(id, inventory, trigger.getBlockPos(), trigger.getInventory());
    }

    /** Client: the slots check validity like the block entity. */
    public RangeTriggerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, new ItemStackHandler(OWN_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return RangeTriggerBlockEntity.isItemValid(slot, stack);
            }
        });
    }

    private RangeTriggerMenu(int id, Inventory inventory, BlockPos pos, IItemHandler items) {
        super(NcMenus.RANGE_TRIGGER.get(), id, pos);
        addSlot(new SlotItemHandler(items, RangeTriggerBlockEntity.SLOT_CARD, 8, 21));
        addSlot(new SlotItemHandler(items, RangeTriggerBlockEntity.SLOT_UPGRADE, 8, 39));
        addPlayerInventory(inventory, 108);
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof RangeTriggerBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveOwn(OWN_SLOTS, index);
    }
}
