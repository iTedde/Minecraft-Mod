package dev.craft.nuclearcontrol;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
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

    private NcBlockEntityTypes() {
    }
}
