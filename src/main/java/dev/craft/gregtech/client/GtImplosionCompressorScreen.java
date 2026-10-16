package dev.craft.gregtech.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.menu.GtImplosionCompressorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** {@code GT_GUIContainer_ImplosionCompressor}: casing warning and progress arrow. */
public class GtImplosionCompressorScreen extends GtMachineScreen<GtImplosionCompressorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/gt/implosion.png");

    public GtImplosionCompressorScreen(GtImplosionCompressorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 4, 0x404040, false);
        if (!menu.isMachine()) {
            graphics.drawString(font, Component.translatable("container.craft.gt.incomplete_casing"), 8,
                    imageHeight - 103, 0x404040, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
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
        return List.of(new RecipeArea(58, 28, 20, 11, RecipeCategories.GT_IMPLOSION));
    }
}
