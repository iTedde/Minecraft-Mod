package dev.craft.redpower.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.craft.Craft;
import dev.craft.redpower.RpItems;
import dev.craft.redpower.table.ProjectTableBlockEntity;
import dev.craft.redpower.table.ProjectTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * RedPower {@code GuiAdvBench}: the write-plan button lights up with a blank plan and a result; a written plan shows its
 * ingredients as ghost items in empty grid slots, red where the storage cannot supply them.
 */
public class ProjectTableScreen extends AbstractContainerScreen<ProjectTableMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/project_table.png");
    private static final int BUTTON_X = 18;
    private static final int BUTTON_Y = 55;
    private static final int BUTTON_SIZE = 14;

    public ProjectTableScreen(ProjectTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        titleLabelX = 60;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private boolean canWritePlan() {
        return menu.getSlot(ProjectTableBlockEntity.PLAN_SLOT).getItem().is(RpItems.BLANK_PLAN)
                && menu.getSlot(ProjectTableMenu.RESULT).hasItem();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (canWritePlan()) {
            graphics.blit(BACKGROUND, leftPos + BUTTON_X, topPos + BUTTON_Y, 176, 0, BUTTON_SIZE, BUTTON_SIZE);
        }
        ItemStack[] plan = menu.getPlanItems();
        if (plan == null) {
            return;
        }
        int mask = menu.getSatisfyMask();
        for (int i = 0; i < ProjectTableBlockEntity.GRID_SIZE; i++) {
            Slot slot = menu.getSlot(i);
            if (plan[i].isEmpty() || slot.hasItem()) {
                continue;
            }
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.renderFakeItem(plan[i], x, y);
            graphics.renderItemDecorations(font, plan[i], x, y);
            // the slot background drawn translucently over the item fades it (white) or tints it red
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
            RenderSystem.enableBlend();
            if ((mask & 1 << i) != 0) {
                graphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            } else {
                graphics.setColor(1.0F, 0.1F, 0.1F, 0.6F);
            }
            graphics.blit(BACKGROUND, x, y, slot.x, slot.y, 16, 16);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - leftPos;
        double y = mouseY - topPos;
        if (x >= BUTTON_X && y >= BUTTON_Y && x <= BUTTON_X + BUTTON_SIZE && y <= BUTTON_Y + BUTTON_SIZE
                && canWritePlan() && minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ProjectTableMenu.BUTTON_WRITE_PLAN);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
