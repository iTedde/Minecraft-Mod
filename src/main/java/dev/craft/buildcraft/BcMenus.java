package dev.craft.buildcraft;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.menu.AutoWorkbenchMenu;
import dev.craft.buildcraft.menu.DistributionPipeMenu;
import dev.craft.buildcraft.menu.EngineMenu;
import dev.craft.buildcraft.menu.PipeFilterMenu;
import dev.craft.buildcraft.menu.RefineryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Menu types of the engines, the refinery, the automatic crafting table and the filter pipes; the client reads the block position from the open packet. */
public final class BcMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    private static final Map<EngineType, Supplier<MenuType<EngineMenu>>> ENGINES = new EnumMap<>(EngineType.class);

    static {
        for (EngineType type : EngineType.values()) {
            if (type.hasMenu()) {
                ENGINES.put(type, MENUS.register(type.getName(), () -> IMenuTypeExtension.create(
                        (id, inventory, buf) -> new EngineMenu(type, id, inventory, buf.readBlockPos()))));
            }
        }
    }

    public static final Supplier<MenuType<RefineryMenu>> REFINERY = MENUS.register("refinery",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new RefineryMenu(id, inventory, buf.readBlockPos())));

    public static final Supplier<MenuType<AutoWorkbenchMenu>> AUTO_WORKBENCH = MENUS.register("auto_workbench",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new AutoWorkbenchMenu(id, inventory)));

    /** Diamond and emerald pipe filters; the client reads the number of filters. */
    public static final Supplier<MenuType<PipeFilterMenu>> PIPE_FILTERS = MENUS.register("pipe_filters",
            () -> IMenuTypeExtension.create(PipeFilterMenu::new));

    /** Weights of the distribution transport pipe (spec 31); no extra data in the open packet. */
    public static final Supplier<MenuType<DistributionPipeMenu>> DISTRIBUTION_PIPE = MENUS.register(
            "distribution_pipe", () -> IMenuTypeExtension.create((id, inventory, buf) -> new DistributionPipeMenu(id,
                    inventory)));

    private BcMenus() {
    }

    public static MenuType<EngineMenu> get(EngineType type) {
        return ENGINES.get(type).get();
    }
}
