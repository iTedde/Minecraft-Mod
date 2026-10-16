package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * IC2 {@code ContainerNuclearReactor}: one slot per usable matrix field (width fixed when the GUI opens, centred),
 * player inventory at y 140. Output and hull heat are synced; the heat also reaches the client block entity.
 */
public class NuclearReactorMenu extends MachineMenu<NuclearReactorBlockEntity> {
    private static final int OUTPUT = 0;
    private static final int HEAT = 1;

    private final Level level;
    private final int size;

    public NuclearReactorMenu(int id, Inventory inventory, NuclearReactorBlockEntity reactor) {
        this(id, inventory, reactor, reactor.getBlockPos(), new SyncedInts(reactor::getOutput, reactor::getHeat));
    }

    public NuclearReactorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private NuclearReactorMenu(int id, Inventory inventory, @Nullable NuclearReactorBlockEntity reactor, BlockPos pos,
                               SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.NUCLEAR_REACTOR), id, reactor, pos, data);
        this.level = inventory.player.level();
        this.size = reactor != null ? reactor.getReactorSize()
                : level.getBlockEntity(pos) instanceof NuclearReactorBlockEntity be ? be.getReactorSize() : 3;
        IItemHandler items = items(reactor, NuclearReactorBlockEntity.COLUMNS * NuclearReactorBlockEntity.ROWS);
        int startX = 89 - 9 * size;
        for (int i = 0; i < NuclearReactorBlockEntity.COLUMNS * NuclearReactorBlockEntity.ROWS; i++) {
            int x = i % NuclearReactorBlockEntity.COLUMNS;
            int y = i / NuclearReactorBlockEntity.COLUMNS;
            if (x < size) {
                addMachineSlot(new FilteredSlot(items, i, startX + 18 * x, 18 + 18 * y,
                        NuclearReactorBlockEntity::isUsefulItem));
            }
        }
        addPlayerInventory(inventory, 140);
    }

    /** Matrix width when the GUI was opened (3–9); selects the background {@code GUI6by<size>}. */
    public int getSize() {
        return size;
    }

    public int getOutput() {
        return data.getInt(OUTPUT);
    }

    public int getHeat() {
        return data.getInt(HEAT);
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
        if (machine == null && level.getBlockEntity(pos) instanceof NuclearReactorBlockEntity reactor) {
            reactor.setClientHeat(getHeat());
        }
    }
}
