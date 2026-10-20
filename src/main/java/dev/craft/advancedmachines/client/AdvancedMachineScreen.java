package dev.craft.advancedmachines.client;

import java.util.List;

import dev.craft.advancedmachines.AdvancedMachineBlockEntity;
import dev.craft.advancedmachines.AdvancedMachineMenu;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.ic2.client.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** {@code GuiRotaryMacerator}, {@code GuiSingularityCompressor}, {@code GuiCentrifugeExtractor} (spec 25 §4). */
public class AdvancedMachineScreen extends MachineScreen<AdvancedMachineMenu> {
    private static final int TEXT_COLOR = 4210752;

    public AdvancedMachineScreen(AdvancedMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "advancedmachines/" + menu.getMachineType().getGuiTexture());
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 49;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("gui.craft.advanced_machine.speed"), 10, 36, TEXT_COLOR,
                false);
        graphics.drawString(font, menu.getMachineType().formatSpeed(menu.getSpeed()), 10, 44, TEXT_COLOR, false);
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        float charge = menu.getChargeLevel();
        if (charge > 0) {
            int height = (int) (14 * charge);
            blit(graphics, x + 56, y + 36 + 14 - height, 176, 14 - height, 14, height);
        }
        int progress = 24 * menu.getProgress() / AdvancedMachineBlockEntity.MAX_PROGRESS;
        blit(graphics, x + 75, y + 34, 176, 14, progress + 1, 16);
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return List.of(new RecipeArea(74, 33, 26, 18, switch (menu.getMachineType()) {
            case ROTARY_MACERATOR -> RecipeCategories.MACERATOR;
            case SINGULARITY_COMPRESSOR -> RecipeCategories.COMPRESSOR;
            case CENTRIFUGE_EXTRACTOR -> RecipeCategories.EXTRACTOR;
        }));
    }
}
