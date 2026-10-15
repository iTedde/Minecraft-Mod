package dev.craft.forestry;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.forestry.menu.CarpenterMenu;
import dev.craft.forestry.menu.ElectricalEngineMenu;
import dev.craft.forestry.menu.FabricatorMenu;
import dev.craft.forestry.menu.SolderingIronMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Menu types of the Forestry machines (block position from the open packet) and the soldering iron. */
public final class FrMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<FabricatorMenu>> FABRICATOR = MENUS.register("thermionic_fabricator",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new FabricatorMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<CarpenterMenu>> CARPENTER = MENUS.register("carpenter",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new CarpenterMenu(id, inventory,
                    buf.readBlockPos())));
    public static final Supplier<MenuType<ElectricalEngineMenu>> ELECTRICAL_ENGINE = MENUS.register(
            "electrical_engine", () -> IMenuTypeExtension.create((id, inventory, buf) -> new ElectricalEngineMenu(id,
                    inventory, buf.readBlockPos())));
    public static final Supplier<MenuType<SolderingIronMenu>> SOLDERING_IRON = MENUS.register("soldering_iron",
            () -> new MenuType<>(SolderingIronMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private FrMenus() {
    }
}
