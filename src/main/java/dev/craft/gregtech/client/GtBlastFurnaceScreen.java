package dev.craft.gregtech.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.menu.GtBlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** {@code GT_GUIContainer_BlastFurnace}: heat capacity or casing warning, progress arrow, error text. */
public class GtBlastFurnaceScreen extends GtMachineScreen<GtBlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/gt/blast_furnace.png");

    public GtBlastFurnaceScreen(GtBlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 4, 0x404040, false);
        Component state = menu.isMachine()
                ? Component.translatable("container.craft.gt.heat_capacity", menu.getHeatCapacity())
                : Component.translatable("container.craft.gt.incomplete_casing");
        graphics.drawString(font, state, 8, imageHeight - 103, 0x404040, false);
        Component bottom = menu.isMissingEnergy() ? Component.translatable("container.craft.gt.insufficient_energy")
                : playerInventoryTitle;
        graphics.drawString(font, bottom, 8, imageHeight - 94, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int scale = menu.getProgressScale();
        if (scale > 0) {
            graphics.blit(TEXTURE, leftPos + 58, topPos + 28, 176, 0, scale, 11);
        }
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return List.of(new RecipeArea(58, 28, 20, 11, RecipeCategories.GT_BLAST_FURNACE));
    }
}
