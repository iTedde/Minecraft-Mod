package dev.craft.buildcraft.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.menu.EngineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * BuildCraft {@code GuiSteamEngine} and {@code GuiCombustionEngine} with the engine ledger of {@code GuiEngine}
 * (opens to the right of the GUI on click and shows output, stored energy and heat).
 */
public class EngineScreen extends AbstractContainerScreen<EngineMenu> {
    private static final ResourceLocation LEDGER = texture("ledger");
    private static final ResourceLocation ICONS = texture("bc_icons");
    private static final int TEXT_COLOR = 0x404040;

    /** BuildCraft {@code SessionVars.openedLedger}: the ledger stays open for the next engine GUI. */
    private static boolean ledgerOpenInSession;

    private final ResourceLocation background;
    private final Ledger ledger = new Ledger();

    public EngineScreen(EngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.background = texture(menu.getEngineType() == EngineType.STIRLING ? "stirling_engine" : "combustion_engine");
        if (ledgerOpenInSession) {
            ledger.setFullyOpen();
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/" + name + ".png");
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null && menu.getCarried().isEmpty()) {
            RecipeArea.renderTooltip(recipeAreas(), graphics, font, leftPos, topPos, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(background, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.getEngineType() == EngineType.STIRLING) {
            int burn = menu.getScaledBurnTime(12);
            if (burn > 0) {
                graphics.blit(background, x + 80, y + 24 + 12 - burn, 176, 12 - burn, 14, burn + 2);
            }
        } else {
            int fuel = menu.getScaledFuel(58);
            if (fuel > 0) {
                displayGauge(graphics, x, y, 19, 104, fuel, menu.getFuelFluid());
            }
            int coolant = menu.getScaledCoolant(58);
            if (coolant > 0) {
                displayGauge(graphics, x, y, 19, 122, coolant, menu.getCoolantFluid());
            }
        }
    }

    /** Fills the gauge bottom-up with 16 px tiles of the fluid texture, then draws the scale over it. */
    private void displayGauge(GuiGraphics graphics, int x, int y, int line, int column, int scaled, Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture());
        int tint = extensions.getTintColor();
        float red = (tint >> 16 & 0xFF) / 255.0F;
        float green = (tint >> 8 & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        int bottom = y + line + 58;
        graphics.enableScissor(x + column, bottom - scaled, x + column + 16, bottom);
        for (int start = 0; start < scaled; start += 16) {
            graphics.blit(x + column, bottom - 16 - start, 0, 16, 16, sprite, red, green, blue, 1.0F);
        }
        graphics.disableScissor();
        graphics.blit(background, x + column, y + line, 176, 0, 16, 60);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ledger.update();
        ledger.draw(graphics, imageWidth, 8);
        graphics.drawString(font, title, titleLabelX, 6, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.craft.inventory"), 8, imageHeight - 96 + 2,
                TEXT_COLOR, false);
        if (ledger.intersects(mouseX, mouseY, leftPos + imageWidth, topPos + 8)) {
            String tooltip = ledger.getTooltip();
            int startX = mouseX - leftPos + 12;
            int startY = mouseY - topPos - 12;
            int width = font.width(tooltip);
            graphics.fillGradient(startX - 3, startY - 3, startX + width + 3, startY + 8 + 3, 0xC0000000,
                    0xC0000000);
            graphics.drawString(font, tooltip, startX, startY, 0xFFFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && RecipeArea.click(recipeAreas(), leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && ledger.intersects((int) mouseX, (int) mouseY, leftPos + imageWidth, topPos + 8)) {
            ledger.toggleOpen();
            return true;
        }
        return handled;
    }

    /** Stirling flame → furnace fuels, combustion fuel gauge → combustion engine fuels. */
    private List<RecipeArea> recipeAreas() {
        return menu.getEngineType() == EngineType.STIRLING
                ? List.of(new RecipeArea(80, 24, 14, 14, RecipeCategories.FUEL))
                : List.of(new RecipeArea(104, 19, 16, 58, RecipeCategories.COMBUSTION_ENGINE));
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return super.hasClickedOutside(mouseX, mouseY, left, top, button)
                && !ledger.intersects((int) mouseX, (int) mouseY, leftPos + imageWidth, topPos + 8);
    }

    /** BuildCraft {@code GuiBuildCraft.Ledger} with the contents of {@code GuiEngine.EngineLedger}. */
    private final class Ledger {
        private static final int OVERLAY_COLOR = 0xD46C1F;
        private static final int HEADER_COLOR = 0xE1C92F;
        private static final int SUBHEADER_COLOR = 0xAAAFB8;
        private static final int MIN_SIZE = 24;
        private static final int MAX_WIDTH = 124;
        private static final int MAX_HEIGHT = 94;

        private boolean open;
        private int width = MIN_SIZE;
        private int height = MIN_SIZE;

        void update() {
            if (open && width < MAX_WIDTH) {
                width += 4;
            } else if (!open && width > MIN_SIZE) {
                width -= 4;
            }
            if (open && height < MAX_HEIGHT) {
                height += 4;
            } else if (!open && height > MIN_SIZE) {
                height -= 4;
            }
        }

        void setFullyOpen() {
            open = true;
            width = MAX_WIDTH;
            height = MAX_HEIGHT;
        }

        void toggleOpen() {
            open = !open;
            ledgerOpenInSession = open;
        }

        boolean intersects(int mouseX, int mouseY, int shiftX, int shiftY) {
            return mouseX >= shiftX && mouseX <= shiftX + width && mouseY >= shiftY && mouseY <= shiftY + height;
        }

        String getTooltip() {
            return menu.getCurrentOutput() + " MJ/t";
        }

        void draw(GuiGraphics graphics, int x, int y) {
            graphics.setColor((OVERLAY_COLOR >> 16 & 0xFF) / 255.0F, (OVERLAY_COLOR >> 8 & 0xFF) / 255.0F,
                    (OVERLAY_COLOR & 0xFF) / 255.0F, 1.0F);
            graphics.blit(LEDGER, x, y, 0, 256 - height, 4, height);
            graphics.blit(LEDGER, x + 4, y, 256 - width + 4, 0, width - 4, 4);
            graphics.blit(LEDGER, x, y, 0, 0, 4, 4);
            graphics.blit(LEDGER, x + 4, y + 4, 256 - width + 4, 256 - height + 4, width - 4, height - 4);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(ICONS, x + 3, y + 4, 0, 0, 16, 16);
            if (width >= MAX_WIDTH) {
                graphics.drawString(font, Component.translatable("gui.craft.buildcraft.energy"), x + 22, y + 8,
                        HEADER_COLOR, true);
                graphics.drawString(font, Component.translatable("gui.craft.buildcraft.current_output")
                        .append(":"), x + 22, y + 20, SUBHEADER_COLOR, true);
                graphics.drawString(font, menu.getCurrentOutput() + " MJ/t", x + 22, y + 32, 0, false);
                graphics.drawString(font, Component.translatable("gui.craft.buildcraft.stored").append(":"),
                        x + 22, y + 44, SUBHEADER_COLOR, true);
                graphics.drawString(font, menu.getEnergy() + " MJ", x + 22, y + 56, 0, false);
                graphics.drawString(font, Component.translatable("gui.craft.buildcraft.heat").append(":"),
                        x + 22, y + 68, SUBHEADER_COLOR, true);
                graphics.drawString(font, menu.getHeat() / 10.0 + " °C", x + 22, y + 80, 0, false);
            }
        }
    }
}
