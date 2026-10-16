package dev.craft.forestry.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.forestry.client.ledger.PowerLedger;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import dev.craft.forestry.menu.FabricatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Forestry {@code GuiFabricator}: liquid glass reservoir, heat bar and melting point marker; the power ledger (the
 * fabricator never sets an error and has no hints).
 */
public class FabricatorScreen extends ForestryScreen<FabricatorMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/fabricator.png");

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND, 211);
        ledgers.add(new PowerLedger(menu::getEnergyStored, menu::getMaxEnergyStored, menu::getMaxEnergyReceived));
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return List.of(new RecipeArea(122, 39, 14, 13, RecipeCategories.FABRICATOR));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTick, mouseX, mouseY);
        drawLiquid(graphics, menu.getFluid(), menu.getFluidAmount(), FabricatorBlockEntity.TANK_CAPACITY, 26, 48, 16);
        int heatScaled = menu.getHeat() * 52 / FabricatorBlockEntity.MAX_HEAT;
        if (heatScaled > 0) {
            graphics.blit(BACKGROUND, leftPos + 55, topPos + 17 + 52 - heatScaled, 192, 52 - heatScaled, 4,
                    heatScaled);
        }
        int meltingPointScaled = menu.getMeltingPoint() * 52 / FabricatorBlockEntity.MAX_HEAT;
        if (meltingPointScaled > 0) {
            graphics.blit(BACKGROUND, leftPos + 52, topPos + 15 + 52 - meltingPointScaled, 196, 0, 10, 5);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && isHovering(26, 48, 16, 16, mouseX, mouseY)) {
            liquidTooltip(graphics, menu.getFluid(), menu.getFluidAmount(), mouseX, mouseY);
        }
    }
}
