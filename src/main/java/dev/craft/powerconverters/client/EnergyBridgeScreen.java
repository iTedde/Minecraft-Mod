package dev.craft.powerconverters.client;

import dev.craft.Craft;
import dev.craft.powerconverters.PowerSystem;
import dev.craft.powerconverters.menu.EnergyBridgeMenu;
import dev.craft.powerconverters.power.EnergyBridgeBlockEntity.SideData;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiEnergyBridge}: one line per side (system and voltage, IN/OUT, rate) on a coloured row and the charge bar.
 * The texts are hard-coded English in the original.
 */
public class EnergyBridgeScreen extends AbstractContainerScreen<EnergyBridgeMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/energy_bridge.png");
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DARK_GRAY = 0x404040;
    private static final int BAR_COLOR = 0xFF00A5FF;

    public EnergyBridgeScreen(EnergyBridgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 195;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        for (Direction side : Direction.values()) {
            int i = side.get3DDataValue();
            SideData data = menu.getDataForSide(side);
            int v;
            if (!isPresent(data)) {
                v = 221;
            } else if (!data.connected()) {
                v = 208;
            } else {
                v = data.outputRate() == 0 ? 234 : 195;
            }
            graphics.blit(BACKGROUND, leftPos + 7, topPos + 15 + 12 * i, 0, v, 162, 12);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, DARK_GRAY, false);
        if (menu.isInputLimited()) {
            graphics.drawString(font, "INPUT LIMITED", 98, 6, WHITE, false);
        } else {
            graphics.drawString(font, "OUTPUT LIMITED", 90, 6, WHITE, false);
        }
        for (Direction side : Direction.values()) {
            int y = 6 + 12 * (side.get3DDataValue() + 1);
            graphics.drawString(font, side.name(), 10, y, WHITE, false);
            SideData data = menu.getDataForSide(side);
            if (isPresent(data)) {
                PowerSystem system = PowerSystem.byId(data.powerSystem());
                String name = system.getAbbreviation();
                if (system.getVoltageNames() != null) {
                    name = name + " " + system.getVoltageNames()[data.voltageIndex()];
                }
                graphics.drawString(font, name, 49, y, WHITE, false);
                graphics.drawString(font, data.consumer() ? "IN" : "OUT", 92, y, WHITE, false);
                graphics.drawString(font, outputRate(data, system), 119, y, WHITE, false);
            } else {
                graphics.drawString(font, "<NONE>", 49, y, WHITE, false);
            }
        }
        graphics.drawString(font, "% CHG", 10, 90, WHITE, false);
        graphics.fill(46, 89, 46 + menu.getEnergyScaled(), 97, BAR_COLOR);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, DARK_GRAY, false);
    }

    private static boolean isPresent(SideData data) {
        return (data.consumer() || data.producer()) && PowerSystem.byId(data.powerSystem()) != null;
    }

    /** {@code getOutputRateString}; formatted with the default locale like the original. */
    private static String outputRate(SideData data, PowerSystem system) {
        if (!data.connected()) {
            return "NO LINK";
        }
        int rate = data.outputRate();
        if (rate > 1000) {
            return String.format(Locale.getDefault(), "%.1f %s%s", rate / 1000.0, "k", system.getUnit());
        }
        return rate + " " + system.getUnit();
    }
}
