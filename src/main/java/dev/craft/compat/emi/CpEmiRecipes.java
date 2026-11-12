package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.craft.chargepads.CpItems;
import dev.craft.chargepads.item.PadModule;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** ChargePads (spec 47): info pages for the pads, hazards, upgrades, upgrade kits and emitter modules. */
final class CpEmiRecipes {
    private CpEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        ItemLike[] pads = {CpItems.STATIC_CHARGE_PAD.get(), CpItems.CRYSTALIZOR_CHARGE_PAD.get(),
                CpItems.LAPOTRONIC_CHARGE_PAD.get()};
        info(registry, "cp_charge_pad", pads);
        info(registry, "cp_hazard", pads);
        info(registry, "cp_upgrades", CpItems.EFFICIENCY_UPGRADE.get(), CpItems.STATIC_CHARGE_PAD.get(),
                CpItems.CRYSTALIZOR_CHARGE_PAD.get(), CpItems.LAPOTRONIC_CHARGE_PAD.get());
        info(registry, "cp_upgrade_kit", CpItems.CRYSTALIZOR_UPGRADE_KIT.get(), CpItems.LAPOTRONIC_UPGRADE_KIT.get(),
                CpItems.MFS_UNIT_UPGRADE.get());
        info(registry, "cp_field_module", modules(true));
        info(registry, "cp_projector_module", modules(false));
    }

    private static ItemLike[] modules(boolean field) {
        List<ItemLike> items = new ArrayList<>();
        for (PadModule module : PadModule.values()) {
            if (module.isField() == field) {
                items.add(CpItems.module(module).get());
            }
        }
        return items.toArray(ItemLike[]::new);
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList();
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
