package dev.craft.nuclearcontrol.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.gregtech.item.GtRadioactiveCellItem;
import dev.craft.ic2.item.reactor.UraniumCellItem;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelSetting;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Reactor sensor location card ({@code ItemCardReactorSensorLocation}, spec 46 §5.3): hull heat, max heat, melting
 * point (85 %), output in EU/t, time to cycle end and On/Off (redstone at the reactor).
 */
public class ReactorSensorCardItem extends PanelCardItem {
    public static final int DISPLAY_ONOFF = 1;
    public static final int DISPLAY_HEAT = 2;
    public static final int DISPLAY_MAXHEAT = 4;
    public static final int DISPLAY_OUTPUT = 8;
    public static final int DISPLAY_TIME = 16;
    public static final int DISPLAY_MELTING = 32;
    private static final int COLOR_ON = 0x00FF00;
    private static final int COLOR_OFF = 0xFF0000;

    public ReactorSensorCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "reactor";
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        BlockPos target = card.getTarget();
        if (target == null || !level.isLoaded(target)
                || !(level.getBlockEntity(target) instanceof NuclearReactorBlockEntity reactor)) {
            return CardState.NO_TARGET;
        }
        card.setInt("heat", reactor.getHeat());
        card.setInt("maxHeat", reactor.getMaxHeat());
        card.setBoolean("reactorPoweredB", level.hasNeighborSignal(target));
        card.setInt("output", reactor.getOutput() * NuclearReactorBlockEntity.ENERGY_GENERATOR_NUCLEAR);
        int timeLeft = 0;
        IItemHandler items = reactor.getItemHandler(null);
        for (int slot = 0; slot < items.getSlots(); slot++) {
            timeLeft = Math.max(timeLeft, cellTimeLeft(items.getStackInSlot(slot)));
        }
        card.setInt("timeLeft", timeLeft * NuclearReactorBlockEntity.TICK_RATE / 20);
        return CardState.OK;
    }

    /** {@code getNuclearCellTimeLeft}: uranium cells (IC2) and thorium/plutonium cells (GregTech), else −1. */
    public static int cellTimeLeft(ItemStack stack) {
        if (stack.getItem() instanceof UraniumCellItem cell) {
            return cell.getMaxDamageEx() - cell.getDamageOfStack(stack);
        }
        if (stack.getItem() instanceof GtRadioactiveCellItem cell) {
            return cell.getMaxNuclearDurability() - cell.getDurabilityOfStack(stack);
        }
        return -1;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        if (shows(settings, DISPLAY_HEAT)) {
            result.add(PanelLine.left(PanelText.labeled("heat", card.getInt("heat"), showLabels)));
        }
        if (shows(settings, DISPLAY_MAXHEAT)) {
            result.add(PanelLine.left(PanelText.labeled("max_heat", card.getInt("maxHeat"), showLabels)));
        }
        if (shows(settings, DISPLAY_MELTING)) {
            result.add(PanelLine.left(PanelText.labeled("melting", card.getInt("maxHeat") * 85 / 100, showLabels)));
        }
        if (shows(settings, DISPLAY_OUTPUT)) {
            result.add(PanelLine.left(PanelText.labeled("output", card.getInt("output"), showLabels)));
        }
        if (shows(settings, DISPLAY_TIME)) {
            int timeLeft = card.getInt("timeLeft");
            String time = String.format("%d:%02d:%02d", timeLeft / 3600, timeLeft % 3600 / 60, timeLeft % 60);
            result.add(PanelLine.left(PanelText.labeled("time_remaining", time, showLabels)));
        }
        if (shows(settings, DISPLAY_ONOFF)) {
            boolean on = card.getBoolean("reactorPoweredB");
            String text = PanelText.text(on ? "on" : "off");
            int color = on ? COLOR_ON : COLOR_OFF;
            if (result.isEmpty()) {
                result.add(PanelLine.left(text, color));
            } else {
                result.set(0, result.get(0).withRight(text, color));
            }
        }
        return result;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("on_off", DISPLAY_ONOFF), new PanelSetting("heat", DISPLAY_HEAT),
                new PanelSetting("max_heat", DISPLAY_MAXHEAT), new PanelSetting("melting", DISPLAY_MELTING),
                new PanelSetting("output", DISPLAY_OUTPUT), new PanelSetting("time_remaining", DISPLAY_TIME));
    }
}
