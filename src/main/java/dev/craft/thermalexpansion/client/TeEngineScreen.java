package dev.craft.thermalexpansion.client;

import dev.craft.thermalexpansion.TeRedstoneControl;
import dev.craft.thermalexpansion.energy.MagmaticEngineBlockEntity;
import dev.craft.thermalexpansion.energy.SteamEngineBlockEntity;
import dev.craft.thermalexpansion.energy.TeEngineBlockEntity;
import dev.craft.thermalexpansion.energy.TeEngineMenu;
import dev.craft.thermalexpansion.energy.TeEngineType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GuiEngineSteam}/{@code GuiEngineMagmatic}: fuel duration flame, tank gauge (water/lava), the engine and redstone
 * tabs on the right, information and tutorial on the left.
 */
public class TeEngineScreen extends TeTabScreen<TeEngineMenu> {
    private final ResourceLocation texture;
    private final Fluid fluid;
    private final int capacity;

    public TeEngineScreen(TeEngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        boolean steam = menu.getEngineType() == TeEngineType.STEAM;
        texture = TeTab.texture(steam ? "engine_steam" : "engine_magmatic");
        fluid = steam ? Fluids.WATER : Fluids.LAVA;
        capacity = steam ? SteamEngineBlockEntity.CAPACITY : MagmaticEngineBlockEntity.CAPACITY;
        addTab(new EngineTab());
        addTab(new RedstoneTab());
        addTab(TextTab.info(Component.translatable("gui.craft.te.info." + menu.getEngineType().getName())));
        addTab(TextTab.tutorial(Component.translatable("gui.craft.te.tutorial.redstone")));
    }

    @Nullable
    @Override
    protected TeRedstoneControl redstoneControl() {
        return minecraft != null && minecraft.level != null
                && minecraft.level.getBlockEntity(menu.getPos()) instanceof TeEngineBlockEntity engine ? engine : null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight);
        int l = (int) (menu.getDurationLevel() * SCALE_SPEED);
        if (l > 0) {
            graphics.blit(texture, x + 98, y + 35 + SCALE_SPEED - l, 176, SCALE_SPEED - l, 16, l);
        }
        l = scaledLiquid(menu.getTankAmount(), capacity);
        if (l > 0) {
            drawLiquid(graphics, fluid, x + 152, y + 9 + SCALE_LIQUID - l, 16, l);
            graphics.blit(texture, x + 152, y + 9, 200, 0, 16, SCALE_LIQUID);
        }
        graphics.blit(texture, x + 152, y + 9, 176, 98, 16, SCALE_LIQUID);
    }

    @Override
    protected void renderGaugeTooltips(GuiGraphics graphics, int mouseX, int mouseY, int x, int y) {
        if (x >= 152 && x < 168 && y >= 9 && y < 69) {
            int amount = Math.min(menu.getTankAmount(), capacity);
            graphics.renderTooltip(font, Component.literal(amount + " / " + capacity + " mB"), mouseX, mouseY);
        }
    }

    /** {@code TabEngine}. */
    private final class EngineTab extends TeTab {
        EngineTab() {
            super(false, 0xD0650B);
            maxHeight = 92;
            maxWidth = 100;
        }

        @Override
        public void draw(GuiGraphics graphics, Font font, int x, int y) {
            drawBackground(graphics, x, y);
            drawIcon(graphics, 3, x + 2, y + 2);
            if (isFullyOpened()) {
                graphics.drawString(font, Component.translatable("gui.craft.te.energy"), x + 22, y + 6, HEADER_COLOR);
                graphics.drawString(font, Component.translatable("gui.craft.te.power_output").append(":"), x + 8,
                        y + 18, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getPower()) + " MJ/t", x + 16, y + 30, TEXT_COLOR, false);
                graphics.drawString(font, Component.translatable("gui.craft.te.max_power").append(":"), x + 8,
                        y + 42, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getEngineType().getMaxPower()) + " MJ/t", x + 16, y + 54,
                        TEXT_COLOR, false);
                graphics.drawString(font, Component.translatable("gui.craft.te.energy_stored").append(":"), x + 8,
                        y + 66, SUBHEADER_COLOR);
                graphics.drawString(font, format(menu.getEnergy()) + " MJ", x + 16, y + 78, TEXT_COLOR, false);
            }
        }

        @Override
        public Component getTooltip() {
            return isFullyOpened() ? null : Component.literal(format(menu.getPower()) + " MJ/t");
        }
    }
}
