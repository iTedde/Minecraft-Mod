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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Energy array location card ({@code ItemCardEnergyArrayLocation}, spec 46 §5.3): up to 6 energy targets
 * ({@code _<n>x/y/z/targetType}). Each target is checked against the range on its own; the card itself is no remote
 * sensor. Per target the values are kept as {@code int} as in the original, and stay when a target goes missing.
 */
public class EnergyArrayCardItem extends PanelCardItem {
    public static final int MAX_CARDS = 6;
    public static final int DISPLAY_ENERGY = 1;
    public static final int DISPLAY_FREE = 2;
    public static final int DISPLAY_STORAGE = 4;
    public static final int DISPLAY_EACH = 8;
    public static final int DISPLAY_TOTAL = 16;
    public static final int DISPLAY_PERCENTAGE = 32;

    public EnergyArrayCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public String cardType() {
        return "energy_array";
    }

    @Override
    public boolean isRemoteSensor() {
        return false;
    }

    public static int getCardCount(CardData card) {
        return card.getInt("cardCount");
    }

    /** {@code initArray}: appends the targets of the energy sensor cards. */
    public static void addCards(CardData array, List<ItemStack> cards) {
        int count = getCardCount(array);
        for (ItemStack stack : cards) {
            CardData card = CardData.of(stack);
            BlockPos target = card.getTarget();
            if (target != null) {
                array.setInt("_" + count + "x", target.getX());
                array.setInt("_" + count + "y", target.getY());
                array.setInt("_" + count + "z", target.getZ());
                array.setInt("_" + count + "targetType", card.getInt("targetType"));
                count++;
            }
        }
        array.setInt("cardCount", count);
    }

    @Override
    public CardState update(Level level, BlockPos panel, CardData card, int range) {
        int count = getCardCount(card);
        if (count == 0) {
            return CardState.INVALID_CARD;
        }
        long totalEnergy = 0L;
        boolean foundAny = false;
        boolean outOfRange = false;
        for (int i = 0; i < count; i++) {
            BlockPos target = new BlockPos(card.getInt("_" + i + "x"), card.getInt("_" + i + "y"),
                    card.getInt("_" + i + "z"));
            if (Math.abs(target.getX() - panel.getX()) <= range && Math.abs(target.getY() - panel.getY()) <= range
                    && Math.abs(target.getZ() - panel.getZ()) <= range) {
                EnergySources.Reading storage = EnergySources.at(level, target, card.getInt("_" + i + "targetType"));
                if (storage != null) {
                    totalEnergy += storage.stored();
                    card.setInt("_" + i + "energy", (int) storage.stored());
                    card.setInt("_" + i + "maxStorage", (int) storage.capacity());
                    foundAny = true;
                }
            } else {
                outOfRange = true;
            }
        }
        card.setLong("energyL", totalEnergy);
        if (foundAny) {
            return CardState.OK;
        }
        return outOfRange ? CardState.OUT_OF_RANGE : CardState.NO_TARGET;
    }

    @Override
    public List<PanelLine> lines(int settings, CardData card, boolean showLabels, Level level) {
        List<PanelLine> result = new ArrayList<>();
        boolean each = shows(settings, DISPLAY_EACH);
        boolean total = shows(settings, DISPLAY_TOTAL);
        boolean energyLine = shows(settings, DISPLAY_ENERGY);
        boolean freeLine = shows(settings, DISPLAY_FREE);
        boolean storageLine = shows(settings, DISPLAY_STORAGE);
        boolean percentageLine = shows(settings, DISPLAY_PERCENTAGE);
        long totalEnergy = 0L;
        long totalStorage = 0L;
        int count = getCardCount(card);
        for (int i = 0; i < count; i++) {
            int energy = card.getInt("_" + i + "energy");
            int storage = card.getInt("_" + i + "maxStorage");
            if (total) {
                totalEnergy += energy;
                totalStorage += storage;
            }
            if (each) {
                if (energyLine) {
                    result.add(PanelLine.left(numbered("energy_n", i, energy, showLabels)));
                }
                if (freeLine) {
                    result.add(PanelLine.left(numbered("energy_free_n", i, storage - energy, showLabels)));
                }
                if (storageLine) {
                    result.add(PanelLine.left(numbered("energy_storage_n", i, storage, showLabels)));
                }
                if (percentageLine) {
                    result.add(PanelLine.left(numbered("energy_percentage_n", i,
                            storage == 0 ? 100L : energy * 100L / storage, showLabels)));
                }
            }
        }
        if (total) {
            if (energyLine) {
                result.add(PanelLine.left(PanelText.labeled("energy", totalEnergy, showLabels)));
            }
            if (freeLine) {
                result.add(PanelLine.left(PanelText.labeled("energy_free", totalStorage - totalEnergy, showLabels)));
            }
            if (storageLine) {
                result.add(PanelLine.left(PanelText.labeled("energy_storage", totalStorage, showLabels)));
            }
            if (percentageLine) {
                result.add(PanelLine.left(PanelText.labeled("energy_percentage",
                        totalStorage == 0L ? 100L : totalEnergy * 100L / totalStorage, showLabels)));
            }
        }
        return result;
    }

    private static String numbered(String key, int index, long value, boolean showLabels) {
        String number = PanelText.number(value);
        return showLabels ? PanelText.text(key, index + 1, number) : number;
    }

    @Override
    public List<PanelSetting> settings() {
        return List.of(new PanelSetting("energy_current", DISPLAY_ENERGY),
                new PanelSetting("energy_storage", DISPLAY_STORAGE), new PanelSetting("energy_free", DISPLAY_FREE),
                new PanelSetting("energy_percentage", DISPLAY_PERCENTAGE),
                new PanelSetting("energy_each", DISPLAY_EACH), new PanelSetting("energy_total", DISPLAY_TOTAL));
    }

    /** Title and "Cards quantity: n". */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CardData card = CardData.of(stack);
        int count = getCardCount(card);
        if (count > 0) {
            if (!card.getTitle().isEmpty()) {
                tooltip.add(Component.literal(card.getTitle()));
            }
            tooltip.add(Component.translatable("tooltip.craft.nuclearcontrol.card_quantity", count));
        }
    }
}
