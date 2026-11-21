package dev.craft.chargepads.menu;

import dev.craft.chargepads.CpMenus;
import dev.craft.chargepads.PadTier;
import dev.craft.chargepads.block.ChargePadBlock;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * {@code ContainerChargePad} (spec 47 §9): battery (8, 58), projector (134, 58) and field (134, 29) from the
 * crystalizor on, upgrades (71, 39), (61, 58), (81, 58) as far as the tier has them; player inventory from y 84.
 * Synced: storage, capacity and maximum input.
 */
public class ChargePadMenu extends AbstractContainerMenu {
    private static final int[][] UPGRADE_POSITIONS = {{71, 39}, {61, 58}, {81, 58}};

    private final BlockPos pos;
    private final PadTier tier;
    private final int padSlots;
    private final SyncedInts data;
    private final ChargePadBlockEntity pad;

    public ChargePadMenu(int id, Inventory inventory, ChargePadBlockEntity pad) {
        this(id, inventory, pad.getBlockPos(), pad.getTier(), pad.getInventory(),
                new SyncedInts(pad::getStorage, pad::getCapacity, pad::getMaxInput), pad);
    }

    /** Client: the tier comes from the block at {@code pos}. */
    public ChargePadMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, tierAt(inventory.player, pos),
                new ItemStackHandler(ChargePadBlockEntity.INVENTORY_SIZE), new SyncedInts(3), null);
    }

    private ChargePadMenu(int id, Inventory inventory, BlockPos pos, PadTier tier, IItemHandler items,
                          SyncedInts data, ChargePadBlockEntity pad) {
        super(CpMenus.CHARGE_PAD.get(), id);
        this.pos = pos;
        this.tier = tier;
        this.data = data;
        this.pad = pad;
        addSlot(new PadSlot(items, PadTier.BATTERY_SLOT, 8, 58, tier));
        if (tier.hasModuleSlots()) {
            addSlot(new PadSlot(items, PadTier.PROJECTOR_SLOT, 134, 58, tier));
            addSlot(new PadSlot(items, PadTier.FIELD_SLOT, 134, 29, tier));
        }
        for (int i = 0; i < tier.getUpgradeSlots(); i++) {
            addSlot(new PadSlot(items, tier.getFirstUpgradeSlot() + i, UPGRADE_POSITIONS[i][0],
                    UPGRADE_POSITIONS[i][1], tier));
        }
        padSlots = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    private static PadTier tierAt(Player player, BlockPos pos) {
        return player.level().getBlockState(pos).getBlock() instanceof ChargePadBlock block
                ? block.getTier() : PadTier.STATIC;
    }

    public BlockPos getPos() {
        return pos;
    }

    public PadTier getTier() {
        return tier;
    }

    public int getStorage() {
        return data.getInt(0);
    }

    public int getCapacity() {
        return data.getInt(1);
    }

    public int getMaxInput() {
        return data.getInt(2);
    }

    /**
     * {@code transferStackInSlot}: from the pad into the player inventory (from the end); from the player first
     * into the last fitting pad slot, otherwise main inventory ↔ hotbar (the main inventory target leaves out its
     * last slot, as in the original).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        int count = stack.getCount();
        int hotbar = padSlots + 27;
        if (index < padSlots) {
            moveItemStackTo(stack, padSlots, slots.size(), true);
        } else {
            boolean intoPad = false;
            for (int target = padSlots - 1; target >= 0 && !intoPad; target--) {
                intoPad = slots.get(target).mayPlace(stack) && moveItemStackTo(stack, target, target + 1, false);
            }
            if (!intoPad) {
                if (index < hotbar) {
                    moveItemStackTo(stack, hotbar, hotbar + 9, false);
                } else {
                    moveItemStackTo(stack, padSlots, hotbar - 1, false);
                }
            }
        }
        if (stack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY);
        } else {
            source.setChanged();
        }
        if (stack.getCount() != count) {
            source.onTake(player, stack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return pad == null || pad.stillValid(player);
    }

    /** Pad slot with the pad's rules: modules stack to 1, everything else to 16. */
    private static final class PadSlot extends SlotItemHandler {
        private final PadTier tier;

        PadSlot(IItemHandler handler, int index, int x, int y, PadTier tier) {
            super(handler, index, x, y);
            this.tier = tier;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ChargePadBlockEntity.isValidForSlot(tier, getSlotIndex(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return ChargePadBlockEntity.slotLimit(tier, getSlotIndex());
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return Math.min(getMaxStackSize(), stack.getMaxStackSize());
        }
    }
}
