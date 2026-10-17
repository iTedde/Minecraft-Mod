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
    public static final DeferredHolder<SoundEvent, SoundEvent> MASS_FAB_LOOP = register("generators/mass_fab_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MASS_FAB_SCRAP_SOLO =
            register("generators/mass_fab_scrap_solo");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEAR_REACTOR_LOOP =
            register("generators/nuclear_reactor_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_LOW_EU = register("generators/geiger_low_eu");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_MED_EU = register("generators/geiger_med_eu");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_HIGH_EU = register("generators/geiger_high_eu");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPRESSOR_OP = register("machines/compressor_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXTRACTOR_OP = register("machines/extractor_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> MACERATOR_OP = register("machines/macerator_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> RECYCLER_OP = register("machines/recycler_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP_OP = register("machines/pump_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINER_OP = register("machines/miner_op");
    public static final DeferredHolder<SoundEvent, SoundEvent> O_MAT = register("machines/o_mat");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELE_CHARGED_LOOP =
            register("machines/teleporter/tele_charged_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELE_USE = register("machines/teleporter/tele_use");
    public static final DeferredHolder<SoundEvent, SoundEvent> TERRAFORMER_LOOP =
            register("terraformers/terraformer_generic_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRO_FURNACE_LOOP =
            register("machines/electro_furnace_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> INTERRUPT_ONE = register("machines/interrupt_one");
    public static final DeferredHolder<SoundEvent, SoundEvent> TREETAP = register("tools/treetap");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH = register("tools/wrench");
    public static final DeferredHolder<SoundEvent, SoundEvent> INSULATION_CUTTERS = register("tools/insulation_cutters");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAINTER = register("tools/painter");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAINSAW_IDLE = register("tools/chainsaw_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHAINSAW_STOP = register("tools/chainsaw_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINING_LASER = register("tools/mining_laser");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINING_LASER_LOW_FOCUS =
            register("tools/mining_laser_low_focus");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINING_LASER_LONG_RANGE =
            register("tools/mining_laser_long_range");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINING_LASER_SCATTER =
            register("tools/mining_laser_scatter");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINING_LASER_EXPLOSIVE =
            register("tools/mining_laser_explosive");
    public static final DeferredHolder<SoundEvent, SoundEvent> OD_SCANNER = register("tools/od_scanner");
    public static final DeferredHolder<SoundEvent, SoundEvent> JETPACK_LOOP = register("tools/jetpack_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DYNAMITE_O_MOTE = register("tools/dynamite_o_mote");

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
