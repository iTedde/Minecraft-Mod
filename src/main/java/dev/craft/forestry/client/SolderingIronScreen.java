package dev.craft.forestry.client;

import dev.craft.Craft;
import dev.craft.forestry.circuit.Circuit;
import dev.craft.forestry.menu.SolderingIronMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Forestry {@code GuiSolderingIron}: layout name with "<"/">" buttons and, next to each tube slot, the circuit it
 * solders with its limit or "(No effect)".
 */
public class SolderingIronScreen extends ForestryScreen<SolderingIronMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/solder.png");
    private static final int SCREEN_COLOR = 0xFFFFFF;

    public SolderingIronScreen(SolderingIronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND, 205);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), button -> click(0))
                .bounds(leftPos + 12, topPos + 10, 12, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> click(1))
                .bounds(leftPos + 130, topPos + 10, 12, 18).build());
    }

    private void click(int id) {
        if (menu.clickMenuButton(minecraft.player, id)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component layout = Component.translatable(menu.getLayout().getNameKey());
        graphics.drawString(font, layout, 8 + (138 - font.width(layout)) / 2, 16, SCREEN_COLOR, false);
        for (int i = 0; i < 4; i++) {
            Circuit circuit = menu.getCircuit(menu.getSolderingInventory()
                    .getItem(SolderingIronMenu.SLOT_INGREDIENT_1 + i));
            Component description = circuit == null
                    ? Component.literal("(").append(Component.translatable("gui.craft.forestry.no_effect")).append(")")
                    : Component.translatable(circuit.getNameKey()).append(" (" + circuit.getLimit() + ")");
            graphics.drawString(font, description, 32, 36 + i * 20, SCREEN_COLOR, false);
        }
    }
}
