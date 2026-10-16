package dev.craft.forestry.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.forestry.client.ledger.ErrorLedger;
import dev.craft.forestry.client.ledger.HintLedger;
import dev.craft.forestry.client.ledger.PowerLedger;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.menu.CarpenterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

/** Forestry {@code GuiCarpenter}: resource tank with scale, the crafting progress bar; error, power and hint ledgers. */
public class CarpenterScreen extends ForestryScreen<CarpenterMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/carpenter.png");

    public CarpenterScreen(CarpenterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, BACKGROUND, 215);
        ledgers.add(new ErrorLedger(menu::getErrorState));
        ledgers.add(new PowerLedger(menu::getEnergyStored, menu::getMaxEnergyStored, menu::getMaxEnergyReceived));
        ledgers.add(new HintLedger(HintLedger.CARPENTER));
    }

    @Override
    protected List<RecipeArea> getRecipeAreas() {
        return List.of(new RecipeArea(108, 65, 11, 8, RecipeCategories.CARPENTER));
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
