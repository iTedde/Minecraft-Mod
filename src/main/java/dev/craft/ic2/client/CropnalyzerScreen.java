package dev.craft.ic2.client;

import dev.craft.ic2.crop.CropCard;
import dev.craft.ic2.crop.CropSeedItem;
import dev.craft.ic2.crop.CropnalyzerMenu;
import dev.craft.ic2.crop.Crops;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code GuiCropnalyzer}: the scanned bag's data, more lines with each scan level. */
public class CropnalyzerScreen extends MachineScreen<CropnalyzerMenu> {
    private static final int WHITE = 0xFFFFFF;
    private static final int GROWTH = 0xADFF2F;
    private static final int GAIN = 0xEEC900;
    private static final int RESISTANCE = 0x00CED1;
    private static final String[] TIERS = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII",
            "XIII", "XIV", "XV", "XVI"};

    public CropnalyzerScreen(CropnalyzerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "cropnalyzer");
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 74, 11, 0, false);
        ItemStack scanned = menu.getScannedStack();
        if (!(scanned.getItem() instanceof CropSeedItem)) {
            return;
        }
        CropSeedItem.Seed seed = CropSeedItem.getSeed(scanned);
        int level = seed.scan();
        if (level < 0) {
            return;
        }
        if (level == 0) {
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.unknown"), 8, 37, WHITE, false);
            return;
        }
        CropCard crop = Crops.get(seed.id());
        graphics.drawString(font, crop.displayName(), 8, 37, WHITE, false);
        if (level >= 2) {
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.tier", tier(crop.tier())), 8, 50,
                    WHITE, false);
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.discovered"), 8, 73, WHITE, false);
            graphics.drawString(font, crop.discoveredBy(), 8, 86, WHITE, false);
        }
        if (level >= 3) {
            graphics.drawString(font, crop.desc(0), 8, 109, WHITE, false);
            graphics.drawString(font, crop.desc(1), 8, 122, WHITE, false);
        }
        if (level >= 4) {
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.growth"), 118, 37, GROWTH, false);
            graphics.drawString(font, String.valueOf(seed.growth()), 118, 50, GROWTH, false);
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.gain"), 118, 73, GAIN, false);
            graphics.drawString(font, String.valueOf(seed.gain()), 118, 86, GAIN, false);
            graphics.drawString(font, Component.translatable("gui.craft.cropnalyzer.resistance"), 118, 109,
                    RESISTANCE, false);
            graphics.drawString(font, String.valueOf(seed.resistance()), 118, 122, RESISTANCE, false);
        }
    }

    /** IC2 {@code getSeedTier}: Roman numerals 1–16, otherwise "0". */
    private static String tier(int tier) {
        return tier >= 1 && tier <= TIERS.length ? TIERS[tier - 1] : "0";
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
    }
}
