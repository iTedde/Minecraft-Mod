package dev.craft.mffs.client;

import dev.craft.Craft;
import dev.craft.mffs.block.SecurityStationBlockEntity;
import dev.craft.mffs.item.PersonalIdCardItem;
import dev.craft.mffs.menu.SecurityStationMenu;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * {@code GuiAdvSecurityStation} (256 × 216): rights buttons of the card in the editing slot (sorted by key, 7 per
 * row, icons of {@code AdvSecStationButtons.png}, hover shows the name), validity −/+, name input; no switch button.
 */
public class SecurityStationScreen extends MffsMachineScreen<SecurityStationMenu> {
    private static final ResourceLocation BUTTONS =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/mffs/security_buttons.png");
    private static final int SIZE = 18;

    private SecurityRight hovered;

    public SecurityStationScreen(SecurityStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "security_station", 256, 216, new int[] {120, 4, 250, 18}, -1, 0);
    }

    private static int buttonX(int index) {
        return index % 7 * (SIZE + 2) + 18;
    }

    private static int buttonY(int index) {
        return index / 7 * (SIZE + 2) + 54;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        ItemStack card = menu.getEditCard();
        if (!(card.getItem() instanceof PersonalIdCardItem)) {
            return;
        }
        for (int index = 0; index < SecurityRight.SORTED.size(); index++) {
            SecurityRight right = SecurityRight.SORTED.get(index);
            int texIndex = right.ordinal();
            int u = (texIndex <= 6 ? texIndex : texIndex - 7) * SIZE;
            int v = (PersonalIdCardItem.hasRight(card, right) ? 0 : SIZE) + (texIndex > 6 ? 36 : 0);
            graphics.blit(BUTTONS, leftPos + buttonX(index), topPos + buttonY(index), u, v, SIZE, SIZE, 128, 128);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hovered = null;
        if (menu.getEditCard().getItem() instanceof PersonalIdCardItem) {
            for (int index = 0; index < SecurityRight.SORTED.size(); index++) {
                int x = buttonX(index);
                int y = buttonY(index);
                if (inside(mouseX, mouseY, x, y, x + SIZE, y + SIZE)) {
                    hovered = SecurityRight.SORTED.get(index);
                }
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hovered != null) {
            graphics.renderTooltip(font, Component.translatable(hovered.translationKey()), mouseX, mouseY);
        }
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, 12, 103, 27, 118)) {
            send(SecurityStationBlockEntity.EVENT_VALIDITY_DOWN, "null");
            return true;
        }
        if (inside(mouseX, mouseY, 68, 103, 83, 118)) {
            send(SecurityStationBlockEntity.EVENT_VALIDITY_UP, "null");
            return true;
        }
        if (hovered != null) {
            send(SecurityStationBlockEntity.EVENT_TOGGLE_RIGHT, hovered.name());
            return true;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, Component.translatable("gui.craft.mffs.security_station"), 8, 8, TEXT);
        text(graphics, deviceName(), 125, 8, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.master"), 200, 38, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.rights_allocation"), 52, 35, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.copy"), 109, 106, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.validity"), 31, 106, TEXT);
    }
}
