package dev.craft.powerconverters;

import dev.craft.Craft;
import dev.craft.powerconverters.menu.EnergyBridgeMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PcMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<EnergyBridgeMenu>> ENERGY_BRIDGE = MENUS.register("energy_bridge",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new EnergyBridgeMenu(id, inventory,
                    buf.readBlockPos())));

    private PcMenus() {
    }
}
