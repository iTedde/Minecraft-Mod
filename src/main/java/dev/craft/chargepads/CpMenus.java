package dev.craft.chargepads;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.chargepads.menu.ChargePadMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CpMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<ChargePadMenu>> CHARGE_PAD = MENUS.register("charge_pad",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new ChargePadMenu(id, inventory, buf.readBlockPos())));

    private CpMenus() {
    }
}
