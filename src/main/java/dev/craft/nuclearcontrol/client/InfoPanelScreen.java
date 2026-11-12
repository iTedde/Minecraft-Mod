package dev.craft.nuclearcontrol.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.PanelCard;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.menu.InfoPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

/**
 * {@code GuiInfoPanel}: 176 × 190. Right: show-labels switch, "T" for the colours (with colour upgrade), "..." for
 * the card settings (text card). Middle: one check box per display line of the card type; top: title text box,
 * saved with Enter or when the GUI closes. Controls are rebuilt when the card or the colour upgrade changes.
 */
public class InfoPanelScreen extends AbstractContainerScreen<InfoPanelMenu> {
    static final ResourceLocation TEXTURE = NuclearControlClient.gui("information_panel");

    private final List<CheckBox> checkBoxes = new ArrayList<>();
    @Nullable
    private EditBox titleBox;
    private ItemStack shownCard = ItemStack.EMPTY;
    private boolean shownColored;
    private boolean modified;

    private record CheckBox(PanelSetting setting, int x, int y, int width, int height) {
    }

    public InfoPanelScreen(InfoPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 190;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Nullable
    InfoPanelBlockEntity panel() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof InfoPanelBlockEntity panel ? panel : null;
    }

    @Override
    protected void init() {
        super.init();
        rebuildControls(true);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        InfoPanelBlockEntity panel = panel();
        ItemStack card = menu.getSlot(InfoPanelBlockEntity.SLOT_CARD).getItem();
        if (panel != null && (!ItemStack.isSameItem(card, shownCard) || panel.isColored() != shownColored)) {
            rebuildControls(false);
        }
    }

    /** {@code initControls}. */
    private void rebuildControls(boolean keepTitle) {
        InfoPanelBlockEntity panel = panel();
        String typed = keepTitle && modified && titleBox != null ? titleBox.getValue() : null;
        clearWidgets();
        checkBoxes.clear();
        titleBox = null;
        if (panel == null) {
            return;
        }
        ItemStack card = menu.getSlot(InfoPanelBlockEntity.SLOT_CARD).getItem();
        shownCard = card.copy();
        shownColored = panel.isColored();
        int right = leftPos + imageWidth - 25;
        int delta = 0;
        if (shownColored) {
            addRenderableWidget(Button.builder(Component.literal("T"),
                    button -> minecraft.setScreen(new ScreenColorScreen(this, panel)))
                    .bounds(right, topPos + 55, 18, 12).build());
            delta = 15;
        }
        if (card.getItem() instanceof PanelCard source) {
            if (source.hasAdvancedSettings()) {
                addRenderableWidget(Button.builder(Component.literal("..."),
                        button -> minecraft.setScreen(new TextCardScreen(this, menu.getPos(), CardData.of(card))))
                        .bounds(right, topPos + 55 + delta, 18, 12).build());
            }
            int row = 0;
            for (PanelSetting setting : source.settings()) {
                Component label = Component.translatable(setting.translationKey());
                checkBoxes.add(new CheckBox(setting, 32, 40 + (font.lineHeight + 1) * row, font.width(label) + 8,
                        font.lineHeight + 1));
                row++;
            }
            titleBox = new EditBox(font, leftPos + 7, topPos + 16, 162, 18, Component.empty());
            titleBox.setMaxLength(32);
            titleBox.setValue(typed != null ? typed : CardData.of(card).getTitle());
            titleBox.setResponder(value -> modified = true);
            addRenderableWidget(titleBox);
        } else {
            modified = false;
        }
    }

    private void saveTitle() {
        if (titleBox != null && modified && menu.getSlot(InfoPanelBlockEntity.SLOT_CARD).getItem()
                .getItem() instanceof PanelCard) {
            NcPayloads.sendText(menu.getPos(), NcPayloads.TITLE, 0, titleBox.getValue());
            modified = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        InfoPanelBlockEntity panel = panel();
        if (panel != null && button == 0) {
            double x = mouseX - leftPos;
            double y = mouseY - topPos;
            if (x >= imageWidth - 25 && x < imageWidth - 7 && y >= 42 && y < 51) {
                boolean show = !panel.getShowLabels();
                panel.setShowLabels(show);
                NcPayloads.send(menu.getPos(), NcPayloads.SHOW_LABELS, show ? 1 : 0);
                return true;
            }
            for (CheckBox box : checkBoxes) {
                if (x >= box.x() && x < box.x() + box.width() && y >= box.y() && y < box.y() + box.height()) {
                    int settings = panel.getDisplaySettings() ^ box.setting().bit();
                    panel.setDisplaySettings(settings);
                    NcPayloads.send(menu.getPos(), NcPayloads.DISPLAY_SETTINGS, settings);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (titleBox != null && titleBox.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveTitle();
                return true;
            }
            return titleBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        saveTitle();
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        InfoPanelBlockEntity panel = panel();
        if (panel == null) {
            return;
        }
        graphics.blit(TEXTURE, leftPos + imageWidth - 25, topPos + 43, 176, panel.getShowLabels() ? 12 : 21, 18, 9);
        int settings = panel.getDisplaySettings();
        for (CheckBox box : checkBoxes) {
            boolean checked = (settings & box.setting().bit()) != 0;
            graphics.blit(TEXTURE, leftPos + box.x(), topPos + box.y() + 1, 176, checked ? 6 : 0, 6, 6);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        for (CheckBox box : checkBoxes) {
            graphics.drawString(font, Component.translatable(box.setting().translationKey()), box.x() + 8, box.y(),
                    0x404040, false);
        }
    }
}
