package dev.craft.ic2.menu;

import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Menus;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** IC2 {@code ContainerMatter}: amplifier slot, UU matter output, progress and scrap value. */
public class MassFabricatorMenu extends MachineMenu<MassFabricatorBlockEntity> {
    private static final int PROGRESS = 0;
    private static final int SCRAP = 1;

    public MassFabricatorMenu(int id, Inventory inventory, MassFabricatorBlockEntity fabricator) {
        this(id, inventory, fabricator, fabricator.getBlockPos(),
                new SyncedInts(fabricator::getProgress, fabricator::getScrap));
    }

    public MassFabricatorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, null, pos, new SyncedInts(2));
    }

    private MassFabricatorMenu(int id, Inventory inventory, @Nullable MassFabricatorBlockEntity fabricator,
                               BlockPos pos, SyncedInts data) {
        super(Ic2Menus.get(Ic2Machine.MASS_FABRICATOR), id, fabricator, pos, data);
        IItemHandler items = items(fabricator, 2);
        addMachineSlot(new FilteredSlot(items, MassFabricatorBlockEntity.SLOT_AMPLIFIER, 114, 54,
                stack -> MassFabricatorBlockEntity.getAmplifierValue(stack) > 0));
        addMachineSlot(new OutputSlot(items, MassFabricatorBlockEntity.SLOT_OUTPUT, 114, 18, inventory.player, false));
        addPlayerInventory(inventory);
    }

    /** Progress in percent (energy / 10 000, at most 100). */
    public int getProgress() {
        return data.getInt(PROGRESS);
    }

    public int getScrap() {
        return data.getInt(SCRAP);
    }

    @Override
    protected int getInputSlot() {
        return MassFabricatorBlockEntity.SLOT_AMPLIFIER;
    }
}
