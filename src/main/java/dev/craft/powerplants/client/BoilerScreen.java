package dev.craft.powerplants.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.boiler.BoilerMenu;
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

/** Boiler GUI (texture after Railcraft {@code gui_boiler_solid.png}, fuel storage slots removed). */
public class BoilerScreen extends AbstractContainerScreen<BoilerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/boiler.png");
    private static final List<RecipeArea> RECIPE_AREAS = List.of(new RecipeArea(62, 22, 14, 14, RecipeCategories.BOILER));
    private static final int TANK_Y = 23;
    private static final int TANK_HEIGHT = 47;
    private static final int WATER_X = 17;
    private static final int STEAM_X = 116;
    private static final int HEAT_X = 40;
    private static final int HEAT_Y = 25;
    private static final int HEAT_HEIGHT = 43;

    public BoilerScreen(BoilerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (y >= TANK_Y && y < TANK_Y + TANK_HEIGHT) {
            if (x >= WATER_X && x < WATER_X + 16) {
                tankTooltip(graphics, Fluids.WATER, menu.getWater(), mouseX, mouseY);
            } else if (x >= STEAM_X && x < STEAM_X + 16) {
                tankTooltip(graphics, PpFluids.STEAM.get(), menu.getSteam(), mouseX, mouseY);
            } else if (x >= HEAT_X && x < HEAT_X + 6) {
                graphics.renderTooltip(font, Component.literal(menu.getTemperature() + " °C"), mouseX, mouseY);
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null && menu.getCarried().isEmpty()) {
            RecipeArea.renderTooltip(RECIPE_AREAS, graphics, font, leftPos, topPos, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && RecipeArea.click(RECIPE_AREAS, leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void tankTooltip(GuiGraphics graphics, Fluid fluid, int amount, int mouseX, int mouseY) {
        graphics.renderComponentTooltip(font, List.of(fluid.getFluidType().getDescription(),
                Component.literal(amount + " / " + BoilerBlockEntity.TANK_CAPACITY + " mB")), mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (!menu.isFormed()) {
            Component incomplete = Component.translatable("gui.craft.structure_incomplete");
            graphics.drawString(font, incomplete, imageWidth - 8 - font.width(incomplete), inventoryLabelY, 0xA00000,
                    false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        drawTank(graphics, x + WATER_X, y + TANK_Y, Fluids.WATER, menu.getWater());
        drawTank(graphics, x + STEAM_X, y + TANK_Y, PpFluids.STEAM.get(), menu.getSteam());
        int heat = (menu.getTemperature() - BoilerBlockEntity.MIN_TEMPERATURE) * HEAT_HEIGHT
                / (BoilerBlockEntity.MAX_TEMPERATURE - BoilerBlockEntity.MIN_TEMPERATURE);
        if (heat > 0) {
            graphics.blit(TEXTURE, x + HEAT_X, y + HEAT_Y + HEAT_HEIGHT - heat, 176, 61 + HEAT_HEIGHT - heat, 6,
                    heat);
        }
        int burn = menu.getScaledBurnTime(12);
        if (burn > 0) {
            graphics.blit(TEXTURE, x + 62, y + 22 + 12 - burn, 176, 47 + 12 - burn, 14, burn + 2);
        }
    }

    /** Fills the tank bottom-up with the fluid's still texture, then draws the scale over it. */
    private void drawTank(GuiGraphics graphics, int left, int top, Fluid fluid, int amount) {
        int scaled = amount * TANK_HEIGHT / BoilerBlockEntity.TANK_CAPACITY;
        if (scaled > 0) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid);
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(extensions.getStillTexture());
            int tint = extensions.getTintColor();
            float red = (tint >> 16 & 0xFF) / 255.0F;
            float green = (tint >> 8 & 0xFF) / 255.0F;
            float blue = (tint & 0xFF) / 255.0F;
            int bottom = top + TANK_HEIGHT;
            graphics.enableScissor(left, bottom - scaled, left + 16, bottom);
            for (int start = 0; start < scaled; start += 16) {
                graphics.blit(left, bottom - 16 - start, 0, 16, 16, sprite, red, green, blue, 1.0F);
            }
            graphics.disableScissor();
        }
        graphics.blit(TEXTURE, left, top, 176, 0, 16, TANK_HEIGHT);
    }
}
