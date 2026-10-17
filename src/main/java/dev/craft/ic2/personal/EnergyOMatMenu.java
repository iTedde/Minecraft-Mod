package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.menu.DisplaySlot;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code ContainerEnergyOMatOpen} (owner: price, upgrade and payment slot, offer buttons) and
 * {@code ContainerEnergyOMatClosed} (customer: price shown, payment slot, paid-for EU). Buttons go through the
 * vanilla container button packet.
 */
public class EnergyOMatMenu extends MachineMenu<EnergyOMatBlockEntity> {
    private static final int OFFER = 0;
    private static final int PAID_FOR = 1;
    private static final int BUFFER = 2;

    private final boolean open;

    public EnergyOMatMenu(int id, Inventory inventory, EnergyOMatBlockEntity seller, boolean open) {
        this(id, inventory, seller, seller.getBlockPos(), open,
                new SyncedInts(seller::getEuOffer, seller::getPaidFor, seller::getEuBuffer));
    }

    public EnergyOMatMenu(int id, Inventory inventory, BlockPos pos, boolean open) {
        this(id, inventory, null, pos, open, new SyncedInts(3));
    }

    private EnergyOMatMenu(int id, Inventory inventory, @Nullable EnergyOMatBlockEntity seller, BlockPos pos,
                           boolean open, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.ENERGY_O_MAT), id, seller, pos, data);
        this.open = open;
        IItemHandler items = items(seller, 3);
        if (open) {
            addMachineSlot(new MachineSlot(items, EnergyOMatBlockEntity.SLOT_PRICE, 24, 17));
            addMachineSlot(new MachineSlot(items, EnergyOMatBlockEntity.SLOT_UPGRADE, 24, 53));
            addMachineSlot(new MachineSlot(items, EnergyOMatBlockEntity.SLOT_PAYMENT, 80, 17));
        } else {
            addMachineSlot(new DisplaySlot(items, EnergyOMatBlockEntity.SLOT_PRICE, 50, 19));
            addMachineSlot(new MachineSlot(items, EnergyOMatBlockEntity.SLOT_PAYMENT, 143, 17));
        }
        addPlayerInventory(inventory);
    }

    public boolean isOpen() {
        return open;
    }

    public int getEuOffer() {
        return data.getInt(OFFER);
    }

    public int getPaidFor() {
        return data.getInt(PAID_FOR);
    }

    public int getEuBuffer() {
        return data.getInt(BUFFER);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (open && machine != null) {
            machine.pressButton(player, id);
            return true;
        }
        return false;
    }

    /** IC2 {@code getInput} returns 1: the upgrade slot when open (original quirk), the payment slot when closed. */
    @Override
    protected int getInputSlot() {
        return 1;
    }
}
