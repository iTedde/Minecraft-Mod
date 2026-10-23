package dev.craft.enderstorage;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * EnderStorage {@code EnderStorageManager} (spec 27 §1): all ender storages of the world, keyed
 * {@code "<freq>|<owner>|<type>"}. Kept as world data of the overworld instead of {@code <world>/EnderStorage/}.
 */
public class EnderStorageData extends SavedData {
    public static final String ITEM = "item";
    private static final String ID = "craft_enderstorage";

    private final Map<String, EnderItemStorage> items = new HashMap<>();
    /** Saved storages not asked for yet; loaded on first access, since the registries come with the tag. */
    private final CompoundTag pending = new CompoundTag();
    private HolderLookup.Provider registries;

    public static EnderStorageData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(EnderStorageData::new, EnderStorageData::load, null), ID);
    }

    /** {@code getStorage(owner, freq, "item")}: created on first use. */
    public EnderItemStorage item(Frequency frequency) {
        String key = frequency.key(ITEM);
        return items.computeIfAbsent(key, k -> {
            EnderItemStorage storage = new EnderItemStorage(this);
            if (pending.contains(k) && registries != null) {
                storage.load(pending.getCompound(k), registries);
                pending.remove(k);
            }
            return storage;
        });
    }

    private static EnderStorageData load(CompoundTag tag, HolderLookup.Provider registries) {
        EnderStorageData data = new EnderStorageData();
        data.registries = registries;
        for (String key : tag.getAllKeys()) {
            data.pending.put(key, tag.getCompound(key).copy());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (String key : pending.getAllKeys()) {
            tag.put(key, pending.getCompound(key));
        }
        items.forEach((key, storage) -> {
            if (!storage.isEmpty()) {
                tag.put(key, storage.save(registries));
            }
        });
        return tag;
    }
}
