package dev.craft.mffs.client;

import dev.craft.Craft;
import dev.craft.mffs.MffsPayloads;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.menu.MffsMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/**
 * Common part of the MFFS GUIs (spec 48 §13): original background, name input (click the name field: name is cleared
 * and typed characters are appended, Backspace deletes, Enter ends), switch mode button with the icons of
 * {@code items.png}.
 */
public abstract class MffsMachineScreen<T extends MffsMachineMenu> extends AbstractContainerScreen<T> {
    public static final ResourceLocation ICONS =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/mffs/items.png");
    protected static final int TEXT = 4210752;

    protected final ResourceLocation background;
    private final int nameX0;
    private final int nameY0;
    private final int nameX1;
    private final int nameY1;
    private final int switchX;
    private final int switchY;
    private boolean editMode;

    protected MffsMachineScreen(T menu, Inventory inventory, Component title, String texture, int width, int height,
                                int[] nameArea, int switchX, int switchY) {
        super(menu, inventory, title);
        this.background = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/mffs/" + texture + ".png");
        this.imageWidth = width;
        this.imageHeight = height;
        this.nameX0 = nameArea[0];
        this.nameY0 = nameArea[1];
        this.nameX1 = nameArea[2];
        this.nameY1 = nameArea[3];
        this.switchX = switchX;
        this.switchY = switchY;
    }

    protected MffsMachineBlockEntity machine() {
        return menu.getMachine(minecraft.player);
    }

    protected String deviceName() {
        MffsMachineBlockEntity machine = machine();
        return machine != null ? machine.getDeviceName() : "";
    }

    protected void send(int key, String value) {
        MffsPayloads.send(menu.getPos(), key, value);
    }

    /** One 16×16 icon of {@code items.png} at GUI position (x, y). */
    protected void icon(GuiGraphics graphics, int x, int y, int u, int v) {
        graphics.blit(ICONS, leftPos + x, topPos + y, u, v, 16, 16, 256, 256);
    }

    protected void text(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    protected void text(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, false);
    }

    protected boolean inside(double mouseX, double mouseY, int x0, int y0, int x1, int y1) {
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        return x >= x0 && y >= y0 && x <= x1 && y <= y1;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        MffsMachineBlockEntity machine = machine();
        if (machine != null) {
            icon(graphics, switchX, switchY, 80 + machine.getSwitchMode() * 16, 112);
        }
        renderMachineBg(graphics);
    }

    protected abstract void renderMachineBg(GuiGraphics graphics);

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (editMode) {
            editMode = false;
        } else if (inside(mouseX, mouseY, nameX0, nameY0, nameX1, nameY1)) {
            send(MffsMachineBlockEntity.EVENT_NAME_CLEAR, "");
            editMode = true;
        }
        if (inside(mouseX, mouseY, switchX, switchY, switchX + 15, switchY + 15)) {
            send(MffsMachineBlockEntity.EVENT_SWITCH_MODE, "");
            return true;
        }
        if (machineClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Further buttons and click areas of the machine. */
    protected boolean machineClicked(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editMode && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                editMode = false;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                send(MffsMachineBlockEntity.EVENT_NAME_BACKSPACE, "");
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editMode) {
            send(MffsMachineBlockEntity.EVENT_NAME_APPEND, String.valueOf(codePoint));
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
}
