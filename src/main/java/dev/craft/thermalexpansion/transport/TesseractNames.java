package dev.craft.thermalexpansion.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * {@code TeleportRegistry.teleportNames} ({@code <world>/cofh/tesseracts.cfg}): per category
 * {@code <owner|_public_>.<type>} the saved frequency names, sorted by the frequency text like the Forge config
 * category. Kept as world data of the overworld.
 */
public class TesseractNames extends SavedData {
    private static final String ID = "craft_tesseracts";
    private static final Factory<TesseractNames> FACTORY = new Factory<>(TesseractNames::new, TesseractNames::load,
            null);

    private final Map<String, SortedMap<String, String>> categories = new HashMap<>();

    public static TesseractNames get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
    }

    /** Frequency text → name of a category; empty if there is none. */
    public SortedMap<String, String> get(String category) {
        SortedMap<String, String> names = categories.get(category);
        return names != null ? names : new TreeMap<>();
    }

    /** {@code TeleportPacket.ENTRY}: save {@code name} for {@code frequency}, or delete the frequency. */
    public void set(String category, int frequency, String name, boolean remove) {
        SortedMap<String, String> names = categories.computeIfAbsent(category, c -> new TreeMap<>());
        if (remove) {
            names.remove(String.valueOf(frequency));
        } else {
            names.put(String.valueOf(frequency), name);
        }
        setDirty();
    }

    private static TesseractNames load(CompoundTag tag, HolderLookup.Provider registries) {
        TesseractNames names = new TesseractNames();
        for (String category : tag.getAllKeys()) {
            CompoundTag entries = tag.getCompound(category);
            SortedMap<String, String> map = new TreeMap<>();
            for (String frequency : entries.getAllKeys()) {
                map.put(frequency, entries.getString(frequency));
            }
            names.categories.put(category, map);
        }
        return names;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        categories.forEach((category, map) -> {
            CompoundTag entries = new CompoundTag();
            map.forEach(entries::putString);
            tag.put(category, entries);
        });
        return tag;
    }
}
