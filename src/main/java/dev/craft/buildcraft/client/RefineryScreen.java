package dev.craft.buildcraft.client;

import dev.craft.Craft;
import dev.craft.buildcraft.factory.RefineryRecipe;
import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.buildcraft.menu.RefineryMenu;
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
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * BuildCraft {@code GuiRefinery}: two filter fields and the recipe result. Clicking a filter field with a filled
 * container on the cursor sets the filter to its liquid.
 */
public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/refinery_filter.png");
    private static final int TEXT_COLOR = 4210752;
    /** Filter 1, filter 2, result. */
    private static final int[][] SLOTS = {{38, 54}, {126, 54}, {82, 54}};

    public RefineryScreen(RefineryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 175;
        imageHeight = 207;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int slot = getSlotAtLocation(mouseX - leftPos, mouseY - topPos);
        if (slot != -1 && menu.getCarried().isEmpty()) {
            Fluid fluid = slotFluid(slot);
            if (fluid != Fluids.EMPTY) {
                graphics.renderTooltip(font, fluid.getFluidType().getDescription(), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (int slot = 0; slot < SLOTS.length; slot++) {
            drawFluid(graphics, slotFluid(slot), leftPos + SLOTS[slot][0], topPos + SLOTS[slot][1]);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, 6, TEXT_COLOR, false);
        graphics.drawString(font, "->", 63, 59, TEXT_COLOR, false);
        graphics.drawString(font, "<-", 106, 59, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.craft.inventory"), 8, imageHeight - 96 + 2,
                TEXT_COLOR, false);
    }

    /** {@code updateSlots}: the filters and the result of the recipe they select. */
    private Fluid slotFluid(int slot) {
        if (slot < 2) {
            return menu.getFilter(slot);
        }
        RefineryRecipe recipe = RefineryRecipe.find(stack(menu.getFilter(0)), stack(menu.getFilter(1)));
        return recipe == null ? Fluids.EMPTY : recipe.result().getFluid();
    }

    private static FluidStack stack(Fluid fluid) {
        return fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, 1000);
    }

    private void drawFluid(GuiGraphics graphics, Fluid fluid, int x, int y) {
        if (fluid == Fluids.EMPTY) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture());
        int tint = extensions.getTintColor();
        graphics.blit(x, y, 0, 16, 16, sprite, (tint >> 16 & 0xFF) / 255.0F, (tint >> 8 & 0xFF) / 255.0F,
                (tint & 0xFF) / 255.0F, 1.0F);
    }

    private static int getSlotAtLocation(double x, double y) {
        for (int i = 0; i < SLOTS.length; i++) {
            if (x >= SLOTS[i][0] && x <= SLOTS[i][0] + 16 && y >= SLOTS[i][1] && y <= SLOTS[i][1] + 16) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        int position = getSlotAtLocation(mouseX - leftPos, mouseY - topPos);
        if (position == 0 || position == 1) {
            if (!LiquidContainers.getLiquidForFilledItem(menu.getCarried()).isEmpty()) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, position);
            }
            return true;
        }
        return result;
    }
}
