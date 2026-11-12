package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.menu.CapacitorMenu;
import dev.craft.mffs.menu.ConverterMenu;
import dev.craft.mffs.menu.ExtractorMenu;
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

    private MffsMenus() {
    }
}
