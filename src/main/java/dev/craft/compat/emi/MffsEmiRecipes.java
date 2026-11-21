package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.craft.mffs.MffsItems;
import dev.craft.mffs.field.ProjectorModule;
import dev.craft.mffs.field.ProjectorUpgrade;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** Modular Force Field System (spec 48): info pages for energy, cards, projector, upgrades and security. */
final class MffsEmiRecipes {
    private MffsEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, "mffs_forcicium", MffsItems.FORCICIUM.get(), MffsItems.FORCICIUM_CELL.get(),
                MffsItems.FORCE_ENERGY_CRYSTAL.get());
        info(registry, "mffs_extractor", MffsItems.EXTRACTOR.get(), MffsItems.EXTRACTOR_BOOSTER.get());
        info(registry, "mffs_capacitor", MffsItems.CAPACITOR.get(), MffsItems.CAPACITOR_UPGRADE_CAPACITY.get(),
                MffsItems.CAPACITOR_UPGRADE_RANGE.get());
        info(registry, "mffs_converter", MffsItems.CONVERTER.get());
        info(registry, "mffs_machine", MffsItems.EXTRACTOR.get(), MffsItems.CAPACITOR.get(),
                MffsItems.CONVERTER.get(), MffsItems.PROJECTOR.get(), MffsItems.DEFENSE_STATION.get(),
                MffsItems.SECURITY_STATION.get(), MffsItems.SECURITY_STORAGE.get(), MffsItems.CONTROL_SYSTEM.get());
        info(registry, "mffs_cards", MffsItems.CARD_BLANK.get(), MffsItems.CARD_POWER_LINK.get(),
                MffsItems.CARD_SECURITY_LINK.get(), MffsItems.CARD_INFINITE_POWER.get());
        info(registry, "mffs_projector", MffsItems.PROJECTOR.get(), MffsItems.PROJECTOR_FOCUS_MATRIX.get(),
                MffsItems.FIELD_MODULATOR_DISTANCE.get(), MffsItems.FIELD_MODULATOR_STRENGTH.get());
        info(registry, "mffs_modules", modules());
        info(registry, "mffs_upgrades", upgrades());
        info(registry, "mffs_security_station", MffsItems.SECURITY_STATION.get(), MffsItems.CARD_PERSONAL_ID.get(),
                MffsItems.CARD_ACCESS.get());
        info(registry, "mffs_security_storage", MffsItems.SECURITY_STORAGE.get());
        info(registry, "mffs_control_system", MffsItems.CONTROL_SYSTEM.get(), MffsItems.CARD_DATA_LINK.get());
        info(registry, "mffs_defense_station", MffsItems.DEFENSE_STATION.get());
        info(registry, "mffs_multitool", MffsItems.MULTITOOL_WRENCH.get(), MffsItems.MULTITOOL_SWITCH.get(),
                MffsItems.MULTITOOL_ID_WRITER.get(), MffsItems.MULTITOOL_FIELD_TELEPORTER.get(),
                MffsItems.MULTITOOL_GUIDE.get(), MffsItems.MULTITOOL_DEBUGGER.get());
    }

    private static ItemLike[] modules() {
        List<ItemLike> items = new ArrayList<>();
        for (ProjectorModule module : ProjectorModule.values()) {
            items.add(MffsItems.module(module).get());
        }
        return items.toArray(ItemLike[]::new);
    }

    private static ItemLike[] upgrades() {
        List<ItemLike> items = new ArrayList<>();
        for (ProjectorUpgrade upgrade : ProjectorUpgrade.values()) {
            items.add(MffsItems.upgrade(upgrade).get());
        }
        return items.toArray(ItemLike[]::new);
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList();
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
