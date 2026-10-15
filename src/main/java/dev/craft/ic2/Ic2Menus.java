package dev.craft.ic2;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.ic2.menu.ElectricMachineMenu;
import dev.craft.ic2.menu.ElectricStorageMenu;
import dev.craft.ic2.menu.GeneratorMenu;
import dev.craft.ic2.menu.IronFurnaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One menu type per IC2 machine; the client side reads the block position from the open packet. */
public final class Ic2Menus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    private static final Map<Ic2Machine, Supplier<MenuType<?>>> TYPES = new EnumMap<>(Ic2Machine.class);

    static {
        for (Ic2Machine machine : Ic2Machine.values()) {
            if (machine.hasMenu()) {
                TYPES.put(machine, MENUS.register(machine.getName(), () -> create(machine)));
            }
        }
    }

    private Ic2Menus() {
    }

    private static MenuType<?> create(Ic2Machine machine) {
        return switch (machine) {
            case GENERATOR, GEOTHERMAL_GENERATOR, WATER_MILL, SOLAR_PANEL, WIND_MILL -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new GeneratorMenu(machine, id, inventory, buf.readBlockPos()));
            case IRON_FURNACE -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new IronFurnaceMenu(id, inventory, buf.readBlockPos()));
            case ELECTRIC_FURNACE, MACERATOR, EXTRACTOR, COMPRESSOR, RECYCLER -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new ElectricMachineMenu(machine, id, inventory, buf.readBlockPos()));
            case BATBOX, MFE, MFSU -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new ElectricStorageMenu(machine, id, inventory, buf.readBlockPos()));
            case LV_TRANSFORMER, MV_TRANSFORMER, HV_TRANSFORMER ->
                    throw new IllegalArgumentException(machine + " has no menu");
        };
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractContainerMenu> MenuType<T> get(Ic2Machine machine) {
        return (MenuType<T>) TYPES.get(machine).get();
    }
}
