package dev.craft.powerconverters.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.powerconverters.PcMenus;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity.SideData;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * {@code ContainerEnergyBridge}: only the player inventory; syncs six values per side, "input limited" and the
 * scaled charge. Shift-click does nothing and there is no distance check.
 */
public class EnergyBridgeMenu extends AbstractContainerMenu {
    private static final int VALUES_PER_SIDE = 6;
    private static final int INPUT_LIMITED = 6 * VALUES_PER_SIDE;
    private static final int ENERGY_SCALED = INPUT_LIMITED + 1;

    private final SyncedInts data;

    /** Client side. */
    public EnergyBridgeMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, new SyncedInts(ENERGY_SCALED + 1));
    }

    /** Server side. */
    public EnergyBridgeMenu(int id, Inventory inventory, EnergyBridgeBlockEntity bridge) {
        this(id, inventory, new SyncedInts(sources(bridge)));
    }

    private EnergyBridgeMenu(int id, Inventory inventory, SyncedInts data) {
        super(PcMenus.ENERGY_BRIDGE.get(), id);
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 113 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 171));
        }
        addDataSlots(data);
    }

    private static IntSupplier[] sources(EnergyBridgeBlockEntity bridge) {
        IntSupplier[] sources = new IntSupplier[ENERGY_SCALED + 1];
        for (Direction side : Direction.values()) {
            int base = side.get3DDataValue() * VALUES_PER_SIDE;
            sources[base] = () -> bridge.getDataForSide(side).voltageIndex();
            sources[base + 1] = () -> bridge.getDataForSide(side).consumer() ? 1 : 0;
            sources[base + 2] = () -> bridge.getDataForSide(side).producer() ? 1 : 0;
            sources[base + 3] = () -> bridge.getDataForSide(side).powerSystem();
            sources[base + 4] = () -> bridge.getDataForSide(side).connected() ? 1 : 0;
            sources[base + 5] = () -> bridge.getDataForSide(side).outputRate();
        }
        sources[INPUT_LIMITED] = () -> bridge.isInputLimited() ? 1 : 0;
        sources[ENERGY_SCALED] = bridge::getEnergyScaled;
        return sources;
    }

    public SideData getDataForSide(Direction side) {
        int base = side.get3DDataValue() * VALUES_PER_SIDE;
        return new SideData(data.getInt(base + 1) != 0, data.getInt(base + 2) != 0, data.getInt(base + 3),
                data.getInt(base + 4) != 0, data.getInt(base), data.getInt(base + 5));
    }

    public boolean isInputLimited() {
        return data.getInt(INPUT_LIMITED) != 0;
    }

    public int getEnergyScaled() {
        return data.getInt(ENERGY_SCALED);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
