package dev.craft.mffs.client;

import dev.craft.Craft;
import dev.craft.mffs.MffsItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * {@code GuiManuelScreen} (256 × 216, spec 48 §11.6): table of contents, monazit/forcicium/cell and the card overview
 * in two pages; "Changelog" and "Version Check" are left out. "-->" and "<--" turn the pages (wrapping round).
 */
public class GuideScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/mffs/guide.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 216;
    private static final int WHITE = 16777215;
    private static final List<String> PAGES = List.of("toc", "monazit", "cards_1", "cards_2");

    private int page;
    private int left;
    private int top;

    public GuideScreen() {
        super(Component.translatable("gui.craft.mffs.guide"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new GuideScreen());
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.literal("-->"), button -> page = (page + 1) % PAGES.size())
                .bounds(width / 2 + 90, height / 2 + 80, 22, 16).build());
        addRenderableWidget(Button.builder(Component.literal("<--"),
                button -> page = (page + PAGES.size() - 1) % PAGES.size())
                .bounds(width / 2 - 110, height / 2 + 80, 22, 16).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BACKGROUND, left, top, 0, 0, WIDTH, HEIGHT, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        text(graphics, Component.translatable("gui.craft.mffs.guide"), 20, 15);
        renderPage(graphics);
        text(graphics, Component.translatable("gui.craft.mffs.guide.page", page, pageTitle(page)), 45, 193);
    }

    private static Component pageTitle(int index) {
        return Component.translatable("gui.craft.mffs.guide.title." + PAGES.get(index));
    }

    private void renderPage(GuiGraphics graphics) {
        switch (page) {
            case 0 -> {
                text(graphics, Component.translatable("gui.craft.mffs.guide.contents"), 90, 45);
                for (int index = 0; index < PAGES.size(); index++) {
                    text(graphics, Component.literal("[" + index + "]: ").append(pageTitle(index)), 20,
                            65 + index * 10);
                }
            }
            case 1 -> {
                item(graphics, MffsItems.MONAZIT_ORE, 30, 45);
                item(graphics, MffsItems.FORCICIUM, 30, 65);
                item(graphics, MffsItems.FORCICIUM_CELL, 30, 85);
                lines(graphics, "monazit", new int[][] {{60, 50}, {60, 70}, {60, 90}, {20, 105}, {20, 115},
                        {20, 125}, {20, 135}, {20, 145}, {20, 155}, {20, 165}});
            }
            case 2 -> {
                item(graphics, MffsItems.CARD_BLANK, 15, 45);
                item(graphics, MffsItems.CARD_POWER_LINK, 15, 95);
                item(graphics, MffsItems.CAPACITOR, 35, 105);
                item(graphics, MffsItems.CARD_BLANK, 140, 105);
                item(graphics, MffsItems.CARD_SECURITY_LINK, 15, 145);
                item(graphics, MffsItems.SECURITY_STATION, 90, 155);
                item(graphics, MffsItems.CARD_BLANK, 220, 155);
                lines(graphics, "cards_1", new int[][] {{35, 45}, {35, 55}, {35, 95}, {55, 110}, {35, 145},
                        {35, 160}, {110, 160}});
            }
            default -> {
                item(graphics, MffsItems.CARD_ACCESS, 15, 45);
                item(graphics, MffsItems.SECURITY_STATION, 35, 55);
                item(graphics, MffsItems.CARD_BLANK, 170, 55);
                item(graphics, MffsItems.CARD_PERSONAL_ID, 15, 85);
                item(graphics, MffsItems.MULTITOOL_ID_WRITER, 35, 100);
                item(graphics, MffsItems.CARD_DATA_LINK, 15, 125);
                item(graphics, MffsItems.MULTITOOL_ID_WRITER, 35, 140);
                item(graphics, MffsItems.CARD_INFINITE_POWER, 15, 165);
                lines(graphics, "cards_2", new int[][] {{35, 45}, {55, 60}, {35, 85}, {55, 98}, {55, 110},
                        {35, 125}, {55, 138}, {55, 150}, {35, 170}});
            }
        }
    }

    private void lines(GuiGraphics graphics, String page, int[][] positions) {
        for (int line = 0; line < positions.length; line++) {
            text(graphics, Component.translatable("gui.craft.mffs.guide." + page + "." + line), positions[line][0],
                    positions[line][1]);
        }
    }

    private void item(GuiGraphics graphics, ItemLike item, int x, int y) {
        graphics.renderItem(new ItemStack(item), left + x, top + y);
    }

    private void text(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(font, text, left + x, top + y, WHITE, false);
    }
}
