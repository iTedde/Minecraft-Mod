package dev.craft.gravisuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * GraviSuite sounds that IC2 does not already have ({@code Treetap.ogg}, {@code wrench.ogg} and
 * {@code JetpackLoop.ogg} are the IC2 files); the id is the ogg path below {@code assets/craft/sounds}.
 */
public final class GsSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Craft.MODID);

    private static final List<DeferredHolder<SoundEvent, SoundEvent>> ALL = new ArrayList<>();

    /** {@code toolChange.ogg}: the GraviTool switches its mode. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL_CHANGE = register("gravisuite/tool_change");

    private GsSounds() {
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
}
