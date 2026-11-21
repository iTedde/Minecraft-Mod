package dev.craft.compactsolars;

import dev.craft.ic2.menu.FilteredSlot;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.SlotFilters;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

/** GUI of a leftover solar array, laid out like the IC2 solar panel: its charge slot, the sun symbol stays off. */
public class SolarArrayMenu extends MachineMenu<SolarArrayBlockEntity> {
    private final SolarArrayType type;

    public SolarArrayMenu(int id, Inventory inventory, SolarArrayBlockEntity array) {
        this(id, inventory, array, array.getArrayType(), array.getBlockPos(),
                new SyncedInts(() -> 0));
    }

    public SolarArrayMenu(int id, Inventory inventory, BlockPos pos, SolarArrayType type) {
        this(id, inventory, null, type, pos, new SyncedInts(1));
    }

    private SolarArrayMenu(int id, Inventory inventory, @Nullable SolarArrayBlockEntity array, SolarArrayType type,
                           BlockPos pos, SyncedInts data) {
        super(CsMenus.SOLAR_ARRAY.get(), id, array, pos, data);
        this.type = type;
        addMachineSlot(new FilteredSlot(items(array, 1), 0, 80, 26, SlotFilters.charge(type.getTier())));
        addPlayerInventory(inventory);
    }

    public SolarArrayType getArrayType() {
        return type;
    }

    public boolean isSunVisible() {
        return data.getInt(0) != 0;
    }

    @Override
    protected int getInputSlot() {
        return 0;
    }
}
