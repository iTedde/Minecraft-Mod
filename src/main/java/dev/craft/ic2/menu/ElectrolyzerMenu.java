package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.ElectrolyzerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerElectrolyzer}: water cell and electrolyzed water cell slot, energy gauge. */
public class ElectrolyzerMenu extends MachineMenu<ElectrolyzerBlockEntity> {
    private static final int GAUGE = 0;

    public ElectrolyzerMenu(int id, Inventory inventory, ElectrolyzerBlockEntity electrolyzer) {
        this(id, inventory, electrolyzer, electrolyzer.getBlockPos(),
                new SyncedInts(() -> electrolyzer.gaugeEnergyScaled(24), electrolyzer::getEnergy));
    }

    public ElectrolyzerMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private ElectrolyzerMenu(int id, Inventory inventory, @Nullable ElectrolyzerBlockEntity electrolyzer, BlockPos pos,
                             SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.ELECTROLYZER), id, electrolyzer, pos, data);
        IItemHandler items = items(electrolyzer, 2);
        addMachineSlot(new FilteredSlot(items, ElectrolyzerBlockEntity.SLOT_WATER, 53, 35,
                stack -> stack.is(Ic2Items.WATER_CELL.get())));
        addMachineSlot(new FilteredSlot(items, ElectrolyzerBlockEntity.SLOT_ELECTROLYZED, 112, 35,
                stack -> stack.is(Ic2Items.ELECTROLYZED_WATER_CELL.get())));
        addPlayerInventory(inventory);
    }

    /** Gauge width 0–24; drawn only while the buffer holds energy. */
    public int getGauge() {
        return data.getInt(GAUGE);
    }

    public boolean hasEnergy() {
        return data.getInt(1) > 0;
    }

    @Override
    protected int getInputSlot() {
        return ElectrolyzerBlockEntity.SLOT_WATER;
    }
}
