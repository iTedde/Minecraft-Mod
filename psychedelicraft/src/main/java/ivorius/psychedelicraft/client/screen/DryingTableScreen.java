/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.screen.DryingTableScreenHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.*;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Updated by Sollace on 3 Jan 2023
 */
public class DryingTableScreen extends AbstractContainerScreen<DryingTableScreenHandler> {
    public static final ResourceLocation TEXTURE = Psychedelicraft.id("textures/gui/drying_table.png");

    public DryingTableScreen(DryingTableScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();
        titleLabelX = 26;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);

        if (menu.getCarried().isEmpty() && (hoveredSlot == null || !hoveredSlot.hasItem())) {
            if (mouseX > leftPos + imageWidth - 30 && mouseX < leftPos + imageWidth && mouseY > topPos && mouseY < topPos + 30) {
                context.renderTooltip(font, Component.translatable("block.psychedelicraft.drying_table.daylight", (int)(menu.getHeatRatio() * 100)), mouseX, mouseY);
            }

            if (mouseX > leftPos + 90 && mouseX < leftPos + 115 && mouseY > topPos + 35 && mouseY < topPos + 50) {
                context.renderTooltip(font, Component.translatable("block.psychedelicraft.drying_table.cooking", (int)(menu.getProgress() * 100)), mouseX, mouseY);
            }
        }
        int timeRemaining = menu.getTimeRemaining();
        if (timeRemaining != 0) {
            context.drawString(this.font, Component.literal(StringUtil.formatTickDuration(timeRemaining, minecraft.level.tickRateManager().tickrate())), leftPos + 90, topPos + 50, CommonColors.GRAY, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;

        context.blit(TEXTURE, centerX, centerY, 0, 0, imageWidth, imageHeight);

        if (menu.getProgress() > 0) {
            context.blit(TEXTURE, centerX + 88, centerY + 34, 176, 59, 25, 16);
        }

        int progress = (int)(menu.getProgress() * 24); //Max 24, progress
        context.blit(TEXTURE, centerX + 88, centerY + 34, 176, 42, progress + 1, 16);

        int heat = (int) (menu.getHeatRatio() * 21); //Max 20, sun
        context.blit(TEXTURE, centerX + 148, centerY + 6 + (21 - heat), 176, 21 + (21 - heat), 21, heat);
    }
}
