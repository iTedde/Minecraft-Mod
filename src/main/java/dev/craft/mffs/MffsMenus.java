package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.menu.CapacitorMenu;
import dev.craft.mffs.menu.ControlSystemMenu;
import dev.craft.mffs.menu.ConverterMenu;
import dev.craft.mffs.menu.DefenseStationMenu;
import dev.craft.mffs.menu.ExtractorMenu;
import dev.craft.mffs.menu.ProjectorMenu;
import dev.craft.mffs.menu.SecurityStationMenu;
import dev.craft.mffs.menu.SecurityStorageMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MffsMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<ExtractorMenu>> EXTRACTOR = MENUS.register("mffs_extractor",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new ExtractorMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<CapacitorMenu>> CAPACITOR = MENUS.register("mffs_capacitor",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new CapacitorMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<ConverterMenu>> CONVERTER = MENUS.register("mffs_converter",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new ConverterMenu(id, inventory,
                    buf.readBlockPos())));

    public static final Supplier<MenuType<ProjectorMenu>> PROJECTOR = MENUS.register("mffs_projector",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new ProjectorMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<SecurityStationMenu>> SECURITY_STATION = MENUS.register(
            "mffs_security_station", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new SecurityStationMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<SecurityStorageMenu>> SECURITY_STORAGE = MENUS.register(
            "mffs_security_storage", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new SecurityStorageMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<ControlSystemMenu>> CONTROL_SYSTEM = MENUS.register(
            "mffs_control_system", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new ControlSystemMenu(id, inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<DefenseStationMenu>> DEFENSE_STATION = MENUS.register(
            "mffs_defense_station", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new DefenseStationMenu(id, inventory, buf.readBlockPos())));

    private MffsMenus() {
    }
}
