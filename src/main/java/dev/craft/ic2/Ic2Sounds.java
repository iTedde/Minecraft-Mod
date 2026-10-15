package dev.craft.ic2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** IC2 sounds; the sound id is also the path of the ogg file below {@code assets/craft/sounds}. */
public final class Ic2Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Craft.MODID);

    private static final List<DeferredHolder<SoundEvent, SoundEvent>> ALL = new ArrayList<>();

    public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR_LOOP = register("generators/generator_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEOTHERMAL_LOOP = register("generators/geothermal_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WATERMILL_LOOP = register("generators/watermill_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WINDGEN_LOOP = register("generators/windgen_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPRESSOR_OP = register("machines/compressor_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXTRACTOR_OP = register("machines/extractor_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACERATOR_OP = register("machines/macerator_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECYCLER_OP = register("machines/recycler_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_FURNACE_LOOP =
            register("machines/electro_furnace_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> INTERRUPT_ONE = register("machines/interrupt_one");
    public static final DeferredHolder<SoundEvent, SoundEvent> TREETAP = register("tools/treetap");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH = register("tools/wrench");

    private Ic2Sounds() {
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
