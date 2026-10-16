package dev.craft.forestry.client.ledger;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Forestry {@code HintLedger}: one hint of the machine, picked at random when the GUI opens. */
public class HintLedger extends Ledger {
    /** {@code hints.properties} without the access-control hints (no access rights, see abweichungen.md). */
    public static final List<String> ELECTRICAL_ENGINE = List.of("energyunit", "energybuffer", "forcedcooldown",
            "cooldownloss", "enginetinbatteries", "enginetinchoke");
    /** {@code hints.properties} without the access-control hints and the pipette (not in the mod). */
    public static final List<String> CARPENTER = List.of("powerrequired", "crating");

    private final String hint;

    public HintLedger(List<String> hints) {
        super(0xEA38FF, 96);
        this.hint = hints.get(ThreadLocalRandom.current().nextInt(hints.size()));
    }

    @Override
    public void draw(GuiGraphics graphics, Font font, int x, int y) {
        drawBackground(graphics, x, y);
        drawIcon(graphics, MISC, 1, x + 3, y + 4);
        if (isFullyOpened()) {
            drawHeader(graphics, font, Component.translatable("gui.craft.forestry.did_you_know").append("?"), x + 22,
                    y + 8, HEADER_COLOR);
            drawWrapped(graphics, font, Component.translatable("gui.craft.forestry.hint." + hint), x + 22, y + 20);
        }
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("gui.craft.forestry.hint." + hint + ".tag");
    }
}
