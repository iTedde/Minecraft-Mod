package dev.craft.mffs.client;

import dev.craft.mffs.block.ProjectorBlockEntity;
import dev.craft.mffs.menu.ProjectorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiProjector} (176 × 186): name field, fill bar of the link, frames on the slots the module uses, access type
 * button (icons 0/16/32/48 at y 80 of {@code items.png}), switch mode button, "PowerLink" FE or "No Link/OOR".
 */
public class ProjectorScreen extends MffsMachineScreen<ProjectorMenu> {
    private static final int ACCESS_X = 92;
    private static final int ACCESS_Y = 56;

    public ProjectorScreen(ProjectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "projector", 176, 186, new int[] {10, 5, 141, 19}, 155, 5);
    }

    private void frame(GuiGraphics graphics, int x, int y) {
        graphics.blit(background, leftPos + x, topPos + y, 177, 143, 16, 16, 256, 256);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        int fill = 79 * menu.getCapacity() / 100;
        graphics.blit(background, leftPos + 8, topPos + 91, 176, 0, fill + 1, 79, 256, 256);
        int type = menu.getModuleType();
        if (type != 0) {
            if (type != 7) {
                frame(graphics, 119, 63);
            }
            if (type != 4 && type != 2) {
                frame(graphics, 155, 63);
            }
            if (type == 1 || type == 2 || type == 6 || type == 7 || type == 8) {
                frame(graphics, 137, 28);
                frame(graphics, 137, 62);
                frame(graphics, 154, 45);
                frame(graphics, 120, 45);
            }
            if (menu.hasCamouflage()) {
                frame(graphics, 137, 45);
            }
        }
        icon(graphics, ACCESS_X, ACCESS_Y, menu.getAccessType() * 16, 80);
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, ACCESS_X, ACCESS_Y, ACCESS_X + 15, ACCESS_Y + 15)) {
            send(ProjectorBlockEntity.EVENT_ACCESS_TYPE, "");
            return true;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, deviceName(), 12, 9, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.projector"), 12, 24, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.type_mode"), 34, 44, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.power_link"), 34, 66, TEXT);
        if (menu.hasPowerSource()) {
            text(graphics, String.valueOf(menu.getLinkPower()), 30, 80, TEXT);
        } else {
            text(graphics, Component.translatable("gui.craft.mffs.no_link_oor"), 30, 80, TEXT);
        }
    }
}
