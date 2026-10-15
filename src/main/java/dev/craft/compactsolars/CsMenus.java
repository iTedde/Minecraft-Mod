package dev.craft.compactsolars;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CsMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<SolarArrayMenu>> SOLAR_ARRAY = MENUS.register("solar_array",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new SolarArrayMenu(id, inventory,
                    buf.readBlockPos(), buf.readEnum(SolarArrayType.class))));

    private CsMenus() {
    }
}
