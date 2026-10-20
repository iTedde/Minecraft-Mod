package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IronChestMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Craft.MODID);

    /** The chest type travels as its ordinal, so the client builds an inventory of the right size. */
    public static final Supplier<MenuType<IronChestMenu>> IRON_CHEST = MENUS.register("iron_chest",
            () -> IMenuTypeExtension.create((id, inventory, buf) ->
                    new IronChestMenu(id, inventory, IronChestType.values()[buf.readVarInt()])));

    private IronChestMenus() {
    }
}
