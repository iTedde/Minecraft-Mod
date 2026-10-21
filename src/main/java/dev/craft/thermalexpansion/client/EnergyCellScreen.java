package dev.craft.thermalexpansion.client;

import dev.craft.thermalexpansion.TeRedstoneControl;
import dev.craft.thermalexpansion.energy.EnergyCellBlockEntity;
import dev.craft.thermalexpansion.energy.EnergyCellMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GuiEnergyCell}: charge gauge, input (receive) and output (send) limits with −/+ buttons: click 5, right click
 * 1, with shift 100 and 10.
 */
public class EnergyCellScreen extends TeTabScreen<EnergyCellMenu> {
    private static final ResourceLocation TEXTURE = TeTab.texture("energy_cell");
    /** Button x of receive −, receive +, send −, send +. */
    private static final int[] BUTTON_X = {28, 44, 118, 134};

    public EnergyCellScreen(EnergyCellMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        addTab(new RedstoneTab());
        addTab(TextTab.info(Component.translatable("gui.craft.te.info.redstone_energy_cell")));
        addTab(TextTab.tutorial(Component.translatable("gui.craft.te.tutorial.redstone")));
    }

    @Nullable
    @Override
    protected TeRedstoneControl redstoneControl() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof EnergyCellBlockEntity cell ? cell : null;
    }

    /** Whether button {@code i} can still change its limit. */
    private boolean enabled(int i) {
        int value = i < 2 ? menu.getReceive() : menu.getSend();
        return i % 2 == 0 ? value > 0 : value < 100;
    }

    private int buttonAt(int x, int y) {
        if (y < 56 || y >= 70) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            if (x >= BUTTON_X[i] && x < BUTTON_X[i] + 14 && enabled(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        int l = (int) ((long) menu.getEnergy() * SCALE_ENERGY / EnergyCellBlockEntity.STORAGE);
        if (l > 0) {
            graphics.blit(TEXTURE, x + 80, y + 18 + SCALE_ENERGY - l, 176, 32 + SCALE_ENERGY - l, 16, l);
        }
        for (int i = 0; i < 4; i++) {
            if (enabled(i)) {
                graphics.blit(TEXTURE, x + BUTTON_X[i], y + 56, 208 + 16 * (i % 2), 144, 16, 16);
            }
        }
        int hovered = buttonAt(mouseX - x, mouseY - y);
        if (hovered >= 0) {
            graphics.blit(TEXTURE, x + BUTTON_X[hovered], y + 56, 208 + 16 * (hovered % 2), 160, 16, 16);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int half = imageWidth / 2;
        Component in = Component.translatable("gui.craft.te.in");
        Component out = Component.translatable("gui.craft.te.out");
        graphics.drawString(font, in, (half - font.width(in)) / 2, 32, 0x404040, false);
        graphics.drawString(font, out, half + (half - font.width(out)) / 2, 32, 0x404040, false);
        int receive = menu.getReceive();
        int send = menu.getSend();
        int xReceive = receive < 10 ? 32 : receive < 100 ? 26 : 20;
        int xSend = send < 10 ? 122 : send < 100 ? 116 : 110;
        graphics.drawString(font, receive + " MJ/t", xReceive, 44, 0x404040, false);
        graphics.drawString(font, send + " MJ/t", xSend, 44, 0x404040, false);
    }

    @Override
    protected void renderGaugeTooltips(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        if (x >= 82 && x < 94 && y >= 19 && y < 59) {
            graphics.renderTooltip(font, Component.literal(menu.getEnergy() + " / " + EnergyCellBlockEntity.STORAGE
                    + " MJ"), mouseX, mouseY);
        }
    }

    @Override
    protected boolean clickBackground(int x, int y, int button) {
        int i = buttonAt(x, y);
        if (i < 0 || minecraft == null) {
            return false;
        }
        boolean shift = Screen.hasShiftDown();
        boolean right = button == 1;
        // Steps 1, 5, 10, 100 (EnergyCellMenu.STEPS); pitch 0.4/0.6/0.8/1.0, +0.1 for "up".
        int step = shift ? (right ? 2 : 3) : (right ? 0 : 1);
        float pitch = (shift ? (right ? 0.8F : 1.0F) : (right ? 0.4F : 0.6F)) + (i % 2 == 1 ? 0.1F : 0.0F);
        sendButton(EnergyCellMenu.BUTTON_LIMIT + 4 * i + step);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, pitch));
        return true;
    }
}
