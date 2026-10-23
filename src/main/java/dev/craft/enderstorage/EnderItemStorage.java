package dev.craft.enderstorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * EnderStorage {@code EnderItemStorage} (spec 27 §3): 27 slots (pack config {@code storage-size=1}) shared by all chests
 * and pouches of one frequency, plus the number of players that have it open (lids of all those chests open).
 */
public class EnderItemStorage extends SimpleContainer {
    public static final int SIZE = 27;

    private final EnderStorageData data;
    private int open;

    public EnderItemStorage(EnderStorageData data) {
        super(SIZE);
        this.data = data;
    }

    public int getOpenCount() {
        return open;
    }

    @Override
    public void startOpen(Player player) {
        open++;
    }

    @Override
    public void stopOpen(Player player) {
        open = Math.max(0, open - 1);
    }

    /** {@code isUseableByPlayer}: always, also after the chest was broken. */
    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        data.setDirty();
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) {
            items.set(i, getItem(i));
        }
        return ContainerHelper.saveAllItems(new CompoundTag(), items, registries);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        for (int i = 0; i < SIZE; i++) {
            getItems().set(i, items.get(i));
        }
    }
}
