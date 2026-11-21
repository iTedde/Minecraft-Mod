package dev.craft.mffs.menu;

import dev.craft.ic2.menu.SyncedInts;
import dev.craft.mffs.MffsMenus;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * {@code ContainerProjector}: slots at the original positions (power link 11/61, module 11/38, upgrades 120–156/82,
 * strength 155/64, distance 119/64, focus up/down/right/left 137/28, 137/62, 154/45, 120/45, camouflage 137/45,
 * security link 92/38), player inventory from (8, 104). Synced: link FE, fill %, access type, source present, module
 * type, camouflage.
 */
public class ProjectorMenu extends MffsMachineMenu {
    public ProjectorMenu(int id, Inventory inventory, ProjectorBlockEntity projector) {
        this(id, inventory, projector.getBlockPos(), projector, projector.getInventory(),
                new SyncedInts(projector::getLinkPower, projector::getPercentageCapacity, projector::getAccessType,
                        () -> projector.hasPowerSource() ? 1 : 0, () -> moduleType(projector),
                        () -> projector.hasUpgrade(ProjectorUpgrade.CAMOUFLAGE) ? 1 : 0), projector);
    }

    public ProjectorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, null, new ItemStackHandler(13), new SyncedInts(6),
                ExtractorMenu.rulesAt(inventory, pos));
    }

    private ProjectorMenu(int id, Inventory inventory, BlockPos pos, ProjectorBlockEntity projector,
                          IItemHandler items, SyncedInts data, MffsMachineBlockEntity rules) {
        super(MffsMenus.PROJECTOR.get(), id, pos, projector, data);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_POWER_LINK, 11, 61, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_MODULE, 11, 38, rules);
        addMachineSlot(items, 2, 120, 82, rules);
        addMachineSlot(items, 3, 138, 82, rules);
        addMachineSlot(items, 4, 156, 82, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_STRENGTH, 155, 64, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_DISTANCE, 119, 64, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_FOCUS_UP, 137, 28, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_FOCUS_DOWN, 137, 62, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_FOCUS_RIGHT, 154, 45, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_FOCUS_LEFT, 120, 45, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_CAMOUFLAGE, 137, 45, rules);
        addMachineSlot(items, ProjectorBlockEntity.SLOT_SECURITY_LINK, 92, 38, rules);
        addPlayerInventory(inventory, 8, 104);
    }

    private static int moduleType(ProjectorBlockEntity projector) {
        ProjectorModule module = projector.getModule();
        return module != null ? module.getTypeId() : 0;
    }

    public int getLinkPower() {
        return getInt(0);
    }

    public int getCapacity() {
        return getInt(1);
    }

    public int getAccessType() {
        return getInt(2);
    }

    public boolean hasPowerSource() {
        return getInt(3) != 0;
    }

    public int getModuleType() {
        return getInt(4);
    }

    public boolean hasCamouflage() {
        return getInt(5) != 0;
    }
}
