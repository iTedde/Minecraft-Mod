package dev.craft.powerplants;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.powerplants.cokeoven.CokeOvenMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PpMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<CokeOvenMenu>> COKE_OVEN = MENUS.register("coke_oven",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new CokeOvenMenu(id, inventory,
                    buf.readBlockPos())));

    private PpMenus() {
    }
}
