package dev.craft.enderstorage;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EsMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    /** Ender chest and ender pouch; the owner's name travels along for the screen. */
    public static final Supplier<MenuType<EnderStorageMenu>> ENDER_STORAGE = MENUS.register("ender_storage",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new EnderStorageMenu(id, inventory, buf.readUtf())));

    private EsMenus() {
    }
}
