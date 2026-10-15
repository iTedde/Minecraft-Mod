package dev.craft.ic2.client;

import dev.craft.ic2.menu.ElectricStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** IC2 {@code GuiElectricBlock}: energy display and the redstone mode button. */
public class ElectricStorageScreen extends MachineScreen<ElectricStorageMenu> {
    private static final int TEXT_COLOR = 0x404040;

    public ElectricStorageScreen(ElectricStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "electric_block");
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IconButton(leftPos + 152, topPos + 4, 20, 20, new ItemStack(Items.REDSTONE),
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                ElectricStorageMenu.BUTTON_REDSTONE_MODE);
                    }
                }));
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("container.craft.electric_block.level"), 79, 25, TEXT_COLOR,
                false);
        int energy = Math.min(menu.getEnergy(), menu.getMaxStorage());
        graphics.drawString(font, " " + energy, 110, 35, TEXT_COLOR, false);
        graphics.drawString(font, "/" + menu.getMaxStorage(), 110, 45, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("container.craft.electric_block.output", menu.getOutput()),
                85, 60, TEXT_COLOR, false);
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        if (menu.getEnergy() > 0) {
            int charge = (int) (24.0F * menu.getChargeLevel());
            blit(graphics, x + 79, y + 34, 176, 14, charge + 1, 16);
        }
    }

    /** IC2 {@code GuiIconButton}: a button showing an item. */
    private static final class IconButton extends Button {
        private final ItemStack icon;

        IconButton(int x, int y, int width, int height, ItemStack icon, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.icon = icon;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            graphics.renderItem(icon, getX() + 2, getY() + 1);
        }
    }
}
