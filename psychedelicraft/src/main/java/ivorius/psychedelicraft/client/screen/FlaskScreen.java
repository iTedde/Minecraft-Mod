/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Created by lukas on 26.10.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class FlaskScreen<T extends FlaskBlockEntity> extends AbstractFluidContraptionScreen<FluidContraptionScreenHandler<T>> {
    protected final ResourceLocation background;

    public FlaskScreen(FluidContraptionScreenHandler<T> handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.background = Psychedelicraft.id("textures/gui/" + BlockEntityType.getKey(handler.getBlockEntity().getType()).getPath() + ".png");
    }

    @Override
    protected void renderBg(GuiGraphics context, float tickDelta, int mouseX, int mouseY) {
        int baseX = (width - imageWidth) / 2;
        int baseY = (height - imageHeight) / 2;
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.blit(background, baseX + 30, baseY + 20, 0, imageHeight, 110, 50);

        drawTanks(context, baseX, baseY);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        context.blit(background, baseX, baseY, 0, 0, imageWidth, imageHeight);
        drawAdditionalInfo(context, baseX, baseY, tickDelta);

        RenderSystem.setShaderColor(1, 1, 1, 1);

        float inputProgress = menu.getBlockEntity().inputSlot.getProgress();
        if (inputProgress > 0 && inputProgress < 1) {
            int width = (int)(45 * inputProgress);
            context.blit(background, baseX + 20 + width, baseY + 40, 176 + width, 0, 45 - width, 16);
        }
        float outputProgress = menu.getBlockEntity().outputSlot.getProgress();
        if (outputProgress > 0 && outputProgress < 1) {
            context.blit(background, baseX + 68, baseY + 60, 176, 17, (int)(53 * outputProgress), 20);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    protected void drawAdditionalInfo(GuiGraphics context, int baseX, int baseY, float tickDelta) {

    }

    protected void drawTanks(GuiGraphics context, int baseX, int baseY) {
        drawTank(context, getTank(), baseX + 48, baseY + 59, 64, 27);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float tickDelta) {
        super.render(context, mouseX, mouseY, tickDelta);
        int baseX = (width - imageWidth) / 2;
        int baseY = (height - imageHeight) / 2;
        drawTankTooltips(context, mouseX, mouseY, baseX, baseY);
    }

    protected void drawTankTooltips(GuiGraphics context, int mouseX, int mouseY, int baseX, int baseY) {
        drawTankTooltip(context, getTank(), baseX + 65, baseY + 33, 40, 30, mouseX, mouseY, getAdditionalTankText());
    }

    protected List<Component> getAdditionalTankText() {
        return List.of();
    }
}
