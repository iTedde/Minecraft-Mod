package dev.craft.nuclearcontrol;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.menu.AlarmMenu;
import dev.craft.nuclearcontrol.menu.CounterMenu;
import dev.craft.nuclearcontrol.menu.InfoPanelMenu;
import dev.craft.nuclearcontrol.menu.RangeTriggerMenu;
import dev.craft.nuclearcontrol.menu.RemoteThermalMonitorMenu;
import dev.craft.nuclearcontrol.menu.ThermalMonitorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All menus carry the block position (the screens read heat level, range … from the synced block entity). */
public final class NcMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<ThermalMonitorMenu>> THERMAL_MONITOR = MENUS.register("thermal_monitor",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new ThermalMonitorMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<RemoteThermalMonitorMenu>> REMOTE_THERMAL_MONITOR =
            MENUS.register("remote_thermal_monitor", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new RemoteThermalMonitorMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<AlarmMenu>> ALARM = MENUS.register("alarm",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new AlarmMenu(id, inventory, buf.readBlockPos())));

    public static final Supplier<MenuType<InfoPanelMenu>> INFORMATION_PANEL = MENUS.register("information_panel",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new InfoPanelMenu(id, inventory, buf.readBlockPos())));
    /** Energy and average counter share the menu; the screen picks its controls by the block entity. */
    public static final Supplier<MenuType<CounterMenu>> COUNTER = MENUS.register("counter",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new CounterMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<RangeTriggerMenu>> RANGE_TRIGGER = MENUS.register("range_trigger",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new RangeTriggerMenu(id, inventory, buf.readBlockPos())));

    private NcMenus() {
    }
}
