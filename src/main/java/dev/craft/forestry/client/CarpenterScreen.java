package dev.craft.forestry.client;

import dev.craft.Craft;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.menu.CarpenterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

/** Forestry {@code GuiCarpenter}: resource tank with scale and the crafting progress bar. */
public class CarpenterScreen extends ForestryScreen<CarpenterMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/carpenter.png");

    public CarpenterScreen(CarpenterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND, 215);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        if (menu.getFluid() != Fluids.EMPTY && menu.getFluidAmount() > 0) {
            drawLiquid(graphics, menu.getFluid(), menu.getFluidAmount(), CarpenterBlockEntity.TANK_CAPACITY, 150, 17,
                    58);
            graphics.blit(BACKGROUND, leftPos + 150, topPos + 17, 176, 0, 16, 60);
        }
        if (menu.isWorking()) {
            int progressScaled = 16 - menu.getCraftingProgressScaled(16);
            graphics.blit(BACKGROUND, leftPos + 98, topPos + 51 + 16 - progressScaled, 176, 76 - progressScaled, 4,
                    progressScaled);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && isHovering(150, 17, 16, 58, mouseX, mouseY)) {
            liquidTooltip(graphics, menu.getFluid(), menu.getFluidAmount(), mouseX, mouseY);
        }
    }
}
