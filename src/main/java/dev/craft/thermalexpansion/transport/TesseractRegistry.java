package dev.craft.thermalexpansion.transport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code TeleportRegistry}: per type, owner key ({@code _public_} or the owner) and frequency, the senders
 * ({@code inputs}) and receivers ({@code outputs}). Global across dimensions, server only; tesseracts leave it when
 * unloaded, and it is cleared when the server stops (the original clears it on the main menu).
 */
public final class TesseractRegistry {
    private static final Map<TesseractType, Map<String, Map<Integer, List<TesseractBlockEntity>>>> SENDERS =
            new EnumMap<>(TesseractType.class);
    private static final Map<TesseractType, Map<String, Map<Integer, List<TesseractBlockEntity>>>> RECEIVERS =
            new EnumMap<>(TesseractType.class);

    private TesseractRegistry() {
    }

    public static void clear() {
        SENDERS.clear();
        RECEIVERS.clear();
    }

    /** {@code getLinkedInputs}: the senders on the tesseract's channel, itself included. */
    public static List<TesseractBlockEntity> senders(TesseractBlockEntity tesseract) {
        return linked(SENDERS, tesseract);
    }

    /** {@code getLinkedOutputs}: the receivers on the tesseract's channel, itself included. */
    public static List<TesseractBlockEntity> receivers(TesseractBlockEntity tesseract) {
        return linked(RECEIVERS, tesseract);
    }

    static void add(TesseractBlockEntity tesseract) {
        if (tesseract.canSend()) {
            add(SENDERS, tesseract);
        }
        if (tesseract.canReceive()) {
            add(RECEIVERS, tesseract);
        }
    }

    static void remove(TesseractBlockEntity tesseract) {
        remove(SENDERS, tesseract);
        remove(RECEIVERS, tesseract);
    }

    private static List<TesseractBlockEntity> linked(
            Map<TesseractType, Map<String, Map<Integer, List<TesseractBlockEntity>>>> map,
            TesseractBlockEntity tesseract) {
        Map<String, Map<Integer, List<TesseractBlockEntity>>> owners = map.get(tesseract.getTesseractType());
        Map<Integer, List<TesseractBlockEntity>> frequencies = owners != null ? owners.get(tesseract.getOwnerKey())
                : null;
        List<TesseractBlockEntity> list = frequencies != null ? frequencies.get(tesseract.getFrequency()) : null;
        return list != null ? list : List.of();
    }

    private static void add(Map<TesseractType, Map<String, Map<Integer, List<TesseractBlockEntity>>>> map,
                            TesseractBlockEntity tesseract) {
        List<TesseractBlockEntity> list = map.computeIfAbsent(tesseract.getTesseractType(), t -> new HashMap<>())
                .computeIfAbsent(tesseract.getOwnerKey(), o -> new HashMap<>())
                .computeIfAbsent(tesseract.getFrequency(), f -> new ArrayList<>());
        if (!list.contains(tesseract)) {
            list.add(tesseract);
        }
    }

    private static void remove(Map<TesseractType, Map<String, Map<Integer, List<TesseractBlockEntity>>>> map,
                               TesseractBlockEntity tesseract) {
        Map<String, Map<Integer, List<TesseractBlockEntity>>> owners = map.get(tesseract.getTesseractType());
        Map<Integer, List<TesseractBlockEntity>> frequencies = owners != null ? owners.get(tesseract.getOwnerKey())
                : null;
        List<TesseractBlockEntity> list = frequencies != null ? frequencies.get(tesseract.getFrequency()) : null;
        if (list != null) {
            list.remove(tesseract);
            if (list.isEmpty()) {
                frequencies.remove(tesseract.getFrequency());
            }
        }
    }
}
