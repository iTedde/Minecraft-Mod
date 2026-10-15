package dev.craft.forestry.client;

import dev.craft.Craft;
import dev.craft.forestry.engine.ElectricalEngineBlockEntity;
import dev.craft.forestry.menu.ElectricalEngineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Forestry {@code GuiEngineTin}: the EU meter coloured by fill level and the circuit board socket. */
public class ElectricalEngineScreen extends ForestryScreen<ElectricalEngineMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/electrical_engine.png");

    public ElectricalEngineScreen(ElectricalEngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND, 166);
    }

    /** {@code getStorageScaled}. */
    private int storageScaled(int scale) {
        return menu.getEuStored() * scale / (menu.getEuStorage() + ElectricalEngineBlockEntity.EU_MAX_ACCEPT - 1);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        int height = storageScaled(46);
        graphics.blit(BACKGROUND, leftPos + 74, topPos + 25 + 46 - height, 176 + 4 * level(storageScaled(100)),
                46 - height, 4, height);
    }

    /** {@code rateLevel}: empty, low, medium, high, maximum. */
    private static int level(int scaled) {
        if (scaled < 5) {
            return 0;
        }
        if (scaled < 30) {
            return 1;
        }
        if (scaled < 60) {
            return 2;
        }
        return scaled < 90 ? 3 : 4;
    }

    /** {@code SocketSlot.getTooltip}: the board (vanilla slot tooltip) or "Empty socket". */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && hoveredSlot instanceof ElectricalEngineMenu.SocketSlot
                && !hoveredSlot.hasItem()) {
            graphics.renderTooltip(font, Component.translatable("gui.craft.forestry.empty_socket"), mouseX, mouseY);
        }
    }
}
