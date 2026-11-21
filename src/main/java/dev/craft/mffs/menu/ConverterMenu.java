package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerConverter}: power link (44, 28), player inventory from (50, 133). Synced: linked FE, fill %, packet
 * size and amount, IC2 output, power source ID.
 */
public class ConverterMenu extends MffsMachineMenu {
    public ConverterMenu(int id, Inventory inventory, ConverterBlockEntity converter) {
        this(id, inventory, converter.getBlockPos(), converter, converter.getInventory(),
                new SyncedInts(converter::getLinkPower, converter::getPercentageCapacity, converter::getPacketSize,
                        converter::getPacketAmount, () -> converter.isIcOutput() ? 1 : 0,
                        converter::getPowerSourceId), converter);
    }

    public ConverterMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(4), new SyncedInts(6),
                ExtractorMenu.rulesAt(inventory, pos));
    }

    private ConverterMenu(int id, Inventory inventory, BlockPos pos, ConverterBlockEntity converter,
                          IItemHandler items, SyncedInts data, MffsMachineBlockEntity rules) {
        super(MffsMenus.CONVERTER.get(), id, pos, converter, data);
        addMachineSlot(items, ConverterBlockEntity.SLOT_POWER_LINK, 44, 28, rules);
        addPlayerInventory(inventory, 50, 133);
    }

    public int getLinkPower() {
        return getInt(0);
    }

    public int getCapacity() {
        return getInt(1);
    }

    public int getPacketSize() {
        return getInt(2);
    }

    public int getPacketAmount() {
        return getInt(3);
    }

    public boolean isIcOutput() {
        return getInt(4) != 0;
    }

    public int getPowerSourceId() {
        return getInt(5);
    }
}
