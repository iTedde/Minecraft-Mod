package dev.craft.omnitools.client;

import java.util.Optional;

import dev.craft.Craft;
import dev.craft.omnitools.LexiconGroups;
import dev.craft.omnitools.LexiconMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * {@code GuiLexicon} (spec 50 §1.3), 176 × 186: arrows at (55, 23) and (105, 23), convert button at (76, 53); lit
 * sprites from (176/192/208, 16), hovered from (…, 32). The group (tag) is shown at y 78.
 */
public class LexiconScreen extends AbstractContainerScreen<LexiconMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/omnitools/lexicon.png");

    public LexiconScreen(LexiconMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 186;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    private boolean multiple() {
        return !menu.getSlot(0).getItem().isEmpty() && !menu.preview().isEmpty();
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        if (menu.getSlot(0).getItem().isEmpty()) {
            return;
        }
        if (menu.canConvert()) {
            graphics.blit(TEXTURE, leftPos + 76, topPos + 53, 208, inside(x, y, 76, 53, 24, 16) ? 32 : 16, 24, 16);
        }
        if (multiple()) {
            graphics.blit(TEXTURE, leftPos + 55, topPos + 23, 176, inside(x, y, 55, 23, 16, 16) ? 32 : 16, 16, 16);
            graphics.blit(TEXTURE, leftPos + 105, topPos + 23, 192, inside(x, y, 105, 23, 16, 16) ? 32 : 16, 16, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        ItemStack preview = menu.preview();
        Optional<TagKey<Item>> group = LexiconGroups.groupOf(menu.getSlot(0).getItem());
        if (!preview.isEmpty() && group.isPresent()) {
            String name = group.get().location().toString();
            graphics.drawString(font, name, (imageWidth - font.width(name)) / 2, 78, 0xFFFFFF, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        ItemStack input = menu.getSlot(0).getItem();
        if (inside(x, y, 76, 53, 24, 16) && !input.isEmpty() && !menu.preview().isEmpty()
                && input.is(menu.preview().getItem())) {
            graphics.renderTooltip(font, Component.translatable("gui.craft.forge_lexicon.no_conversion"), mouseX,
                    mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        if (!menu.getSlot(0).getItem().isEmpty() && minecraft != null && minecraft.gameMode != null) {
            int id = -1;
            if (multiple() && inside(x, y, 55, 23, 16, 16)) {
                id = LexiconMenu.BUTTON_PREVIOUS;
            } else if (multiple() && inside(x, y, 105, 23, 16, 16)) {
                id = LexiconMenu.BUTTON_NEXT;
            } else if (menu.canConvert() && inside(x, y, 76, 53, 24, 16)) {
                id = LexiconMenu.BUTTON_CONVERT;
            }
            if (id >= 0) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
