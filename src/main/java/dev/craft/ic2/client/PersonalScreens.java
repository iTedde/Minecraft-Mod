package dev.craft.ic2.client;

import dev.craft.ic2.personal.EnergyOMatBlockEntity;
import dev.craft.ic2.personal.EnergyOMatMenu;
import dev.craft.ic2.personal.PersonalSafeMenu;
import dev.craft.ic2.personal.TradeOMatMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiPersonalChest}, {@code GuiTradeOMat*} and {@code GuiEnergyOMat*}. */
public final class PersonalScreens {
    private static final int TEXT = 4210752;
    private static final int RED = 16733525;

    private PersonalScreens() {
    }

    /** 176 × 222, title at (8, 6). */
    public static class Safe extends MachineScreen<PersonalSafeMenu> {
        public Safe(PersonalSafeMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, "personal_safe");
            imageHeight = 222;
            inventoryLabelY = imageHeight - 94;
        }

        @Override
        protected void init() {
            super.init();
            titleLabelX = 8;
        }

        @Override
        protected void renderGauges(GuiGraphics graphics, int x, int y) {
        }
    }

    public static class TradeOMat extends MachineScreen<TradeOMatMenu> {
        public TradeOMat(TradeOMatMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, menu.isOpen() ? "trade_o_mat_open" : "trade_o_mat_closed");
        }

        @Override
        protected void renderGauges(GuiGraphics graphics, int x, int y) {
        }

        @Override
        protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            super.renderLabels(graphics, mouseX, mouseY);
            if (menu.isOpen()) {
                graphics.drawString(font, Component.translatable("gui.craft.trade_o_mat.total_trades0"), 112, 20, TEXT,
                        false);
                graphics.drawString(font, Component.translatable("gui.craft.trade_o_mat.total_trades1"), 112, 28, TEXT,
                        false);
                graphics.drawString(font, String.valueOf(menu.getValue()), 112, 36, TEXT, false);
            } else {
                graphics.drawString(font, Component.translatable("gui.craft.o_mat.want"), 12, 23, TEXT, false);
                graphics.drawString(font, Component.translatable("gui.craft.o_mat.offer"), 12, 42, TEXT, false);
                graphics.drawString(font, Component.translatable("gui.craft.trade_o_mat.stock"), 12, 60, TEXT, false);
                graphics.drawString(font, String.valueOf(menu.getValue()), 50, 60, menu.getValue() > 0 ? TEXT : RED,
                        false);
            }
        }
    }

    public static class EnergyOMat extends MachineScreen<EnergyOMatMenu> {
        public EnergyOMat(EnergyOMatMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title, menu.isOpen() ? "energy_o_mat_open" : "energy_o_mat_closed");
        }

        @Override
        protected void init() {
            super.init();
            if (menu.isOpen()) {
                button(EnergyOMatBlockEntity.BUTTON_MINUS_1000, 112, 43, "-1000");
                button(EnergyOMatBlockEntity.BUTTON_MINUS_100, 140, 43, "-100");
                button(EnergyOMatBlockEntity.BUTTON_PLUS_1000, 112, 55, "+1000");
                button(EnergyOMatBlockEntity.BUTTON_PLUS_100, 140, 55, "+100");
            }
        }

        private void button(int id, int x, int y, String label) {
            addRenderableWidget(Button.builder(Component.literal(label), b -> minecraft.gameMode
                            .handleInventoryButtonClick(menu.containerId, id))
                    .bounds(leftPos + x, topPos + y, 28, 12).build());
        }

        @Override
        protected void renderGauges(GuiGraphics graphics, int x, int y) {
        }

        @Override
        protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            super.renderLabels(graphics, mouseX, mouseY);
            if (menu.isOpen()) {
                graphics.drawString(font, Component.translatable("gui.craft.o_mat.offer"), 112, 20, TEXT, false);
                graphics.drawString(font, menu.getEuOffer() + " EU", 112, 28, TEXT, false);
            } else {
                graphics.drawString(font, Component.translatable("gui.craft.o_mat.want"), 12, 23, TEXT, false);
                graphics.drawString(font, Component.translatable("gui.craft.o_mat.offer"), 12, 42, TEXT, false);
                graphics.drawString(font, menu.getEuOffer() + " EU", 50, 42, TEXT, false);
                graphics.drawString(font, Component.translatable("gui.craft.energy_o_mat.paid_for", menu.getPaidFor()),
                        12, 60, TEXT, false);
            }
        }
    }
}
