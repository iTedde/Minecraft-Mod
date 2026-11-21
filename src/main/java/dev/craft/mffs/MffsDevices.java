package dev.craft.mffs;

import dev.craft.mffs.block.MffsMachineBlockEntity;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;

/**
 * {@code Linkgrid} (spec 48 §5): all MFFS machines of a level by kind and device ID. Cards find their targets here
 * after the target moved; not saved, machines register themselves when they first tick.
 */
public final class MffsDevices {
    private static final Map<Level, Map<Kind, Map<Integer, MffsMachineBlockEntity>>> LEVELS = new WeakHashMap<>();
    private static final Random RANDOM = new Random();

    /** Device kinds with a list of their own (each kind has its own ID space). */
    public enum Kind {
        PROJECTOR, EXTRACTOR, CAPACITOR, CONVERTER, DEFENSE_STATION, SECURITY_STATION, SECURITY_STORAGE, CONTROL_SYSTEM;

        /** {@code MFFSMaschines.displayName}, shown by data link cards and the control system. */
        public String translationKey() {
            return "gui.craft.mffs.kind." + name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Kind byName(String name) {
            for (Kind kind : values()) {
                if (kind.name().equals(name)) {
                    return kind;
                }
            }
            return null;
        }
    }

    private MffsDevices() {
    }

    private static Map<Integer, MffsMachineBlockEntity> devices(Level level, Kind kind) {
        return LEVELS.computeIfAbsent(level, key -> new EnumMap<>(Kind.class))
                .computeIfAbsent(kind, key -> new HashMap<>());
    }

    /**
     * {@code refreshID}: keeps a remembered ID (even if another machine holds it), otherwise draws a random unused
     * one; registers the machine under it. ID 0 means "none" in Craft and is never drawn.
     */
    public static synchronized int register(MffsMachineBlockEntity machine, int rememberedId) {
        Map<Integer, MffsMachineBlockEntity> devices = devices(machine.getLevel(), machine.getDeviceKind());
        int id = rememberedId;
        if (id == 0) {
            do {
                id = RANDOM.nextInt();
            } while (id == 0 || devices.containsKey(id));
        }
        devices.put(id, machine);
        return id;
    }

    public static synchronized void unregister(MffsMachineBlockEntity machine) {
        Map<Integer, MffsMachineBlockEntity> devices = devices(machine.getLevel(), machine.getDeviceKind());
        devices.remove(machine.getDeviceId(), machine);
    }

    public static synchronized MffsMachineBlockEntity get(Level level, Kind kind, int id) {
        MffsMachineBlockEntity machine = devices(level, kind).get(id);
        return machine != null && !machine.isRemoved() ? machine : null;
    }

    public static synchronized Collection<MffsMachineBlockEntity> all(Level level, Kind kind) {
        return List.copyOf(devices(level, kind).values());
    }
}
