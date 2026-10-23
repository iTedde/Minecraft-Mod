package dev.craft.enderstorage;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/**
 * EnderStorage {@code ContainerEnderItemStorage} at the pack size 3 × 9: the vanilla chest layout, plus the owner's
 * name for the screen (empty for global storages).
 */
public class EnderStorageMenu extends ChestMenu {
    private final String ownerName;

    /** Client side. */
    public EnderStorageMenu(int id, Inventory inventory, String ownerName) {
        this(id, inventory, new SimpleContainer(EnderItemStorage.SIZE), ownerName);
    }

    public EnderStorageMenu(int id, Inventory inventory, Container storage, String ownerName) {
        super(EsMenus.ENDER_STORAGE.get(), id, inventory, storage, 3);
        this.ownerName = ownerName;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
