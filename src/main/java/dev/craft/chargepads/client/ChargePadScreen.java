package dev.craft.chargepads.client;

import dev.craft.Craft;
import dev.craft.chargepads.PadTier;
import dev.craft.chargepads.menu.ChargePadMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiChargePad} on {@code GUIChargePad.png} (176 × 166, spec 47 §9): title, "EU  : …", "Max.: …", small
 * "@ …/t"; energy bar (12 × 26) at (10, 20); missing slots covered with the lock (176, 0); small "Field" and
 * "Projector" labels above the module slots.
 */
public class ChargePadScreen extends AbstractContainerScreen<ChargePadMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/chargepads/charge_pad.png");
    private static final int TEXT = 0x404040;

    public ChargePadScreen(ChargePadMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int storage = menu.getStorage();
        if (storage > 0 && menu.getCapacity() > 0) {
            int height = 1 + (int) (26.0F * storage / menu.getCapacity());
            graphics.blit(TEXTURE, leftPos + 10, topPos + 46 - height, 194, 27 - height, 12, height);
        }
        PadTier tier = menu.getTier();
        if (!tier.hasModuleSlots()) {
            lock(graphics, 133, 28);
            lock(graphics, 133, 57);
        }
        if (tier.getUpgradeSlots() < 3) {
            lock(graphics, 80, 57);
            if (tier.getUpgradeSlots() < 2) {
                lock(graphics, 60, 57);
            }
        }
    }

    private void lock(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, leftPos + x, topPos + y, 176, 0, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, TEXT, false);
        graphics.drawString(font, "EU  : " + menu.getStorage(), 30, 19, TEXT, false);
        graphics.drawString(font, "Max.: " + menu.getCapacity(), 30, 28, TEXT, false);
        graphics.pose().pushPose();
        graphics.pose().translate(30.0F, 38.0F, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, String.format("@ %d/t", menu.getMaxInput()), 0, 0, TEXT, false);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(142.0F, 22.0F, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        Component field = Component.translatable("gui.craft.chargepads.field");
        Component projector = Component.translatable("gui.craft.chargepads.projector");
        graphics.drawString(font, field, -font.width(field) / 2, 0, TEXT, false);
        graphics.drawString(font, projector, -font.width(projector) / 2, 58, TEXT, false);
        graphics.pose().popPose();
    }
}
