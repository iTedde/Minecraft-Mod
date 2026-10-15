package dev.craft.gregtech;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.gregtech.menu.GtBasicMachineMenu;
import dev.craft.gregtech.menu.GtBlastFurnaceMenu;
import dev.craft.gregtech.menu.GtElectrolyzerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    /** {@code GT_Container_BasicMachine}, shared by all basic machines (the GUI texture comes with the machine). */
    public static final Supplier<MenuType<GtBasicMachineMenu>> BASIC_MACHINE = MENUS.register("gt_basic_machine",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtBasicMachineMenu(id, inventory,
                    buf.readBlockPos())));

    /** {@code GT_Container_Electrolyzer}. */
    public static final Supplier<MenuType<GtElectrolyzerMenu>> ELECTROLYZER = MENUS.register("gt_electrolyzer",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtElectrolyzerMenu(id, inventory,
                    buf.readBlockPos())));

    /** {@code GT_Container_BlastFurnace}. */
    public static final Supplier<MenuType<GtBlastFurnaceMenu>> BLAST_FURNACE = MENUS.register("gt_blast_furnace",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtBlastFurnaceMenu(id, inventory,
                    buf.readBlockPos())));

    private GtMenus() {
    }
}
