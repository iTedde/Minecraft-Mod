package dev.craft.nuclearcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The two alarm sounds of the pack ({@code allowedAlarms=default,sci-fi}); id = ogg path below {@code sounds}. */
public final class NcSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Craft.MODID);

    private static final List<DeferredHolder<SoundEvent, SoundEvent>> ALL = new ArrayList<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_DEFAULT = register("nuclearcontrol/alarm_default");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALARM_SCI_FI = register("nuclearcontrol/alarm_sci_fi");

    /** Names shown in the howler alarm's list, in the config order. */
    public static final List<String> ALARM_NAMES = List.of("default", "sci-fi");

    private NcSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUNDS.register(path,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Craft.MODID, path)));
        ALL.add(holder);
        return holder;
    }

    public static List<DeferredHolder<SoundEvent, SoundEvent>> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** {@code "ic2nuclearControl.alarm-" + name}. */
    public static SoundEvent alarm(String name) {
        return "sci-fi".equals(name) ? ALARM_SCI_FI.get() : ALARM_DEFAULT.get();
    }
}
