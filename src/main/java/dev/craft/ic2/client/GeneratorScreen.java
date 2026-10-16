package dev.craft.ic2.client;

import java.util.List;

import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.menu.GeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiGenerator}, {@code GuiGeoGenerator}, {@code GuiWaterGenerator}, {@code GuiSolarGenerator}, {@code GuiWindGenerator}. */
public class GeneratorScreen extends MachineScreen<GeneratorMenu> {
    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.getKind().getName());
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        switch (menu.getKind()) {
            case GENERATOR -> {
                if (menu.getFuel() > 0) {
                    int fuel = menu.getFuelGauge();
                    blit(graphics, x + 66, y + 36 + 12 - fuel, 176, 12 - fuel, 14, fuel + 2);
                }
                blit(graphics, x + 94, y + 35, 176, 14, menu.gaugeStorageScaled(24), 17);
            }
            case GEOTHERMAL_GENERATOR -> {
                if (menu.getFuel() > 0) {
                    int fuel = menu.getFuelGauge();
                    blit(graphics, x + 66, y + 36 + 14 - fuel, 176, 14 - fuel, 14, fuel);
                }
                if (menu.getStorage() > 0) {
                    blit(graphics, x + 89, y + 39, 177, 19, menu.gaugeStorageScaled(22), 6);
                }
            }
            case WATER_MILL -> {
                if (menu.getFuel() > 0) {
                    int fuel = menu.getFuelGauge();
                    blit(graphics, x + 80, y + 36 + 14 - fuel, 176, 14 - fuel, 14, fuel);
                }
            }
            case SOLAR_PANEL -> {
                if (menu.isSunVisible()) {
                    blit(graphics, x + 80, y + 45, 176, 0, 14, 14);
                }
            }
            case WIND_MILL -> {
                int power = menu.getFuelGauge();
                blit(graphics, x + 80, y + 45 + 14 - power, 176, 14 - power, 14, power);
                int overheat = menu.getOverheatGauge();
                blit(graphics, x + 80, y + 45 + 14 - overheat, 176, 28 - overheat, 14, overheat);
            }
            default -> {
            }
        }
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return switch (menu.getKind()) {
            case GENERATOR -> List.of(new RecipeArea(65, 35, 16, 16, RecipeCategories.GENERATOR));
            case GEOTHERMAL_GENERATOR -> List.of(new RecipeArea(65, 35, 16, 16, RecipeCategories.GEOTHERMAL_GENERATOR));
            default -> List.of();
        };
    }
}
