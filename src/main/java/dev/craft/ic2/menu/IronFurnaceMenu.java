package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.IronFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Iron furnace (IC2 {@code ContainerIronFurnace}). */
public class IronFurnaceMenu extends MachineMenu<IronFurnaceBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int FUEL = 1;
    private static final int MAX_FUEL = 2;

    public IronFurnaceMenu(int id, Inventory inventory, IronFurnaceBlockEntity furnace) {
        this(id, inventory, furnace, furnace.getBlockPos(),
                new SyncedInts(furnace::getProgress, furnace::getFuel, furnace::getMaxFuel));
    }

    public IronFurnaceMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private IronFurnaceMenu(int id, Inventory inventory, @Nullable IronFurnaceBlockEntity furnace, BlockPos pos,
                            SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.IRON_FURNACE), id, furnace, pos, data);
        IItemHandler items = items(furnace, 3);
        addMachineSlot(new MachineSlot(items, 0, 56, 17));
        addMachineSlot(new MachineSlot(items, 1, 56, 53));
        addMachineSlot(new OutputSlot(items, 2, 116, 35, inventory.player, true));
        addPlayerInventory(inventory);
    }

    public int getFuel() {
        return data.getInt(FUEL);
    }

    /** IC2 {@code gaugeFuelScaled}: relative to the burn time of the last fuel item (160 if unknown). */
    public int gaugeFuelScaled(int scale) {
        int maxFuel = data.getInt(MAX_FUEL);
        if (maxFuel == 0) {
            maxFuel = getFuel();
            if (maxFuel == 0) {
                maxFuel = IronFurnaceBlockEntity.OPERATION_LENGTH;
            }
        }
        return getFuel() * scale / maxFuel;
    }

    /** IC2 {@code gaugeProgressScaled}. */
    public int gaugeProgressScaled(int scale) {
        return data.getInt(PROGRESS) * scale / IronFurnaceBlockEntity.OPERATION_LENGTH;
    }

    @Override
    protected int getInputSlot() {
        return 0;
    }
}
