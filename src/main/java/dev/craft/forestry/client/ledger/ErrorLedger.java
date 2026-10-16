package dev.craft.forestry.client.ledger;

import java.util.function.Supplier;

import dev.craft.forestry.machine.ErrorState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Forestry {@code ErrorLedger}: only visible while the machine reports an error; icon, description and help. */
public class ErrorLedger extends Ledger {
    private final Supplier<ErrorState> state;

    public ErrorLedger(Supplier<ErrorState> state) {
        super(0xFF3535, 72);
        this.state = state;
    }

    @Override
    public void draw(GuiGraphics graphics, Font font, int x, int y) {
        ErrorState error = state.get();
        drawBackground(graphics, x, y);
        drawIcon(graphics, ERRORS, error.getIconIndex(), x + 3, y + 4);
        if (isFullyOpened()) {
            drawHeader(graphics, font, getTooltip(), x + 22, y + 8, HEADER_COLOR);
            drawWrapped(graphics, font, Component.translatable(error.getHelpKey()), x + 22, y + 20);
        }
    }

    @Override
    public boolean isVisible() {
        return state.get() != ErrorState.OK;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable(state.get().getDescriptionKey());
    }
}
