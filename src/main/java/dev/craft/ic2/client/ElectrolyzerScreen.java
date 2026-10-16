package dev.craft.ic2.client;

import java.util.List;

import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.menu.ElectrolyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiElectrolyzer}: energy gauge between the two cell slots. */
public class ElectrolyzerScreen extends MachineScreen<ElectrolyzerMenu> {
    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "electrolyzer");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        if (menu.hasEnergy()) {
            blit(graphics, x + 79, y + 34, 176, 14, menu.getGauge() + 1, 16);
        }
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return List.of(new RecipeArea(78, 33, 26, 18, RecipeCategories.ELECTROLYZER));
    }
}
