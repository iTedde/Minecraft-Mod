package dev.craft.powerplants;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.powerplants.boiler.BoilerMenu;
import dev.craft.powerplants.cokeoven.CokeOvenMenu;
import dev.craft.powerplants.turbine.SteamTurbineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<CokeOvenMenu>> COKE_OVEN = MENUS.register("coke_oven",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new CokeOvenMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<BoilerMenu>> BOILER = MENUS.register("boiler",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new BoilerMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<SteamTurbineMenu>> STEAM_TURBINE = MENUS.register("steam_turbine",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new SteamTurbineMenu(id, inventory,
                    buf.readBlockPos())));

    private PpMenus() {
    }
}
