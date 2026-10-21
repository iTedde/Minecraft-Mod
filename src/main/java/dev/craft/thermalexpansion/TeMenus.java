package dev.craft.thermalexpansion;

import java.util.function.Supplier;

import dev.craft.Craft;
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

    private TeMenus() {
    }
}
