package dev.craft.nuclearcontrol.item;

import java.util.List;

import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Time card ({@code ItemTimeCard}, spec 46 §5.3): the time of day as {@code hh:mm}, 0:00 = midnight
 * ({@code (dayTime − 18000) % 24000}; before the first midnight the original shows negative values – kept).
 */
public class TimeCardItem extends PanelCardItem {
    public TimeCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "time";
    }

    @Override
    public boolean isRemoteSensor() {
        return false;
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        return CardState.OK;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        return List.of(PanelLine.left(PanelText.labeled("time", clock(level.getDayTime()), showLabels)));
    }

    public static String clock(long dayTime) {
        int time = (int) ((dayTime - 18000L) % 24000L);
        int hours = time / 1000;
        int minutes = time % 1000 * 6 / 100;
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
