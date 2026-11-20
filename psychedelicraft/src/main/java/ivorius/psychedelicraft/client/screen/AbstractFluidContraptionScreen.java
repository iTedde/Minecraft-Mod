package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.screen.FluidContraptionScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

/**
 * Created by lukas on 13.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public abstract class AbstractFluidContraptionScreen<T extends FluidContraptionScreenHandler<?>> extends AbstractContainerScreen<T> {

    protected AbstractFluidContraptionScreen(T handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        titleLabelY = 10;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        titleLabelX = 83 - font.width(title) / 2;
        super.render(context, mouseX, mouseY, delta);
        renderTooltip(context, mouseX, mouseY);
    }

    protected Resovoir getTank() {
        return menu.getTank();
    }

    public static void drawTank(GuiGraphics context, Resovoir tank, int x, int y, int width, int height) {
        if (tank.getContents().isEmpty()) {
            return;
        }

        float fluidHeight = Mth.clamp((float) tank.getContents().amount() / (float) tank.getCapacity(), 0, 1);
        int fluidHeightPixels = Mth.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(tank.getContents());

        float[] color = appearance.rgba();

        RenderUtil.drawRepeatingSprite(context, appearance.sprite(), x, y - fluidHeightPixels, width, fluidHeightPixels, color[0], color[1], color[2], color[3]);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawTank(GuiGraphics context, ItemFluids fluids, int capacity, int x, int y, int width, int height) {
        if (fluids.isEmpty()) {
            return;
        }

        float fluidHeight = Mth.clamp((float) fluids.amount() / (float) capacity, 0, 1);
        int fluidHeightPixels = Mth.ceil(fluidHeight * height);

        FluidBoxRenderer.FluidAppearance appearance = FluidBoxRenderer.FluidAppearance.of(fluids);

        float[] color = appearance.rgba();

        RenderUtil.drawRepeatingSprite(context, appearance.sprite(), x, y + height - fluidHeightPixels, width, fluidHeightPixels, color[0], color[1], color[2], color[3]);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void drawTankTooltip(GuiGraphics context, Resovoir tank, int x, int y, int width, int height, int mouseX, int mouseY, List<Component> details) {
        drawTankTooltip(context, tank.getContents(), tank.getCapacity(), x, y, width, height, mouseX, mouseY, details);
    }

    public static void drawTankTooltip(GuiGraphics context, ItemFluids fluids, long capacity, int x, int y, int width, int height, int mouseX, int mouseY, List<Component> details) {
        if (rectContains(mouseX, mouseY, x, y, width, height)) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(fluids.getName());
            if (!fluids.isEmpty()) {
                if (capacity <= 0) {
                    tooltip.add(Component.translatable("psychedelicraft.container.amount", FluidVolumes.format(fluids.amount())).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("psychedelicraft.container.levels", FluidVolumes.format(fluids.amount()), FluidVolumes.format(capacity)).withStyle(ChatFormatting.GRAY));
                }
            }
            fluids.addToTooltip(Item.TooltipContext.EMPTY, tooltip::add, TooltipFlag.NORMAL);
            tooltip.addAll(details);
            context.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    public static boolean rectContains(int x, int y, int rectX, int rectY, int width, int height) {
        return x >= rectX
            && y >= rectY
            && x < rectX + width
            && y < rectY + height;
    }
}
