package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.AlarmBlock;
import dev.craft.nuclearcontrol.block.InfoPanelBlock;
import dev.craft.nuclearcontrol.block.InfoPanelExtenderBlock;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlock;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Own blocks instead of the metadata block {@code BlockNuclearControlMain} (spec 46 §1). All: material iron,
 * hardness 0.5 (1.4.7 resistance {@code 0.5 × 5 × 3 / 5 / 3} = 0.5).
 */
public final class NcBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Craft.MODID);

    public static final DeferredBlock<ThermalMonitorBlock> THERMAL_MONITOR = BLOCKS.register("thermal_monitor",
            () -> new ThermalMonitorBlock(properties().noOcclusion()));
    public static final DeferredBlock<AlarmBlock> INDUSTRIAL_ALARM = BLOCKS.register("industrial_alarm",
            () -> new AlarmBlock(properties().noOcclusion().lightLevel(AlarmBlock::lightLevel), true));
    public static final DeferredBlock<AlarmBlock> HOWLER_ALARM = BLOCKS.register("howler_alarm",
            () -> new AlarmBlock(properties().noOcclusion(), false));
    public static final DeferredBlock<RemoteThermalMonitorBlock> REMOTE_THERMAL_MONITOR =
            BLOCKS.register("remote_thermal_monitor", () -> new RemoteThermalMonitorBlock(properties()));
    public static final DeferredBlock<InfoPanelBlock> INFORMATION_PANEL = BLOCKS.register("information_panel",
            () -> new InfoPanelBlock(properties().lightLevel(InfoPanelBlock::lightLevel)));
    public static final DeferredBlock<InfoPanelExtenderBlock> INFORMATION_PANEL_EXTENDER =
            BLOCKS.register("information_panel_extender",
                    () -> new InfoPanelExtenderBlock(properties().lightLevel(InfoPanelBlock::lightLevel)));

    private NcBlocks() {
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.5F).sound(SoundType.METAL);
    }
}
