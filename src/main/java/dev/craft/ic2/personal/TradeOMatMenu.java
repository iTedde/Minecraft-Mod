package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.menu.DisplaySlot;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code ContainerTradeOMatOpen} (owner: all four slots, trade count) and {@code ContainerTradeOMatClosed}
 * (customer: patterns only shown, stock).
 */
public class TradeOMatMenu extends MachineMenu<TradeOMatBlockEntity> {
    private final boolean open;

    public TradeOMatMenu(int id, Inventory inventory, TradeOMatBlockEntity trader, boolean open) {
        this(id, inventory, trader, trader.getBlockPos(), open,
                new SyncedInts(open ? trader::getTotalTradeCount : trader::getStock));
    }

    public TradeOMatMenu(int id, Inventory inventory, BlockPos pos, boolean open) {
        this(id, inventory, null, pos, open, new SyncedInts(1));
    }

    private TradeOMatMenu(int id, Inventory inventory, @Nullable TradeOMatBlockEntity trader, BlockPos pos,
                          boolean open, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.TRADE_O_MAT), id, trader, pos, data);
        this.open = open;
        IItemHandler items = items(trader, 4);
        if (open) {
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_WANT, 24, 17));
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_OFFER, 24, 53));
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_PAYMENT, 80, 17));
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_GOODS, 80, 53));
        } else {
            addMachineSlot(new DisplaySlot(items, TradeOMatBlockEntity.SLOT_WANT, 50, 19));
            addMachineSlot(new DisplaySlot(items, TradeOMatBlockEntity.SLOT_OFFER, 50, 38));
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_PAYMENT, 143, 17));
            addMachineSlot(new MachineSlot(items, TradeOMatBlockEntity.SLOT_GOODS, 143, 53));
        }
        addPlayerInventory(inventory);
    }

    public boolean isOpen() {
        return open;
    }

    /** Trade count (owner) or stock (customer). */
    public int getValue() {
        return data.getInt(0);
    }

    /** IC2 {@code getInput} returns 2 (the payment slot). */
    @Override
    protected int getInputSlot() {
        return TradeOMatBlockEntity.SLOT_PAYMENT;
    }
}
