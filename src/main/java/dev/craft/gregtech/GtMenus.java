package dev.craft.gregtech;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.gregtech.menu.GtBasicMachineMenu;
import dev.craft.gregtech.menu.GtBlastFurnaceMenu;
import dev.craft.gregtech.menu.GtGrinderMenu;
import dev.craft.gregtech.menu.GtImplosionCompressorMenu;
import dev.craft.gregtech.menu.GtTankMachineMenu;
import dev.craft.gregtech.menu.GtVacuumFreezerMenu;
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
    public static final Supplier<MenuType<GtTankMachineMenu>> ELECTROLYZER = MENUS.register("gt_electrolyzer",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtTankMachineMenu(id, inventory,
                    buf.readBlockPos(), GtMachine.INDUSTRIAL_ELECTROLYZER)));

    /** {@code GT_Container_Centrifuge}. */
    public static final Supplier<MenuType<GtTankMachineMenu>> CENTRIFUGE = MENUS.register("gt_centrifuge",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtTankMachineMenu(id, inventory,
                    buf.readBlockPos(), GtMachine.INDUSTRIAL_CENTRIFUGE)));

    /** {@code GT_Container_BlastFurnace}. */
    public static final Supplier<MenuType<GtBlastFurnaceMenu>> BLAST_FURNACE = MENUS.register("gt_blast_furnace",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtBlastFurnaceMenu(id, inventory,
                    buf.readBlockPos())));

    /** {@code GT_Container_ImplosionCompressor}. */
    public static final Supplier<MenuType<GtImplosionCompressorMenu>> IMPLOSION_COMPRESSOR =
            MENUS.register("gt_implosion_compressor", () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new GtImplosionCompressorMenu(id, inventory, buf.readBlockPos())));

    /** {@code GT_Container_Grinder}. */
    public static final Supplier<MenuType<GtGrinderMenu>> GRINDER = MENUS.register("gt_grinder",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtGrinderMenu(id, inventory,
                    buf.readBlockPos())));

    /** {@code GT_Container_VacuumFreezer}. */
    public static final Supplier<MenuType<GtVacuumFreezerMenu>> VACUUM_FREEZER = MENUS.register("gt_vacuum_freezer",
            () -> IMenuTypeExtension.create((id, inventory, buf) -> new GtVacuumFreezerMenu(id, inventory,
                    buf.readBlockPos())));

    private GtMenus() {
    }
}
