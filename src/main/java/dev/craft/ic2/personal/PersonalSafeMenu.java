package dev.craft.ic2.personal;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.menu.MachineMenu;
import dev.craft.ic2.menu.MachineSlot;
import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerPersonalChest}: 6 × 9 slots, player inventory at y 140; opening moves the door. */
public class PersonalSafeMenu extends MachineMenu<PersonalSafeBlockEntity> {
    public PersonalSafeMenu(int id, Inventory inventory, PersonalSafeBlockEntity safe) {
        this(id, inventory, safe, safe.getBlockPos());
        safe.startOpen();
    }

    public PersonalSafeMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos);
    }

    private PersonalSafeMenu(int id, Inventory inventory, @Nullable PersonalSafeBlockEntity safe, BlockPos pos) {
        super(Ic2Menus.get(Ic2Machine.PERSONAL_SAFE), id, safe, pos, new SyncedInts(0));
        IItemHandler items = items(safe, PersonalSafeBlockEntity.SLOTS);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 9; x++) {
                addMachineSlot(new MachineSlot(items, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }
        addPlayerInventory(inventory, 140);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (machine != null) {
            machine.stopOpen();
        }
    }
}
