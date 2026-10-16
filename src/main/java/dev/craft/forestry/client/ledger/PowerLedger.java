package dev.craft.forestry.client.ledger;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Forestry {@code PowerLedger} of a machine: stored MJ (the float as Java prints it), storage and maximum input. */
public class PowerLedger extends Ledger {
    private final Supplier<Float> stored;
    private final IntSupplier maxStored;
    private final IntSupplier maxReceived;

    public PowerLedger(Supplier<Float> stored, IntSupplier maxStored, IntSupplier maxReceived) {
        super(0xD46C1F, 94);
        this.stored = stored;
        this.maxStored = maxStored;
        this.maxReceived = maxReceived;
    }

    private String stored() {
        return stored.get() + " MJ";
    }

    @Override
    public void draw(GuiGraphics graphics, Font font, int x, int y) {
        drawBackground(graphics, x, y);
        drawIcon(graphics, MISC, 0, x + 3, y + 4);
        if (isFullyOpened()) {
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.energy"), x + 22, y + 8,
                    HEADER_COLOR);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.stored").append(":"), x + 22,
                    y + 20, SUBHEADER_COLOR);
            drawValue(graphics, font, stored(), x + 22, y + 32);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.max_energy").append(":"), x + 22,
                    y + 44, SUBHEADER_COLOR);
            drawValue(graphics, font, (float) maxStored.getAsInt() + " MJ", x + 22, y + 56);
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.max_input").append(":"), x + 22,
                    y + 68, SUBHEADER_COLOR);
            drawValue(graphics, font, (float) maxReceived.getAsInt() + " MJ", x + 22, y + 80);
        }
    }

    @Override
    public Component getTooltip() {
        return Component.literal(stored());
    }
}
