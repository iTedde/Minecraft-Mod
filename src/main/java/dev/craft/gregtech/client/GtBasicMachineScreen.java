package dev.craft.gregtech.client;

import dev.craft.Craft;
import dev.craft.gregtech.machine.GtMachineBlock;
import dev.craft.gregtech.menu.GtBasicMachineMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** {@code GT_GUIContainer_BasicMachine}: title top left, three toggle buttons and the progress arrow. */
public class GtBasicMachineScreen extends AbstractContainerScreen<GtBasicMachineMenu> {
    private static final int[] BUTTON_X = {8, 26, 44};
    private static final int BUTTON_Y = 63;

    private final ResourceLocation texture;

    public GtBasicMachineScreen(GtBasicMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        String gui = "bender";
        if (Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.getBlockState(menu.getPos()).getBlock() instanceof GtMachineBlock block) {
            gui = block.getMachine().getGui();
        }
        this.texture = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/gt/" + gui + ".png");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 4, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.isOutputEnabled()) {
            graphics.blit(texture, x + 7, y + 62, 176, 18, 18, 18);
        }
        if (menu.isItemTransfer()) {
            graphics.blit(texture, x + 25, y + 62, 176, 36, 18, 18);
        }
        if (menu.isSeparatedInputs()) {
            graphics.blit(texture, x + 43, y + 62, 176, 54, 18, 18);
        }
        int progress = menu.getProgressBar();
        if (progress > 0) {
            graphics.blit(texture, x + 78, y + 24, 176, 0, progress, 18);
        }
    }

    /** The holo slots of the original act as toggle buttons. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            for (int i = 0; i < BUTTON_X.length; i++) {
                double dx = mouseX - leftPos - BUTTON_X[i];
                double dy = mouseY - topPos - BUTTON_Y;
                if (dx >= 0 && dx < 16 && dy >= 0 && dy < 16 && minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
