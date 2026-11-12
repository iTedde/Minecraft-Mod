package dev.craft.nuclearcontrol.item;

import java.util.List;

import dev.craft.nuclearcontrol.block.AverageCounterBlockEntity;
import dev.craft.nuclearcontrol.block.EnergyCounterBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Counter sensor location card ({@code ItemCardMultipleSensorLocation} damage 0, spec 46 §5.3): reading of an energy
 * counter ({@code energy}, EU) or an average counter ({@code average}, EU/t). Once {@code average} is set it stays on
 * the card, so the card keeps showing the output line ({@code hasField("average")}) – as in the original.
 */
public class CounterSensorCardItem extends PanelCardItem {
    public static final int DISPLAY_ENERGY = 1;

    public CounterSensorCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "counter";
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        BlockPos target = card.getTarget();
        BlockEntity blockEntity = target == null ? null : level.getBlockEntity(target);
        if (blockEntity instanceof EnergyCounterBlockEntity counter) {
            card.setLong("energy", counter.getCounter());
            return CardState.OK;
        }
        if (blockEntity instanceof AverageCounterBlockEntity counter) {
            card.setInt("average", counter.getAverage());
            return CardState.OK;
        }
        return CardState.NO_TARGET;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        if (!shows(settings, DISPLAY_ENERGY)) {
            return List.of();
        }
        if (card.has("average")) {
            return List.of(PanelLine.left(PanelText.labeled("output", card.getInt("average"), showLabels)));
        }
        return List.of(PanelLine.left(PanelText.labeled("energy_counter", card.getLong("energy"), showLabels)));
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("energy_current", DISPLAY_ENERGY));
    }
}
