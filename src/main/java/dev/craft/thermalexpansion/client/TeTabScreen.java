package dev.craft.thermalexpansion.client;

import java.util.ArrayList;
import java.util.List;

import dev.craft.thermalexpansion.TeRedstoneControl;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

/**
 * CoFH {@code GuiAdv}: the sliding tabs (right: energy, redstone, configuration; left: information, tutorial), their
 * tooltips and clicks, the open tab remembered for the next GUI ({@code SessionVars}), and the liquid gauges. The
 * redstone buttons are menu buttons 0–2 (ignored, low, high) in every TE menu.
 */
public abstract class TeTabScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    public static final int BUTTON_REDSTONE_IGNORED = 0;
    public static final int BUTTON_REDSTONE_LOW = 1;
    public static final int BUTTON_REDSTONE_HIGH = 2;
    protected static final int SCALE_ENERGY = 42;
    protected static final int SCALE_SPEED = 16;
    protected static final int SCALE_LIQUID = 60;
    @Nullable
    private static Class<?> openedLeftTab;
    @Nullable
    private static Class<?> openedRightTab;

    private final List<TeTab> leftTabs = new ArrayList<>();
    private final List<TeTab> rightTabs = new ArrayList<>();

    protected TeTabScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    protected void addTab(TeTab tab) {
        if (tab.leftSide) {
            leftTabs.add(tab);
            if (tab.getClass() == openedLeftTab) {
                tab.setFullyOpen();
            }
        } else {
            rightTabs.add(tab);
            if (tab.getClass() == openedRightTab) {
                tab.setFullyOpen();
            }
        }
    }

    /** The block entity behind the redstone tab, {@code null} while not loaded on the client. */
    @Nullable
    protected abstract TeRedstoneControl redstoneControl();

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        TeTab tab = tabAt(mouseX - leftPos, mouseY - topPos);
        Component tooltip = tab != null ? tab.getTooltip() : null;
        if (tooltip != null) {
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        } else if (tab == null) {
            renderTooltip(graphics, mouseX, mouseY);
            if (menu.getCarried().isEmpty()) {
                renderGaugeTooltips(graphics, mouseX, mouseY, mouseX - leftPos, mouseY - topPos);
            }
        }
    }

    /** {@code drawTooltips}: tooltips of the gauges; {@code x}/{@code y} relative to the GUI. */
    protected void renderGaugeTooltips(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int y = 4;
        for (TeTab tab : leftTabs) {
            tab.update();
            tab.shiftX = 0;
            tab.shiftY = y;
            tab.draw(graphics, font, 0, y);
            y += tab.currentHeight;
        }
        y = 4;
        for (TeTab tab : rightTabs) {
            tab.update();
            tab.shiftX = imageWidth;
            tab.shiftY = y;
            tab.draw(graphics, font, imageWidth, y);
            y += tab.currentHeight;
        }
    }

    @Nullable
    private TeTab tabAt(int x, int y) {
        for (TeTab tab : leftTabs) {
            if (tab.intersects(x, y)) {
                return tab;
            }
        }
        for (TeTab tab : rightTabs) {
            if (tab.intersects(x, y)) {
                return tab;
            }
        }
        return null;
    }

    /** A click outside the tabs ({@code x}/{@code y} relative to the GUI); true if it was used. */
    protected boolean clickBackground(int x, int y, int button) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        TeTab tab = tabAt((int) mouseX - leftPos, (int) mouseY - topPos);
        if (tab == null) {
            if (clickBackground((int) mouseX - leftPos, (int) mouseY - topPos, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!tab.mouseClicked((int) mouseX - leftPos - tab.shiftX, (int) mouseY - topPos - tab.shiftY, button)) {
            for (TeTab other : tab.leftSide ? leftTabs : rightTabs) {
                if (other != tab && other.open) {
                    other.open = false;
                }
            }
            tab.open = !tab.open;
            Class<?> opened = tab.open ? tab.getClass() : null;
            if (tab.leftSide) {
                openedLeftTab = opened;
            } else {
                openedRightTab = opened;
            }
        }
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return tabAt((int) mouseX - left, (int) mouseY - top) == null
                && super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    /** A menu button with the click sound at {@code pitch}. */
    protected void pressButton(int id, float pitch) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        }
    }

    /** A menu button without sound. */
    protected void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    protected static String format(float value) {
        return String.format("%.1f", value);
    }

    /** {@code getScaledLiquid}: a full gauge from the capacity on. */
    protected static int scaledLiquid(int amount, int capacity) {
        return amount >= capacity ? SCALE_LIQUID : amount * SCALE_LIQUID / capacity;
    }

    /** {@code drawLiquid}: the still texture of {@code fluid}, tiled in 16 px pieces over the area. */
    protected void drawLiquid(GuiGraphics graphics, Fluid fluid, int x, int y, int width, int height) {
        if (fluid == Fluids.EMPTY || minecraft == null) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture());
        int tint = extensions.getTintColor();
        float red = (tint >> 16 & 0xFF) / 255.0F;
        float green = (tint >> 8 & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        graphics.enableScissor(x, y, x + width, y + height);
        for (int dx = 0; dx < width; dx += 16) {
            for (int dy = 0; dy < height; dy += 16) {
                graphics.blit(x + dx, y + dy, 0, 16, 16, sprite, red, green, blue, 1.0F);
            }
        }
        graphics.disableScissor();
    }

    /** {@code TabRedstone}. */
    protected final class RedstoneTab extends TeTab {
        public RedstoneTab() {
            super(false, 0xD0230A);
            maxHeight = 92;
            maxWidth = 112;
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            drawBackground(graphics, x, y);
            if (isFullyOpened()) {
                drawPanel(graphics, x + 24, y + 16, 64, 24);
            }
            drawIcon(graphics, 2, x + 2, y + 2);
            TeRedstoneControl control = redstoneControl();
            if (!isFullyOpened() || control == null) {
                return;
            }
            graphics.drawString(font, Component.translatable("gui.craft.te.redstone_control"), x + 20, y + 6,
                    HEADER_COLOR);
            graphics.drawString(font, Component.translatable("gui.craft.te.control_status").append(":"), x + 16,
                    y + 42, SUBHEADER_COLOR);
            String state;
            if (control.getRedstoneDisable()) {
                drawIcon(graphics, 80, x + 28, y + 20);
                drawIcon(graphics, 65, x + 48, y + 20);
                drawIcon(graphics, 66, x + 68, y + 20);
                state = "gui.craft.te.redstone_state_ignored";
            } else if (control.getRedstoneState()) {
                drawIcon(graphics, 64, x + 28, y + 20);
                drawIcon(graphics, 65, x + 48, y + 20);
                drawIcon(graphics, 82, x + 68, y + 20);
                state = "gui.craft.te.redstone_state_high";
            } else {
                drawIcon(graphics, 64, x + 28, y + 20);
                drawIcon(graphics, 81, x + 48, y + 20);
                drawIcon(graphics, 66, x + 68, y + 20);
                state = "gui.craft.te.redstone_state_low";
            }
            graphics.drawString(font, Component.translatable(control.getRedstoneDisable()
                    ? "gui.craft.te.redstone_control_off" : "gui.craft.te.redstone_control_on"), x + 24, y + 54,
                    TEXT_COLOR, false);
            graphics.drawString(font, Component.translatable("gui.craft.te.signal_required").append(":"), x + 16,
                    y + 66, SUBHEADER_COLOR);
            graphics.drawString(font, Component.translatable(state), x + 24, y + 78, TEXT_COLOR, false);
        }

        @Override
        public Component getTooltip() {
            TeRedstoneControl control = redstoneControl();
            if (isFullyOpened() || control == null) {
                return null;
            }
            if (control.getRedstoneDisable()) {
                return Component.translatable("gui.craft.te.redstone_control_off");
            }
            return Component.translatable("gui.craft.te.redstone_control_on").append(", ").append(
                    Component.translatable(control.getRedstoneState() ? "gui.craft.te.redstone_state_high"
                            : "gui.craft.te.redstone_state_low"));
        }

        @Override
        public boolean mouseClicked(int x, int y, int button) {
            TeRedstoneControl control = redstoneControl();
            if (!isFullyOpened() || control == null) {
                return false;
            }
            if (x < 24 || x > 88 || y < 16 || y > 40) {
                return false;
            }
            if (y >= 20 && y < 36) {
                boolean disable = control.getRedstoneDisable();
                boolean state = control.getRedstoneState();
                if (x >= 28 && x < 44 && !disable) {
                    pressButton(BUTTON_REDSTONE_IGNORED, 0.4F);
                } else if (x >= 48 && x < 64 && (disable || state)) {
                    pressButton(BUTTON_REDSTONE_LOW, 0.6F);
                } else if (x >= 68 && x < 84 && (disable || !state)) {
                    pressButton(BUTTON_REDSTONE_HIGH, 0.8F);
                }
            }
            return true;
        }
    }

    /** {@code TabInfo} and {@code TabTutorial}: a left tab with wrapped white text. */
    protected static final class TextTab extends TeTab {
        private final int icon;
        private final String header;
        private final Component text;
        private List<FormattedCharSequence> lines = List.of();

        public TextTab(boolean left, int color, int icon, String header, Component text) {
            super(left, color);
            this.icon = icon;
            this.header = header;
            this.text = text;
        }

        /** {@code TabInfo}. */
        public static TextTab info(Component text) {
            return new TextTab(true, 0x555555, 0, "gui.craft.te.information", text);
        }

        /** {@code TabTutorial}. */
        public static TextTab tutorial(Component text) {
            return new TextTab(true, 0x5A09BB, 1, "gui.craft.te.tutorial", text);
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            if (lines.isEmpty()) {
                lines = font.split(text, maxWidth - 8);
                maxHeight = 22 + 4 + lines.size() * font.lineHeight;
            }
            drawBackground(graphics, x, y);
            drawIcon(graphics, icon, x + 4 - currentWidth, y + 3);
            if (isFullyOpened()) {
                graphics.drawString(font, Component.translatable(header), x - currentWidth + 22, y + 6,
                        HEADER_COLOR);
                int lineY = y + 20;
                for (FormattedCharSequence line : lines) {
                    graphics.drawString(font, line, x + 8 - currentWidth, lineY, 0xFFFFFF, false);
                    lineY += font.lineHeight;
                }
            }
        }

        @Override
        public Component getTooltip() {
            return isFullyOpened() ? null : Component.translatable(header);
        }
    }
}
