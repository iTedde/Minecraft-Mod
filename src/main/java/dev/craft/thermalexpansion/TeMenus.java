package dev.craft.thermalexpansion;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.energy.EnergyCellMenu;
import dev.craft.thermalexpansion.energy.TeEngineMenu;
import dev.craft.thermalexpansion.energy.TeEngineType;
import dev.craft.thermalexpansion.machine.TeMachineMenu;
import dev.craft.thermalexpansion.machine.TeMachineType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Menus of Thermal Expansion; the open packet carries the machine type and the block position. */
public final class TeMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    public static final Supplier<MenuType<TeMachineMenu>> MACHINE = MENUS.register("te_machine",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new TeMachineMenu(id, inventory,
                    TeMachineType.values()[buf.readVarInt()], buf.readBlockPos())));

    public static final Supplier<MenuType<TeEngineMenu>> ENGINE = MENUS.register("te_engine",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new TeEngineMenu(id, inventory,
                    TeEngineType.values()[buf.readVarInt()], buf.readBlockPos())));
    public static final Supplier<MenuType<EnergyCellMenu>> ENERGY_CELL = MENUS.register("te_energy_cell",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new EnergyCellMenu(id, inventory,
                    buf.readBlockPos())));

    private TeMenus() {
    }
}
