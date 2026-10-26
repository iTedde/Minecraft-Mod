package dev.craft.powerconverters;

/** The BuildCraft and IC2 converter blocks (the metadata variants of {@code converterBlockBuildCraft/IC2}). */
public enum ConverterType {
    BC_CONSUMER("bc_consumer", PowerSystem.BUILDCRAFT, 0, true),
    BC_PRODUCER("bc_producer", PowerSystem.BUILDCRAFT, 0, false),
    IC2_LV_CONSUMER("ic2_lv_consumer", PowerSystem.INDUSTRIALCRAFT, 0, true),
    IC2_LV_PRODUCER("ic2_lv_producer", PowerSystem.INDUSTRIALCRAFT, 0, false),
    IC2_MV_CONSUMER("ic2_mv_consumer", PowerSystem.INDUSTRIALCRAFT, 1, true),
    IC2_MV_PRODUCER("ic2_mv_producer", PowerSystem.INDUSTRIALCRAFT, 1, false),
    IC2_HV_CONSUMER("ic2_hv_consumer", PowerSystem.INDUSTRIALCRAFT, 2, true),
    IC2_HV_PRODUCER("ic2_hv_producer", PowerSystem.INDUSTRIALCRAFT, 2, false),
    IC2_EV_CONSUMER("ic2_ev_consumer", PowerSystem.INDUSTRIALCRAFT, 3, true),
    IC2_EV_PRODUCER("ic2_ev_producer", PowerSystem.INDUSTRIALCRAFT, 3, false),
    /** New content (spec 33): EU/MJ from the bridge as FE; there is no FE consumer. */
    FE_PRODUCER("fe_producer", PowerSystem.FE, 0, false);

    private final String name;
    private final PowerSystem system;
    private final int voltageIndex;
    private final boolean consumer;

    ConverterType(String name, PowerSystem system, int voltageIndex, boolean consumer) {
        this.name = name;
        this.system = system;
        this.voltageIndex = voltageIndex;
        this.consumer = consumer;
    }

    public String getName() {
        return name;
    }

    public PowerSystem getSystem() {
        return system;
    }

    public int getVoltageIndex() {
        return voltageIndex;
    }

    public boolean isConsumer() {
        return consumer;
    }

    /** Whether {@link #counterpart()} exists (all BC and IC2 converters). */
    public boolean hasCounterpart() {
        return system != PowerSystem.FE;
    }

    /** The consumer/producer of the same system and voltage (shapeless conversion recipe). */
    public ConverterType counterpart() {
        return values()[ordinal() ^ 1];
    }
}
