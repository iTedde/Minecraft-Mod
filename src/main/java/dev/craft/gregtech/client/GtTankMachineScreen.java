package dev.craft.gregtech.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.menu.GtTankMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GT_GUIContainer_Electrolyzer} (progress bar growing upwards, title top left) and
 * {@code GT_GUIContainer_Centrifuge} (four arrows growing outwards, two-line title top right); error text instead of
 * "Inventory".
 */
public class GtTankMachineScreen extends GtMachineScreen<GtTankMachineMenu> {
    private final GtMachine machine;
    private final ResourceLocation texture;

    private GtTankMachineScreen(GtTankMachineMenu menu, Inventory inventory, Component title, GtMachine machine) {
        super(menu, inventory, title);
        this.machine = machine;
        this.texture = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                "textures/gui/gt/" + machine.getGui() + ".png");
    }

    public static GtTankMachineScreen electrolyzer(GtTankMachineMenu menu, Inventory inventory, Component title) {
        return new GtTankMachineScreen(menu, inventory, title, GtMachine.INDUSTRIAL_ELECTROLYZER);
    }

    public static GtTankMachineScreen centrifuge(GtTankMachineMenu menu, Inventory inventory, Component title) {
        return new GtTankMachineScreen(menu, inventory, title, GtMachine.INDUSTRIAL_CENTRIFUGE);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (machine == GtMachine.INDUSTRIAL_CENTRIFUGE) {
            graphics.drawString(font, Component.translatable("container.craft.gt.centrifuge_1"), 110, 4, 0x404040,
                    false);
            graphics.drawString(font, Component.translatable("container.craft.gt.centrifuge_2"), 110, 12, 0x404040,
                    false);
        } else {
            graphics.drawString(font, title, 8, 4, 0x404040, false);
        }
        Component bottom = menu.isMissingEnergy() ? Component.translatable("container.craft.gt.insufficient_energy")
                : playerInventoryTitle;
        graphics.drawString(font, bottom, 8, imageHeight - 94, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int scale = menu.getProgressScale();
        if (scale <= 0) {
            return;
        }
        if (machine == GtMachine.INDUSTRIAL_CENTRIFUGE) {
            graphics.blit(texture, leftPos + 83, topPos + 33 - scale, 193, 33 - scale, 10, scale);
            graphics.blit(texture, leftPos + 78 - scale, topPos + 38, 188 - scale, 38, scale, 10);
            graphics.blit(texture, leftPos + 83, topPos + 53, 193, 53, 10, scale);
            graphics.blit(texture, leftPos + 98, topPos + 38, 208, 38, scale, 10);
        } else {
            graphics.blit(texture, leftPos + 73, topPos + 44 - scale, 183, 44 - scale, 30, scale);
        }
    }

    /** Electrolyzer: the upward bar; centrifuge: the four arrows around the input slot. */
    @Override
    protected List<RecipeArea> getRecipeAreas() {
        if (machine == GtMachine.INDUSTRIAL_CENTRIFUGE) {
            return List.of(new RecipeArea(83, 23, 10, 10, RecipeCategories.GT_CENTRIFUGE),
                    new RecipeArea(68, 38, 10, 10, RecipeCategories.GT_CENTRIFUGE),
                    new RecipeArea(83, 53, 10, 10, RecipeCategories.GT_CENTRIFUGE),
                    new RecipeArea(98, 38, 10, 10, RecipeCategories.GT_CENTRIFUGE));
        }
        return List.of(new RecipeArea(73, 34, 30, 10, RecipeCategories.GT_ELECTROLYZER));
    }
}
