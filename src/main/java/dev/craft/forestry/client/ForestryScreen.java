package dev.craft.forestry.client;

import java.util.List;
import java.util.function.Consumer;

import dev.craft.core.client.ExtraAreas;
import dev.craft.core.client.RecipeArea;
import dev.craft.forestry.client.ledger.LedgerManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Forestry {@code GuiForestry}: the background texture, the title centred at y 6 and liquid gauges drawn like
 * {@code LiquidTankSlot} (16 px wide icon pieces stacked from the bottom), and the ledgers at the right edge.
 */
public abstract class ForestryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
        implements ExtraAreas {
    protected static final int TITLE_COLOR = 0x404040;

    /** Subclasses add their ledgers in the constructor, in {@code GuiForestry.initLedgers} order. */
    protected final LedgerManager ledgers = new LedgerManager();

    private final ResourceLocation background;

    protected ForestryScreen(T menu, Inventory inventory, Component title, ResourceLocation background,
                             int imageHeight) {
        super(menu, inventory, title);
        this.background = background;
        this.imageWidth = 176;
        this.imageHeight = imageHeight;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null && menu.getCarried().isEmpty()) {
            RecipeArea.renderTooltip(getRecipeAreas(), graphics, font, leftPos, topPos, mouseX, mouseY);
        }
        ledgers.drawTooltip(graphics, font, mouseX, mouseY, leftPos, topPos, imageWidth);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && RecipeArea.click(getRecipeAreas(), leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && ledgers.click(mouseX, mouseY, leftPos, topPos, imageWidth)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        return super.hasClickedOutside(mouseX, mouseY, left, top, button)
                && !ledgers.contains(mouseX, mouseY, leftPos, topPos, imageWidth);
    }

    @Override
    public void addExtraAreas(Consumer<Rect2i> consumer) {
        ledgers.addAreas(consumer, leftPos, topPos, imageWidth);
    }

    /** Areas that open the recipe viewer; none by default. */
    protected List<RecipeArea> getRecipeAreas() {
        return List.of();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        ledgers.draw(graphics, font, leftPos, topPos, imageWidth);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, TITLE_COLOR, false);
    }

    /** Liquid {@code amount} of {@code capacity} in a gauge of {@code height} px at (x, y) relative to the GUI. */
    protected void drawLiquid(GuiGraphics graphics, Fluid fluid, int amount, int capacity, int x, int y, int height) {
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture());
        int tint = extensions.getTintColor();
        float red = (tint >> 16 & 0xFF) / 255.0F;
        float green = (tint >> 8 & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        int scaled = Math.min(height, amount * height / capacity);
        int start = 0;
        while (scaled > 0) {
            int piece = Math.min(16, scaled);
            scaled -= piece;
            // The top part of the icon, placed bottom-up.
            int top = topPos + y + height - piece - start;
            graphics.enableScissor(leftPos + x, top, leftPos + x + 16, top + piece);
            graphics.blit(leftPos + x, top, 0, 16, 16, sprite, red, green, blue, 1.0F);
            graphics.disableScissor();
            start += 16;
        }
    }

    protected boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width && mouseY >= topPos + y
                && mouseY < topPos + y + height;
    }

    /** {@code LiquidTankSlot.getTooltip}: the liquid name and the amount. */
    protected void liquidTooltip(GuiGraphics graphics, Fluid fluid, int amount, int mouseX, int mouseY) {
        if (fluid == Fluids.EMPTY || amount <= 0) {
            return;
        }
        graphics.renderComponentTooltip(font, List.of(fluid.getFluidType().getDescription(),
                Component.literal(amount + " mB")), mouseX, mouseY);
    }
}
