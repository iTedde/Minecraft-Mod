package dev.craft.nuclearcontrol.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.gregtech.machine.GtDeviceInformation;
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
 * GregTech sensor card ({@code GT_SensorCard_Item}, spec 46 §8): the three information lines of a GregTech machine
 * ("Progress:", "<n>secs", "/<n>secs"), each switchable (primary/secondary/tertiary). Tooltip "Insert into Display
 * Panel" before title and coordinates.
 */
public class GtSensorCardItem extends PanelCardItem {
    public static final int DISPLAY_MAIN = 1;
    public static final int DISPLAY_SECOND = 2;
    public static final int DISPLAY_TERTIARY = 4;

    public GtSensorCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "gregtech";
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        BlockPos target = card.getTarget();
        if (target == null || !(level.getBlockEntity(target) instanceof GtDeviceInformation machine)) {
            return CardState.NO_TARGET;
        }
        card.setString("mString1", machine.getMainInfo());
        card.setString("mString2", machine.getSecondaryInfo());
        card.setString("mString3", machine.getTertiaryInfo());
        return CardState.OK;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        if (shows(settings, DISPLAY_MAIN)) {
            result.add(PanelLine.left(card.getString("mString1")));
        }
        if (shows(settings, DISPLAY_SECOND)) {
            result.add(PanelLine.left(card.getString("mString2")));
        }
        if (shows(settings, DISPLAY_TERTIARY)) {
            result.add(PanelLine.left(card.getString("mString3")));
        }
        return result;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("gt_primary", DISPLAY_MAIN), new PanelSetting("gt_secondary", DISPLAY_SECOND),
                new PanelSetting("gt_tertiary", DISPLAY_TERTIARY));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.nuclearcontrol.gt_sensor_card"));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
