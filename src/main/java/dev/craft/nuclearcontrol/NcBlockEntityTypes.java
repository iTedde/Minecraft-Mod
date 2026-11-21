package dev.craft.nuclearcontrol;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import dev.craft.nuclearcontrol.block.AverageCounterBlockEntity;
import dev.craft.nuclearcontrol.block.EnergyCounterBlockEntity;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import dev.craft.nuclearcontrol.block.InfoPanelExtenderBlockEntity;
import dev.craft.nuclearcontrol.block.RangeTriggerBlockEntity;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NcBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Craft.MODID);

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<ThermalMonitorBlockEntity>> THERMAL_MONITOR =
            BLOCK_ENTITY_TYPES.register("thermal_monitor", () -> BlockEntityType.Builder.of(
                    ThermalMonitorBlockEntity::new, NcBlocks.THERMAL_MONITOR.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<RemoteThermalMonitorBlockEntity>> REMOTE_THERMAL_MONITOR =
            BLOCK_ENTITY_TYPES.register("remote_thermal_monitor", () -> BlockEntityType.Builder.of(
                    RemoteThermalMonitorBlockEntity::new, NcBlocks.REMOTE_THERMAL_MONITOR.get()).build(null));
    /** Howler and industrial alarm share the tile ({@code TileEntityIndustrialAlarm extends TileEntityHowlerAlarm}). */
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<AlarmBlockEntity>> ALARM =
            BLOCK_ENTITY_TYPES.register("alarm", () -> BlockEntityType.Builder.of(AlarmBlockEntity::new,
                    NcBlocks.HOWLER_ALARM.get(), NcBlocks.INDUSTRIAL_ALARM.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<InfoPanelBlockEntity>> INFORMATION_PANEL =
            BLOCK_ENTITY_TYPES.register("information_panel", () -> BlockEntityType.Builder.of(
                    InfoPanelBlockEntity::new, NcBlocks.INFORMATION_PANEL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<InfoPanelExtenderBlockEntity>> INFORMATION_PANEL_EXTENDER =
            BLOCK_ENTITY_TYPES.register("information_panel_extender", () -> BlockEntityType.Builder.of(
                    InfoPanelExtenderBlockEntity::new, NcBlocks.INFORMATION_PANEL_EXTENDER.get()).build(null));

    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<EnergyCounterBlockEntity>> ENERGY_COUNTER =
            BLOCK_ENTITY_TYPES.register("energy_counter", () -> BlockEntityType.Builder.of(
                    EnergyCounterBlockEntity::new, NcBlocks.ENERGY_COUNTER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<AverageCounterBlockEntity>> AVERAGE_COUNTER =
            BLOCK_ENTITY_TYPES.register("average_counter", () -> BlockEntityType.Builder.of(
                    AverageCounterBlockEntity::new, NcBlocks.AVERAGE_COUNTER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final Supplier<BlockEntityType<RangeTriggerBlockEntity>> RANGE_TRIGGER =
            BLOCK_ENTITY_TYPES.register("range_trigger", () -> BlockEntityType.Builder.of(
                    RangeTriggerBlockEntity::new, NcBlocks.RANGE_TRIGGER.get()).build(null));

    private NcBlockEntityTypes() {
    }
}
