package dev.craft.forestry.client.ledger;

import java.util.function.IntSupplier;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Forestry {@code GuiEngine.EngineLedger}: current output, stored MJ and heat (tenths of a degree, "C"). */
public class EngineLedger extends Ledger {
    private final IntSupplier output;
    private final IntSupplier stored;
    private final IntSupplier heat;

    public EngineLedger(IntSupplier output, IntSupplier stored, IntSupplier heat) {
        super(0xD46C1F, 94);
        this.output = output;
        this.stored = stored;
        this.heat = heat;
    }

    @Override
    public void draw(GuiGraphics graphics, Font font, int x, int y) {
        drawBackground(graphics, x, y);
        drawIcon(graphics, MISC, 0, x + 3, y + 4);
        if (isFullyOpened()) {
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.energy"), x + 22, y + 8,
                    HEADER_COLOR);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.current_output").append(":"),
                    x + 22, y + 20, SUBHEADER_COLOR);
            drawValue(graphics, font, output.getAsInt() + " MJ/t", x + 22, y + 32);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.stored").append(":"), x + 22,
                    y + 44, SUBHEADER_COLOR);
            drawValue(graphics, font, stored.getAsInt() + " MJ", x + 22, y + 56);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.heat").append(":"), x + 22,
                    y + 68, SUBHEADER_COLOR);
            drawValue(graphics, font, heat.getAsInt() / 10.0 + " C", x + 22, y + 80);
        }
    }

    @Override
    public Component getTooltip() {
        return Component.literal(output.getAsInt() + " MJ/t");
    }
}
