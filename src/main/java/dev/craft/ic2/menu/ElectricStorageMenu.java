package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * BatBox, MFE and MFSU (IC2 {@code ContainerElectricBlock}): charge and discharge slot. Button 0 cycles the
 * redstone mode (sent with the vanilla container button packet).
 */
public class ElectricStorageMenu extends MachineMenu<ElectricStorageBlockEntity> {
    public static final int BUTTON_REDSTONE_MODE = 0;
    private static final int ENERGY = 0;
    private static final int MAX_STORAGE = 1;
    private static final int OUTPUT = 2;

    private final Ic2Machine kind;

    public ElectricStorageMenu(int id, Inventory inventory, ElectricStorageBlockEntity storage) {
        this(storage.getMachine(), id, inventory, storage, storage.getBlockPos(), storage.getTier(), new SyncedInts(
                storage::getEnergy, storage::getMaxStorage, storage::getOutput));
    }

    public ElectricStorageMenu(Ic2Machine kind, int id, Inventory inventory, BlockPos pos) {
        this(kind, id, inventory, null, pos, ElectricStorageBlockEntity.Spec.of(kind).tier(), new SyncedInts(3));
    }

    private ElectricStorageMenu(Ic2Machine kind, int id, Inventory inventory,
                                @Nullable ElectricStorageBlockEntity storage, BlockPos pos, int tier, SyncedInts data) {
        super(Ic2Menus.get(kind), id, storage, pos, data);
        this.kind = kind;
        IItemHandler items = items(storage, 2);
        addMachineSlot(new FilteredSlot(items, 0, 56, 17, SlotFilters.charge(tier)));
        addMachineSlot(new FilteredSlot(items, 1, 56, 53, SlotFilters.discharge(tier)));
        addPlayerInventory(inventory);
    }

    public Ic2Machine getKind() {
        return kind;
    }

    public int getEnergy() {
        return data.getInt(ENERGY);
    }

    public int getMaxStorage() {
        return data.getInt(MAX_STORAGE);
    }

    public int getOutput() {
        return data.getInt(OUTPUT);
    }

    /** IC2 {@code getChargeLevel}: energy relative to the capacity, capped at 1. */
    public float getChargeLevel() {
        int max = getMaxStorage();
        return max <= 0 ? 0.0F : Math.min(1.0F, (float) getEnergy() / max);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_REDSTONE_MODE && machine != null) {
            machine.cycleRedstoneMode(player);
            return true;
        }
        return false;
    }

    @Override
    protected int getInputSlot() {
        return 0;
    }
}
