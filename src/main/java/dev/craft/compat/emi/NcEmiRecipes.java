package dev.craft.compat.emi;

import java.util.Arrays;
import java.util.List;

import dev.craft.nuclearcontrol.NcItems;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

/** IC2 Nuclear Control (spec 46): info pages for the monitors, alarms, panel, kits/cards and counters. */
final class NcEmiRecipes {
    private NcEmiRecipes() {
    }

    static void register(EmiRegistry registry) {
        info(registry, "nc_thermal_monitor", NcItems.THERMAL_MONITOR.get());
        info(registry, "nc_remote_thermal_monitor", NcItems.REMOTE_THERMAL_MONITOR.get());
        info(registry, "nc_thermometer", NcItems.THERMOMETER.get(), NcItems.DIGITAL_THERMOMETER.get());
        info(registry, "nc_alarm", NcItems.INDUSTRIAL_ALARM.get(), NcItems.HOWLER_ALARM.get());
        info(registry, "nc_information_panel", NcItems.INFORMATION_PANEL.get(),
                NcItems.INFORMATION_PANEL_EXTENDER.get(), NcItems.RANGE_UPGRADE.get(), NcItems.COLOR_UPGRADE.get());
        info(registry, "nc_sensor_kit", NcItems.REMOTE_SENSOR_KIT.get(), NcItems.ENERGY_SENSOR_KIT.get(),
                NcItems.LIQUID_SENSOR_KIT.get(), NcItems.COUNTER_SENSOR_KIT.get(), NcItems.GT_SENSOR_KIT.get(),
                NcItems.REACTOR_SENSOR_LOCATION_CARD.get(), NcItems.ENERGY_SENSOR_LOCATION_CARD.get(),
                NcItems.LIQUID_SENSOR_LOCATION_CARD.get(), NcItems.COUNTER_SENSOR_LOCATION_CARD.get(),
                NcItems.GT_SENSOR_CARD.get());
        info(registry, "nc_energy_array_card", NcItems.ENERGY_ARRAY_LOCATION_CARD.get(),
                NcItems.ENERGY_SENSOR_LOCATION_CARD.get());
        info(registry, "nc_text_time_card", NcItems.TEXT_CARD.get(), NcItems.TIME_CARD.get());
        info(registry, "nc_counter", NcItems.ENERGY_COUNTER.get(), NcItems.AVERAGE_COUNTER.get());
        info(registry, "nc_range_trigger", NcItems.RANGE_TRIGGER.get());
    }

    private static void info(EmiRegistry registry, String name, ItemLike... items) {
        List<EmiIngredient> stacks = Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList();
        registry.addRecipe(new EmiInfoRecipe(stacks, List.of(Component.translatable("emi.craft.info." + name)),
                Ic2EmiRecipes.id("info/" + name)));
    }
}
