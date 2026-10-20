package dev.craft.powerplants.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/** Steam turbine GUI (texture Railcraft {@code gui_engine_steam.png}): steam tank, buffer bar, output in EU/t. */
public class SteamTurbineScreen extends AbstractContainerScreen<SteamTurbineMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/steam_turbine.png");
    private static final int TANK_X = 71;
    private static final int TANK_Y = 23;
    private static final int TANK_HEIGHT = 47;
    private static final int BUFFER_X = 94;
    private static final int BUFFER_Y = 25;
    private static final int BUFFER_HEIGHT = 43;

    public SteamTurbineScreen(SteamTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        if (x >= TANK_X && x < TANK_X + 16 && y >= TANK_Y && y < TANK_Y + TANK_HEIGHT) {
            graphics.renderComponentTooltip(font, List.of(PpFluids.STEAM_TYPE.get().getDescription(),
                    Component.literal(menu.getSteam() + " / " + menu.getTankCapacity() + " mB")),
                    mouseX, mouseY);
        } else if (x >= BUFFER_X && x < BUFFER_X + 6 && y >= BUFFER_Y && y < BUFFER_Y + BUFFER_HEIGHT) {
            graphics.renderTooltip(font, Component.literal(menu.getBuffer() + " / "
                    + menu.getBufferCapacity() + " EU"), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        graphics.drawString(font, menu.getProduction() + " EU/t", 108, 42, 0x404040, false);
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
        int scaled = Math.min(menu.getSteam() * TANK_HEIGHT / menu.getTankCapacity(), TANK_HEIGHT);
        if (scaled > 0) {
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(PpFluids.STEAM.get());
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(extensions.getStillTexture());
            int bottom = y + TANK_Y + TANK_HEIGHT;
            graphics.enableScissor(x + TANK_X, bottom - scaled, x + TANK_X + 16, bottom);
            for (int start = 0; start < scaled; start += 16) {
                graphics.blit(x + TANK_X, bottom - 16 - start, 0, 16, 16, sprite);
            }
            graphics.disableScissor();
        }
        graphics.blit(TEXTURE, x + TANK_X, y + TANK_Y, 176, 0, 16, TANK_HEIGHT);
        int buffer = Math.min(menu.getBuffer() * BUFFER_HEIGHT / menu.getBufferCapacity(), BUFFER_HEIGHT);
        if (buffer > 0) {
            graphics.blit(TEXTURE, x + BUFFER_X, y + BUFFER_Y + BUFFER_HEIGHT - buffer, 176,
                    47 + BUFFER_HEIGHT - buffer, 6, buffer);
        }
    }
}
