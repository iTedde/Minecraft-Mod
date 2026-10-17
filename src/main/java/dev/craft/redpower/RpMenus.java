package dev.craft.redpower;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.redpower.table.ProjectTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RpMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<ProjectTableMenu>> PROJECT_TABLE = MENUS.register("project_table",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new ProjectTableMenu(id, inventory)));

    private RpMenus() {
    }
}
