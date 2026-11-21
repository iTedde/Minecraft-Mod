package dev.craft.nuclearcontrol.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Text card ({@code ItemCardText}, spec 46 §5.3): 10 lines of free text ({@code line_0} … {@code line_9}), edited
 * through the "..." button of the panel GUI. Empty lines at the end are dropped.
 */
public class TextCardItem extends PanelCardItem {
    public static final int LINES = 10;

    public TextCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "text";
    }

    @Override
    public boolean isRemoteSensor() {
        return false;
    }

    @Override
    public boolean hasAdvancedSettings() {
        return true;
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        return CardState.OK;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        boolean started = false;
        for (int i = LINES - 1; i >= 0; i--) {
            String text = card.getString("line_" + i);
            if (!text.isEmpty() || started) {
                started = true;
                result.addFirst(PanelLine.left(text));
            }
        }
        return result;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String title = CardData.of(stack).getTitle();
        if (!title.isEmpty()) {
            tooltip.add(Component.literal(title));
        }
    }
}
