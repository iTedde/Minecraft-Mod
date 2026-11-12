package dev.craft.nuclearcontrol.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.EnergySources;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Energy sensor location card ({@code ItemCardEnergySensorLocation}, spec 46 §5.3): stored energy, free, storage
 * and fill percentage of an EU or MJ storage ({@code targetType} from the kit).
 */
public class EnergySensorCardItem extends PanelCardItem {
    public static final int DISPLAY_ENERGY = 1;
    public static final int DISPLAY_FREE = 2;
    public static final int DISPLAY_STORAGE = 4;
    public static final int DISPLAY_PERCENTAGE = 8;

    public EnergySensorCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "energy";
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        BlockPos target = card.getTarget();
        EnergySources.Reading storage = target == null ? null
                : EnergySources.at(level, target, card.getInt("targetType"));
        if (storage == null) {
            return CardState.NO_TARGET;
        }
        card.setLong("energyL", storage.stored());
        card.setLong("maxStorageL", storage.capacity());
        return CardState.OK;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        long energy = card.getLong("energyL");
        long storage = card.getLong("maxStorageL");
        if (shows(settings, DISPLAY_ENERGY)) {
            result.add(PanelLine.left(PanelText.labeled("energy", energy, showLabels)));
        }
        if (shows(settings, DISPLAY_FREE)) {
            result.add(PanelLine.left(PanelText.labeled("energy_free", storage - energy, showLabels)));
        }
        if (shows(settings, DISPLAY_STORAGE)) {
            result.add(PanelLine.left(PanelText.labeled("energy_storage", storage, showLabels)));
        }
        if (shows(settings, DISPLAY_PERCENTAGE)) {
            result.add(PanelLine.left(PanelText.labeled("energy_percentage",
                    storage == 0L ? 100L : energy * 100L / storage, showLabels)));
        }
        return result;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("energy_current", DISPLAY_ENERGY),
                new PanelSetting("energy_storage", DISPLAY_STORAGE), new PanelSetting("energy_free", DISPLAY_FREE),
                new PanelSetting("energy_percentage", DISPLAY_PERCENTAGE));
    }
}
