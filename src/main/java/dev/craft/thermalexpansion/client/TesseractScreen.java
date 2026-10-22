package dev.craft.thermalexpansion.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import dev.craft.thermalexpansion.TeFluids;
import dev.craft.thermalexpansion.TeRedstoneControl;
import dev.craft.thermalexpansion.transport.TesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractMenu;
import dev.craft.thermalexpansion.transport.TesseractPayloads;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/**
 * {@code GuiTeleport}: frequency field with set/disable, name field with save/delete, and the list of saved names of
 * this tesseract's category (10 lines; the name of the current frequency is underlaid with molten ender). Clicking a
 * name fills both fields; typing a known frequency fills the name. Tabs: redstone and configuration (access, mode).
 */
public class TesseractScreen extends TeTabScreen<TesseractMenu> {
    private static final ResourceLocation TEXTURE = TeTab.texture("tesseract");
    private static final int LIST_X = 8;
    private static final int LIST_Y = 58;
    private static final int LIST_WIDTH = 128;
    private static final int LINE_HEIGHT = 10;
    private static final int DISPLAY_LINES = 10;

    @Nullable
    private EditBox frequencyBox;
    @Nullable
    private EditBox nameBox;
    private List<String> lines = new ArrayList<>();
    private Map<String, String> frequenciesByName = new HashMap<>();
    private int startLine;
    private int selectedLine = -1;
    private int namesVersion = -1;

    public TesseractScreen(TesseractMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 166;
        addTab(new RedstoneTab());
        addTab(new ConfigTab());
        addTab(TextTab.info(Component.translatable("gui.craft.te.info.tesseract")));
        addTab(TextTab.tutorial(Component.translatable("gui.craft.te.tutorial.redstone").append("\n\n")
                .append(Component.translatable("gui.craft.te.tutorial.tesseract"))));
    }

    @Nullable
    private TesseractBlockEntity tesseract() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof TesseractBlockEntity tesseract
                ? tesseract : null;
    }

    @Nullable
    @Override
    protected TeRedstoneControl redstoneControl() {
        return tesseract();
    }

    @Override
    protected void init() {
        super.init();
        String frequency = frequencyBox != null ? frequencyBox.getValue() : initialFrequency();
        String name = nameBox != null ? nameBox.getValue() : "";
        nameBox = new EditBox(font, leftPos + 12, topPos + 44, 128, 12, Component.empty());
        nameBox.setMaxLength(TesseractBlockEntity.MAX_NAME_LENGTH);
        nameBox.setBordered(false);
        nameBox.setValue(name);
        frequencyBox = new EditBox(font, leftPos + 106, topPos + 28, 26, 12, Component.empty());
        frequencyBox.setMaxLength(3);
        frequencyBox.setBordered(false);
        frequencyBox.setFilter(text -> text.chars().allMatch(Character::isDigit));
        frequencyBox.setValue(frequency);
        frequencyBox.setResponder(text -> {
            String known = TesseractPayloads.clientNames().get(text);
            if (known != null && nameBox != null) {
                nameBox.setValue(known);
            }
        });
        addRenderableWidget(frequencyBox);
        addRenderableWidget(nameBox);
        setInitialFocus(frequencyBox);
        namesVersion = -1;
    }

    private String initialFrequency() {
        TesseractBlockEntity tesseract = tesseract();
        return tesseract != null && tesseract.getFrequency() != -1 ? String.valueOf(tesseract.getFrequency()) : "";
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (namesVersion != TesseractPayloads.clientNamesVersion()) {
            namesVersion = TesseractPayloads.clientNamesVersion();
            updateNames();
        }
    }

    /** {@code updateNames}. */
    private void updateNames() {
        Map<String, String> names = TesseractPayloads.clientNames();
        lines = new ArrayList<>(names.values());
        frequenciesByName = new HashMap<>();
        names.forEach((frequency, name) -> frequenciesByName.put(name, frequency));
        selectedLine = -1;
        startLine = Math.max(0, Math.min(startLine, lines.size() - DISPLAY_LINES));
        TesseractBlockEntity tesseract = tesseract();
        if (tesseract == null || nameBox == null || frequencyBox == null) {
            return;
        }
        String current = names.get(String.valueOf(tesseract.getFrequency()));
        if (current != null) {
            nameBox.setValue(current);
            frequencyBox.setValue(frequenciesByName.get(current));
        }
        for (int i = 0; i < lines.size(); i++) {
            String name = lines.get(i);
            if (name.equals(nameBox.getValue())
                    && String.valueOf(tesseract.getFrequency()).equals(frequenciesByName.get(name))) {
                selectedLine = i;
            }
        }
    }

    private boolean canSet() {
        return frequencyBox != null && !frequencyBox.getValue().isEmpty();
    }

    private boolean canDisable() {
        TesseractBlockEntity tesseract = tesseract();
        return tesseract != null && tesseract.getFrequency() != -1;
    }

    private boolean canAddEntry() {
        if (nameBox == null || frequencyBox == null || nameBox.getValue().isEmpty()
                || frequencyBox.getValue().isEmpty()) {
            return false;
        }
        String frequency = frequenciesByName.get(nameBox.getValue());
        return frequency == null || frequency.equals(frequencyBox.getValue());
    }

    private boolean canRemoveEntry() {
        if (nameBox == null || frequencyBox == null || nameBox.getValue().isEmpty()
                || frequencyBox.getValue().isEmpty()) {
            return false;
        }
        return frequencyBox.getValue().equals(frequenciesByName.get(nameBox.getValue()));
    }

    private boolean canScrollUp() {
        return startLine != 0;
    }

    private boolean canScrollDown() {
        return lines.size() > DISPLAY_LINES && startLine < lines.size() - DISPLAY_LINES;
    }

    private static boolean in(int x, int y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        int mx = mouseX - x;
        int my = mouseY - y;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        if (canSet()) {
            graphics.blit(TEXTURE, x + 131, y + 18, 208, in(mx, my, 131, 18, 20, 20) ? 212 : 192, 20, 20);
        }
        if (canDisable()) {
            graphics.blit(TEXTURE, x + 151, y + 18, 228, in(mx, my, 151, 18, 20, 20) ? 212 : 192, 20, 20);
        }
        if (canAddEntry()) {
            graphics.blit(TEXTURE, x + 139, y + 40, 208, in(mx, my, 139, 40, 16, 16) ? 144 : 128, 16, 16);
        }
        if (canRemoveEntry()) {
            graphics.blit(TEXTURE, x + 155, y + 40, 224, in(mx, my, 155, 40, 16, 16) ? 144 : 128, 16, 16);
        }
        if (canScrollUp()) {
            graphics.blit(TEXTURE, x + 147, y + 65, 208, in(mx, my, 147, 65, 16, 16) ? 80 : 64, 16, 16);
        }
        if (canScrollDown()) {
            graphics.blit(TEXTURE, x + 147, y + 137, 224, in(mx, my, 147, 137, 16, 16) ? 80 : 64, 16, 16);
        }
        int row = selectedLine - startLine;
        if (canDisable() && selectedLine >= 0 && row >= 0 && row < DISPLAY_LINES) {
            drawLiquid(graphics, TeFluids.MOLTEN_ENDER.get(), x + LIST_X, y + LIST_Y + row * LINE_HEIGHT, LIST_WIDTH,
                    LINE_HEIGHT);
        }
        for (int i = 0; i < DISPLAY_LINES && startLine + i < lines.size(); i++) {
            graphics.drawString(font, font.plainSubstrByWidth(lines.get(startLine + i), LIST_WIDTH - 4),
                    x + LIST_X + 2, y + LIST_Y + i * LINE_HEIGHT + 1, 0xE0E0E0, false);
        }
    }

    @Override
    protected void renderTitles(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        TesseractBlockEntity tesseract = tesseract();
        Component status = tesseract == null || tesseract.getFrequency() == -1
                ? Component.translatable("gui.craft.te.tesseract.inactive")
                : Component.translatable("gui.craft.te.tesseract.frequency", tesseract.getFrequency());
        graphics.drawString(font, status, 8, 28, 0x404040, false);
    }

    @Override
    protected void renderGaugeTooltips(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        String key = null;
        if (in(x, y, 131, 18, 20, 20) && canSet()) {
            key = "gui.craft.te.tesseract.set";
        } else if (in(x, y, 151, 18, 20, 20) && canDisable()) {
            key = "gui.craft.te.tesseract.disable";
        } else if (in(x, y, 139, 40, 16, 16) && canAddEntry()) {
            key = "gui.craft.te.tesseract.save";
        } else if (in(x, y, 155, 40, 16, 16) && canRemoveEntry()) {
            key = "gui.craft.te.tesseract.delete";
        }
        if (key != null) {
            graphics.renderTooltip(font, Component.translatable(key), mouseX, mouseY);
        }
    }

    @Override
    protected boolean clickBackground(int x, int y, int button) {
        TesseractBlockEntity tesseract = tesseract();
        if (tesseract == null || nameBox == null || frequencyBox == null) {
            return false;
        }
        if (in(x, y, LIST_X, LIST_Y, LIST_WIDTH, DISPLAY_LINES * LINE_HEIGHT)) {
            int line = startLine + (y - LIST_Y) / LINE_HEIGHT;
            if (line < lines.size()) {
                selectedLine = line;
                String name = lines.get(line);
                if (!name.equalsIgnoreCase(nameBox.getValue())) {
                    nameBox.setValue(name);
                    String frequency = frequenciesByName.get(name);
                    if (frequency != null) {
                        frequencyBox.setValue(frequency);
                    }
                }
            }
            return true;
        }
        if (in(x, y, 131, 18, 20, 20) && canSet()) {
            setFrequency();
        } else if (in(x, y, 151, 18, 20, 20) && canDisable()) {
            TesseractPayloads.configure(menu.getPos(), -1, tesseract.getMode(), tesseract.getAccess());
            click(0.6F);
        } else if (in(x, y, 139, 40, 16, 16) && canAddEntry()) {
            saveEntry();
        } else if (in(x, y, 155, 40, 16, 16) && canRemoveEntry()) {
            TesseractPayloads.entry(menu.getPos(), Integer.parseInt(frequencyBox.getValue()), nameBox.getValue(),
                    true);
            selectedLine = -1;
            click(0.5F);
        } else if (in(x, y, 147, 65, 16, 16)) {
            scroll(-1);
        } else if (in(x, y, 147, 137, 16, 16)) {
            scroll(1);
        } else {
            return false;
        }
        return true;
    }

    private void setFrequency() {
        TesseractBlockEntity tesseract = tesseract();
        if (tesseract != null && frequencyBox != null) {
            TesseractPayloads.configure(menu.getPos(), Integer.parseInt(frequencyBox.getValue()), tesseract.getMode(),
                    tesseract.getAccess());
            click(0.8F);
        }
    }

    private void saveEntry() {
        if (frequencyBox != null && nameBox != null) {
            TesseractPayloads.entry(menu.getPos(), Integer.parseInt(frequencyBox.getValue()), nameBox.getValue(),
                    false);
            click(0.7F);
        }
    }

    private void scroll(int lines) {
        startLine = Math.max(0, Math.min(startLine + lines, this.lines.size() - DISPLAY_LINES));
    }

    private void click(float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (in((int) mouseX - leftPos, (int) mouseY - topPos, LIST_X, LIST_Y, LIST_WIDTH,
                DISPLAY_LINES * LINE_HEIGHT) && scrollY != 0) {
            scroll(scrollY > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || frequencyBox == null || nameBox == null) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        boolean enter = keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
        if (frequencyBox.isFocused()) {
            if (enter) {
                if (canSet()) {
                    setFrequency();
                }
                return true;
            }
            // GuiTeleport: the inventory key closes the GUI only while the frequency field has the focus.
            return frequencyBox.keyPressed(keyCode, scanCode, modifiers)
                    || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (nameBox.isFocused()) {
            if (enter) {
                if (canAddEntry()) {
                    saveEntry();
                }
                return true;
            }
            return nameBox.keyPressed(keyCode, scanCode, modifiers) || nameBox.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** {@code TabTeleportConfig}: access (owner only; changing it disables the tesseract) and mode. */
    private final class ConfigTab extends TeTab {
        ConfigTab() {
            super(false, 0x089E4C);
            maxHeight = 92;
            maxWidth = 100;
        }

        private boolean isOwner(TesseractBlockEntity tesseract) {
            return minecraft != null && minecraft.player != null && tesseract.isOwner(minecraft.player);
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            drawBackground(graphics, x, y);
            if (isFullyOpened()) {
                drawPanel(graphics, x + 18, y + 16, 64, 24);
            }
            drawIcon(graphics, 5, x + 2, y + 2);
            TesseractBlockEntity tesseract = tesseract();
            if (!isFullyOpened() || tesseract == null) {
                return;
            }
            graphics.drawString(font, Component.translatable("gui.craft.te.configuration"), x + 20, y + 6,
                    HEADER_COLOR);
            graphics.drawString(font, Component.translatable("gui.craft.te.access").append(":"), x + 8, y + 42,
                    SUBHEADER_COLOR);
            String access;
            if (tesseract.isPublic()) {
                drawIcon(graphics, 67, x + 24, y + 20);
                access = "gui.craft.te.access_public";
            } else if (tesseract.isFriends()) {
                drawIcon(graphics, 68, x + 24, y + 20);
                access = "gui.craft.te.access_friends";
            } else {
                drawIcon(graphics, 69, x + 24, y + 20);
                access = "gui.craft.te.access_private";
            }
            graphics.drawString(font, Component.translatable(access), x + 16, y + 54, TEXT_COLOR, false);
            if (!isOwner(tesseract)) {
                drawIcon(graphics, 63, x + 24, y + 20);
            }
            graphics.drawString(font, Component.translatable("gui.craft.te.mode").append(":"), x + 8, y + 66,
                    SUBHEADER_COLOR);
            String mode = switch (tesseract.getMode()) {
                case TesseractBlockEntity.MODE_SEND -> "gui.craft.te.mode_send";
                case TesseractBlockEntity.MODE_RECEIVE -> "gui.craft.te.mode_receive";
                default -> "gui.craft.te.mode_both";
            };
            drawIcon(graphics, 70 + tesseract.getMode(), x + 60, y + 20);
            graphics.drawString(font, Component.translatable(mode), x + 16, y + 78, TEXT_COLOR, false);
        }

        @Nullable
        @Override
        public Component getTooltip() {
            return isFullyOpened() ? null : Component.translatable("gui.craft.te.configuration");
        }

        @Override
        public boolean mouseClicked(int x, int y, int button) {
            TesseractBlockEntity tesseract = tesseract();
            if (!isFullyOpened() || tesseract == null) {
                return false;
            }
            if (x < 18 || x > 82 || y < 16 || y > 40) {
                return false;
            }
            if (in(x, y, 24, 20, 16, 16) && isOwner(tesseract) && button <= 1) {
                // +2 / −2, wrapped to 0–2: public and private alternate, friends is never reached from them.
                int access = tesseract.getAccess() + (button == 0 ? 2 : -2);
                access = access > 2 ? 0 : access < 0 ? 2 : access;
                TesseractPayloads.configure(menu.getPos(), -1, tesseract.getMode(), (byte) access);
                click(button == 0 ? 0.8F : 0.6F);
            } else if (in(x, y, 60, 20, 16, 16) && button <= 1) {
                int mode = tesseract.getMode() + (button == 0 ? 1 : -1);
                mode = mode > 2 ? 0 : mode < 0 ? 2 : mode;
                TesseractPayloads.configure(menu.getPos(), tesseract.getFrequency(), (byte) mode,
                        tesseract.getAccess());
                click(button == 0 ? 0.8F : 0.6F);
            }
            return true;
        }
    }
}
