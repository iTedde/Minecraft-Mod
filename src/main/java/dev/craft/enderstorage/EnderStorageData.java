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
    public static final String LIQUID = "liquid";
    private static final String ID = "craft_enderstorage";

    private final Map<String, EnderItemStorage> items = new HashMap<>();
    private final Map<String, EnderLiquidStorage> liquids = new HashMap<>();
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
            CompoundTag saved = takePending(k);
            if (saved != null) {
                storage.load(saved, registries);
            }
            return storage;
        });
    }

    /** {@code getStorage(owner, freq, "liquid")}: created on first use. */
    public EnderLiquidStorage liquid(Frequency frequency) {
        String key = frequency.key(LIQUID);
        return liquids.computeIfAbsent(key, k -> {
            EnderLiquidStorage storage = new EnderLiquidStorage(this);
            CompoundTag saved = takePending(k);
            if (saved != null) {
                storage.readFromNBT(registries, saved.getCompound("tank"));
            }
            return storage;
        });
    }

    private CompoundTag takePending(String key) {
        if (!pending.contains(key) || registries == null) {
            return null;
        }
        CompoundTag saved = pending.getCompound(key);
        pending.remove(key);
        return saved;
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
        liquids.forEach((key, storage) -> {
            if (!storage.isEmpty()) {
                CompoundTag compound = new CompoundTag();
                compound.put("tank", storage.writeToNBT(registries, new CompoundTag()));
                tag.put(key, compound);
            }
        });
        return tag;
    }
}
