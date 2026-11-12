package dev.craft.nuclearcontrol.client;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.item.TextCardItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * {@code GuiCardText}: 226 × 146 with a 10-line text area (one text box per line here) and "Ok". Ok and Escape save
 * all lines and return to the panel GUI.
 */
public class TextCardScreen extends Screen {
    private static final ResourceLocation TEXTURE = NuclearControlClient.gui("text_card");
    private static final int WIDTH = 226;
    private static final int HEIGHT = 146;
    private static final int LINE_HEIGHT = 11;

    private final InfoPanelScreen parent;
    private final BlockPos pos;
    private final String[] lines = new String[TextCardItem.LINES];
    private final EditBox[] boxes = new EditBox[TextCardItem.LINES];

    public TextCardScreen(InfoPanelScreen parent, BlockPos pos, CardData card) {
        super(Component.translatable("item.craft.text_card"));
        this.parent = parent;
        this.pos = pos;
        for (int i = 0; i < lines.length; i++) {
            lines[i] = card.getString("line_" + i);
        }
    }

    @Override
    protected void init() {
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null) {
                lines[i] = boxes[i].getValue();
            }
            EditBox box = new EditBox(font, left + 10, top + 7 + i * LINE_HEIGHT, WIDTH - 20, LINE_HEIGHT,
                    Component.empty());
            box.setBordered(false);
            box.setMaxLength(NcPayloads.MAX_TEXT);
            box.setTextColor(0x404040);
            box.setValue(lines[i]);
            boxes[i] = addRenderableWidget(box);
        }
        setInitialFocus(boxes[0]);
        addRenderableWidget(Button.builder(Component.literal("Ok"), button -> onClose())
                .bounds(left + WIDTH - 60 - 8, top + 120, 60, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i].isFocused()) {
                if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_DOWN) && i + 1 < boxes.length) {
                    setFocused(boxes[i + 1]);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_UP && i > 0) {
                    setFocused(boxes[i - 1]);
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, (width - WIDTH) / 2, (height - HEIGHT) / 2, 0, 0, WIDTH, HEIGHT);
    }

    /** {@code commit}: every line is sent, then back to the panel GUI. */
    @Override
    public void onClose() {
        for (int i = 0; i < boxes.length; i++) {
            NcPayloads.sendText(pos, NcPayloads.TEXT_LINE, i, boxes[i].getValue());
        }
        minecraft.setScreen(parent);
    }
}
