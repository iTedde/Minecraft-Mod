package dev.craft.nuclearcontrol.card;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * {@code IPanelDataSource}: a card for the information panel (and, for energy cards, the range trigger). The server
 * calls {@link #update} every refresh period; the client builds the lines from the stored values.
 */
public interface PanelCard {
    /** Key of the display settings ({@code getCardType}); every card type has its own check boxes. */
    String cardType();

    /**
     * {@code IRemoteSensor}: the panel checks the target of {@code craft:sensor_target} against its range before the
     * update (no target → invalid card, too far → out of range).
     */
    default boolean isRemoteSensor() {
        return true;
    }

    /** Server: reads the target and stores the values in {@code card}. {@code range} is per axis. */
    CardState update(Level level, BlockPos panel, CardData card, int range);

    /** Client: the lines for the display bits {@code settings}. */
    List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level);

    /** The check boxes, empty for cards without settings. */
    List<PanelSetting> settings();

    /** {@code IAdvancedCardSettings}: the panel GUI shows a "..." button (text card). */
    default boolean hasAdvancedSettings() {
        return false;
    }
}
