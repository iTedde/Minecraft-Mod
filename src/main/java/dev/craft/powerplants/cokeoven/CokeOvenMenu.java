package dev.craft.powerplants.cokeoven;

import dev.craft.ic2.menu.FilteredSlot;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.OutputSlot;
import dev.craft.ic2.menu.SyncedInts;
import dev.craft.powerplants.PpMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Coke oven GUI (Railcraft coke oven layout without the creosote tank): coal in, coke out, progress. */
public class CokeOvenMenu extends MachineMenu<CokeOvenBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int ACTIVE = 1;
    private static final int FORMED = 2;

    public CokeOvenMenu(int id, Inventory inventory, CokeOvenBlockEntity oven) {
        this(id, inventory, oven, oven.getBlockPos(), new SyncedInts(oven::getProgress,
                () -> oven.isActive() ? 1 : 0, () -> oven.isFormed() ? 1 : 0));
    }

    public CokeOvenMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(3));
    }

    private CokeOvenMenu(int id, Inventory inventory, @Nullable CokeOvenBlockEntity oven, BlockPos pos,
                         SyncedInts data) {
        super(PpMenus.COKE_OVEN.get(), id, oven, pos, data);
        IItemHandler items = items(oven, 2);
        addMachineSlot(new FilteredSlot(items, CokeOvenBlockEntity.SLOT_INPUT, 16, 43, CokeOvenBlockEntity::isInput));
        addMachineSlot(new OutputSlot(items, CokeOvenBlockEntity.SLOT_OUTPUT, 62, 43, inventory.player, false));
        addPlayerInventory(inventory);
    }

    public int gaugeProgressScaled(int scale) {
        return data.getInt(PROGRESS) * scale / CokeOvenBlockEntity.OPERATION_LENGTH;
    }

    public boolean isBurning() {
        return data.getInt(ACTIVE) != 0;
    }

    public boolean isFormed() {
        return data.getInt(FORMED) != 0;
    }

    @Override
    protected int getInputSlot() {
        return CokeOvenBlockEntity.SLOT_INPUT;
    }
}
